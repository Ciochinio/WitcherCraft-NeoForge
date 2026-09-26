package net.redboltmedia.witchercraft;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Locale;

/**
 * HAND-MAINTAINED (locked code element, ~/World Map/Fast Travel). CLIENTBOUND: the outcome of a travel
 * request, or notice that the travel session ended. {@code price} is the current price where it matters
 * (price changed, not enough XP, travelling). Until the map travel mode exists (stage 4), the client
 * shows the outcome as an action-bar message.
 */
@EventBusSubscriber
public record FastTravelResultMessage(FastTravel.Result result, int price) implements CustomPacketPayload {
	private static final FastTravel.Result[] RESULTS = FastTravel.Result.values();
	public static final Type<FastTravelResultMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "fast_travel_result"));
	public static final StreamCodec<RegistryFriendlyByteBuf, FastTravelResultMessage> STREAM_CODEC = StreamCodec.of(
		(buffer, message) -> {
			buffer.writeVarInt(message.result.ordinal());
			buffer.writeVarInt(message.price);
		},
		buffer -> {
			int ordinal = buffer.readVarInt();
			if (ordinal < 0 || ordinal >= RESULTS.length)
				throw new IllegalArgumentException("Unknown fast-travel result");
			return new FastTravelResultMessage(RESULTS[ordinal], buffer.readVarInt());
		});

	public FastTravelResultMessage {
		if (result == null || price < 0)
			throw new IllegalArgumentException("Invalid fast-travel result");
	}

	@Override
	public Type<FastTravelResultMessage> type() {
		return TYPE;
	}

	/** The player-facing text for a result; the price fills %s where the text has it. */
	public static Component text(FastTravel.Result result, int price) {
		String key = "message.witchercraft.fast_travel.result." + result.name().toLowerCase(Locale.ROOT);
		return Component.translatableWithFallback(key, result.name(), price);
	}

	public static void handleData(FastTravelResultMessage message, IPayloadContext context) {
		if (context.flow() == PacketFlow.CLIENTBOUND)
			context.enqueueWork(() -> {
				if (message.result != FastTravel.Result.SESSION_ENDED && Minecraft.getInstance().player != null)
					Minecraft.getInstance().gui.setOverlayMessage(text(message.result, message.price), false);
			});
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, FastTravelResultMessage::handleData);
	}
}
