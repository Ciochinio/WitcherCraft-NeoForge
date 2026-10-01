package net.redboltmedia.witchercraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * The {@code witchercraft:alchemy} recipe type and everything the server asks
 * about alchemy recipes: all of them, the one matching a grid, and which items
 * are valid bases and ingredients.
 *
 * Server-side only. The per-reload index (sorted recipes, ingredient allowlist)
 * is rebuilt whenever the recipe manager's map changes, i.e. on world load and
 * on /reload, and the overlap check logs its warnings at that point.
 *
 * HAND-MAINTAINED: locked code element. {@link #register} is called from
 * WitchercraftMod's "mod init" user code block.
 */
@EventBusSubscriber
public final class AlchemyRecipes {
	private AlchemyRecipes() {
	}

	private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, WitchercraftMod.MODID);
	private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, WitchercraftMod.MODID);

	public static final DeferredHolder<RecipeType<?>, RecipeType<AlchemyRecipe>> TYPE = RECIPE_TYPES.register("alchemy", id -> RecipeType.simple(id));
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AlchemyRecipe>> SERIALIZER = RECIPE_SERIALIZERS.register("alchemy",
			() -> new RecipeSerializer<>(AlchemyRecipe.MAP_CODEC, AlchemyRecipe.STREAM_CODEC));

	/** Every alcohol counts as a potion base. Add new alcohols to data/witchercraft/tags/item/alcohol.json. */
	public static final TagKey<Item> ALCOHOL = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "alcohol"));

	/** Everything the base slot accepts. Add new bases (such as mutagens) to data/witchercraft/tags/item/alchemy_base.json. */
	public static final TagKey<Item> BASE = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "alchemy_base"));

	public static void register(IEventBus modEventBus) {
		RECIPE_TYPES.register(modEventBus);
		RECIPE_SERIALIZERS.register(modEventBus);
	}

	// ---- bases -----------------------------------------------------------------------

	/**
	 * Whether a stack may go in the base slot: anything in the
	 * {@code witchercraft:alchemy_base} item tag (any alcohol, White Gull, Tallow,
	 * Saltpeter, and later mutagens). A tag rather than derived from recipes,
	 * because a base must be valid before any recipe uses it. Item tags are synced
	 * to the client, so both sides predict placement identically.
	 */
	public static boolean isBase(ItemStack stack) {
		return !stack.isEmpty() && stack.is(BASE);
	}

	// ---- queries ---------------------------------------------------------------------

	/** Every alchemy recipe, sorted by id. */
	public static List<RecipeHolder<AlchemyRecipe>> all(MinecraftServer server) {
		return index(server).recipes;
	}

	/** The id of every alchemy recipe, sorted. */
	public static Set<Identifier> ids(MinecraftServer server) {
		return index(server).byId.keySet();
	}

	/** One recipe by id, if it is loaded. */
	public static Optional<RecipeHolder<AlchemyRecipe>> byId(MinecraftServer server, Identifier id) {
		return Optional.ofNullable(index(server).byId.get(id));
	}

	public static Identifier idOf(RecipeHolder<AlchemyRecipe> holder) {
		return holder.id().identifier();
	}

	/** The recipe matching a grid. If several match, the lowest id wins, so results never depend on load order. */
	public static Optional<RecipeHolder<AlchemyRecipe>> find(MinecraftServer server, AlchemyInput input) {
		for (RecipeHolder<AlchemyRecipe> holder : index(server).recipes)
			if (holder.value().matches(input, server.overworld()))
				return Optional.of(holder);
		return Optional.empty();
	}

	/** Whether a stack is used by at least one alchemy recipe as an ingredient (tags expanded). */
	public static boolean isIngredient(MinecraftServer server, ItemStack stack) {
		return !stack.isEmpty() && index(server).ingredientItems.contains(stack.getItem());
	}

	/** The ingredient allowlist: every item any recipe uses as an ingredient. */
	public static Set<Item> ingredientItems(MinecraftServer server) {
		return index(server).ingredientItems;
	}

	// ---- per-reload index --------------------------------------------------------------

	private record Index(RecipeMap source, List<RecipeHolder<AlchemyRecipe>> recipes, Map<Identifier, RecipeHolder<AlchemyRecipe>> byId, Set<Item> ingredientItems) {
	}

	private static Index cached;

	private static synchronized Index index(MinecraftServer server) {
		RecipeMap map = server.getRecipeManager().recipeMap();
		if (cached == null || cached.source() != map)
			cached = build(map);
		return cached;
	}

	private static Index build(RecipeMap map) {
		List<RecipeHolder<AlchemyRecipe>> recipes = new ArrayList<>(map.byType(TYPE.get()));
		recipes.sort(Comparator.comparing(holder -> holder.id().identifier().toString()));

		Set<Item> ingredientItems = new HashSet<>();
		for (RecipeHolder<AlchemyRecipe> holder : recipes)
			for (Ingredient ingredient : holder.value().ingredients())
				ingredientItems.addAll(itemsOf(ingredient));

		Map<Identifier, RecipeHolder<AlchemyRecipe>> byId = new LinkedHashMap<>();
		for (RecipeHolder<AlchemyRecipe> holder : recipes)
			byId.put(idOf(holder), holder);

		warnAboutOverlaps(recipes);
		WitchercraftMod.LOGGER.info("Loaded {} alchemy recipes using {} ingredient items", recipes.size(), ingredientItems.size());
		return new Index(map, List.copyOf(recipes), Collections.unmodifiableMap(byId), Set.copyOf(ingredientItems));
	}

	@SuppressWarnings("deprecation")
	private static Set<Item> itemsOf(Ingredient ingredient) {
		return ingredient.items().map(Holder::value).collect(Collectors.toSet());
	}

	// ---- overlap check ------------------------------------------------------------------

	/**
	 * Warn about every pair of recipes that some grid would match both. Only the
	 * lower id can ever be brewed then, which is almost always a content mistake.
	 */
	private static void warnAboutOverlaps(List<RecipeHolder<AlchemyRecipe>> recipes) {
		for (int i = 0; i < recipes.size(); i++) {
			for (int j = i + 1; j < recipes.size(); j++) {
				if (canMatchSameGrid(recipes.get(i).value(), recipes.get(j).value()))
					WitchercraftMod.LOGGER.warn("Alchemy recipes {} and {} can match the same grid; only {} can be brewed from it",
							recipes.get(i).id().identifier(), recipes.get(j).id().identifier(), recipes.get(i).id().identifier());
			}
		}
	}

	/**
	 * Two recipes share a grid when their bases share an item and their
	 * ingredient lists can be paired one-to-one so that each pair shares an item.
	 */
	private static boolean canMatchSameGrid(AlchemyRecipe a, AlchemyRecipe b) {
		if (a.ingredients().size() != b.ingredients().size())
			return false;
		if (!intersects(itemsOf(a.base()), itemsOf(b.base())))
			return false;
		int n = a.ingredients().size();
		boolean[][] shares = new boolean[n][n];
		for (int x = 0; x < n; x++) {
			Set<Item> ax = itemsOf(a.ingredients().get(x));
			for (int y = 0; y < n; y++)
				shares[x][y] = intersects(ax, itemsOf(b.ingredients().get(y)));
		}
		return pairAll(shares, 0, new boolean[n]);
	}

	private static boolean pairAll(boolean[][] shares, int row, boolean[] used) {
		if (row == shares.length)
			return true;
		for (int col = 0; col < shares.length; col++) {
			if (!used[col] && shares[row][col]) {
				used[col] = true;
				if (pairAll(shares, row + 1, used))
					return true;
				used[col] = false;
			}
		}
		return false;
	}

	private static boolean intersects(Set<Item> a, Set<Item> b) {
		for (Item item : a)
			if (b.contains(item))
				return true;
		return false;
	}

	// ---- rebuild eagerly so load-time warnings appear at load time -----------------
	// ---- and send the client its copy of the allowlist ------------------------------

	@SubscribeEvent
	public static void onServerStarted(ServerStartedEvent event) {
		index(event.getServer());
	}

	/**
	 * Fires for one player on login, and for everyone (player == null) after
	 * /reload. On login {@link AlchemyKnowledge} sends the known recipes itself,
	 * after granting starters; after /reload they are re-sent here, since recipes
	 * may have changed.
	 */
	@SubscribeEvent
	public static void onDatapackSync(OnDatapackSyncEvent event) {
		event.getRelevantPlayers().forEach(player -> {
			sendIndex(player);
			if (event.getPlayer() == null)
				AlchemyKnowledge.syncKnown(player);
		});
	}

	/**
	 * Send a player the recipe index: the ingredient allowlist, every recipe's id,
	 * category and result item (never its base or ingredients), and the knowledge
	 * mode. Enough for slot placement, locked book entries, and manuscript names.
	 */
	public static void sendIndex(ServerPlayer player) {
		Index index = index(player.level().getServer());
		List<AlchemyIndexSyncMessage.Entry> entries = new ArrayList<>(index.recipes().size());
		for (RecipeHolder<AlchemyRecipe> holder : index.recipes())
			entries.add(new AlchemyIndexSyncMessage.Entry(idOf(holder), holder.value().category(), holder.value().result().item().value()));
		PacketDistributor.sendToPlayer(player, new AlchemyIndexSyncMessage(List.copyOf(index.ingredientItems()), entries, AlchemyKnowledge.mode()));
	}
}
