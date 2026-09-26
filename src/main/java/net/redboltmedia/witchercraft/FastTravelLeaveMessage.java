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

/**
 * HAND-MAINTAINED (locked code element, ~/World Map/Fast Travel). SERVERBOUND: the player left travel mode
 * (closed the map or switched tabs). Ends the session and cancels a journey still being prepared. The server
 * never relies on this message alone; sessions also end by distance, expiry, death, and logout.
 */
@EventBusSubscriber
public record FastTravelLeaveMessage() implements CustomPacketPayload {
	public static final FastTravelLeaveMessage INSTANCE = new FastTravelLeaveMessage();
	public static final Type<FastTravelLeaveMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "fast_travel_leave"));
	public static final StreamCodec<RegistryFriendlyByteBuf, FastTravelLeaveMessage> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<FastTravelLeaveMessage> type() {
		return TYPE;
	}

	public static void handleData(FastTravelLeaveMessage message, IPayloadContext context) {
		if (context.flow() == PacketFlow.SERVERBOUND && context.player() instanceof ServerPlayer player)
			context.enqueueWork(() -> FastTravel.endSession(player));
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, FastTravelLeaveMessage::handleData);
	}
}
