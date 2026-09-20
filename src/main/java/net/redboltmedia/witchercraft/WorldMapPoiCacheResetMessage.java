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

/** Invalidates all cached POI presentation after login or definition reload. */
@EventBusSubscriber
public record WorldMapPoiCacheResetMessage(UUID worldId, long definitionGeneration) implements CustomPacketPayload {
	public static final Type<WorldMapPoiCacheResetMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "world_map_poi_cache_reset"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WorldMapPoiCacheResetMessage> STREAM_CODEC = StreamCodec.of((buffer, message) -> {
		buffer.writeLong(message.worldId.getMostSignificantBits());
		buffer.writeLong(message.worldId.getLeastSignificantBits());
		buffer.writeVarLong(message.definitionGeneration);
	}, buffer -> new WorldMapPoiCacheResetMessage(new UUID(buffer.readLong(), buffer.readLong()), buffer.readVarLong()));

	public WorldMapPoiCacheResetMessage {
		if (worldId == null || definitionGeneration < 0)
			throw new IllegalArgumentException("Invalid world-map POI cache reset");
	}

	@Override
	public Type<WorldMapPoiCacheResetMessage> type() {
		return TYPE;
	}

	public static void handleData(WorldMapPoiCacheResetMessage message, IPayloadContext context) {
		if (context.flow() == PacketFlow.CLIENTBOUND)
			context.enqueueWork(() -> WorldMapPoiClientCache.reset(message.worldId, message.definitionGeneration));
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, WorldMapPoiCacheResetMessage::handleData);
	}
}
