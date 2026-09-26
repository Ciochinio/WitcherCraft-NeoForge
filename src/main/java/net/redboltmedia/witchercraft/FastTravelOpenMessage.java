package net.redboltmedia.witchercraft;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * HAND-MAINTAINED (locked code element, ~/World Map/Fast Travel). CLIENTBOUND: the server started a travel
 * session at the signpost whose lower half is at ({@code originX}, {@code originZ}); open the map in travel
 * mode. {@code originPresentation} is the player's presentation UUID for that sign's marker, or null when the
 * player does not know it, in which case the client recognizes the origin by position.
 */
@EventBusSubscriber
public record FastTravelOpenMessage(int originX, int originZ, @Nullable UUID originPresentation) implements CustomPacketPayload {
	public static final Type<FastTravelOpenMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "fast_travel_open"));
	public static final StreamCodec<RegistryFriendlyByteBuf, FastTravelOpenMessage> STREAM_CODEC = StreamCodec.of(
		(buffer, message) -> {
			buffer.writeVarInt(message.originX);
			buffer.writeVarInt(message.originZ);
			buffer.writeBoolean(message.originPresentation != null);
			if (message.originPresentation != null)
				UUIDUtil.STREAM_CODEC.encode(buffer, message.originPresentation);
		},
		buffer -> {
			int x = buffer.readVarInt();
			int z = buffer.readVarInt();
			UUID presentation = buffer.readBoolean() ? UUIDUtil.STREAM_CODEC.decode(buffer) : null;
			return new FastTravelOpenMessage(x, z, presentation);
		});

	@Override
	public Type<FastTravelOpenMessage> type() {
		return TYPE;
	}

	public static void handleData(FastTravelOpenMessage message, IPayloadContext context) {
		if (context.flow() == PacketFlow.CLIENTBOUND)
			context.enqueueWork(() -> FastTravelClient.open(message.originX, message.originZ, message.originPresentation));
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, FastTravelOpenMessage::handleData);
	}
}
