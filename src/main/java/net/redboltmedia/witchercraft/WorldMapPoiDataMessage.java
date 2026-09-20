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

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** One bounded batch of authorized POI marker presentations. */
@EventBusSubscriber
public record WorldMapPoiDataMessage(int requestId, long definitionGeneration, Identifier dimension, List<WorldMapPoiMarker> markers)
	implements CustomPacketPayload {
	public static final int MAX_MARKERS = 64;
	private static final int MAX_IDENTIFIER_LENGTH = 256;
	private static final int MAX_TRANSLATION_KEY_LENGTH = 160;
	public static final Type<WorldMapPoiDataMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "world_map_poi_data"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WorldMapPoiDataMessage> STREAM_CODEC = StreamCodec.of((buffer, message) -> {
		buffer.writeVarInt(message.requestId);
		buffer.writeVarLong(message.definitionGeneration);
		buffer.writeUtf(message.dimension.toString(), MAX_IDENTIFIER_LENGTH);
		buffer.writeVarInt(message.markers.size());
		for (WorldMapPoiMarker marker : message.markers) {
			buffer.writeByte(marker instanceof WorldMapPoiMarker.Discovered ? 1 : 0);
			writeUuid(buffer, marker.markerId());
			buffer.writeDouble(marker.x());
			buffer.writeDouble(marker.z());
			buffer.writeDouble(marker.minimumZoom());
			buffer.writeBoolean(marker.defaultVisible());
			if (marker instanceof WorldMapPoiMarker.Discovered discovered) {
				buffer.writeUtf(discovered.translationKey(), MAX_TRANSLATION_KEY_LENGTH);
				buffer.writeUtf(discovered.descriptionTranslationKey(), MAX_TRANSLATION_KEY_LENGTH);
				buffer.writeUtf(discovered.category().toString(), MAX_IDENTIFIER_LENGTH);
				buffer.writeUtf(discovered.icon().toString(), MAX_IDENTIFIER_LENGTH);
			}
		}
	}, buffer -> {
		int requestId = buffer.readVarInt();
		long generation = buffer.readVarLong();
		Identifier dimension = Identifier.tryParse(buffer.readUtf(MAX_IDENTIFIER_LENGTH));
		int count = buffer.readVarInt();
		if (requestId < 0 || generation < 0 || dimension == null || count < 0 || count > MAX_MARKERS)
			throw new IllegalArgumentException("Invalid world-map POI data batch");
		List<WorldMapPoiMarker> markers = new ArrayList<>(count);
		for (int index = 0; index < count; index++) {
			int state = buffer.readUnsignedByte();
			UUID markerId = readUuid(buffer);
			double x = buffer.readDouble();
			double z = buffer.readDouble();
			double minimumZoom = buffer.readDouble();
			boolean defaultVisible = buffer.readBoolean();
			if (state == 0) {
				markers.add(new WorldMapPoiMarker.Unknown(markerId, x, z, minimumZoom, defaultVisible));
			} else if (state == 1) {
				String translationKey = buffer.readUtf(MAX_TRANSLATION_KEY_LENGTH);
				String descriptionTranslationKey = buffer.readUtf(MAX_TRANSLATION_KEY_LENGTH);
				Identifier category = Identifier.tryParse(buffer.readUtf(MAX_IDENTIFIER_LENGTH));
				Identifier icon = Identifier.tryParse(buffer.readUtf(MAX_IDENTIFIER_LENGTH));
				if (category == null || icon == null)
					throw new IllegalArgumentException("Invalid discovered POI identifiers");
				markers.add(new WorldMapPoiMarker.Discovered(markerId, x, z, translationKey, descriptionTranslationKey, category, icon, minimumZoom, defaultVisible));
			} else {
				throw new IllegalArgumentException("Invalid world-map POI knowledge state");
			}
		}
		return new WorldMapPoiDataMessage(requestId, generation, dimension, markers);
	});

	public WorldMapPoiDataMessage {
		if (requestId < 0 || definitionGeneration < 0 || dimension == null || markers == null || markers.size() > MAX_MARKERS)
			throw new IllegalArgumentException("Invalid world-map POI data batch");
		markers = List.copyOf(markers);
		Set<UUID> unique = new HashSet<>();
		for (WorldMapPoiMarker marker : markers)
			if (marker == null || !unique.add(marker.markerId()))
				throw new IllegalArgumentException("Invalid or duplicate world-map POI data entry");
	}

	@Override
	public Type<WorldMapPoiDataMessage> type() {
		return TYPE;
	}

	public static void handleData(WorldMapPoiDataMessage message, IPayloadContext context) {
		if (context.flow() == PacketFlow.CLIENTBOUND)
			context.enqueueWork(() -> WorldMapPoiClientCache.acceptData(message));
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, WorldMapPoiDataMessage::handleData);
	}

	private static void writeUuid(RegistryFriendlyByteBuf buffer, UUID id) {
		buffer.writeLong(id.getMostSignificantBits());
		buffer.writeLong(id.getLeastSignificantBits());
	}

	private static UUID readUuid(RegistryFriendlyByteBuf buffer) {
		return new UUID(buffer.readLong(), buffer.readLong());
	}
}
