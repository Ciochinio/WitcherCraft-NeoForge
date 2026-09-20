package net.redboltmedia.witchercraft;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Canonical server-issued identity shared by every world-map client cache. */
@EventBusSubscriber
public final class WorldMapWorldIdentity {
	private static final Map<MinecraftServer, UUID> IDENTITIES = new ConcurrentHashMap<>();

	private WorldMapWorldIdentity() {
	}

	public static UUID get(MinecraftServer server) {
		if (server == null || !server.isSameThread())
			throw new IllegalStateException("World-map identity may only be accessed on the server thread");
		return IDENTITIES.computeIfAbsent(server, WorldMapWorldIdentity::readOrCreate);
	}

	@SubscribeEvent
	public static void onServerStopping(ServerStoppingEvent event) {
		IDENTITIES.remove(event.getServer());
	}

	private static UUID readOrCreate(MinecraftServer server) {
		Path path = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("witchercraft_world_map_identity.dat");
		try {
			if (Files.isRegularFile(path))
				return UUID.fromString(Files.readString(path).trim());
			UUID created = UUID.randomUUID();
			Files.createDirectories(path.getParent());
			Path temporary = path.resolveSibling(path.getFileName() + ".tmp-" + UUID.randomUUID());
			Files.writeString(temporary, created.toString());
			try {
				Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException ignored) {
				Files.move(temporary, path);
			}
			return created;
		} catch (IOException | IllegalArgumentException exception) {
			throw new IllegalStateException("Cannot establish WitcherCraft world-map identity", exception);
		}
	}
}
