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
import java.util.UUID;

/** Complete authoritative personal waypoint state for the receiving player. */
@EventBusSubscriber
public record WorldMapWaypointSnapshotMessage(List<WorldMapWaypoints.Waypoint> waypoints) implements CustomPacketPayload {
	private static final int MAX_DIMENSION_LENGTH = 256;
	private static final int MAX_ID_LENGTH = 32;
	private static final int MAX_NAME_WIRE_CHARACTERS = WorldMapWaypoints.MAX_NAME_CHARACTERS * 2;
	public static final Type<WorldMapWaypointSnapshotMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "world_map_waypoint_snapshot"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WorldMapWaypointSnapshotMessage> STREAM_CODEC = StreamCodec.of((buffer, message) -> {
		buffer.writeVarInt(message.waypoints.size());
		for (WorldMapWaypoints.Waypoint waypoint : message.waypoints) {
			buffer.writeLong(waypoint.id().getMostSignificantBits());
			buffer.writeLong(waypoint.id().getLeastSignificantBits());
			buffer.writeUtf(waypoint.dimension().toString(), MAX_DIMENSION_LENGTH);
			buffer.writeDouble(waypoint.x());
			buffer.writeDouble(waypoint.z());
			buffer.writeUtf(waypoint.name(), MAX_NAME_WIRE_CHARACTERS);
			buffer.writeUtf(waypoint.icon().id(), MAX_ID_LENGTH);
			buffer.writeBoolean(waypoint.visible());
		}
	}, buffer -> {
		int count = buffer.readVarInt();
		if (count < 0 || count > WorldMapWaypoints.MAX_WAYPOINTS_PER_PLAYER)
			throw new IllegalArgumentException("Invalid personal waypoint snapshot count");
		List<WorldMapWaypoints.Waypoint> waypoints = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			UUID id = new UUID(buffer.readLong(), buffer.readLong());
			Identifier dimension = Identifier.tryParse(buffer.readUtf(MAX_DIMENSION_LENGTH));
			double x = buffer.readDouble();
			double z = buffer.readDouble();
			String name = buffer.readUtf(MAX_NAME_WIRE_CHARACTERS);
			WorldMapWaypoints.WaypointIcon icon = WorldMapWaypoints.WaypointIcon.byId(buffer.readUtf(MAX_ID_LENGTH));
			if (dimension == null || icon == null || !Double.isFinite(x) || !Double.isFinite(z))
				throw new IllegalArgumentException("Invalid personal waypoint snapshot entry");
			waypoints.add(new WorldMapWaypoints.Waypoint(id, dimension, x, z, name, icon, buffer.readBoolean()));
		}
		return new WorldMapWaypointSnapshotMessage(waypoints);
	});

	public WorldMapWaypointSnapshotMessage {
		waypoints = List.copyOf(waypoints);
		if (waypoints.size() > WorldMapWaypoints.MAX_WAYPOINTS_PER_PLAYER)
			throw new IllegalArgumentException("Too many personal waypoints in snapshot");
	}

	@Override
	public Type<WorldMapWaypointSnapshotMessage> type() {
		return TYPE;
	}

	public static void handleData(WorldMapWaypointSnapshotMessage message, IPayloadContext context) {
		if (context.flow() == PacketFlow.CLIENTBOUND)
			context.enqueueWork(() -> WorldMapWaypointClientCache.acceptSnapshot(message.waypoints));
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, WorldMapWaypointSnapshotMessage::handleData);
	}
}
