package net.redboltmedia.witchercraft;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client-owned optional action-bar presentation for an authoritative POI discovery.
 * A non-empty {@code customName} is shown after the translated kind, as in "Signpost: name".
 */
@EventBusSubscriber
public record WorldMapPoiDiscoveredMessage(String translationKey, String fallbackName, String customName) implements CustomPacketPayload {
	private static final int MAX_TEXT = 160;
	public static final Type<WorldMapPoiDiscoveredMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "world_map_poi_discovered"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WorldMapPoiDiscoveredMessage> STREAM_CODEC = StreamCodec.of(
		(buffer, message) -> {
			buffer.writeUtf(message.translationKey, MAX_TEXT);
			buffer.writeUtf(message.fallbackName, MAX_TEXT);
			buffer.writeUtf(message.customName, MAX_TEXT);
		},
		buffer -> new WorldMapPoiDiscoveredMessage(buffer.readUtf(MAX_TEXT), buffer.readUtf(MAX_TEXT), buffer.readUtf(MAX_TEXT)));

	public WorldMapPoiDiscoveredMessage {
		if (translationKey == null || translationKey.length() > MAX_TEXT || fallbackName == null || fallbackName.length() > MAX_TEXT
			|| customName == null || customName.length() > MAX_TEXT)
			throw new IllegalArgumentException("Invalid POI discovery presentation");
	}

	@Override public Type<WorldMapPoiDiscoveredMessage> type() { return TYPE; }

	public static void handleData(WorldMapPoiDiscoveredMessage message, IPayloadContext context) {
		if (context.flow() == PacketFlow.CLIENTBOUND)
			context.enqueueWork(() -> {
				if (WorldMapClientConfig.discoveryActionBar() && Minecraft.getInstance().player != null) {
					Component name = WorldMapPoiMarker.displayName(Component.translatableWithFallback(message.translationKey, message.fallbackName), message.customName);
					Minecraft.getInstance().gui.setOverlayMessage(Component.translatableWithFallback("message.witchercraft.poi.discovered", "Discovered: %s", name), false);
				}
			});
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, WorldMapPoiDiscoveredMessage::handleData);
	}
}
