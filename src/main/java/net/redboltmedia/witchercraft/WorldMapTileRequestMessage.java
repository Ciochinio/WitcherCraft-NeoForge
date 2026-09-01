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

/** Client request for a bounded set of visible world-map chunks. */
@EventBusSubscriber
public record WorldMapTileRequestMessage(int requestId, long[] positions, long[] capturedTimes) implements CustomPacketPayload {
	public static final int MAX_POSITIONS = 64;
	public static final Type<WorldMapTileRequestMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "world_map_tile_request"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WorldMapTileRequestMessage> STREAM_CODEC = StreamCodec.of((buffer, message) -> {
		buffer.writeVarInt(message.requestId);
		buffer.writeVarInt(message.positions.length);
		for (long position : message.positions)
			buffer.writeLong(position);
		for (long capturedTime : message.capturedTimes)
			buffer.writeLong(capturedTime);
	}, buffer -> {
		int requestId = buffer.readVarInt();
		if (requestId <= 0)
			throw new IllegalArgumentException("Invalid world-map request ID: " + requestId);
		int count = buffer.readVarInt();
		if (count < 0 || count > MAX_POSITIONS)
			throw new IllegalArgumentException("Invalid world-map tile request count: " + count);
		long[] positions = new long[count];
		long[] capturedTimes = new long[count];
		for (int i = 0; i < count; i++)
			positions[i] = buffer.readLong();
		for (int i = 0; i < count; i++)
			capturedTimes[i] = buffer.readLong();
		return new WorldMapTileRequestMessage(requestId, positions, capturedTimes);
	});

	public WorldMapTileRequestMessage {
		if (positions.length != capturedTimes.length)
			throw new IllegalArgumentException("World-map request positions and revisions differ in length");
		positions = positions.clone();
		capturedTimes = capturedTimes.clone();
	}

	@Override
	public long[] positions() {
		return positions.clone();
	}
	@Override public long[] capturedTimes() { return capturedTimes.clone(); }

	@Override
	public Type<WorldMapTileRequestMessage> type() {
		return TYPE;
	}

	public static void handleData(WorldMapTileRequestMessage message, IPayloadContext context) {
		if (context.flow() == PacketFlow.SERVERBOUND && context.player() instanceof ServerPlayer player)
			context.enqueueWork(() -> WorldMapTerrainCapture.requestTiles(player, message.requestId, message.positions, message.capturedTimes));
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, WorldMapTileRequestMessage::handleData);
	}
}
