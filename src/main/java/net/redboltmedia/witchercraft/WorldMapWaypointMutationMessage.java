package net.redboltmedia.witchercraft;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/** Bounded client intent for one personal waypoint operation. */
@EventBusSubscriber
public record WorldMapWaypointMutationMessage(int requestId, Operation operation, UUID waypointId, String dimension, double x, double z, String name, String icon, boolean value)
	implements CustomPacketPayload {
	private static final UUID NO_ID = new UUID(0L, 0L);
	private static final int MAX_DIMENSION_LENGTH = 256;
	private static final int MAX_ID_LENGTH = 32;
	private static final int MAX_NAME_WIRE_CHARACTERS = WorldMapWaypoints.MAX_NAME_CHARACTERS * 2;
	public static final Type<WorldMapWaypointMutationMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "world_map_waypoint_mutation"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WorldMapWaypointMutationMessage> STREAM_CODEC = StreamCodec.of((buffer, message) -> {
		buffer.writeVarInt(message.requestId);
		buffer.writeByte(message.operation.ordinal());
		buffer.writeLong(message.waypointId.getMostSignificantBits());
		buffer.writeLong(message.waypointId.getLeastSignificantBits());
		buffer.writeUtf(message.dimension, MAX_DIMENSION_LENGTH);
		buffer.writeDouble(message.x);
		buffer.writeDouble(message.z);
		buffer.writeUtf(message.name, MAX_NAME_WIRE_CHARACTERS);
		buffer.writeUtf(message.icon, MAX_ID_LENGTH);
		buffer.writeBoolean(message.value);
	}, buffer -> {
		int requestId = buffer.readVarInt();
		if (requestId <= 0)
			throw new IllegalArgumentException("Invalid waypoint request ID");
		int operationId = buffer.readUnsignedByte();
		if (operationId >= Operation.values().length)
			throw new IllegalArgumentException("Invalid waypoint operation");
		UUID waypointId = new UUID(buffer.readLong(), buffer.readLong());
		String dimension = buffer.readUtf(MAX_DIMENSION_LENGTH);
		double x = buffer.readDouble();
		double z = buffer.readDouble();
		String name = buffer.readUtf(MAX_NAME_WIRE_CHARACTERS);
		String icon = buffer.readUtf(MAX_ID_LENGTH);
		return new WorldMapWaypointMutationMessage(requestId, Operation.values()[operationId], waypointId, dimension, x, z, name, icon, buffer.readBoolean());
	});

	public static WorldMapWaypointMutationMessage requestSnapshot(int requestId) {
		return new WorldMapWaypointMutationMessage(requestId, Operation.REQUEST_SNAPSHOT, NO_ID, "", 0, 0, "", "", false);
	}

	@Override
	public Type<WorldMapWaypointMutationMessage> type() {
		return TYPE;
	}

	public static void handleData(WorldMapWaypointMutationMessage message, IPayloadContext context) {
		if (context.flow() == PacketFlow.SERVERBOUND && context.player() instanceof ServerPlayer player)
			context.enqueueWork(() -> handleOnServer(player, message));
	}

	private static void handleOnServer(ServerPlayer player, WorldMapWaypointMutationMessage message) {
		WorldMapWaypoints data = WorldMapWaypoints.get(player.level().getServer());
		WorldMapWaypoints.OperationResult result = switch (message.operation) {
			case REQUEST_SNAPSHOT -> new WorldMapWaypoints.OperationResult(WorldMapWaypoints.Status.SUCCESS, null);
			case CREATE -> data.create(player, Identifier.tryParse(message.dimension), message.x, message.z, message.name, WorldMapWaypoints.WaypointIcon.byId(message.icon));
			case EDIT -> data.edit(player, message.waypointId, message.name, WorldMapWaypoints.WaypointIcon.byId(message.icon));
			case SET_VISIBLE -> data.setVisible(player, message.waypointId, message.value);
			case DELETE -> data.delete(player, message.waypointId);
		};
		PacketDistributor.sendToPlayer(player, new WorldMapWaypointResultMessage(message.requestId, message.operation, result.status()));
		PacketDistributor.sendToPlayer(player, new WorldMapWaypointSnapshotMessage(data.getWaypoints(player)));
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, WorldMapWaypointMutationMessage::handleData);
	}

	public enum Operation {
		REQUEST_SNAPSHOT, CREATE, EDIT, SET_VISIBLE, DELETE
	}
}
