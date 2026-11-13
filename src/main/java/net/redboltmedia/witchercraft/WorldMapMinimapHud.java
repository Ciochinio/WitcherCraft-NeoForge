package net.redboltmedia.witchercraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

/**
 * Player-centered HUD view over the same authorized terrain cache used by the full-screen world map.
 */
@EventBusSubscriber(Dist.CLIENT)
public final class WorldMapMinimapHud {
	private static final int MARGIN = 8;
	private static final int BACKGROUND = 0xD0181714;
	private static final int TEXT = 0xFFE8DDBE;
	private static final int WAYPOINT_BASE_SIZE = 14;
	private static final int POI_BASE_SIZE = 14;
	private static final int TRACKING_BASE_SIZE = 16;
	private static final Identifier PLAYER_MARKER = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "textures/screens/map_player_arrow.png");
	private static final Identifier SQUARE_FRAME = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "textures/screens/minimap_frame_square.png");
	private static final Identifier TRACKING_MARKER = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "textures/screens/map_tracking_marker.png");
	private static final Identifier UNKNOWN_POI_ICON = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "textures/screens/map_poi_unknown.png");

	private WorldMapMinimapHud() {}

	@SubscribeEvent
	public static void render(RenderGuiEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!shouldRender(minecraft))
			return;

		GuiGraphicsExtractor graphics = event.getGuiGraphics();
		int size = size(graphics);
		if (size < 32)
			return;

		int x = switch (WorldMapClientConfig.minimapCorner()) {
			case TOP_LEFT, BOTTOM_LEFT -> MARGIN;
			case TOP_RIGHT, BOTTOM_RIGHT -> graphics.guiWidth() - MARGIN - size;
		};
		int y = switch (WorldMapClientConfig.minimapCorner()) {
			case TOP_LEFT -> MARGIN;
			case TOP_RIGHT -> topRightY(minecraft);
			case BOTTOM_LEFT, BOTTOM_RIGHT -> graphics.guiHeight() - MARGIN - size;
		};
		int inset = Math.min((int)Math.round(size * WorldMapClientConfig.minimapViewportInset() / 64.0), (size - 32) / 2);
		int viewportX = x + inset;
		int viewportY = y + inset;
		int viewportSize = size - inset * 2;

		float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
		float yaw = minecraft.player.getViewYRot(partialTick);
		double centerX = Mth.lerp(partialTick, minecraft.player.xo, minecraft.player.getX());
		double centerZ = Mth.lerp(partialTick, minecraft.player.zo, minecraft.player.getZ());
		double rotation = WorldMapClientConfig.minimapRotation() ? 180.0 - yaw : 0.0;

		WorldMapWaypointClientCache.requestSnapshotIfNeeded();
		Identifier dimension = minecraft.player.level().dimension().identifier();
		int poiRequestSize = WorldMapClientConfig.minimapRotation() ? (int)Math.ceil(viewportSize * Math.sqrt(2.0)) : viewportSize;
		WorldMapPoiClientCache.updateView(dimension, poiRequestSize, poiRequestSize, centerX, centerZ, WorldMapClientConfig.minimapZoom());
		graphics.fill(viewportX, viewportY, viewportX + viewportSize, viewportY + viewportSize, BACKGROUND);
		graphics.enableScissor(viewportX, viewportY, viewportX + viewportSize, viewportY + viewportSize);
		WorldMapClientTileCache.renderAndRequest(graphics, viewportX, viewportY, viewportSize, viewportSize, centerX, centerZ, WorldMapClientConfig.minimapZoom(), rotation);
		drawPois(graphics, dimension, viewportX, viewportY, viewportSize, centerX, centerZ, rotation);
		drawWaypoints(graphics, dimension, viewportX, viewportY, viewportSize, centerX, centerZ, rotation);
		drawTrackingTarget(graphics, dimension, viewportX, viewportY, viewportSize, centerX, centerZ, rotation);
		graphics.disableScissor();
		drawSquareFrame(graphics, x, y, size);
		drawNorth(graphics, minecraft.font, viewportX, viewportY, viewportSize, yaw);
		drawPlayer(graphics, x + size / 2.0f, y + size / 2.0f, size, yaw);
	}

	private static boolean shouldRender(Minecraft minecraft) {
		return minecraft.player != null && minecraft.level != null && minecraft.screen == null && !minecraft.options.hideGui
			&& minecraft.player.level().dimension().equals(Level.OVERWORLD)
			&& WorldMapClientConfig.minimapEnabled() && WorldMapServerConfig.minimapEnabled();
	}

	private static int size(GuiGraphicsExtractor graphics) {
		return Math.min(WorldMapClientConfig.minimapSize(), Math.min(graphics.guiWidth() - 2 * MARGIN, graphics.guiHeight() - 2 * MARGIN));
	}

	// A top-right map sits below vanilla's status effect rows (good row at y 1, bad row at y 27, 24 tall) and slides
	// when a row appears or goes. The effect icons themselves are left to vanilla.
	private static final int EFFECT_ROW_STEP = 26;
	private static final int EFFECTS_GAP = 2;
	/** Slide speed: share of the remaining distance covered per second (exponential ease-out). */
	private static final float SLIDE_RATE = 12.0F;
	private static float slideY = Float.NaN;
	private static long slideLastNanos;

	private static int topRightY(Minecraft minecraft) {
		int target = MARGIN;
		boolean good = false, bad = false;
		for (MobEffectInstance effect : minecraft.player.getActiveEffects()) {
			if (!effect.showIcon() || !IClientMobEffectExtensions.of(effect).isVisibleInGui(effect))
				continue;
			if (effect.getEffect().value().isBeneficial())
				good = true;
			else
				bad = true;
		}
		if (bad)
			target = 2 * EFFECT_ROW_STEP + EFFECTS_GAP - 1;
		else if (good)
			target = EFFECT_ROW_STEP + EFFECTS_GAP - 1;
		target = Math.max(MARGIN, target);
		long now = System.nanoTime();
		float seconds = (now - slideLastNanos) / 1.0E9F;
		slideLastNanos = now;
		// first frame, or back after a pause (screen open, other dimension): snap instead of sliding from a stale spot
		if (Float.isNaN(slideY) || seconds > 0.5F)
			slideY = target;
		else
			slideY += (target - slideY) * Math.min(1.0F, seconds * SLIDE_RATE);
		if (Math.abs(target - slideY) < 0.5F)
			slideY = target;
		return Math.round(slideY);
	}

	private static void drawSquareFrame(GuiGraphicsExtractor graphics, int x, int y, int size) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, SQUARE_FRAME, x, y, 0.0F, 0.0F, size, size, 64, 64, 64, 64);
	}

	private static void drawWaypoints(GuiGraphicsExtractor graphics, Identifier dimension, int x, int y, int size,
			double centerX, double centerZ, double rotationDegrees) {
		if (!WorldMapPoiFilterPreferences.personalWaypoints(WorldMapPoiClientCache.worldId()))
			return;
		int markerSize = Math.max(8, (int)Math.round(WAYPOINT_BASE_SIZE * WorldMapClientConfig.markerScale()));
		double half = size / 2.0;
		double radians = Math.toRadians(rotationDegrees);
		double cosine = Math.cos(radians);
		double sine = Math.sin(radians);
		double zoom = WorldMapClientConfig.minimapZoom();
		for (WorldMapWaypoints.Waypoint waypoint : WorldMapWaypointClientCache.waypoints(dimension)) {
			if (!waypoint.visible())
				continue;
			double scaledX = (waypoint.x() - centerX) * zoom;
			double scaledZ = (waypoint.z() - centerZ) * zoom;
			double positionX = cosine * scaledX - sine * scaledZ;
			double positionY = sine * scaledX + cosine * scaledZ;
			if (Math.abs(positionX) > half - markerSize / 2.0 || Math.abs(positionY) > half - markerSize / 2.0)
				continue;
			graphics.pose().pushMatrix();
			graphics.pose().translate((float)(x + half + positionX), (float)(y + half + positionY));
			MapPage.drawWaypointIcon(graphics, waypoint.icon().atlasIndex(), -markerSize / 2, -markerSize / 2, markerSize, MapLayout.WAYPOINT_COLOR);
			graphics.pose().popMatrix();
		}
	}

	private static void drawPois(GuiGraphicsExtractor graphics, Identifier dimension, int x, int y, int size,
			double centerX, double centerZ, double rotationDegrees) {
		int markerSize = Math.max(8, (int)Math.round(POI_BASE_SIZE * WorldMapClientConfig.markerScale()));
		double half = size / 2.0;
		double radians = Math.toRadians(rotationDegrees);
		double cosine = Math.cos(radians);
		double sine = Math.sin(radians);
		double zoom = WorldMapClientConfig.minimapZoom();
		java.util.UUID worldId = WorldMapPoiClientCache.worldId();
		for (WorldMapPoiMarker marker : WorldMapPoiClientCache.markers(dimension)) {
			if (!WorldMapPoiFilterPreferences.poiVisible(worldId, marker) || zoom < marker.minimumZoom())
				continue;
			double scaledX = (marker.x() - centerX) * zoom;
			double scaledZ = (marker.z() - centerZ) * zoom;
			double positionX = cosine * scaledX - sine * scaledZ;
			double positionY = sine * scaledX + cosine * scaledZ;
			if (Math.abs(positionX) > half - markerSize / 2.0 || Math.abs(positionY) > half - markerSize / 2.0)
				continue;
			Identifier texture = marker instanceof WorldMapPoiMarker.Discovered discovered ? discovered.icon() : UNKNOWN_POI_ICON;
			graphics.pose().pushMatrix();
			graphics.pose().translate((float)(x + half + positionX), (float)(y + half + positionY));
			graphics.blit(RenderPipelines.GUI_TEXTURED, texture, -markerSize / 2, -markerSize / 2, 0.0F, 0.0F,
				markerSize, markerSize, 64, 64, 64, 64);
			graphics.pose().popMatrix();
		}
	}

	private static void drawTrackingTarget(GuiGraphicsExtractor graphics, Identifier dimension, int x, int y, int size,
			double centerX, double centerZ, double rotationDegrees) {
		WorldMapWaypointClientCache.TemporaryPin target = WorldMapWaypointClientCache.temporaryPin(dimension);
		if (target == null)
			return;
		double arrivalRadius = WorldMapClientConfig.minimapTargetArrivalRadius();
		double targetOffsetX = target.x() - centerX;
		double targetOffsetZ = target.z() - centerZ;
		if (arrivalRadius > 0.0 && targetOffsetX * targetOffsetX + targetOffsetZ * targetOffsetZ <= arrivalRadius * arrivalRadius) {
			WorldMapWaypointClientCache.removeTemporaryPin(dimension);
			return;
		}
		int markerSize = Math.max(8, (int)Math.round(TRACKING_BASE_SIZE * WorldMapClientConfig.markerScale()));
		double radians = Math.toRadians(rotationDegrees);
		double cosine = Math.cos(radians);
		double sine = Math.sin(radians);
		double scaledX = targetOffsetX * WorldMapClientConfig.minimapZoom();
		double scaledZ = targetOffsetZ * WorldMapClientConfig.minimapZoom();
		double positionX = cosine * scaledX - sine * scaledZ;
		double positionY = sine * scaledX + cosine * scaledZ;
		double limit = size / 2.0 - markerSize / 2.0 - 1.0;
		boolean outside = Math.abs(positionX) > limit || Math.abs(positionY) > limit;
		if (outside) {
			double scale = Math.min(positionX == 0.0 ? Double.POSITIVE_INFINITY : limit / Math.abs(positionX),
				positionY == 0.0 ? Double.POSITIVE_INFINITY : limit / Math.abs(positionY));
			positionX *= scale;
			positionY *= scale;
		}
		float markerX = (float)(x + size / 2.0 + positionX);
		float markerY = (float)(y + size / 2.0 + positionY);
		graphics.pose().pushMatrix();
		graphics.pose().translate(markerX, markerY);
		if (outside) {
			graphics.pose().rotate((float)Math.atan2(positionX, -positionY));
			graphics.blit(RenderPipelines.GUI_TEXTURED, TRACKING_MARKER, -markerSize / 2, -markerSize / 2, 0.0F, 0.0F,
				markerSize, markerSize, 16, 16, 16, 16);
		} else {
			MapPage.drawWaypointIcon(graphics, 0, -markerSize / 2, -markerSize / 2, markerSize, MapLayout.WAYPOINT_COLOR);
		}
		graphics.pose().popMatrix();
	}

	private static void drawNorth(GuiGraphicsExtractor graphics, Font font, int x, int y, int size, float yaw) {
		double angle = WorldMapClientConfig.minimapRotation() ? Math.toRadians(180.0 - yaw) : 0.0;
		double dx = Math.sin(angle);
		double dy = -Math.cos(angle);
		double radius = size / 2.0 - 10.0;
		double edgeScale = Math.max(Math.abs(dx), Math.abs(dy));
		if (edgeScale > 0.0)
			radius /= edgeScale;
		int labelX = (int)Math.round(x + size / 2.0 + dx * radius - font.width("N") / 2.0);
		int labelY = (int)Math.round(y + size / 2.0 + dy * radius - font.lineHeight / 2.0);
		graphics.text(font, "N", labelX, labelY, TEXT, false);
	}

	private static void drawPlayer(GuiGraphicsExtractor graphics, float centerX, float centerY, int mapSize, float yaw) {
		int markerSize = Math.max(10, Math.min(18, mapSize / 7));
		graphics.pose().pushMatrix();
		graphics.pose().translate(centerX, centerY);
		graphics.pose().rotate((float)Math.toRadians(WorldMapClientConfig.minimapRotation() ? 180.0 : yaw));
		graphics.blit(RenderPipelines.GUI_TEXTURED, PLAYER_MARKER, -markerSize / 2, -markerSize / 2, 0.0F, 0.0F,
			markerSize, markerSize, 64, 64, 64, 64);
		graphics.pose().popMatrix();
	}
}
