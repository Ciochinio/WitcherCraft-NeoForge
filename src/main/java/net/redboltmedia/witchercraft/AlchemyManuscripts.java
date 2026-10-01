package net.redboltmedia.witchercraft;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import net.redboltmedia.witchercraft.init.WitchercraftModItems;

/**
 * Alchemy manuscripts: one generic item (MCreator element {@code Manuscript}, locked
 * code in {@code item/ManuscriptItem}) whose stack stores the recipe it teaches in
 * the {@code witchercraft:manuscript_recipe} data component. Right-click reads it.
 *
 * Naming: "Manuscript: <result>" is resolved from the recipe id on whichever side
 * asks. On the server thread (death messages, /give feedback) it comes from the
 * loaded recipes; anywhere else (client rendering, tooltips) from the client's
 * id-to-result index, so the client never needs the recipe's contents. An id
 * neither side knows gives a plain "Manuscript".
 *
 * Sources: loot chests through NeoForge's built-in {@code neoforge:add_table}
 * loot modifier (data/witchercraft/loot_modifiers/), and villager trades as
 * {@code villager_trade} datapack entries added to a profession's trade tag.
 * Both pick the recipe with the loot function {@code witchercraft:random_manuscript}
 * ({@link RandomManuscriptFunction}), so a new recipe becomes findable with no
 * extra data. How to change the sources: TECHNICAL_DESIGN_DOCUMENT.md 6.11.
 * Both are gated by the loot condition {@code witchercraft:alchemy_manuscripts_enabled},
 * false in ALL_KNOWN mode. Loot is checked per chest roll; trades only when a
 * villager's offers are generated, so offers made before a switch to ALL_KNOWN
 * stay (reading such a manuscript then just says it is known).
 *
 * HAND-MAINTAINED: locked code element. {@link #register} is called from
 * WitchercraftMod's "mod init" user code block.
 */
public final class AlchemyManuscripts {
	private AlchemyManuscripts() {
	}

