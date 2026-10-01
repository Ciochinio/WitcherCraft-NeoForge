package net.redboltmedia.witchercraft;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Which alchemy recipes each player knows. Server side.
 *
 * The recorded set is a player data attachment ({@link #DATA}), saved with the
 * player file and kept on death. It is its own attachment, never a field of
 * MCreator's PlayerVariables. Unlike WorldMapPoiKnowledge (a world SavedData
 * keyed by UUID), alchemy knowledge is purely per-player, so it travels with the
 * player file and gets copy-on-death for free; the two patterns differ on
 * purpose.
 *
 * Everything that asks "does this player know X" goes through
 * {@link #effectiveKnown}: in ALL_KNOWN mode every recipe counts as known
 * without being recorded, otherwise the recorded set minus ids that no longer
 * name a loaded recipe (renamed or removed recipes are skipped silently).
 * Recording still happens in every mode, so switching back from ALL_KNOWN
 * restores each player's real progress.
 *
 * The client only ever receives the full contents of recipes the player
 * effectively knows ({@link AlchemyKnownSyncMessage}).
 *
 * HAND-MAINTAINED: locked code element. {@link #ATTACHMENT_TYPES} is attached to
 * the mod bus in WitchercraftMod's "mod init" user code block.
 */
@EventBusSubscriber
public final class AlchemyKnowledge {
	private AlchemyKnowledge() {
	}

	/** Server config {@code alchemy.recipeKnowledge}: how much of the recipe list players see. */
	public enum Mode {
		/** Known recipes only; learned from starters, manuscripts, and successful brews. */
		DISCOVERY,
		/** Like DISCOVERY, plus every unknown recipe as a locked entry (result icon and name only). */
		SHOW_LOCKED,
		/** Every recipe in full; nothing to learn. */
		ALL_KNOWN
	}

	/** What one player has learned. Immutable: every change replaces the attachment. */
	public record Data(Set<Identifier> known, boolean startersGranted) {
		public static final Data EMPTY = new Data(Set.of(), false);

		private static final Codec<Set<Identifier>> ID_SET = Identifier.CODEC.listOf().xmap(list -> Set.copyOf(list),
				set -> set.stream().sorted(Comparator.comparing(Identifier::toString)).toList());

		public static final MapCodec<Data> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				ID_SET.optionalFieldOf("known", Set.of()).forGetter(Data::known),
				Codec.BOOL.optionalFieldOf("starters_granted", false).forGetter(Data::startersGranted)).apply(i, Data::new));

		Data with(Set<Identifier> newKnown) {
			return new Data(Set.copyOf(newKnown), startersGranted);
		}
	}

	public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, WitchercraftMod.MODID);
	public static final Supplier<AttachmentType<Data>> DATA = ATTACHMENT_TYPES.register("alchemy_knowledge",
			() -> AttachmentType.builder(() -> Data.EMPTY).serialize(Data.MAP_CODEC).copyOnDeath().build());

	// ---- queries ----------------------------------------------------------------------

	public static Mode mode() {
		return WorldMapServerConfig.alchemyRecipeKnowledge();
	}

	/** The recipes this player counts as knowing, in recipe id order. Every "is it known" check uses this. */
	public static Set<Identifier> effectiveKnown(ServerPlayer player) {
		Set<Identifier> all = AlchemyRecipes.ids(server(player));
		if (mode() == Mode.ALL_KNOWN)
			return all;
		Set<Identifier> recorded = player.getData(DATA).known();
		Set<Identifier> known = new LinkedHashSet<>();
		for (Identifier id : all)
			if (recorded.contains(id))
				known.add(id);
		return known;
	}

	public static boolean isKnown(ServerPlayer player, Identifier id) {
		return effectiveKnown(player).contains(id);
	}

	/** The ids this player has recorded, including any that no longer name a loaded recipe. */
	public static Set<Identifier> recorded(ServerPlayer player) {
		return player.getData(DATA).known();
	}

	// ---- changes ----------------------------------------------------------------------

	/**
	 * Record a recipe (in every mode). If the player did not effectively know it
	 * before, the client receives it, with a "New recipe learned" toast when
	 * {@code announce} is set. Returns whether it was new to the player.
	 */
	public static boolean learn(ServerPlayer player, Identifier id, boolean announce) {
		boolean wasKnown = isKnown(player, id);
		Data data = player.getData(DATA);
		if (!data.known().contains(id)) {
			Set<Identifier> known = new LinkedHashSet<>(data.known());
			known.add(id);
			player.setData(DATA, data.with(known));
		}
		if (!wasKnown)
			AlchemyRecipes.byId(server(player), id).ifPresent(holder -> send(player, false, announce, List.of(holder)));
		return !wasKnown;
	}

	/** Record every loaded recipe. Returns how many were newly recorded. */
	public static int learnAll(ServerPlayer player) {
		Data data = player.getData(DATA);
		Set<Identifier> known = new LinkedHashSet<>(data.known());
		int before = known.size();
		known.addAll(AlchemyRecipes.ids(server(player)));
		player.setData(DATA, data.with(known));
		syncKnown(player);
		return known.size() - before;
	}

	/** Remove one recipe from the record. Returns whether it was recorded. */
	public static boolean forget(ServerPlayer player, Identifier id) {
		Data data = player.getData(DATA);
		if (!data.known().contains(id))
			return false;
		Set<Identifier> known = new LinkedHashSet<>(data.known());
		known.remove(id);
		player.setData(DATA, data.with(known));
		syncKnown(player);
		return true;
	}

	/** Clear the record but keep the starters-granted flag, so starters are not re-granted on the next login. */
	public static void forgetAll(ServerPlayer player) {
		player.setData(DATA, player.getData(DATA).with(Set.of()));
		syncKnown(player);
	}

	/** Back to a brand-new player: clear everything, then grant the starters again. */
	public static void reset(ServerPlayer player) {
		player.setData(DATA, Data.EMPTY);
		grantStarters(player);
		syncKnown(player);
	}

	/** Every recipe marked {@code "starter": true}, once per player. Later starter changes do not re-grant. */
	private static void grantStarters(ServerPlayer player) {
		Data data = player.getData(DATA);
		if (data.startersGranted())
			return;
		Set<Identifier> known = new LinkedHashSet<>(data.known());
		for (RecipeHolder<AlchemyRecipe> holder : AlchemyRecipes.all(server(player)))
			if (holder.value().starter())
				known.add(AlchemyRecipes.idOf(holder));
		player.setData(DATA, new Data(Set.copyOf(known), true));
	}

	// ---- sync -------------------------------------------------------------------------

	/** Replace the client's known recipes with the full contents of every effectively known recipe. */
	public static void syncKnown(ServerPlayer player) {
		MinecraftServer server = server(player);
		List<RecipeHolder<AlchemyRecipe>> known = new ArrayList<>();
		for (Identifier id : effectiveKnown(player))
			AlchemyRecipes.byId(server, id).ifPresent(known::add);
		send(player, true, false, known);
	}

	private static void send(ServerPlayer player, boolean replace, boolean announce, List<RecipeHolder<AlchemyRecipe>> recipes) {
		List<AlchemyKnownSyncMessage.Entry> entries = new ArrayList<>(recipes.size());
		for (RecipeHolder<AlchemyRecipe> holder : recipes)
			entries.add(new AlchemyKnownSyncMessage.Entry(AlchemyRecipes.idOf(holder), holder.value()));
		PacketDistributor.sendToPlayer(player, new AlchemyKnownSyncMessage(replace, announce, entries));
	}

	private static MinecraftServer server(ServerPlayer player) {
		return player.level().getServer();
	}

	// ---- events -----------------------------------------------------------------------

	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			grantStarters(player);
			syncKnown(player);
		}
	}

	/**
	 * A changed knowledge mode takes effect without a restart: when the server
	 * config reloads, every online player gets the index (it carries the mode)
	 * and their known recipes again.
	 */
	@SubscribeEvent
	public static void onConfigReload(ModConfigEvent.Reloading event) {
		ModConfig config = event.getConfig();
		if (config.getType() != ModConfig.Type.SERVER || !WitchercraftMod.MODID.equals(config.getModId()))
			return;
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null)
			return;
		server.execute(() -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				AlchemyRecipes.sendIndex(player);
				syncKnown(player);
			}
		});
	}
}
