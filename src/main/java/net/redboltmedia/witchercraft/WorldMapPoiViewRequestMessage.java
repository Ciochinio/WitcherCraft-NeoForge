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

import java.util.HashSet;
import java.util.Set;

/** Client request for authorized markers in a bounded set of map cells. */
@EventBusSubscriber
public record WorldMapPoiViewRequestMessage(int requestId, Identifier dimension, long definitionGeneration, long[] cells)
	implements CustomPacketPayload {
	public static final int MAX_CELLS = 64;
	private static final int MAX_IDENTIFIER_LENGTH = 256;
	public static final Type<WorldMapPoiViewRequestMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "world_map_poi_view_request"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WorldMapPoiViewRequestMessage> STREAM_CODEC = StreamCodec.of((buffer, message) -> {
		buffer.writeVarInt(message.requestId);
		buffer.writeUtf(message.dimension.toString(), MAX_IDENTIFIER_LENGTH);
		buffer.writeVarLong(message.definitionGeneration);
		buffer.writeVarInt(message.cells.length);
		for (long cell : message.cells)
			buffer.writeLong(cell);
	}, buffer -> {
		int requestId = buffer.readVarInt();
		Identifier dimension = Identifier.tryParse(buffer.readUtf(MAX_IDENTIFIER_LENGTH));
		long generation = buffer.readVarLong();
		int count = buffer.readVarInt();
		if (requestId <= 0 || dimension == null || generation < 0 || count <= 0 || count > MAX_CELLS)
			throw new IllegalArgumentException("Invalid world-map POI view request");
		long[] cells = new long[count];
		Set<Long> unique = new HashSet<>();
		for (int index = 0; index < count; index++) {
			cells[index] = buffer.readLong();
			if (!unique.add(cells[index]) || !WorldMapPoiSpatialIndex.validCell(cells[index]))
				throw new IllegalArgumentException("Invalid or duplicate world-map POI cell");
		}
		return new WorldMapPoiViewRequestMessage(requestId, dimension, generation, cells);
	});

	public WorldMapPoiViewRequestMessage {
		if (requestId <= 0 || dimension == null || definitionGeneration < 0 || cells == null || cells.length == 0 || cells.length > MAX_CELLS)
			throw new IllegalArgumentException("Invalid world-map POI view request");
		Set<Long> unique = new HashSet<>();
		for (long cell : cells)
			if (!unique.add(cell) || !WorldMapPoiSpatialIndex.validCell(cell))
				throw new IllegalArgumentException("Invalid or duplicate world-map POI cell");
		cells = cells.clone();
	}

	@Override
	public long[] cells() {
		return cells.clone();
	}

	@Override
	public Type<WorldMapPoiViewRequestMessage> type() {
		return TYPE;
	}

	public static void handleData(WorldMapPoiViewRequestMessage message, IPayloadContext context) {
		if (context.flow() == PacketFlow.SERVERBOUND && context.player() instanceof ServerPlayer player)
			context.enqueueWork(() -> WorldMapPoiManager.requestMarkers(player, message));
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, WorldMapPoiViewRequestMessage::handleData);
	}
}
