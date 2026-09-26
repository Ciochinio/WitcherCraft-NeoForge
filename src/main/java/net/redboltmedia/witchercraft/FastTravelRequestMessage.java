package net.redboltmedia.witchercraft;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * HAND-MAINTAINED (locked code element, ~/World Map/Fast Travel). SERVERBOUND: the player confirmed a
 * journey to the discovered signpost the client knows as {@code destination} (its presentation UUID) at
 * the price the client displayed. The server re-derives everything; see {@link FastTravel#request}.
 */
@EventBusSubscriber
public record FastTravelRequestMessage(UUID destination, int quotedPrice) implements CustomPacketPayload {
	public static final Type<FastTravelRequestMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "fast_travel_request"));
	public static final StreamCodec<RegistryFriendlyByteBuf, FastTravelRequestMessage> STREAM_CODEC = StreamCodec.of(
		(buffer, message) -> {
			UUIDUtil.STREAM_CODEC.encode(buffer, message.destination);
			buffer.writeVarInt(message.quotedPrice);
		},
		buffer -> new FastTravelRequestMessage(UUIDUtil.STREAM_CODEC.decode(buffer), buffer.readVarInt()));

	public FastTravelRequestMessage {
		if (destination == null || quotedPrice < 0)
			throw new IllegalArgumentException("Invalid fast-travel request");
	}

	@Override
	public Type<FastTravelRequestMessage> type() {
		return TYPE;
	}

	public static void handleData(FastTravelRequestMessage message, IPayloadContext context) {
		if (context.flow() == PacketFlow.SERVERBOUND && context.player() instanceof ServerPlayer player)
			context.enqueueWork(() -> FastTravel.request(player, message.destination, message.quotedPrice));
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, FastTravelRequestMessage::handleData);
	}
}
