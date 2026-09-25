package net.redboltmedia.witchercraft;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import net.redboltmedia.witchercraft.block.FastTravelSignBlock;
import net.redboltmedia.witchercraft.init.WitchercraftModBlocks;

import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HAND-MAINTAINED (locked code element, ~/World Map/Fast Travel). Server-side lifecycle for
 * fast-travel signposts: registration as shared POIs, stale-record removal, and placement naming.
 *
 * The {@code FastTravelSign} block and its Blockly procedures own placement and destruction; they
 * call this class through the locked procedures {@code FastTravelSignRegister},
 * {@code FastTravelSignRemove}, and {@code FastTravelSignDropsItem}. The shared POI store is the
 * authoritative sign state: each placement gets a fresh random identity and its name lives in the
 * POI record, so no block entity is needed.
 */
@EventBusSubscriber
public final class FastTravelSigns {
	public static final Identifier DEFINITION_ID = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "fast_travel_sign");
	public static final Identifier PROVIDER_ID = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "fast_travel_sign");
	/** POI source for signs placed by players. These count toward the player-placed sign limit. */
	public static final Identifier SOURCE_PLAYER = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "player_placed");
	/** POI source reserved for village-generated signs (stage 2). */
	public static final Identifier SOURCE_VILLAGE = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "village");
	public static final int MAX_NAME_CHARACTERS = WorldMapPoiInstance.MAX_CUSTOM_NAME_CHARACTERS;
	/** How long after placement the placer may still submit a name. */
	private static final int NAMING_TIMEOUT_TICKS = 20 * 60 * 5;

	private static final Map<UUID, NamingSession> NAMING = new ConcurrentHashMap<>();

	private FastTravelSigns() {
	}

	/** True for the lower half, which is the anchor of a sign. */
	public static boolean isLowerHalf(BlockState state) {
		return state.is(WitchercraftModBlocks.FAST_TRAVEL_SIGN.get()) && !state.getValue(FastTravelSignBlock.UPPER);
	}

	public static boolean isUpperHalf(BlockState state) {
		return state.is(WitchercraftModBlocks.FAST_TRAVEL_SIGN.get()) && state.getValue(FastTravelSignBlock.UPPER);
	}

	/** A complete two-block sign stands with its lower half at {@code anchor}. */
	public static boolean isCompleteSign(BlockGetter level, BlockPos anchor) {
		return isLowerHalf(level.getBlockState(anchor)) && isUpperHalf(level.getBlockState(anchor.above()));
	}

	/** The map shows names as "Signpost: <name>", so the default name is just the coordinates. */
	public static String defaultName(BlockPos anchor) {
		return anchor.getX() + ", " + anchor.getZ();
	}

	/**
	 * Registers a player-placed sign whose lower half was just placed at {@code anchor}, and opens the
	 * naming screen for the placer. Returns false when placement must be undone. Client-side calls
	 * return true so the client predicts the upper half; the server result is authoritative.
	 */
	public static boolean registerPlaced(LevelAccessor world, BlockPos anchor, Entity placer) {
		if (!(world instanceof ServerLevel level))
			return true;
		if (!level.dimension().equals(Level.OVERWORLD)) {
			reject(placer, "overworld_only", "Signposts can only be placed in the Overworld.");
			return false;
		}
		if (level.isOutsideBuildHeight(anchor.above())) {
			reject(placer, "no_room", "There is no room for a signpost here.");
			return false;
		}
		MinecraftServer server = level.getServer();
		int limit = WorldMapServerConfig.playerSignLimit();
		if (limit > 0 && WorldMapPoiManager.countInstances(server, PROVIDER_ID, SOURCE_PLAYER) >= limit) {
			reject(placer, "limit_reached", "This world has reached its signpost limit.");
			return false;
		}
		boolean naming = placer instanceof ServerPlayer;
		WorldMapPoiInstance instance = register(level, anchor, SOURCE_PLAYER, !naming);
		if (instance == null) {
			reject(placer, "failed", "This signpost could not be registered.");
			return false;
		}
		if (placer instanceof ServerPlayer player) {
			// The sign stays undiscoverable until naming ends, so nobody discovers it under its default name.
			NamingSession previous = NAMING.put(player.getUUID(), new NamingSession(instance.markerId(), server.getTickCount() + NAMING_TIMEOUT_TICKS));
			if (previous != null)
				finishNaming(server, player, previous.markerId());
			PacketDistributor.sendToPlayer(player, new FastTravelSignNameMessage(instance.customName()));
		}
		return true;
	}

	/**
	 * Creates a destination record for a sign at {@code anchor} with a fresh identity and the default
	 * name, replacing any older record at the same anchor.
	 */
	public static @Nullable WorldMapPoiInstance register(ServerLevel level, BlockPos anchor, Identifier source, boolean discoverable) {
		MinecraftServer server = level.getServer();
		Identifier dimension = level.dimension().identifier();
		WorldMapPoiInstance previous = WorldMapPoiManager.lifecycleInstanceAt(server, dimension, anchor);
		if (previous != null)
			WorldMapPoiManager.removeInstance(server, previous.markerId());
		String identity = PROVIDER_ID + "|" + dimension + "|" + UUID.randomUUID();
		WorldMapPoiInstance instance = WorldMapPoiInstance.observed(DEFINITION_ID, PROVIDER_ID, source, identity, dimension, anchor)
			.withCustomName(defaultName(anchor));
		return WorldMapPoiManager.putLifecycleInstance(server, instance, discoverable) ? instance : null;
	}

	/**
	 * Deletes the destination record of a sign that no longer stands, checking both anchors a broken
	 * half could belong to ({@code pos} as a lower half, or the block below it for an upper half).
	 * Returns true when a record was removed, which is when the Blockly caller may drop an item.
	 */
	public static boolean removeIfGone(LevelAccessor world, BlockPos pos) {
		if (!(world instanceof ServerLevel level))
			return false;
		MinecraftServer server = level.getServer();
		Identifier dimension = level.dimension().identifier();
		boolean removed = false;
		for (BlockPos anchor : new BlockPos[] {pos, pos.below()}) {
			WorldMapPoiInstance instance = WorldMapPoiManager.lifecycleInstanceAt(server, dimension, anchor);
			if (instance != null && PROVIDER_ID.equals(instance.providerType()) && !isCompleteSign(level, anchor))
				removed |= WorldMapPoiManager.removeInstance(server, instance.markerId());
		}
		return removed;
	}

	public static boolean dropsItem() {
		return WorldMapServerConfig.signDropsItem();
	}

	/**
	 * Ends the placement naming session. Only the placer's latest sign can be named, once. An empty or
	 * invalid name keeps the default. Either way the sign then becomes discoverable.
	 */
	public static void applyName(ServerPlayer player, String submitted) {
		NamingSession session = NAMING.remove(player.getUUID());
		MinecraftServer server = player.level().getServer();
		if (session == null)
			return;
		String name = submitted == null ? "" : submitted.strip();
		if (server.getTickCount() <= session.expiresAtTick() && !name.isEmpty() && WorldMapPoiInstance.validCustomName(name))
			WorldMapPoiManager.renameInstance(server, session.markerId(), name);
		finishNaming(server, player, session.markerId());
	}

	/** Releases a sign held during naming and discovers it for its placer right away. */
	private static void finishNaming(MinecraftServer server, @Nullable ServerPlayer placer, UUID markerId) {
		WorldMapPoiManager.makeDiscoverable(server, markerId);
		if (placer != null)
			WorldMapPoiManager.discoverFor(placer, markerId);
	}

	private static void reject(Entity placer, String reason, String fallback) {
		if (placer instanceof ServerPlayer player)
			player.sendSystemMessage(Component.translatableWithFallback("message.witchercraft.fast_travel.sign." + reason, fallback), true);
	}

	@SubscribeEvent
	public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		NamingSession session = NAMING.remove(event.getEntity().getUUID());
		if (session != null && event.getEntity() instanceof ServerPlayer player)
			finishNaming(player.level().getServer(), player, session.markerId());
	}

	/** Releases abandoned naming sessions (the client never answered) so their signs become discoverable. */
	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post event) {
		MinecraftServer server = event.getServer();
		if (NAMING.isEmpty() || server.getTickCount() % 20 != 0)
			return;
		NAMING.entrySet().removeIf(entry -> {
			if (server.getTickCount() <= entry.getValue().expiresAtTick())
				return false;
			finishNaming(server, server.getPlayerList().getPlayer(entry.getKey()), entry.getValue().markerId());
			return true;
		});
	}

	@SubscribeEvent
	public static void onServerStopping(ServerStoppingEvent event) {
		NAMING.clear();
	}

	private record NamingSession(UUID markerId, int expiresAtTick) {
	}
}
