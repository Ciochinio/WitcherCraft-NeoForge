package net.redboltmedia.witchercraft;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.ArrayList;

/**
 * CLIENTBOUND: a mob started winding up an attack on this player (WitchercraftAttackWindup), lasting
 * `ticks`. WitchercraftHud draws one closing tick per entry around the crosshair. Sent only when
 * WitchercraftParryIndicatorEnabled allows it.
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber
public record WitchercraftParryIndicatorMessage(int mobId, int ticks) implements CustomPacketPayload {
	public static final Type<WitchercraftParryIndicatorMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "parry_indicator"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WitchercraftParryIndicatorMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, WitchercraftParryIndicatorMessage::mobId, ByteBufCodecs.VAR_INT, WitchercraftParryIndicatorMessage::ticks, WitchercraftParryIndicatorMessage::new);

	/** start is the client level's game time when the message arrived. */
	public record Incoming(int mobId, long start, int ticks) {
	}

	/** Client main thread only (written in enqueueWork, read while drawing the HUD). */
	public static final List<Incoming> INCOMING = new ArrayList<>();

	@Override
	public Type<WitchercraftParryIndicatorMessage> type() {
		return TYPE;
	}

	public static void handleData(WitchercraftParryIndicatorMessage message, IPayloadContext context) {
		if (context.flow() != PacketFlow.CLIENTBOUND)
			return;
		context.enqueueWork(() -> {
			if (Minecraft.getInstance().level == null)
				return;
			INCOMING.removeIf(in -> in.mobId() == message.mobId());
			INCOMING.add(new Incoming(message.mobId(), Minecraft.getInstance().level.getGameTime(), message.ticks()));
		});
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, WitchercraftParryIndicatorMessage::handleData);
	}
}