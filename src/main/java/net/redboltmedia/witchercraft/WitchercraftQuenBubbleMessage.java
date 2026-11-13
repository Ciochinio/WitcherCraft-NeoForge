package net.redboltmedia.witchercraft;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.player.Player;
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
 * CLIENTBOUND: a player's Active Shield (alt Quen) pool, sent to that player and everyone tracking them so
 * WitchercraftQuenBubbleClient can draw the bubble. Effects and player variables only sync to their owner,
 * so other players would never see the bubble without this. pool 0 = shield down. hit = an absorbed hit
 * (sent by WitchercraftQuenBubbleHit from QuenAbsorb), dx/dz = horizontal direction to the attacker or 0.
 *
 * Sent on every up/down change and every KEEPALIVE_TICKS while up (pool updates and players who start
 * tracking mid-hold). The client drops a bubble that hears nothing for STALE_TICKS.
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber
public record WitchercraftQuenBubbleMessage(int entityId, float pool, boolean hit, float dx, float dz) implements CustomPacketPayload {
	public static final Type<WitchercraftQuenBubbleMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "quen_bubble"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WitchercraftQuenBubbleMessage> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, WitchercraftQuenBubbleMessage::entityId,
			ByteBufCodecs.FLOAT, WitchercraftQuenBubbleMessage::pool, ByteBufCodecs.BOOL, WitchercraftQuenBubbleMessage::hit, ByteBufCodecs.FLOAT, WitchercraftQuenBubbleMessage::dx, ByteBufCodecs.FLOAT,
			WitchercraftQuenBubbleMessage::dz, WitchercraftQuenBubbleMessage::new);
	public static final int KEEPALIVE_TICKS = 5;
	public static final int STALE_TICKS = 20;
	public static final float FADE_IN_TICKS = 4.0F;
	/** 0 = the bubble vanishes the moment the shield drops. */
	public static final float FADE_OUT_TICKS = 0.0F;

	/** Client side: one entry per player whose bubble is up or still fading out. */
	public static final class Bubble {
		public boolean up;
		public float pool;
		public long lastHeard;
		/** Game time of the last up/down switch and the fade value at that moment, so a fade never jumps. */
		public long switchTime;
		public float fadeAtSwitch;
		public float hitTime = -1000.0F;
		public float hitDx;
		public float hitDz;

		/** 0 = invisible, 1 = fully up. */
		public float fade(float now) {
			float t = now - switchTime;
			if (!up && FADE_OUT_TICKS <= 0.0F)
				return 0.0F;
			return up ? Math.min(1.0F, fadeAtSwitch + t / FADE_IN_TICKS) : Math.max(0.0F, fadeAtSwitch - t / FADE_OUT_TICKS);
		}

		public void setUp(boolean value, long now) {
			if (up == value)
				return;
			fadeAtSwitch = fade(now);
			switchTime = now;
			up = value;
		}
	}

	/** Client main thread only (written in enqueueWork, read while rendering). */
	public static final Map<Integer, Bubble> BUBBLES = new HashMap<>();

	private static final Map<ServerPlayer, Boolean> SENT_UP = new WeakHashMap<>();

	@Override
	public Type<WitchercraftQuenBubbleMessage> type() {
		return TYPE;
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;
		float pool = (float) player.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftQuenShield;
		boolean up = player.hasEffect(WitchercraftModMobEffects.QUEN_ACTIVE_SHIELD) && pool > 0;
		boolean wasUp = SENT_UP.getOrDefault(player, false);
		if (up == wasUp && !(up && player.tickCount % KEEPALIVE_TICKS == 0))
			return;
		SENT_UP.put(player, up);
		PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new WitchercraftQuenBubbleMessage(player.getId(), up ? pool : 0, false, 0, 0));
	}

	public static void handleData(WitchercraftQuenBubbleMessage message, IPayloadContext context) {
		if (context.flow() != PacketFlow.CLIENTBOUND)
			return;
		context.enqueueWork(() -> {
			if (Minecraft.getInstance().level == null)
				return;
			long now = Minecraft.getInstance().level.getGameTime();
			boolean up = message.pool() > 0;
			Bubble bubble = BUBBLES.get(message.entityId());
			if (bubble == null) {
				if (!up)
					return;
				bubble = new Bubble();
				BUBBLES.put(message.entityId(), bubble);
			}
			bubble.setUp(up, now);
			if (up)
				bubble.pool = message.pool();
			if (message.hit()) {
				bubble.hitTime = now;
				bubble.hitDx = message.dx();
				bubble.hitDz = message.dz();
			}
			bubble.lastHeard = now;
		});
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, WitchercraftQuenBubbleMessage::handleData);
	}
}