	private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, WitchercraftMod.MODID);
	private static final DeferredRegister<MapCodec<? extends LootItemCondition>> LOOT_CONDITIONS = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, WitchercraftMod.MODID);
	private static final DeferredRegister<MapCodec<? extends LootItemFunction>> LOOT_FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, WitchercraftMod.MODID);

	/** The recipe id a manuscript teaches. */
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Identifier>> RECIPE = COMPONENTS.registerComponentType("manuscript_recipe",
			builder -> builder.persistent(Identifier.CODEC).networkSynchronized(Identifier.STREAM_CODEC));

	public static final DeferredHolder<MapCodec<? extends LootItemCondition>, MapCodec<EnabledCondition>> ENABLED_CONDITION = LOOT_CONDITIONS
			.register("alchemy_manuscripts_enabled", () -> EnabledCondition.MAP_CODEC);

	public static final DeferredHolder<MapCodec<? extends LootItemFunction>, MapCodec<RandomManuscriptFunction>> RANDOM_MANUSCRIPT_FUNCTION = LOOT_FUNCTIONS
			.register("random_manuscript", () -> RandomManuscriptFunction.MAP_CODEC);

	public static void register(IEventBus modEventBus) {
		COMPONENTS.register(modEventBus);
		LOOT_CONDITIONS.register(modEventBus);
		LOOT_FUNCTIONS.register(modEventBus);
	}

	/**
	 * Every recipe a manuscript may teach, in recipe id order: not a starter, not
	 * {@code "manuscript": false}, and in one of {@code categories} (all categories
	 * when empty).
	 */
	public static List<Identifier> droppable(MinecraftServer server, List<AlchemyRecipe.Category> categories) {
		List<Identifier> ids = new ArrayList<>();
		for (RecipeHolder<AlchemyRecipe> holder : AlchemyRecipes.all(server)) {
			AlchemyRecipe recipe = holder.value();
			if (recipe.dropsAsManuscript() && (categories.isEmpty() || categories.contains(recipe.category())))
				ids.add(AlchemyRecipes.idOf(holder));
		}
		return ids;
	}

	/** A manuscript teaching {@code recipeId}. */
	public static ItemStack create(Identifier recipeId) {
		ItemStack stack = new ItemStack(WitchercraftModItems.MANUSCRIPT.get());
		stack.set(RECIPE.get(), recipeId);
		return stack;
	}

	// ---- name and tooltip -------------------------------------------------------------

	/** "Manuscript: <result>", or null when the id is missing or unknown, so the item keeps its plain name. */
	public static Component name(ItemStack stack) {
		Item result = resultOf(stack.get(RECIPE.get()));
		if (result == null)
			return null;
		return Component.translatable("item.witchercraft.manuscript.named", result.getDefaultInstance().getHoverName());
	}

	/** Client tooltip: how to use it, or that it is already known. */
	public static void tooltip(ItemStack stack, Consumer<Component> lines) {
		Identifier id = stack.get(RECIPE.get());
		if (id == null || AlchemyClientIndex.resultOf(id) == null)
			return;
		if (AlchemyClientIndex.isKnown(id))
			lines.accept(Component.translatable("item.witchercraft.manuscript.tooltip.known").withStyle(ChatFormatting.GRAY));
		else
			lines.accept(Component.translatable("item.witchercraft.manuscript.tooltip.use").withStyle(ChatFormatting.GRAY));
	}

	private static Item resultOf(Identifier id) {
		if (id == null)
			return null;
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server != null && server.isSameThread())
			return AlchemyRecipes.byId(server, id).map(holder -> holder.value().result().item().value()).orElse(null);
		return AlchemyClientIndex.resultOf(id);
	}

	// ---- reading ----------------------------------------------------------------------

	/**
	 * Right-click. A manuscript with no or an unloaded recipe does nothing. One the
	 * player already knows (effective known set, so every recipe in ALL_KNOWN)
	 * says so and is kept. Otherwise the recipe is learned with the "New recipe
	 * learned" toast and one manuscript is consumed (kept in creative, like the
	 * knowledge book). The client predicts the outcome from its own index so the
	 * arm only swings when the manuscript is actually read.
	 */
	public static InteractionResult read(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		Identifier id = stack.get(RECIPE.get());
		if (id == null)
			return InteractionResult.PASS;
		if (!(player instanceof ServerPlayer serverPlayer)) {
			if (AlchemyClientIndex.resultOf(id) == null)
				return InteractionResult.PASS;
			return AlchemyClientIndex.isKnown(id) ? InteractionResult.FAIL : InteractionResult.SUCCESS;
		}
		if (AlchemyRecipes.byId(level.getServer(), id).isEmpty())
			return InteractionResult.PASS;
		if (AlchemyKnowledge.isKnown(serverPlayer, id)) {
			serverPlayer.sendOverlayMessage(Component.translatable("item.witchercraft.manuscript.already_known"));
			return InteractionResult.FAIL;
		}
		AlchemyKnowledge.learn(serverPlayer, id, true);
		player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
		stack.consume(1, player);
		return InteractionResult.SUCCESS;
	}

	// ---- loot condition ----------------------------------------------------------------

	/**
	 * {@code witchercraft:alchemy_manuscripts_enabled}: true unless the knowledge mode
	 * is ALL_KNOWN. Used by the manuscript loot modifiers and as the
	 * {@code merchant_predicate} of manuscript trades.
	 */
	public static final class EnabledCondition implements LootItemCondition {
		private static final EnabledCondition INSTANCE = new EnabledCondition();
		public static final MapCodec<EnabledCondition> MAP_CODEC = MapCodec.unit(INSTANCE);

		private EnabledCondition() {
		}

		@Override
		public MapCodec<EnabledCondition> codec() {
			return MAP_CODEC;
		}

		@Override
		public boolean test(LootContext context) {
			return AlchemyKnowledge.mode() != AlchemyKnowledge.Mode.ALL_KNOWN;
		}
	}

	// ---- loot function -----------------------------------------------------------------

	/**
	 * {@code witchercraft:random_manuscript}: turns the stack into a manuscript for a
	 * random droppable recipe ({@link #droppable}), each equally likely. Optional
	 * {@code "categories": ["potion", "oil", ...]} limits the pick; omitted means
	 * every category. With nothing to pick (say, no decoction recipes yet) the
	 * result is empty, which vanilla drops from chests and treats as "no offer"
	 * for trades. Works in loot tables and in a villager trade's
	 * {@code given_item_modifiers}.
	 */
	public static final class RandomManuscriptFunction extends LootItemConditionalFunction {
		public static final MapCodec<RandomManuscriptFunction> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> commonFields(i)
				.and(AlchemyRecipe.Category.CODEC.listOf().optionalFieldOf("categories", List.of()).forGetter(f -> f.categories))
				.apply(i, RandomManuscriptFunction::new));

		private final List<AlchemyRecipe.Category> categories;

		private RandomManuscriptFunction(List<LootItemCondition> predicates, List<AlchemyRecipe.Category> categories) {
			super(predicates);
			this.categories = List.copyOf(categories);
		}

		@Override
		public MapCodec<RandomManuscriptFunction> codec() {
			return MAP_CODEC;
		}

		@Override
		protected ItemStack run(ItemStack stack, LootContext context) {
			List<Identifier> ids = droppable(context.getLevel().getServer(), categories);
			if (ids.isEmpty())
				return ItemStack.EMPTY;
			Identifier id = ids.get(context.getRandom().nextInt(ids.size()));
			ItemStack manuscript = create(id);
			manuscript.setCount(stack.getCount());
			return manuscript;
		}
	}
}
