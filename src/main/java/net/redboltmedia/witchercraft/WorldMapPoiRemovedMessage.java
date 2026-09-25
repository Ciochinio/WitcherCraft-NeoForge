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

/**
 * Tells one client that a POI it knew was deleted, such as a destroyed fast-travel sign.
 * Carries only that player's random presentation UUID, never the authoritative marker identity.
 */
@EventBusSubscriber
public record WorldMapPoiRemovedMessage(Identifier dimension, UUID presentationId) implements CustomPacketPayload {
	private static final int MAX_IDENTIFIER_LENGTH = 256;
	public static final Type<WorldMapPoiRemovedMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "world_map_poi_removed"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WorldMapPoiRemovedMessage> STREAM_CODEC = StreamCodec.of((buffer, message) -> {
		buffer.writeUtf(message.dimension.toString(), MAX_IDENTIFIER_LENGTH);
		buffer.writeLong(message.presentationId.getMostSignificantBits());
		buffer.writeLong(message.presentationId.getLeastSignificantBits());
	}, buffer -> {
		Identifier dimension = Identifier.tryParse(buffer.readUtf(MAX_IDENTIFIER_LENGTH));
		if (dimension == null)
			throw new IllegalArgumentException("Invalid world-map POI removal dimension");
		return new WorldMapPoiRemovedMessage(dimension, new UUID(buffer.readLong(), buffer.readLong()));
	});

	public WorldMapPoiRemovedMessage {
		if (dimension == null || presentationId == null)
			throw new IllegalArgumentException("Invalid world-map POI removal");
	}

	@Override
	public Type<WorldMapPoiRemovedMessage> type() {
		return TYPE;
	}

	public static void handleData(WorldMapPoiRemovedMessage message, IPayloadContext context) {
		if (context.flow() == PacketFlow.CLIENTBOUND)
			context.enqueueWork(() -> WorldMapPoiClientCache.acceptRemoval(message.dimension, message.presentationId));
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, WorldMapPoiRemovedMessage::handleData);
	}
}
