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

/** Marks the end of one authorized world-map tile request batch. */
@EventBusSubscriber
public record WorldMapTileRequestCompleteMessage(int requestId) implements CustomPacketPayload {
	public static final Type<WorldMapTileRequestCompleteMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "world_map_tile_request_complete"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WorldMapTileRequestCompleteMessage> STREAM_CODEC = StreamCodec.of((buffer, message) -> buffer.writeVarInt(message.requestId), buffer -> new WorldMapTileRequestCompleteMessage(buffer.readVarInt()));

	@Override
	public Type<WorldMapTileRequestCompleteMessage> type() {
		return TYPE;
	}

	public static void handleData(WorldMapTileRequestCompleteMessage message, IPayloadContext context) {
		if (context.flow() == PacketFlow.CLIENTBOUND)
			context.enqueueWork(() -> WorldMapClientTileCache.completeRequest(message.requestId));
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, WorldMapTileRequestCompleteMessage::handleData);
	}
}
