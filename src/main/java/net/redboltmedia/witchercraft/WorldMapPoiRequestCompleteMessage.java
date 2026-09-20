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

import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

/** Completes one POI cell request and identifies its authoritative server world. */
@EventBusSubscriber
public record WorldMapPoiRequestCompleteMessage(int requestId, boolean accepted, UUID worldId, long definitionGeneration, long[] cells)
	implements CustomPacketPayload {
	public static final Type<WorldMapPoiRequestCompleteMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "world_map_poi_request_complete"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WorldMapPoiRequestCompleteMessage> STREAM_CODEC = StreamCodec.of((buffer, message) -> {
		buffer.writeVarInt(message.requestId);
		buffer.writeBoolean(message.accepted);
		buffer.writeLong(message.worldId.getMostSignificantBits());
		buffer.writeLong(message.worldId.getLeastSignificantBits());
		buffer.writeVarLong(message.definitionGeneration);
		buffer.writeVarInt(message.cells.length);
		for (long cell : message.cells)
			buffer.writeLong(cell);
	}, buffer -> {
		int requestId = buffer.readVarInt();
		boolean accepted = buffer.readBoolean();
		UUID worldId = new UUID(buffer.readLong(), buffer.readLong());
		long generation = buffer.readVarLong();
		int count = buffer.readVarInt();
		if (requestId <= 0 || generation < 0 || count < 0 || count > WorldMapPoiViewRequestMessage.MAX_CELLS)
			throw new IllegalArgumentException("Invalid world-map POI completion");
		long[] cells = new long[count];
		Set<Long> unique = new HashSet<>();
		for (int index = 0; index < count; index++) {
			cells[index] = buffer.readLong();
			if (!unique.add(cells[index]) || !WorldMapPoiSpatialIndex.validCell(cells[index]))
				throw new IllegalArgumentException("Invalid world-map POI completion cell");
		}
		return new WorldMapPoiRequestCompleteMessage(requestId, accepted, worldId, generation, cells);
	});

	public WorldMapPoiRequestCompleteMessage {
		if (requestId <= 0 || worldId == null || definitionGeneration < 0 || cells == null || cells.length > WorldMapPoiViewRequestMessage.MAX_CELLS)
			throw new IllegalArgumentException("Invalid world-map POI completion");
		Set<Long> unique = new HashSet<>();
		for (long cell : cells)
			if (!unique.add(cell) || !WorldMapPoiSpatialIndex.validCell(cell))
				throw new IllegalArgumentException("Invalid world-map POI completion cell");
		cells = cells.clone();
	}

	@Override
	public long[] cells() {
		return cells.clone();
	}

	@Override
	public Type<WorldMapPoiRequestCompleteMessage> type() {
		return TYPE;
	}

	public static void handleData(WorldMapPoiRequestCompleteMessage message, IPayloadContext context) {
		if (context.flow() == PacketFlow.CLIENTBOUND)
			context.enqueueWork(() -> WorldMapPoiClientCache.completeRequest(message));
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, WorldMapPoiRequestCompleteMessage::handleData);
	}
}
