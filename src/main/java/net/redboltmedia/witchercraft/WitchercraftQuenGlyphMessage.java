package net.redboltmedia.witchercraft;

import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

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
 * CLIENTBOUND: whether a player has the normal Quen shield, sent to that player and everyone tracking them so
 * WitchercraftQuenGlyphClient can draw the orbiting glyph. Effects only sync to their owner, so other players
 * would never see it without this (same reason as WitchercraftQuenBubbleMessage).
 *
 * Sent on every up/down change and every KEEPALIVE_TICKS while up (players who start tracking mid-shield).
 * The client drops a glyph that hears nothing for STALE_TICKS.
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber
public record WitchercraftQuenGlyphMessage(int entityId, boolean up) implements CustomPacketPayload {
	public static final Type<WitchercraftQuenGlyphMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "quen_glyph"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WitchercraftQuenGlyphMessage> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, WitchercraftQuenGlyphMessage::entityId, ByteBufCodecs.BOOL,
			WitchercraftQuenGlyphMessage::up, WitchercraftQuenGlyphMessage::new);
	public static final int KEEPALIVE_TICKS = 5;
	public static final int STALE_TICKS = 20;

	/** Client main thread only: entity id to the game time it was last reported up. */
	public static final Map<Integer, Long> GLYPHS = new HashMap<>();

	private static final Map<ServerPlayer, Boolean> SENT_UP = new WeakHashMap<>();

	@Override
	public Type<WitchercraftQuenGlyphMessage> type() {
		return TYPE;
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;
		boolean up = player.hasEffect(WitchercraftModMobEffects.QUEN_EFFECT);
		boolean wasUp = SENT_UP.getOrDefault(player, false);
		if (up == wasUp && !(up && player.tickCount % KEEPALIVE_TICKS == 0))
			return;
		SENT_UP.put(player, up);
		PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new WitchercraftQuenGlyphMessage(player.getId(), up));
	}

	public static void handleData(WitchercraftQuenGlyphMessage message, IPayloadContext context) {
		if (context.flow() != PacketFlow.CLIENTBOUND)
			return;
		context.enqueueWork(() -> {
			if (Minecraft.getInstance().level == null)
				return;
			if (message.up())
				GLYPHS.put(message.entityId(), Minecraft.getInstance().level.getGameTime());
			else
				GLYPHS.remove(message.entityId());
		});
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, WitchercraftQuenGlyphMessage::handleData);
	}
}
