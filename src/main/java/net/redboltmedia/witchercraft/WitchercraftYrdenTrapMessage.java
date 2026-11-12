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
 * Sent when the trap goes up, moves (recast) or ends, and every KEEPALIVE_TICKS while up.
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber
public record WitchercraftYrdenTrapMessage(int ownerId, double x, double y, double z, boolean up) implements CustomPacketPayload {
	public static final Type<WitchercraftYrdenTrapMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "yrden_trap"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WitchercraftYrdenTrapMessage> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, WitchercraftYrdenTrapMessage::ownerId, ByteBufCodecs.DOUBLE,
			WitchercraftYrdenTrapMessage::x, ByteBufCodecs.DOUBLE, WitchercraftYrdenTrapMessage::y, ByteBufCodecs.DOUBLE, WitchercraftYrdenTrapMessage::z, ByteBufCodecs.BOOL, WitchercraftYrdenTrapMessage::up,
			WitchercraftYrdenTrapMessage::new);
	public static final int KEEPALIVE_TICKS = 10;
	public static final int STALE_TICKS = 30;

	/** Client side: one entry per visible trap. */
	public static final class Trap {
		public double x, y, z;
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
		boolean moved = up && wasUp && (last[0] != vars.witchercraftYrdenX || last[1] != vars.witchercraftYrdenY || last[2] != vars.witchercraftYrdenZ);
		if (up == wasUp && !moved && !(up && player.tickCount % KEEPALIVE_TICKS == 0))
			return;
		if (up)
			SENT.put(player, new double[]{vars.witchercraftYrdenX, vars.witchercraftYrdenY, vars.witchercraftYrdenZ});
		else
			SENT.remove(player);
		PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new WitchercraftYrdenTrapMessage(player.getId(), vars.witchercraftYrdenX, vars.witchercraftYrdenY, vars.witchercraftYrdenZ, up));
	}

	public static void handleData(WitchercraftYrdenTrapMessage message, IPayloadContext context) {
		if (context.flow() != PacketFlow.CLIENTBOUND)
			return;
		context.enqueueWork(() -> {
			if (Minecraft.getInstance().level == null)
				return;
			long now = Minecraft.getInstance().level.getGameTime();
			if (!message.up()) {
				TRAPS.remove(message.ownerId());
				return;
			}
			Trap trap = TRAPS.get(message.ownerId());
			if (trap == null || trap.x != message.x() || trap.y != message.y() || trap.z != message.z()) {
				trap = new Trap();
				trap.x = message.x();
				trap.y = message.y();
				trap.z = message.z();
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
