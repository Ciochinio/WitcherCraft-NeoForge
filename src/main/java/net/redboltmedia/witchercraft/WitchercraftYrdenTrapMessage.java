package net.redboltmedia.witchercraft;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.client.Minecraft;

import java.util.WeakHashMap;
import java.util.Map;
import java.util.HashMap;

/**
 * CLIENTBOUND: where a player's Yrden trap is (x, y, z = the cast position) and whether it is up, sent to
 * the caster and everyone tracking them so WitchercraftYrdenTrapClient can draw the pillars and glyphs.
 * The trap itself lives in the caster's player variables (witchercraftYrdenX/Y/Z/Ticks, set by YrdenCast,
 * run down by YrdenTrapTick), which only sync to their owner.
 *
 * mode: 0 = base trap, 1 = Magic Trap turret (witchercraftYrdenMode). shotTarget: the entity a turret shot
 * just hit (sent by WitchercraftYrdenTurretShoot), -1 otherwise.
 *
 * Sent when the trap goes up, moves (recast), changes mode or ends, and every KEEPALIVE_TICKS while up.
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber
public record WitchercraftYrdenTrapMessage(int ownerId, double x, double y, double z, boolean up, int mode, int shotTarget) implements CustomPacketPayload {
	public static final Type<WitchercraftYrdenTrapMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "yrden_trap"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WitchercraftYrdenTrapMessage> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, WitchercraftYrdenTrapMessage::ownerId, ByteBufCodecs.DOUBLE,
			WitchercraftYrdenTrapMessage::x, ByteBufCodecs.DOUBLE, WitchercraftYrdenTrapMessage::y, ByteBufCodecs.DOUBLE, WitchercraftYrdenTrapMessage::z, ByteBufCodecs.BOOL, WitchercraftYrdenTrapMessage::up,
			ByteBufCodecs.VAR_INT, WitchercraftYrdenTrapMessage::mode, ByteBufCodecs.INT, WitchercraftYrdenTrapMessage::shotTarget, WitchercraftYrdenTrapMessage::new);
	public static final int BOLT_TICKS = 4;

	/** Client side: a turret bolt being drawn, from the glyph to an entity. */
	public static final class Bolt {
		public int ownerId;
		public int targetId;
		public long time;
		/** The turret's cast position (the bolt starts at the glyph above its ground). */
		public double x, y, z;
	}

	/** Client main thread only. */
	public static final java.util.List<Bolt> BOLTS = new java.util.ArrayList<>();
	public static final int KEEPALIVE_TICKS = 10;
	public static final int STALE_TICKS = 30;

	/** Client side: one entry per visible trap. */
	public static final class Trap {
		public double x, y, z;
		public int mode;
		public long upSince;
		public long lastHeard;
	}

	/** Client main thread only (written in enqueueWork, read while rendering). Keyed by the caster's id. */
	public static final Map<Integer, Trap> TRAPS = new HashMap<>();

	private static final Map<ServerPlayer, double[]> SENT = new WeakHashMap<>();

	@Override
	public Type<WitchercraftYrdenTrapMessage> type() {
		return TYPE;
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;
		WitchercraftModVariables.PlayerVariables vars = player.getData(WitchercraftModVariables.PLAYER_VARIABLES);
		boolean up = vars.witchercraftYrdenTicks > 0;
		double[] last = SENT.get(player);
		boolean wasUp = last != null;
		boolean moved = up && wasUp && (last[0] != vars.witchercraftYrdenX || last[1] != vars.witchercraftYrdenY || last[2] != vars.witchercraftYrdenZ || last[3] != vars.witchercraftYrdenMode);
		if (up == wasUp && !moved && !(up && player.tickCount % KEEPALIVE_TICKS == 0))
			return;
		if (up)
			SENT.put(player, new double[]{vars.witchercraftYrdenX, vars.witchercraftYrdenY, vars.witchercraftYrdenZ, vars.witchercraftYrdenMode});
		else
			SENT.remove(player);
		PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
				new WitchercraftYrdenTrapMessage(player.getId(), vars.witchercraftYrdenX, vars.witchercraftYrdenY, vars.witchercraftYrdenZ, up, (int) vars.witchercraftYrdenMode, -1));
	}

	public static void handleData(WitchercraftYrdenTrapMessage message, IPayloadContext context) {
		if (context.flow() != PacketFlow.CLIENTBOUND)
			return;
		context.enqueueWork(() -> {
			if (Minecraft.getInstance().level == null)
				return;
			long now = Minecraft.getInstance().level.getGameTime();
			if (message.shotTarget() >= 0) {
				Bolt bolt = new Bolt();
				bolt.ownerId = message.ownerId();
				bolt.targetId = message.shotTarget();
				bolt.time = now;
				bolt.x = message.x();
				bolt.y = message.y();
				bolt.z = message.z();
				BOLTS.add(bolt);
			}
			if (!message.up()) {
				TRAPS.remove(message.ownerId());
				return;
			}
			Trap trap = TRAPS.get(message.ownerId());
			if (trap == null || trap.x != message.x() || trap.y != message.y() || trap.z != message.z() || trap.mode != message.mode()) {
				trap = new Trap();
				trap.x = message.x();
				trap.y = message.y();
				trap.z = message.z();
				trap.mode = message.mode();
				trap.upSince = now;
				TRAPS.put(message.ownerId(), trap);
			}
			trap.lastHeard = now;
		});
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, WitchercraftYrdenTrapMessage::handleData);
	}
}
