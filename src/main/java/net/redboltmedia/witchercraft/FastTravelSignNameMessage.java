package net.redboltmedia.witchercraft;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * HAND-MAINTAINED (locked code element, ~/World Map/Fast Travel). Placement naming for a signpost.
 * CLIENTBOUND: open {@link FastTravelSignNameScreen} prefilled with the sign's default name.
 * SERVERBOUND: the popup closed. A non-empty name renames the sender's own latest placement, an empty
 * one keeps the default; either way the sign becomes discoverable (see {@link FastTravelSigns#applyName}).
 * The packet carries no sign identity.
 */
@EventBusSubscriber
public record FastTravelSignNameMessage(String name) implements CustomPacketPayload {
	private static final int MAX_LENGTH = FastTravelSigns.MAX_NAME_CHARACTERS * 2;
	public static final Type<FastTravelSignNameMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "fast_travel_sign_name"));
	public static final StreamCodec<RegistryFriendlyByteBuf, FastTravelSignNameMessage> STREAM_CODEC = StreamCodec.of(
		(buffer, message) -> buffer.writeUtf(message.name, MAX_LENGTH),
		buffer -> new FastTravelSignNameMessage(buffer.readUtf(MAX_LENGTH)));

	public FastTravelSignNameMessage {
		if (name == null || name.length() > MAX_LENGTH)
			throw new IllegalArgumentException("Invalid signpost name");
	}

	@Override
	public Type<FastTravelSignNameMessage> type() {
		return TYPE;
	}

	public static void handleData(FastTravelSignNameMessage message, IPayloadContext context) {
		if (context.flow() == PacketFlow.CLIENTBOUND)
			context.enqueueWork(() -> FastTravelSignNameScreen.open(message.name));
		else if (context.player() instanceof ServerPlayer player)
			context.enqueueWork(() -> FastTravelSigns.applyName(player, message.name));
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, FastTravelSignNameMessage::handleData);
	}
}
