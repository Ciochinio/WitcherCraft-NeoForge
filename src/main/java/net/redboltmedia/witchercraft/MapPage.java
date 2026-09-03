package net.redboltmedia.witchercraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import org.lwjgl.glfw.GLFW;

/** Interactive terrain map with player state and personal waypoint presentation. */
public final class MapPage implements GuiPage {
	private static final double MIN_ZOOM = 0.25;
	private static final double MAX_ZOOM = 16.0;
	private static final double ZOOM_STEP = 1.25;
	private static final double ZOOM_EASING = 0.22;
	private static final double ZOOM_SNAP_EPSILON = 0.0005;
	private static final long PING_DELAY_NANOS = 300_000_000L;
	private static final int PLAYER_MARKER_BASE_SIZE = 16;
	private static final int WAYPOINT_MARKER_BASE_SIZE = 22;
	private static final int ATLAS_WIDTH = 1774;
	private static final int ATLAS_HEIGHT = 887;
	private static final Identifier PLAYER_MARKER = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "textures/gui/map/player_arrow.png");
	private static final Identifier WAYPOINT_ICONS = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "textures/gui/map/waypoint_icons.png");
	private final WorldMapWaypointManagerOverlay manager = new WorldMapWaypointManagerOverlay(this::showOnMap);

	private double centerX;
	private double centerZ;
	private double zoom = 1.0;
	private double targetZoom = 1.0;
	private double zoomAnchorX;
	private double zoomAnchorZ;
	private double zoomAnchorScreenX;
	private double zoomAnchorScreenY;
	private boolean zoomAnimating;
	private boolean dragging;
	private boolean creating;
	private double createX;
	private double createZ;
	private String createName = "";
	private int selectedIcon;
	private int pendingCreateRequest;
	private Component createError;
	private MapSelection hoverSelection;
	private boolean pendingPing;
	private long pendingPingAt;
	private Identifier pendingPingDimension;
	private double pendingPingX;
	private double pendingPingZ;
	private WorldMapWaypoints.Waypoint contextWaypoint;
	private int contextPendingRequest;
	private Component contextError;

	@Override
	public String id() {
		return "map";
	}

	@Override
	public Component navLabel() {
		return Component.translatable("gui.witchercraft.shell.nav.map");
	}

	@Override
	public void onShown() {
		zoom = 1.0;
		targetZoom = 1.0;
		zoomAnimating = false;
		dragging = false;
		creating = false;
		manager.close();
		pendingCreateRequest = 0;
		createError = null;
		pendingPing = false;
		hoverSelection = null;
		closeContextMenu();
		contextPendingRequest = 0;
		centerOnPlayer();
		WorldMapClientTileCache.markViewDirty();
		WorldMapWaypointClientCache.requestSnapshot();
	}

	@Override
	public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, int mouseX, int mouseY, float partial) {
		checkCreateResult();
		checkContextResult();
		Font font = Minecraft.getInstance().font;
		int vx = x + MapLayout.VIEW_X, vy = y + MapLayout.VIEW_Y;
		int vw = Math.min(MapLayout.VIEW_W, w - MapLayout.VIEW_X);
		int vh = Math.min(MapLayout.VIEW_H, h - MapLayout.VIEW_Y);
		animateZoom(vx, vy, vw, vh);
		placePendingPingIfReady();
		g.fill(vx, vy, vx + vw, vy + vh, MapLayout.VIEW_BG);
		g.enableScissor(vx, vy, vx + vw, vy + vh);
		WorldMapClientTileCache.renderAndRequest(g, vx, vy, vw, vh, centerX, centerZ, zoom);
		drawWaypoints(g, vx, vy, vw, vh);
		hoverSelection = !creating && !manager.isOpen() && contextWaypoint == null ? markerAt(mouseX, mouseY, vx, vy, vw, vh) : null;
		drawPlayer(g, vx, vy, vw, vh);
		if (creating)
			drawCreationOverlay(g, font, vx, vy, vw, vh, mouseX, mouseY);
		g.disableScissor();
		drawBorder(g, vx, vy, vw, vh, MapLayout.VIEW_BORDER);

		int bx = x + MapLayout.BAR_X, by = y + MapLayout.BAR_Y;
		int bw = Math.min(MapLayout.BAR_W, w - MapLayout.BAR_X);
		g.fill(bx, by, bx + bw, by + MapLayout.BAR_H, MapLayout.BAR_BG);
		drawButton(g, font, bx + MapLayout.WAYPOINTS_X, by + MapLayout.BUTTON_Y, MapLayout.WAYPOINTS_W, Component.translatableWithFallback("gui.witchercraft.map.waypoints", "Waypoints"), mouseX, mouseY, !creating && !manager.isOpen());
		drawButton(g, font, bx + MapLayout.FILTERS_X, by + MapLayout.BUTTON_Y, MapLayout.FILTERS_W, Component.translatableWithFallback("gui.witchercraft.map.filters", "Filters"), mouseX, mouseY, false);
		drawButton(g, font, bx + MapLayout.CENTER_X, by + MapLayout.BUTTON_Y, MapLayout.CENTER_W, Component.translatableWithFallback("gui.witchercraft.map.center", "Center"), mouseX, mouseY, true);
		drawButton(g, font, bx + MapLayout.ZOOM_OUT_X, by + MapLayout.BUTTON_Y, MapLayout.ZOOM_W, Component.literal("-"), mouseX, mouseY, contextWaypoint == null);
		drawButton(g, font, bx + MapLayout.ZOOM_IN_X, by + MapLayout.BUTTON_Y, MapLayout.ZOOM_W, Component.literal("+"), mouseX, mouseY, contextWaypoint == null);

		g.text(font, Component.literal(String.format(java.util.Locale.ROOT, "%.2fx", zoom)), bx + MapLayout.HINT_X, by + MapLayout.HINT_Y, MapLayout.TEXT_DIM, false);
		String help = Component.translatableWithFallback("gui.witchercraft.map.help", "Drag Move | RMB Target/Menu | x2 Waypoint | Wheel Zoom").getString();
		g.text(font, Component.literal(fit(font, help, MapLayout.HELP_W)), bx + MapLayout.HELP_X, by + MapLayout.HELP_Y, MapLayout.TEXT_DIM, false);
		if (hoverSelection != null)
			drawSelectionCard(g, font, vx, vy, vw, vh);
		if (contextWaypoint != null)
			drawContextMenu(g, font, vx, vy, vw, vh, mouseX, mouseY);
		if (manager.isOpen())
			manager.render(g, vx, vy, vw, vh, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(int x, int y, int w, int h, double mouseX, double mouseY, int button, boolean doubleClick) {
		int vx = x + MapLayout.VIEW_X, vy = y + MapLayout.VIEW_Y;
		int vw = Math.min(MapLayout.VIEW_W, w - MapLayout.VIEW_X);
		int vh = Math.min(MapLayout.VIEW_H, h - MapLayout.VIEW_Y);
		if (manager.isOpen())
			return manager.mouseClicked(vx, vy, vw, vh, mouseX, mouseY, button);
		if (creating)
			return handleCreationClick(vx, vy, vw, vh, mouseX, mouseY, button);
		if (contextWaypoint != null)
			return handleContextClick(vx, vy, vw, vh, mouseX, mouseY, button);
		if (button == 1 && inside(mouseX, mouseY, vx, vy, vw, vh)) {
			var player = Minecraft.getInstance().player;
			if (player == null)
				return true;
			Identifier dimension = player.level().dimension().identifier();
			if (temporaryPinAt(mouseX, mouseY, vx, vy, vw, vh)) {
				pendingPing = false;
				WorldMapWaypointClientCache.removeTemporaryPin(dimension);
				return true;
			}
			WorldMapWaypoints.Waypoint waypoint = waypointAt(mouseX, mouseY, vx, vy, vw, vh);
			if (waypoint != null) {
				pendingPing = false;
				openContextMenu(waypoint);
				return true;
			}
			double worldX = centerX + (mouseX - (vx + vw / 2.0)) / zoom;
			double worldZ = centerZ + (mouseY - (vy + vh / 2.0)) / zoom;
			if (doubleClick) {
				pendingPing = false;
				openCreation(worldX, worldZ);
			} else {
				pendingPing = true;
				pendingPingAt = System.nanoTime();
				pendingPingDimension = dimension;
				pendingPingX = worldX;
				pendingPingZ = worldZ;
			}
			return true;
		}
		return mouseClicked(x, y, w, h, mouseX, mouseY, button);
	}

	@Override
	public boolean mouseClicked(int x, int y, int w, int h, double mouseX, double mouseY, int button) {
		if (button != 0)
			return false;
		int vx = x + MapLayout.VIEW_X, vy = y + MapLayout.VIEW_Y;
		if (inside(mouseX, mouseY, vx, vy, MapLayout.VIEW_W, MapLayout.VIEW_H)) {
			dragging = true;
			return true;
		}
		int bx = x + MapLayout.BAR_X, by = y + MapLayout.BAR_Y + MapLayout.BUTTON_Y;
		if (inside(mouseX, mouseY, bx + MapLayout.WAYPOINTS_X, by, MapLayout.WAYPOINTS_W, MapLayout.BUTTON_H)) {
			manager.open();
			dragging = false;
			pendingPing = false;
			return true;
		}
		if (inside(mouseX, mouseY, bx + MapLayout.CENTER_X, by, MapLayout.CENTER_W, MapLayout.BUTTON_H)) {
			centerOnPlayer();
			return true;
		}
		if (inside(mouseX, mouseY, bx + MapLayout.ZOOM_OUT_X, by, MapLayout.ZOOM_W, MapLayout.BUTTON_H)) {
			setZoom(targetZoom / ZOOM_STEP, vx + MapLayout.VIEW_W / 2.0, vy + MapLayout.VIEW_H / 2.0, vx, vy);
			return true;
		}
		if (inside(mouseX, mouseY, bx + MapLayout.ZOOM_IN_X, by, MapLayout.ZOOM_W, MapLayout.BUTTON_H)) {
			setZoom(targetZoom * ZOOM_STEP, vx + MapLayout.VIEW_W / 2.0, vy + MapLayout.VIEW_H / 2.0, vx, vy);
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseReleased(int x, int y, int w, int h, double mouseX, double mouseY, int button) {
		if (button == 0 && dragging) {
			dragging = false;
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseDragged(int x, int y, int w, int h, double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (creating || manager.isOpen())
			return true;
		if (button == 0 && dragging) {
			centerX -= dragX / zoom;
			centerZ -= dragY / zoom;
			clampToWorldBorder();
			WorldMapClientTileCache.markViewDirty();
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(int x, int y, int w, int h, double mouseX, double mouseY, double scrollX, double scrollY) {
		if (manager.isOpen())
			return manager.mouseScrolled(scrollY);
		if (contextWaypoint != null)
			return true;
		if (creating)
			return true;
		int vx = x + MapLayout.VIEW_X, vy = y + MapLayout.VIEW_Y;
		if (!inside(mouseX, mouseY, vx, vy, MapLayout.VIEW_W, MapLayout.VIEW_H) || scrollY == 0)
			return false;
		setZoom(targetZoom * Math.pow(ZOOM_STEP, scrollY), mouseX, mouseY, vx, vy);
		return true;
	}

	@Override
	public boolean keyPressed(int keyCode) {
		if (manager.isOpen())
			return manager.keyPressed(keyCode);
		if (contextWaypoint != null && keyCode == GLFW.GLFW_KEY_ESCAPE) {
			closeContextMenu();
			return true;
		}
		if (!creating)
			return false;
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			if (pendingCreateRequest == 0)
				creating = false;
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			submitCreation();
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_BACKSPACE && pendingCreateRequest == 0 && !createName.isEmpty()) {
			int end = createName.offsetByCodePoints(createName.length(), -1);
			createName = createName.substring(0, end);
			createError = null;
			return true;
		}
		return true;
	}

	@Override
	public boolean charTyped(int codepoint) {
		if (manager.isOpen())
			return manager.charTyped(codepoint);
		if (!creating || pendingCreateRequest != 0)
			return creating;
		if (!Character.isValidCodePoint(codepoint) || Character.isISOControl(codepoint))
			return true;
		if (createName.codePointCount(0, createName.length()) < WorldMapWaypoints.MAX_NAME_CHARACTERS) {
			createName += Character.toString(codepoint);
			createError = null;
		}
		return true;
	}

	@Override
	public void onClose() {
		dragging = false;
		creating = false;
		manager.close();
		pendingPing = false;
		closeContextMenu();
	}

	private void showOnMap(WorldMapWaypoints.Waypoint waypoint) {
		centerX = waypoint.x();
		centerZ = waypoint.z();
		clampToWorldBorder();
		targetZoom = zoom;
		zoomAnimating = false;
		WorldMapClientTileCache.markViewDirty();
	}

	private void openCreation(double worldX, double worldZ) {
		dragging = false;
		closeContextMenu();
		createX = worldX;
		createZ = worldZ;
		createName = "";
		selectedIcon = 0;
		pendingCreateRequest = 0;
		createError = null;
		creating = true;
	}

	private void placePendingPingIfReady() {
		if (pendingPing && System.nanoTime() - pendingPingAt >= PING_DELAY_NANOS) {
			pendingPing = false;
			WorldMapWaypointClientCache.placeTemporaryPin(pendingPingDimension, pendingPingX, pendingPingZ);
		}
	}

	private boolean handleCreationClick(int vx, int vy, int vw, int vh, double mouseX, double mouseY, int button) {
		if (button != 0)
			return true;
		int ox = vx + (vw - MapLayout.CREATE_W) / 2;
		int oy = vy + (vh - MapLayout.CREATE_H) / 2;
		int startX = ox + MapLayout.CREATE_PADDING;
		for (int i = 0; i < WorldMapWaypoints.WaypointIcon.values().length; i++) {
			int iconX = startX + i * (MapLayout.CREATE_ICON_SIZE + MapLayout.CREATE_ICON_GAP);
			if (inside(mouseX, mouseY, iconX, oy + MapLayout.CREATE_ICON_Y, MapLayout.CREATE_ICON_SIZE, MapLayout.CREATE_ICON_SIZE)) {
				if (pendingCreateRequest == 0)
					selectedIcon = i;
				return true;
			}
		}
		int createButtonX = ox + MapLayout.CREATE_W - MapLayout.CREATE_PADDING - MapLayout.CREATE_ACTION_W;
		if (inside(mouseX, mouseY, createButtonX, oy + MapLayout.CREATE_ACTION_Y, MapLayout.CREATE_ACTION_W, MapLayout.BUTTON_H)) {
			submitCreation();
			return true;
		}
		if (inside(mouseX, mouseY, startX, oy + MapLayout.CREATE_ACTION_Y, MapLayout.CREATE_ACTION_W, MapLayout.BUTTON_H)) {
			if (pendingCreateRequest == 0)
				creating = false;
			return true;
		}
		return true;
	}

	private void submitCreation() {
		if (!creating || pendingCreateRequest != 0)
			return;
		String normalized = createName.strip();
		if (normalized.isEmpty()) {
			createError = Component.translatableWithFallback("gui.witchercraft.map.waypoint.name_required", "Enter a waypoint name");
			return;
		}
		var player = Minecraft.getInstance().player;
		if (player == null)
			return;
		createName = normalized;
		pendingCreateRequest = WorldMapWaypointClientCache.create(player.level().dimension().identifier(), createX, createZ, createName, WorldMapWaypoints.WaypointIcon.values()[selectedIcon]);
		if (pendingCreateRequest == 0)
			createError = Component.translatableWithFallback("gui.witchercraft.map.waypoint.not_connected", "Not connected to a server");
	}

	private void checkCreateResult() {
		if (pendingCreateRequest == 0)
			return;
		WorldMapWaypointResultMessage result = WorldMapWaypointClientCache.takeResult(pendingCreateRequest);
		if (result == null)
			return;
		pendingCreateRequest = 0;
		if (result.status() == WorldMapWaypoints.Status.SUCCESS) {
			creating = false;
			createError = null;
		} else {
			String fallback = switch (result.status()) {
				case LIMIT_REACHED -> "Waypoint limit reached";
				case INVALID_DIMENSION -> "That dimension is unavailable";
				case OUTSIDE_WORLD_BORDER -> "Waypoint is outside the world border";
				default -> "The server rejected this waypoint";
			};
			createError = Component.translatableWithFallback("gui.witchercraft.map.waypoint.error." + result.status().name().toLowerCase(java.util.Locale.ROOT), fallback);
		}
	}

	private void drawCreationOverlay(GuiGraphicsExtractor g, Font font, int vx, int vy, int vw, int vh, int mouseX, int mouseY) {
		g.fill(vx, vy, vx + vw, vy + vh, MapLayout.OVERLAY_DIM);
		int ox = vx + (vw - MapLayout.CREATE_W) / 2;
		int oy = vy + (vh - MapLayout.CREATE_H) / 2;
		g.fill(ox, oy, ox + MapLayout.CREATE_W, oy + MapLayout.CREATE_H, MapLayout.OVERLAY_BG);
		drawBorder(g, ox, oy, MapLayout.CREATE_W, MapLayout.CREATE_H, MapLayout.OVERLAY_BORDER);
		g.text(font, Component.translatableWithFallback("gui.witchercraft.map.waypoint.create", "Create waypoint"), ox + MapLayout.CREATE_PADDING, oy + 9, MapLayout.TEXT, false);
		Component coords = Component.literal("X " + (int) Math.floor(createX) + "  Z " + (int) Math.floor(createZ));
		g.text(font, coords, ox + MapLayout.CREATE_W - MapLayout.CREATE_PADDING - font.width(coords), oy + 9, MapLayout.TEXT_DIM, false);
		int fieldX = ox + MapLayout.CREATE_PADDING;
		int fieldW = MapLayout.CREATE_W - MapLayout.CREATE_PADDING * 2;
		g.text(font, Component.translatableWithFallback("gui.witchercraft.map.waypoint.name", "Name"), fieldX, oy + 23, MapLayout.TEXT_DIM, false);
		g.fill(fieldX, oy + MapLayout.CREATE_NAME_Y, fieldX + fieldW, oy + MapLayout.CREATE_NAME_Y + MapLayout.CREATE_NAME_H, MapLayout.FIELD_BG);
		drawBorder(g, fieldX, oy + MapLayout.CREATE_NAME_Y, fieldW, MapLayout.CREATE_NAME_H, createError == null ? MapLayout.VIEW_BORDER : MapLayout.ERROR);
		String shownName = createName;
		if (pendingCreateRequest == 0 && (System.currentTimeMillis() / 500L & 1L) == 0L)
			shownName += "_";
		while (!shownName.isEmpty() && font.width(shownName) > fieldW - 10) {
			int first = shownName.offsetByCodePoints(0, 1);
			shownName = shownName.substring(first);
		}
		g.text(font, Component.literal(shownName), fieldX + 5, oy + MapLayout.CREATE_NAME_Y + 6, MapLayout.TEXT, false);

		g.text(font, Component.translatableWithFallback("gui.witchercraft.map.waypoint.icon", "Icon"), fieldX, oy + 56, MapLayout.TEXT_DIM, false);
		for (int i = 0; i < WorldMapWaypoints.WaypointIcon.values().length; i++) {
			int iconX = fieldX + i * (MapLayout.CREATE_ICON_SIZE + MapLayout.CREATE_ICON_GAP);
			if (i == selectedIcon)
				drawBorder(g, iconX - 2, oy + MapLayout.CREATE_ICON_Y - 2, MapLayout.CREATE_ICON_SIZE + 4, MapLayout.CREATE_ICON_SIZE + 4, MapLayout.SELECTED);
			drawWaypointIcon(g, WorldMapWaypoints.WaypointIcon.values()[i].atlasIndex(), iconX, oy + MapLayout.CREATE_ICON_Y, MapLayout.CREATE_ICON_SIZE, MapLayout.WAYPOINT_COLOR);
		}
		if (createError != null)
			g.text(font, createError, fieldX, oy + 96, MapLayout.ERROR, false);
		drawButton(g, font, fieldX, oy + MapLayout.CREATE_ACTION_Y, MapLayout.CREATE_ACTION_W, Component.translatableWithFallback("gui.cancel", "Cancel"), mouseX, mouseY, pendingCreateRequest == 0);
		int createButtonX = ox + MapLayout.CREATE_W - MapLayout.CREATE_PADDING - MapLayout.CREATE_ACTION_W;
		Component action = pendingCreateRequest == 0 ? Component.translatableWithFallback("gui.witchercraft.map.waypoint.save", "Save") : Component.translatableWithFallback("gui.witchercraft.map.waypoint.saving", "Saving...");
		drawButton(g, font, createButtonX, oy + MapLayout.CREATE_ACTION_Y, MapLayout.CREATE_ACTION_W, action, mouseX, mouseY, pendingCreateRequest == 0 && !createName.isBlank());
	}

	private void drawWaypoints(GuiGraphicsExtractor g, int x, int y, int w, int h) {
		var player = Minecraft.getInstance().player;
		if (player == null)
			return;
		int alpha = waypointAlpha();
		float visualScale = markerVisualScale();
		int size = Math.max(8, (int) Math.round(WAYPOINT_MARKER_BASE_SIZE * WorldMapClientConfig.markerScale()));
		for (WorldMapWaypoints.Waypoint waypoint : WorldMapWaypointClientCache.waypoints(player.level().dimension().identifier())) {
			if (!waypoint.visible())
				continue;
			double px = x + w / 2.0 + (waypoint.x() - centerX) * zoom;
			double py = y + h / 2.0 + (waypoint.z() - centerZ) * zoom;
			double radius = size * visualScale;
			if (px + radius < x || px - radius >= x + w || py + radius < y || py - radius >= y + h)
				continue;
			g.pose().pushMatrix();
			g.pose().translate((float) px, (float) py);
			g.pose().scale(visualScale, visualScale);
			int tint = (alpha << 24) | (MapLayout.WAYPOINT_COLOR & 0x00FFFFFF);
			drawWaypointIcon(g, waypoint.icon().atlasIndex(), -size / 2, -size / 2, size, tint);
			g.pose().popMatrix();
		}
		drawTemporaryPin(g, player.level().dimension().identifier(), x, y, w, h, alpha, visualScale);
	}

	private void drawTemporaryPin(GuiGraphicsExtractor g, Identifier dimension, int x, int y, int w, int h, int alpha, float visualScale) {
		WorldMapWaypointClientCache.TemporaryPin pin = WorldMapWaypointClientCache.temporaryPin(dimension);
		if (pin == null)
			return;
		double px = x + w / 2.0 + (pin.x() - centerX) * zoom;
		double py = y + h / 2.0 + (pin.z() - centerZ) * zoom;
		int size = Math.max(8, (int) Math.round(WAYPOINT_MARKER_BASE_SIZE * WorldMapClientConfig.markerScale()));
		g.pose().pushMatrix();
		g.pose().translate((float) px, (float) py);
		g.pose().scale(visualScale, visualScale);
		drawWaypointIcon(g, 0, -size / 2, -size / 2, size, (alpha << 24) | (MapLayout.WAYPOINT_COLOR & 0x00FFFFFF));
		g.pose().popMatrix();
	}

	private int waypointAlpha() {
		if (zoom >= 1.0)
			return 255;
		double progress = (zoom - MIN_ZOOM) / (1.0 - MIN_ZOOM);
		return (int) Math.round(89 + Math.max(0, Math.min(1, progress)) * 166);
	}

	private MapSelection markerAt(double mouseX, double mouseY, int x, int y, int w, int h) {
		var player = Minecraft.getInstance().player;
		if (player == null)
			return null;
		float visualScale = markerVisualScale();
		int size = Math.max(8, (int) Math.round(WAYPOINT_MARKER_BASE_SIZE * WorldMapClientConfig.markerScale()));
		double radius = size * visualScale / 2.0 + 3;
		double bestDistance = radius * radius;
		MapSelection found = null;
		for (WorldMapWaypoints.Waypoint waypoint : WorldMapWaypointClientCache.waypoints(player.level().dimension().identifier())) {
			if (!waypoint.visible())
				continue;
			double px = x + w / 2.0 + (waypoint.x() - centerX) * zoom;
			double py = y + h / 2.0 + (waypoint.z() - centerZ) * zoom;
			double distance = (mouseX - px) * (mouseX - px) + (mouseY - py) * (mouseY - py);
			if (distance <= bestDistance) {
				bestDistance = distance;
				found = waypointSelection(waypoint);
			}
		}
		WorldMapWaypointClientCache.TemporaryPin pin = WorldMapWaypointClientCache.temporaryPin(player.level().dimension().identifier());
		if (pin != null) {
			double px = x + w / 2.0 + (pin.x() - centerX) * zoom;
			double py = y + h / 2.0 + (pin.z() - centerZ) * zoom;
			double distance = (mouseX - px) * (mouseX - px) + (mouseY - py) * (mouseY - py);
			if (distance <= bestDistance)
				found = targetSelection(pin.x(), pin.z());
		}
		return found;
	}

	private static MapSelection waypointSelection(WorldMapWaypoints.Waypoint waypoint) {
		return new MapSelection(Component.literal(waypoint.name()), Component.literal("X " + (int) Math.floor(waypoint.x()) + "  Z " + (int) Math.floor(waypoint.z())), MapLayout.WAYPOINT_COLOR);
	}

	private static MapSelection targetSelection(double x, double z) {
		return new MapSelection(Component.translatableWithFallback("gui.witchercraft.map.waypoint.target", "Target"), Component.literal("X " + (int) Math.floor(x) + "  Z " + (int) Math.floor(z)), MapLayout.WAYPOINT_COLOR);
	}

	private void drawSelectionCard(GuiGraphicsExtractor g, Font font, int vx, int vy, int vw, int vh) {
		int x = vx + (vw - MapLayout.SELECTION_W) / 2;
		int y = vy + vh - MapLayout.SELECTION_BOTTOM - MapLayout.SELECTION_H;
		g.fill(x, y, x + MapLayout.SELECTION_W, y + MapLayout.SELECTION_H, MapLayout.OVERLAY_BG);
		drawBorder(g, x, y, MapLayout.SELECTION_W, MapLayout.SELECTION_H, MapLayout.OVERLAY_BORDER);
		g.text(font, hoverSelection.name(), x + (MapLayout.SELECTION_W - font.width(hoverSelection.name())) / 2, y + 8, hoverSelection.color(), false);
		g.text(font, hoverSelection.detail(), x + (MapLayout.SELECTION_W - font.width(hoverSelection.detail())) / 2, y + 23, MapLayout.TEXT_DIM, false);
	}

	private void openContextMenu(WorldMapWaypoints.Waypoint waypoint) {
		contextWaypoint = waypoint;
		contextError = null;
	}

	private void closeContextMenu() {
		contextWaypoint = null;
		contextError = null;
	}

	private void drawContextMenu(GuiGraphicsExtractor g, Font font, int vx, int vy, int vw, int vh, int mouseX, int mouseY) {
		int x = contextMenuX(vx, vw);
		int y = contextMenuY(vy, vh);
		g.fill(x, y, x + MapLayout.CONTEXT_W, y + MapLayout.CONTEXT_H, MapLayout.OVERLAY_BG);
		drawBorder(g, x, y, MapLayout.CONTEXT_W, MapLayout.CONTEXT_H, MapLayout.OVERLAY_BORDER);
		int innerW = MapLayout.CONTEXT_W - MapLayout.CONTEXT_PADDING * 2;
		Component title = contextError == null ? Component.literal(fit(font, contextWaypoint.name(), innerW)) : Component.literal(fit(font, contextError.getString(), innerW));
		g.text(font, title, x + (MapLayout.CONTEXT_W - font.width(title)) / 2, y + 8, contextError == null ? MapLayout.WAYPOINT_COLOR : MapLayout.ERROR, false);
		drawContextButtons(g, font, x, y + 29, mouseX, mouseY,
			Component.translatableWithFallback("gui.witchercraft.map.waypoint.target_action", "Target"), Component.translatableWithFallback("gui.witchercraft.map.waypoint.delete", "Delete"));
	}

	private void drawContextButtons(GuiGraphicsExtractor g, Font font, int x, int y, int mouseX, int mouseY, Component left, Component right) {
		int buttonW = (MapLayout.CONTEXT_W - MapLayout.CONTEXT_PADDING * 2 - 4) / 2;
		boolean enabled = contextPendingRequest == 0;
		drawButton(g, font, x + MapLayout.CONTEXT_PADDING, y, buttonW, left, mouseX, mouseY, enabled);
		drawButton(g, font, x + MapLayout.CONTEXT_PADDING + buttonW + 4, y, buttonW, right, mouseX, mouseY, enabled);
	}

	private boolean handleContextClick(int vx, int vy, int vw, int vh, double mouseX, double mouseY, int button) {
		int x = contextMenuX(vx, vw);
		int y = contextMenuY(vy, vh);
		if (button != 0 || !inside(mouseX, mouseY, x, y, MapLayout.CONTEXT_W, MapLayout.CONTEXT_H)) {
			closeContextMenu();
			return true;
		}
		if (contextPendingRequest != 0)
			return true;
		int buttonW = (MapLayout.CONTEXT_W - MapLayout.CONTEXT_PADDING * 2 - 4) / 2;
		int buttonY = y + 29;
		boolean left = inside(mouseX, mouseY, x + MapLayout.CONTEXT_PADDING, buttonY, buttonW, MapLayout.BUTTON_H);
		boolean right = inside(mouseX, mouseY, x + MapLayout.CONTEXT_PADDING + buttonW + 4, buttonY, buttonW, MapLayout.BUTTON_H);
		if (left) {
			WorldMapWaypointClientCache.placeTemporaryPin(contextWaypoint.dimension(), contextWaypoint.x(), contextWaypoint.z());
			closeContextMenu();
		} else if (right) {
			contextPendingRequest = WorldMapWaypointClientCache.delete(contextWaypoint.id());
			if (contextPendingRequest == 0)
				contextError = Component.translatableWithFallback("gui.witchercraft.map.waypoint.not_connected", "Not connected to a server");
		}
		return true;
	}

	private int contextMenuX(int vx, int vw) {
		double markerX = vx + vw / 2.0 + (contextWaypoint.x() - centerX) * zoom;
		return Math.max(vx, Math.min(vx + vw - MapLayout.CONTEXT_W, (int) Math.round(markerX - MapLayout.CONTEXT_W / 2.0)));
	}

	private int contextMenuY(int vy, int vh) {
		double markerY = vy + vh / 2.0 + (contextWaypoint.z() - centerZ) * zoom;
		int y = (int) Math.round(markerY + markerHitRadius() + MapLayout.CONTEXT_MARKER_GAP);
		return Math.max(vy, Math.min(vy + vh - MapLayout.CONTEXT_H, y));
	}

	private void checkContextResult() {
		if (contextPendingRequest == 0)
			return;
		WorldMapWaypointResultMessage result = WorldMapWaypointClientCache.takeResult(contextPendingRequest);
		if (result == null)
			return;
		contextPendingRequest = 0;
		if (result.status() == WorldMapWaypoints.Status.SUCCESS) {
			closeContextMenu();
		} else {
			contextError = Component.translatableWithFallback("gui.witchercraft.map.waypoint.error." + result.status().name().toLowerCase(java.util.Locale.ROOT), "The server rejected this change");
		}
	}

	private WorldMapWaypoints.Waypoint waypointAt(double mouseX, double mouseY, int x, int y, int w, int h) {
		var player = Minecraft.getInstance().player;
		if (player == null)
			return null;
		double radius = markerHitRadius();
		double bestDistance = radius * radius;
		WorldMapWaypoints.Waypoint found = null;
		for (WorldMapWaypoints.Waypoint waypoint : WorldMapWaypointClientCache.waypoints(player.level().dimension().identifier())) {
			if (!waypoint.visible())
				continue;
			double px = x + w / 2.0 + (waypoint.x() - centerX) * zoom;
			double py = y + h / 2.0 + (waypoint.z() - centerZ) * zoom;
			double distance = (mouseX - px) * (mouseX - px) + (mouseY - py) * (mouseY - py);
			if (distance <= bestDistance) {
				bestDistance = distance;
				found = waypoint;
			}
		}
		return found;
	}

	private boolean temporaryPinAt(double mouseX, double mouseY, int x, int y, int w, int h) {
		var player = Minecraft.getInstance().player;
		if (player == null)
			return false;
		WorldMapWaypointClientCache.TemporaryPin pin = WorldMapWaypointClientCache.temporaryPin(player.level().dimension().identifier());
		if (pin == null)
			return false;
		double px = x + w / 2.0 + (pin.x() - centerX) * zoom;
		double py = y + h / 2.0 + (pin.z() - centerZ) * zoom;
		double radius = markerHitRadius();
		return (mouseX - px) * (mouseX - px) + (mouseY - py) * (mouseY - py) <= radius * radius;
	}

	private double markerHitRadius() {
		int size = Math.max(8, (int) Math.round(WAYPOINT_MARKER_BASE_SIZE * WorldMapClientConfig.markerScale()));
		return size * markerVisualScale() / 2.0 + 3;
	}

	private static String fit(Font font, String value, int width) {
		if (font.width(value) <= width)
			return value;
		String result = value;
		while (!result.isEmpty() && font.width(result + "...") > width)
			result = result.substring(0, result.offsetByCodePoints(0, result.codePointCount(0, result.length()) - 1));
		return result + "...";
	}

	static void drawWaypointIcon(GuiGraphicsExtractor g, int index, int x, int y, int size, int color) {
		int column = index % 4;
		int row = index / 4;
		int u = column * ATLAS_WIDTH / 4;
		int nextU = (column + 1) * ATLAS_WIDTH / 4;
		int v = row * ATLAS_HEIGHT / 2;
		int nextV = (row + 1) * ATLAS_HEIGHT / 2;
		g.blit(RenderPipelines.GUI_TEXTURED, WAYPOINT_ICONS, x, y, u, v, size, size, nextU - u, nextV - v, ATLAS_WIDTH, ATLAS_HEIGHT, color);
	}

	private float markerVisualScale() {
		return (float) Math.max(0.5, Math.min(2.5, Math.sqrt(zoom)));
	}

	private void setZoom(double requested, double mouseX, double mouseY, int vx, int vy) {
		targetZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, requested));
		zoomAnchorScreenX = mouseX;
		zoomAnchorScreenY = mouseY;
		zoomAnchorX = centerX + (mouseX - (vx + MapLayout.VIEW_W / 2.0)) / zoom;
		zoomAnchorZ = centerZ + (mouseY - (vy + MapLayout.VIEW_H / 2.0)) / zoom;
		zoomAnimating = targetZoom != zoom;
	}

	private void animateZoom(int vx, int vy, int vw, int vh) {
		if (!zoomAnimating)
			return;
		if (Math.abs(targetZoom - zoom) < ZOOM_SNAP_EPSILON) {
			zoom = targetZoom;
			zoomAnimating = false;
		} else {
			zoom += (targetZoom - zoom) * ZOOM_EASING;
		}
		centerX = zoomAnchorX - (zoomAnchorScreenX - (vx + vw / 2.0)) / zoom;
		centerZ = zoomAnchorZ - (zoomAnchorScreenY - (vy + vh / 2.0)) / zoom;
		clampToWorldBorder();
	}

	private void clampToWorldBorder() {
		if (Minecraft.getInstance().level == null)
			return;
		var border = Minecraft.getInstance().level.getWorldBorder();
		centerX = Math.max(border.getMinX(), Math.min(border.getMaxX(), centerX));
		centerZ = Math.max(border.getMinZ(), Math.min(border.getMaxZ(), centerZ));
	}

	private void centerOnPlayer() {
		if (Minecraft.getInstance().player != null) {
			centerX = Minecraft.getInstance().player.getX();
			centerZ = Minecraft.getInstance().player.getZ();
			targetZoom = zoom;
			zoomAnimating = false;
			WorldMapClientTileCache.markViewDirty();
		}
	}

	private void drawGrid(GuiGraphicsExtractor g, int x, int y, int w, int h) {
		double spacing = 16.0 * zoom;
		while (spacing < 8.0)
			spacing *= 4.0;
		double originX = x + w / 2.0 - centerX * zoom;
		double originY = y + h / 2.0 - centerZ * zoom;
		int firstX = (int) Math.floor((x - originX) / spacing) - 1;
		int lastX = (int) Math.ceil((x + w - originX) / spacing) + 1;
		int firstY = (int) Math.floor((y - originY) / spacing) - 1;
		int lastY = (int) Math.ceil((y + h - originY) / spacing) + 1;
		for (int i = firstX; i <= lastX; i++) {
			int px = (int) Math.round(originX + i * spacing);
			g.fill(px, y, px + 1, y + h, i == 0 ? MapLayout.GRID_AXIS : (i % 4 == 0 ? MapLayout.GRID_MAJOR : MapLayout.GRID_MINOR));
		}
		for (int i = firstY; i <= lastY; i++) {
			int py = (int) Math.round(originY + i * spacing);
			g.fill(x, py, x + w, py + 1, i == 0 ? MapLayout.GRID_AXIS : (i % 4 == 0 ? MapLayout.GRID_MAJOR : MapLayout.GRID_MINOR));
		}
	}

	private void drawPlayer(GuiGraphicsExtractor g, int x, int y, int w, int h) {
		var player = Minecraft.getInstance().player;
		if (player == null)
			return;
		double px = x + w / 2.0 + (player.getX() - centerX) * zoom;
		double py = y + h / 2.0 + (player.getZ() - centerZ) * zoom;
		g.pose().pushMatrix();
		g.pose().translate((float) px, (float) py);
		float visualScale = markerVisualScale();
		g.pose().scale(visualScale, visualScale);
		g.pose().rotate((float) Math.toRadians(player.getYRot()));
		int size = Math.max(1, (int) Math.round(PLAYER_MARKER_BASE_SIZE * WorldMapClientConfig.markerScale()));
		g.blit(RenderPipelines.GUI_TEXTURED, PLAYER_MARKER, -size / 2, -size / 2, 0.0F, 0.0F, size, size, 64, 64, 64, 64);
		g.pose().popMatrix();
	}

	static void drawButton(GuiGraphicsExtractor g, Font font, int x, int y, int w, Component label, int mouseX, int mouseY, boolean enabled) {
		boolean hover = enabled && inside(mouseX, mouseY, x, y, w, MapLayout.BUTTON_H);
		g.fill(x, y, x + w, y + MapLayout.BUTTON_H, enabled ? (hover ? MapLayout.BUTTON_HOVER : MapLayout.BUTTON_BG) : MapLayout.BUTTON_DISABLED);
		g.text(font, label, x + (w - font.width(label)) / 2, y + 7, enabled ? MapLayout.TEXT : MapLayout.TEXT_DIM, false);
	}

	static void drawBorder(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
		g.fill(x, y, x + w, y + 1, color);
		g.fill(x, y + h - 1, x + w, y + h, color);
		g.fill(x, y, x + 1, y + h, color);
		g.fill(x + w - 1, y, x + w, y + h, color);
	}

	static boolean inside(double px, double py, int x, int y, int w, int h) {
		return px >= x && px < x + w && py >= y && py < y + h;
	}

	/** POIs can use this same shape later, with their description in detail. */
	private record MapSelection(Component name, Component detail, int color) {
	}
}
