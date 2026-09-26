package net.redboltmedia.witchercraft;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.RegisterEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * HAND-MAINTAINED (locked code element, ~/World Map/Fast Travel). Authoritative server side of fast
 * travel: travel sessions, destination requests, temporary destination loading, and the guarded
 * XP-debit-and-teleport commit.
 *
 * A session exists only after the player right-clicks a signpost ({@link #startSession}); opening the
 * map any other way never allows travel. The client names a destination by the presentation UUID it was
 * shown and the price it displayed. The server resolves that UUID through the player's own discovery
 * knowledge, never trusts coordinates, and charges nothing unless every check passes at commit time.
 * Server thread only.
 */
@EventBusSubscriber
public final class FastTravel {
	/** The player must stay within this distance (blocks) of the origin sign. */
	public static final double SESSION_RANGE = 8.0;
	private static final int SESSION_TICKS = 20 * 60 * 5;
	private static final int LOAD_TIMEOUT_TICKS = 20 * 10;
	private static final int TICKET_RADIUS = 1;
	private static final int TICKET_REFRESH_TICKS = 20;
	private static final Identifier TICKET_ID = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "fast_travel");
	/** Loads the destination area, expiring by itself 40 ticks after its last refresh so it can never leak. */
	private static final TicketType TICKET = new TicketType(40L, TicketType.FLAG_LOADING);
	/** Arrival columns around the sign: the four sides first (north, east, south, west), then corners, then the outer ring. */
	private static final int[][] ARRIVAL_OFFSETS = arrivalOffsets();
	private static final int[] ARRIVAL_HEIGHTS = {0, 1, -1};

	private static final Map<UUID, Session> SESSIONS = new HashMap<>();
	private static final Map<UUID, Pending> PENDING = new HashMap<>();

	private FastTravel() {
	}

	/** Why a request failed, or how it went. The ordinal is the wire format of {@link FastTravelResultMessage}. */
	public enum Result {
		SUCCESS, TRAVELLING, SESSION_ENDED, NO_SESSION, TOO_FAR, DISABLED, UNKNOWN_DESTINATION, SAME_SIGN, INVALID_STATE,
		IN_COMBAT, MONSTERS_NEARBY, NOT_ENOUGH_XP, PRICE_CHANGED, BUSY, DESTINATION_GONE, NO_SAFE_SPOT, TIMEOUT
	}

	@SubscribeEvent
	public static void registerTicketType(RegisterEvent event) {
		event.register(Registries.TICKET_TYPE, TICKET_ID, () -> TICKET);
	}

	/**
	 * Starts a travel session at the sign whose lower half is {@code anchor}, replacing any earlier session.
	 * Returns SUCCESS, DISABLED, BUSY (a journey is being prepared), or NO_SESSION (not a registered,
	 * complete signpost).
	 */
	public static Result startSession(ServerPlayer player, BlockPos anchor) {
		ServerLevel level = player.level();
		if (!WorldMapServerConfig.fastTravelEnabled() || !level.dimension().equals(Level.OVERWORLD))
			return Result.DISABLED;
		if (PENDING.containsKey(player.getUUID()))
			return Result.BUSY;
		WorldMapPoiInstance origin = WorldMapPoiManager.lifecycleInstanceAt(level.getServer(), level.dimension().identifier(), anchor);
		if (origin == null || !FastTravelSigns.PROVIDER_ID.equals(origin.providerType()) || !FastTravelSigns.isCompleteSign(level, anchor))
			return Result.NO_SESSION;
		SESSIONS.put(player.getUUID(), new Session(origin.markerId(), anchor.immutable(), level.getServer().getTickCount() + SESSION_TICKS));
		return Result.SUCCESS;
	}

	/**
	 * A player right-clicked either half of a signpost at {@code clicked}: start a session there and open the
	 * map in travel mode, or explain why not. Called through the locked procedure FastTravelSignStartTravel.
	 */
	public static void openFromSign(ServerPlayer player, BlockPos clicked) {
		ServerLevel level = player.level();
		BlockPos anchor = FastTravelSigns.isLowerHalf(level.getBlockState(clicked)) ? clicked : clicked.below();
		Result result = startSession(player, anchor);
		if (result != Result.SUCCESS) {
			reply(player, result, 0);
			return;
		}
		UUID origin = SESSIONS.get(player.getUUID()).originMarker();
		PacketDistributor.sendToPlayer(player, new FastTravelOpenMessage(anchor.getX(), anchor.getZ(), WorldMapPoiManager.presentationId(player, origin)));
	}

	/** The origin marker of the player's session, or null without one. */
	public static @Nullable UUID sessionOrigin(ServerPlayer player) {
		Session session = SESSIONS.get(player.getUUID());
		return session == null ? null : session.originMarker();
	}

	/** Ends the session because the player left travel mode, cancelling a journey still being prepared. */
	public static void endSession(ServerPlayer player) {
		SESSIONS.remove(player.getUUID());
		Pending pending = PENDING.remove(player.getUUID());
		if (pending != null)
			releaseTicket(player.level().getServer(), pending);
	}

	/** Handles a confirmed destination. Answers with a result message either way. */
	public static void request(ServerPlayer player, UUID destinationPresentation, int quotedPrice) {
		if (PENDING.containsKey(player.getUUID())) {
			reply(player, Result.BUSY, 0);
			return;
		}
		Session session = SESSIONS.get(player.getUUID());
		Result state = checkPlayer(player, session);
		if (state != Result.SUCCESS) {
			reply(player, state, 0);
			return;
		}
		WorldMapPoiInstance destination = WorldMapPoiManager.discoveredByPresentation(player, destinationPresentation);
		if (destination == null || !FastTravelSigns.PROVIDER_ID.equals(destination.providerType())
			|| !destination.dimension().equals(Level.OVERWORLD.identifier())) {
			reply(player, Result.UNKNOWN_DESTINATION, 0);
			return;
		}
		if (destination.markerId().equals(session.originMarker())) {
			reply(player, Result.SAME_SIGN, 0);
			return;
		}
		int price = FastTravelCosts.price(session.originAnchor(), destination.anchor(), player.isCreative());
		if (price != quotedPrice) {
			reply(player, Result.PRICE_CHANGED, price);
			return;
		}
		if (FastTravelCosts.spendablePoints(player) < price) {
			reply(player, Result.NOT_ENOUGH_XP, price);
			return;
		}
		MinecraftServer server = player.level().getServer();
		ChunkPos chunk = ChunkPos.containing(destination.anchor());
		server.overworld().getChunkSource().addTicketAndLoadWithRadius(TICKET, chunk, TICKET_RADIUS);
		int tick = server.getTickCount();
		PENDING.put(player.getUUID(), new Pending(session.originMarker(), destination.markerId(), destinationPresentation, destination.anchor(),
			chunk, price, tick, tick + LOAD_TIMEOUT_TICKS));
		reply(player, Result.TRAVELLING, price);
	}

	/** Session and player-state checks, repeated at request and at commit. */
	private static Result checkPlayer(ServerPlayer player, @Nullable Session session) {
		ServerLevel level = player.level();
		if (!WorldMapServerConfig.fastTravelEnabled())
			return Result.DISABLED;
		if (session == null || level.getServer().getTickCount() > session.expiresAtTick() || !level.dimension().equals(Level.OVERWORLD))
			return Result.NO_SESSION;
		if (player.distanceToSqr(Vec3.atBottomCenterOf(session.originAnchor())) > SESSION_RANGE * SESSION_RANGE)
			return Result.TOO_FAR;
		if (WorldMapPoiManager.instance(level.getServer(), session.originMarker()) == null || !FastTravelSigns.isCompleteSign(level, session.originAnchor()))
			return Result.NO_SESSION;
		if (!player.isAlive() || player.isSleeping() || player.isPassenger() || player.isSpectator())
			return Result.INVALID_STATE;
		if (WorldMapServerConfig.blockTravelInCombat() && player.hasEffect(WitchercraftModMobEffects.IN_COMBAT))
			return Result.IN_COMBAT;
		if (WorldMapServerConfig.blockTravelNearMonsters() && NearbyMonsters.preventRest(level, player, player.position()))
			return Result.MONSTERS_NEARBY;
		return Result.SUCCESS;
	}

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post event) {
		MinecraftServer server = event.getServer();
		int tick = server.getTickCount();
		Iterator<Map.Entry<UUID, Pending>> pendingIterator = PENDING.entrySet().iterator();
		while (pendingIterator.hasNext()) {
			Map.Entry<UUID, Pending> entry = pendingIterator.next();
			Pending pending = entry.getValue();
			ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
			if (player == null) {
				releaseTicket(server, pending);
				pendingIterator.remove();
			} else if (areaLoaded(server.overworld(), pending.chunk())) {
				pendingIterator.remove();
				Result result = commit(player, pending);
				releaseTicket(server, pending);
				reply(player, result, pending.price());
			} else if (tick > pending.deadlineTick()) {
				pendingIterator.remove();
				releaseTicket(server, pending);
				reply(player, Result.TIMEOUT, pending.price());
			} else if ((tick - pending.startedTick()) % TICKET_REFRESH_TICKS == 0) {
				server.overworld().getChunkSource().addTicketWithRadius(TICKET, pending.chunk(), TICKET_RADIUS);
			}
		}
		if (tick % 20 != 0 || SESSIONS.isEmpty())
			return;
		Iterator<Map.Entry<UUID, Session>> sessionIterator = SESSIONS.entrySet().iterator();
		while (sessionIterator.hasNext()) {
			Map.Entry<UUID, Session> entry = sessionIterator.next();
			if (PENDING.containsKey(entry.getKey()))
				continue;
			ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
			Result state = player == null ? Result.NO_SESSION : checkPlayer(player, entry.getValue());
			// Combat, monsters, sleeping, and riding only block setting off; they do not end the session.
			if (state == Result.NO_SESSION || state == Result.TOO_FAR || state == Result.DISABLED) {
				sessionIterator.remove();
				if (player != null)
					reply(player, Result.SESSION_ENDED, 0);
			}
		}
	}

	/** Re-checks everything with the destination loaded, then debits and teleports in one step. */
	private static Result commit(ServerPlayer player, Pending pending) {
		Session session = SESSIONS.get(player.getUUID());
		if (session == null || !session.originMarker().equals(pending.originMarker()))
			return Result.NO_SESSION;
		Result state = checkPlayer(player, session);
		if (state != Result.SUCCESS)
			return state;
		ServerLevel level = player.level();
		WorldMapPoiInstance destination = WorldMapPoiManager.discoveredByPresentation(player, pending.destinationPresentation());
		if (destination == null || !destination.markerId().equals(pending.destinationMarker()) || !FastTravelSigns.isCompleteSign(level, destination.anchor()))
			return Result.DESTINATION_GONE;
		int price = FastTravelCosts.price(session.originAnchor(), destination.anchor(), player.isCreative());
		if (price != pending.price())
			return Result.PRICE_CHANGED;
		if (FastTravelCosts.spendablePoints(player) < price)
			return Result.NOT_ENOUGH_XP;
		Vec3 arrival = arrivalSpot(level, destination.anchor());
		if (arrival == null)
			return Result.NO_SAFE_SPOT;
		FastTravelCosts.debit(player, price);
		double dx = destination.anchor().getX() + 0.5 - arrival.x;
		double dz = destination.anchor().getZ() + 0.5 - arrival.z;
		float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
		player.teleportTo(level, arrival.x, arrival.y, arrival.z, Set.of(), yaw, 0.0F, true);
		player.resetFallDistance();
		player.setDeltaMovement(Vec3.ZERO);
		SESSIONS.remove(player.getUUID());
		WitchercraftMod.LOGGER.info("Player {} fast-travelled to signpost {} at [{}, {}, {}] for {} XP", player.getGameProfile().name(),
			destination.markerId(), destination.anchor().getX(), destination.anchor().getY(), destination.anchor().getZ(), price);
		return Result.SUCCESS;
	}

	/** A safe standing position beside the sign, searched in a fixed order; null when none exists. */
	private static @Nullable Vec3 arrivalSpot(ServerLevel level, BlockPos anchor) {
		for (int[] offset : ARRIVAL_OFFSETS)
			for (int dy : ARRIVAL_HEIGHTS) {
				Vec3 spot = DismountHelper.findSafeDismountLocation(EntityType.PLAYER, level, anchor.offset(offset[0], dy, offset[1]), true);
				if (spot != null)
					return spot;
			}
		return null;
	}

	private static int[][] arrivalOffsets() {
		int[][] sides = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}, {1, -1}, {1, 1}, {-1, 1}, {-1, -1}};
		java.util.List<int[]> offsets = new java.util.ArrayList<>(java.util.List.of(sides));
		java.util.List<int[]> ring = new java.util.ArrayList<>();
		for (int dx = -2; dx <= 2; dx++)
			for (int dz = -2; dz <= 2; dz++)
				if (Math.max(Math.abs(dx), Math.abs(dz)) == 2)
					ring.add(new int[] {dx, dz});
		ring.sort(java.util.Comparator.<int[]>comparingInt(offset -> offset[0] * offset[0] + offset[1] * offset[1])
			.thenComparingInt(offset -> offset[1]).thenComparingInt(offset -> offset[0]));
		offsets.addAll(ring);
		return offsets.toArray(int[][]::new);
	}

	private static boolean areaLoaded(ServerLevel level, ChunkPos center) {
		for (int dx = -TICKET_RADIUS; dx <= TICKET_RADIUS; dx++)
			for (int dz = -TICKET_RADIUS; dz <= TICKET_RADIUS; dz++)
				if (level.getChunkSource().getChunkNow(center.x() + dx, center.z() + dz) == null)
					return false;
		return true;
	}

	private static void releaseTicket(MinecraftServer server, Pending pending) {
		server.overworld().getChunkSource().removeTicketWithRadius(TICKET, pending.chunk(), TICKET_RADIUS);
	}

	private static void reply(ServerPlayer player, Result result, int price) {
		PacketDistributor.sendToPlayer(player, new FastTravelResultMessage(result, price));
	}

	private static void endWithNotice(ServerPlayer player) {
		boolean had = SESSIONS.containsKey(player.getUUID()) || PENDING.containsKey(player.getUUID());
		endSession(player);
		if (had)
			reply(player, Result.SESSION_ENDED, 0);
	}

	@SubscribeEvent
	public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof ServerPlayer player)
			endSession(player);
	}

	@SubscribeEvent
	public static void onDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof ServerPlayer player)
			endWithNotice(player);
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player)
			endWithNotice(player);
	}

	@SubscribeEvent
	public static void onServerStopping(ServerStoppingEvent event) {
		SESSIONS.clear();
		PENDING.clear();
	}

	private record Session(UUID originMarker, BlockPos originAnchor, int expiresAtTick) {
	}

	private record Pending(UUID originMarker, UUID destinationMarker, UUID destinationPresentation, BlockPos destinationAnchor,
		ChunkPos chunk, int price, int startedTick, int deadlineTick) {
	}
}
