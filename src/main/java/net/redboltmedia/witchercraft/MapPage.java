package net.redboltmedia.witchercraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;

import org.lwjgl.glfw.GLFW;

import java.util.List;

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
	private int selectedColor;
	private int pendingCreateRequest;
	private Component createError;
	private List<Component> tooltip;
	private boolean pendingPing;
	private long pendingPingAt;
	private Identifier pendingPingDimension;
	private double pendingPingX;
	private double pendingPingZ;

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
		pendingCreateRequest = 0;
		createError = null;
		pendingPing = false;
		centerOnPlayer();
		WorldMapClientTileCache.markViewDirty();
		WorldMapWaypointClientCache.requestSnapshot();
	}

	@Override
	public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, int mouseX, int mouseY, float partial) {
		checkCreateResult();
		tooltip = null;
		Font font = Minecraft.getInstance().font;
		int vx = x + MapLayout.VIEW_X, vy = y + MapLayout.VIEW_Y;
		int vw = Math.min(MapLayout.VIEW_W, w - MapLayout.VIEW_X);
		int vh = Math.min(MapLayout.VIEW_H, h - MapLayout.VIEW_Y);
		animateZoom(vx, vy, vw, vh);
		placePendingPingIfReady();
		g.fill(vx, vy, vx + vw, vy + vh, MapLayout.VIEW_BG);
		g.enableScissor(vx, vy, vx + vw, vy + vh);
		WorldMapClientTileCache.renderAndRequest(g, vx, vy, vw, vh, centerX, centerZ, zoom);
		drawWaypoints(g, font, vx, vy, vw, vh, mouseX, mouseY);
		drawPlayer(g, vx, vy, vw, vh);
		if (creating)
			drawCreationOverlay(g, font, vx, vy, vw, vh, mouseX, mouseY);
		g.disableScissor();
		drawBorder(g, vx, vy, vw, vh, MapLayout.VIEW_BORDER);

		int bx = x + MapLayout.BAR_X, by = y + MapLayout.BAR_Y;
		int bw = Math.min(MapLayout.BAR_W, w - MapLayout.BAR_X);
		g.fill(bx, by, bx + bw, by + MapLayout.BAR_H, MapLayout.BAR_BG);
		drawButton(g, font, bx + MapLayout.WAYPOINTS_X, by + MapLayout.BUTTON_Y, MapLayout.WAYPOINTS_W, Component.translatableWithFallback("gui.witchercraft.map.waypoints", "Waypoints"), mouseX, mouseY, false);
		drawButton(g, font, bx + MapLayout.FILTERS_X, by + MapLayout.BUTTON_Y, MapLayout.FILTERS_W, Component.translatableWithFallback("gui.witchercraft.map.filters", "Filters"), mouseX, mouseY, false);
		drawButton(g, font, bx + MapLayout.CENTER_X, by + MapLayout.BUTTON_Y, MapLayout.CENTER_W, Component.translatableWithFallback("gui.witchercraft.map.center", "Center"), mouseX, mouseY, true);
		drawButton(g, font, bx + MapLayout.ZOOM_OUT_X, by + MapLayout.BUTTON_Y, MapLayout.ZOOM_W, Component.literal("-"), mouseX, mouseY, true);
		drawButton(g, font, bx + MapLayout.ZOOM_IN_X, by + MapLayout.BUTTON_Y, MapLayout.ZOOM_W, Component.literal("+"), mouseX, mouseY, true);

		if (inside(mouseX, mouseY, vx, vy, vw, vh)) {
			double wx = centerX + (mouseX - (vx + vw / 2.0)) / zoom;
			double wz = centerZ + (mouseY - (vy + vh / 2.0)) / zoom;
			Component coords = Component.literal("X " + (int) Math.floor(wx) + "  Z " + (int) Math.floor(wz));
			g.text(font, coords, bx + MapLayout.COORD_X, by + MapLayout.COORD_Y, MapLayout.TEXT, false);
		}
		g.text(font, Component.literal(String.format(java.util.Locale.ROOT, "%.2fx", zoom)), bx + MapLayout.HINT_X, by + MapLayout.HINT_Y, MapLayout.TEXT_DIM, false);
	}

	@Override
	public boolean mouseClicked(int x, int y, int w, int h, double mouseX, double mouseY, int button, boolean doubleClick) {
		int vx = x + MapLayout.VIEW_X, vy = y + MapLayout.VIEW_Y;
		int vw = Math.min(MapLayout.VIEW_W, w - MapLayout.VIEW_X);
		int vh = Math.min(MapLayout.VIEW_H, h - MapLayout.VIEW_Y);
		if (creating)
			return handleCreationClick(vx, vy, vw, vh, mouseX, mouseY, button);
		if (button == 1 && inside(mouseX, mouseY, vx, vy, vw, vh)) {
			double worldX = centerX + (mouseX - (vx + vw / 2.0)) / zoom;
			double worldZ = centerZ + (mouseY - (vy + vh / 2.0)) / zoom;
			if (doubleClick) {
				pendingPing = false;
				openCreation(worldX, worldZ);
			} else {
				var player = Minecraft.getInstance().player;
				if (player != null) {
					pendingPing = true;
					pendingPingAt = System.nanoTime();
					pendingPingDimension = player.level().dimension().identifier();
					pendingPingX = worldX;
					pendingPingZ = worldZ;
				}
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
		if (creating)
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
	public List<Component> pollTooltip() {
		List<Component> result = tooltip;
		tooltip = null;
		return result;
	}

	@Override
	public void onClose() {
		dragging = false;
		creating = false;
		pendingPing = false;
	}

	private void openCreation(double worldX, double worldZ) {
		dragging = false;
		createX = worldX;
		createZ = worldZ;
		createName = "";
		selectedIcon = 0;
		selectedColor = 0;
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
			int ix = startX + i * (MapLayout.CREATE_ICON_SIZE + MapLayout.CREATE_ICON_GAP);
			if (inside(mouseX, mouseY, ix, oy + MapLayout.CREATE_ICON_Y, MapLayout.CREATE_ICON_SIZE, MapLayout.CREATE_ICON_SIZE)) {
				if (pendingCreateRequest == 0)
					selectedIcon = i;
				return true;
			}
		}
		for (int i = 0; i < WorldMapWaypoints.WaypointColor.values().length; i++) {
			int cx = startX + i * (MapLayout.CREATE_COLOR_SIZE + MapLayout.CREATE_COLOR_GAP);
			if (inside(mouseX, mouseY, cx, oy + MapLayout.CREATE_COLOR_Y, MapLayout.CREATE_COLOR_SIZE, MapLayout.CREATE_COLOR_SIZE)) {
				if (pendingCreateRequest == 0)
					selectedColor = i;
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
		pendingCreateRequest = WorldMapWaypointClientCache.create(player.level().dimension().identifier(), createX, createZ, createName,
			WorldMapWaypoints.WaypointIcon.values()[selectedIcon], WorldMapWaypoints.WaypointColor.values()[selectedColor]);
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
		WorldMapWaypoints.WaypointColor tint = WorldMapWaypoints.WaypointColor.values()[selectedColor];
		for (int i = 0; i < WorldMapWaypoints.WaypointIcon.values().length; i++) {
			int ix = fieldX + i * (MapLayout.CREATE_ICON_SIZE + MapLayout.CREATE_ICON_GAP);
			if (i == selectedIcon)
				drawBorder(g, ix - 2, oy + MapLayout.CREATE_ICON_Y - 2, MapLayout.CREATE_ICON_SIZE + 4, MapLayout.CREATE_ICON_SIZE + 4, MapLayout.SELECTED);
			drawWaypointIcon(g, WorldMapWaypoints.WaypointIcon.values()[i].atlasIndex(), ix, oy + MapLayout.CREATE_ICON_Y, MapLayout.CREATE_ICON_SIZE, tint.argb());
		}
		g.text(font, Component.translatableWithFallback("gui.witchercraft.map.waypoint.color", "Color"), fieldX, oy + 94, MapLayout.TEXT_DIM, false);
		for (int i = 0; i < WorldMapWaypoints.WaypointColor.values().length; i++) {
			int cx = fieldX + i * (MapLayout.CREATE_COLOR_SIZE + MapLayout.CREATE_COLOR_GAP);
			g.fill(cx, oy + MapLayout.CREATE_COLOR_Y, cx + MapLayout.CREATE_COLOR_SIZE, oy + MapLayout.CREATE_COLOR_Y + MapLayout.CREATE_COLOR_SIZE, WorldMapWaypoints.WaypointColor.values()[i].argb());
			if (i == selectedColor)
				drawBorder(g, cx - 2, oy + MapLayout.CREATE_COLOR_Y - 2, MapLayout.CREATE_COLOR_SIZE + 4, MapLayout.CREATE_COLOR_SIZE + 4, MapLayout.SELECTED);
		}
		if (createError != null)
			g.text(font, createError, fieldX, oy + 126, MapLayout.ERROR, false);
		drawButton(g, font, fieldX, oy + MapLayout.CREATE_ACTION_Y, MapLayout.CREATE_ACTION_W, Component.translatableWithFallback("gui.cancel", "Cancel"), mouseX, mouseY, pendingCreateRequest == 0);
		int createButtonX = ox + MapLayout.CREATE_W - MapLayout.CREATE_PADDING - MapLayout.CREATE_ACTION_W;
		Component action = pendingCreateRequest == 0 ? Component.translatableWithFallback("gui.witchercraft.map.waypoint.save", "Save") : Component.translatableWithFallback("gui.witchercraft.map.waypoint.saving", "Saving...");
		drawButton(g, font, createButtonX, oy + MapLayout.CREATE_ACTION_Y, MapLayout.CREATE_ACTION_W, action, mouseX, mouseY, pendingCreateRequest == 0 && !createName.isBlank());
	}

	private void drawWaypoints(GuiGraphicsExtractor g, Font font, int x, int y, int w, int h, int mouseX, int mouseY) {
		var player = Minecraft.getInstance().player;
		if (player == null)
			return;
		int alpha = waypointAlpha();
		float visualScale = markerVisualScale();
		int size = Math.max(8, (int) Math.round(WAYPOINT_MARKER_BASE_SIZE * WorldMapClientConfig.markerScale()));
		double bestDistance = Double.MAX_VALUE;
		WorldMapWaypoints.Waypoint hovered = null;
		for (WorldMapWaypoints.Waypoint waypoint : WorldMapWaypointClientCache.waypoints(player.level().dimension().identifier())) {
			if (!waypoint.visible())
				continue;
			double px = x + w / 2.0 + (waypoint.x() - centerX) * zoom;
			double py = y + h / 2.0 + (waypoint.z() - centerZ) * zoom;
			double radius = size * visualScale;
			if (px + radius < x || px - radius >= x + w || py + radius < y || py - radius >= y + h)
				continue;
			int drawSize = waypoint.tracked() ? size + 4 : size;
			g.pose().pushMatrix();
			g.pose().translate((float) px, (float) py);
			g.pose().scale(visualScale, visualScale);
			if (waypoint.tracked())
				drawBorder(g, -drawSize / 2 - 2, -drawSize / 2 - 2, drawSize + 4, drawSize + 4, (alpha << 24) | (MapLayout.SELECTED & 0x00FFFFFF));
			int tint = (alpha << 24) | (waypoint.color().argb() & 0x00FFFFFF);
			drawWaypointIcon(g, waypoint.icon().atlasIndex(), -drawSize / 2, -drawSize / 2, drawSize, tint);
			g.pose().popMatrix();
			double dx = mouseX - px;
			double dy = mouseY - py;
			double distance = dx * dx + dy * dy;
			double hoverRadius = drawSize * visualScale / 2.0 + 3;
			if (!creating && distance <= hoverRadius * hoverRadius && distance < bestDistance) {
				bestDistance = distance;
				hovered = waypoint;
			}
		}
		drawTemporaryPin(g, player.level().dimension().identifier(), x, y, w, h, mouseX, mouseY, alpha, visualScale);
		if (hovered != null) {
			Component name = Component.literal(hovered.name()).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(hovered.color().argb() & 0x00FFFFFF)));
			tooltip = List.of(name, Component.literal("X " + (int) Math.floor(hovered.x()) + "  Z " + (int) Math.floor(hovered.z())));
		}
	}

	private void drawTemporaryPin(GuiGraphicsExtractor g, Identifier dimension, int x, int y, int w, int h, int mouseX, int mouseY, int alpha, float visualScale) {
		WorldMapWaypointClientCache.TemporaryPin pin = WorldMapWaypointClientCache.temporaryPin(dimension);
		if (pin == null)
			return;
		double px = x + w / 2.0 + (pin.x() - centerX) * zoom;
		double py = y + h / 2.0 + (pin.z() - centerZ) * zoom;
		int size = Math.max(8, (int) Math.round(WAYPOINT_MARKER_BASE_SIZE * WorldMapClientConfig.markerScale()));
		g.pose().pushMatrix();
		g.pose().translate((float) px, (float) py);
		g.pose().scale(visualScale, visualScale);
		drawWaypointIcon(g, 0, -size / 2, -size / 2, size, (alpha << 24) | (WorldMapWaypoints.WaypointColor.GOLD.argb() & 0x00FFFFFF));
		g.pose().popMatrix();
		double dx = mouseX - px;
		double dy = mouseY - py;
		double hoverRadius = size * visualScale / 2.0 + 3;
		if (!creating && dx * dx + dy * dy <= hoverRadius * hoverRadius) {
			Component name = Component.translatableWithFallback("gui.witchercraft.map.waypoint.target", "Target").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(WorldMapWaypoints.WaypointColor.GOLD.argb() & 0x00FFFFFF)));
			tooltip = List.of(name, Component.literal("X " + (int) Math.floor(pin.x()) + "  Z " + (int) Math.floor(pin.z())));
		}
	}

	private int waypointAlpha() {
		if (zoom >= 1.0)
			return 255;
		double progress = (zoom - MIN_ZOOM) / (1.0 - MIN_ZOOM);
		return (int) Math.round(89 + Math.max(0, Math.min(1, progress)) * 166);
	}

	private static void drawWaypointIcon(GuiGraphicsExtractor g, int index, int x, int y, int size, int color) {
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

	private void drawButton(GuiGraphicsExtractor g, Font font, int x, int y, int w, Component label, int mouseX, int mouseY, boolean enabled) {
		boolean hover = enabled && inside(mouseX, mouseY, x, y, w, MapLayout.BUTTON_H);
		g.fill(x, y, x + w, y + MapLayout.BUTTON_H, enabled ? (hover ? MapLayout.BUTTON_HOVER : MapLayout.BUTTON_BG) : MapLayout.BUTTON_DISABLED);
		g.text(font, label, x + (w - font.width(label)) / 2, y + 7, enabled ? MapLayout.TEXT : MapLayout.TEXT_DIM, false);
	}

	private static void drawBorder(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
		g.fill(x, y, x + w, y + 1, color);
		g.fill(x, y + h - 1, x + w, y + h, color);
		g.fill(x, y, x + 1, y + h, color);
		g.fill(x + w - 1, y, x + w, y + h, color);
	}

	private static boolean inside(double px, double py, int x, int y, int w, int h) {
		return px >= x && px < x + w && py >= y && py < y + h;
	}
}
