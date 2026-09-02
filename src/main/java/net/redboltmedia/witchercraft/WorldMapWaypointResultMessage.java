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

/** Server acceptance or rejection for one client waypoint request. */
@EventBusSubscriber
public record WorldMapWaypointResultMessage(int requestId, WorldMapWaypointMutationMessage.Operation operation, WorldMapWaypoints.Status status) implements CustomPacketPayload {
	public static final Type<WorldMapWaypointResultMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "world_map_waypoint_result"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WorldMapWaypointResultMessage> STREAM_CODEC = StreamCodec.of((buffer, message) -> {
		buffer.writeVarInt(message.requestId);
		buffer.writeByte(message.operation.ordinal());
		buffer.writeByte(message.status.ordinal());
	}, buffer -> {
		int requestId = buffer.readVarInt();
		int operationId = buffer.readUnsignedByte();
		int statusId = buffer.readUnsignedByte();
		if (requestId <= 0 || operationId >= WorldMapWaypointMutationMessage.Operation.values().length || statusId >= WorldMapWaypoints.Status.values().length)
			throw new IllegalArgumentException("Invalid personal waypoint result");
		return new WorldMapWaypointResultMessage(requestId, WorldMapWaypointMutationMessage.Operation.values()[operationId], WorldMapWaypoints.Status.values()[statusId]);
	});

	@Override
	public Type<WorldMapWaypointResultMessage> type() {
		return TYPE;
	}

	public static void handleData(WorldMapWaypointResultMessage message, IPayloadContext context) {
		if (context.flow() == PacketFlow.CLIENTBOUND)
			context.enqueueWork(() -> WorldMapWaypointClientCache.acceptResult(message));
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, WorldMapWaypointResultMessage::handleData);
	}
}
