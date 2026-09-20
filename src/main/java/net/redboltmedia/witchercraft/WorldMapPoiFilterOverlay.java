package net.redboltmedia.witchercraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import org.lwjgl.glfw.GLFW;

import java.util.UUID;

/** Small modal for world-scoped waypoint and POI visibility preferences. */
public final class WorldMapPoiFilterOverlay {
	private boolean open;

	public boolean isOpen() {
		return open;
	}

	public void open() {
		open = true;
	}

	public void close() {
		open = false;
	}

	public void render(GuiGraphicsExtractor g, int vx, int vy, int vw, int vh, int mouseX, int mouseY) {
		if (!open)
			return;
		Font font = Minecraft.getInstance().font;
		g.fill(vx, vy, vx + vw, vy + vh, MapLayout.OVERLAY_DIM);
		int x = vx + (vw - MapLayout.FILTER_W) / 2;
		int y = vy + (vh - MapLayout.FILTER_H) / 2;
		g.fill(x, y, x + MapLayout.FILTER_W, y + MapLayout.FILTER_H, MapLayout.OVERLAY_BG);
		MapPage.drawBorder(g, x, y, MapLayout.FILTER_W, MapLayout.FILTER_H, MapLayout.OVERLAY_BORDER);
		g.text(font, Component.translatableWithFallback("gui.witchercraft.map.filters.title", "Map filters"), x + MapLayout.FILTER_PADDING, y + 9, MapLayout.TEXT, false);
		UUID worldId = WorldMapPoiClientCache.worldId();
		drawRow(g, font, x, y, 0, Component.translatableWithFallback("gui.witchercraft.map.filters.waypoints", "Personal waypoints"),
			WorldMapPoiFilterPreferences.state(worldId, WorldMapPoiFilterPreferences.Filter.PERSONAL_WAYPOINTS), mouseX, mouseY);
		drawRow(g, font, x, y, 1, Component.translatableWithFallback("gui.witchercraft.map.filters.unknown", "Undiscovered POIs"),
			WorldMapPoiFilterPreferences.state(worldId, WorldMapPoiFilterPreferences.Filter.UNKNOWN_POIS), mouseX, mouseY);
		drawRow(g, font, x, y, 2, Component.translatableWithFallback("gui.witchercraft.map.filters.discovered", "Discovered POIs"),
			WorldMapPoiFilterPreferences.state(worldId, WorldMapPoiFilterPreferences.Filter.DISCOVERED_POIS), mouseX, mouseY);
	}

	private void drawRow(GuiGraphicsExtractor g, Font font, int x, int y, int row, Component label,
		WorldMapPoiFilterPreferences.DisplayState state, int mouseX, int mouseY) {
		int rowX = x + MapLayout.FILTER_PADDING;
		int rowY = y + MapLayout.FILTER_ROW_Y + row * MapLayout.FILTER_ROW_GAP;
		int rowW = MapLayout.FILTER_W - MapLayout.FILTER_PADDING * 2;
		boolean hover = MapPage.inside(mouseX, mouseY, rowX, rowY, rowW, MapLayout.FILTER_ROW_H);
		g.fill(rowX, rowY, rowX + rowW, rowY + MapLayout.FILTER_ROW_H, hover ? MapLayout.BUTTON_HOVER : MapLayout.BUTTON_BG);
		Component stateLabel = switch (state) {
			case DEFAULT -> Component.translatableWithFallback("gui.witchercraft.map.filters.default", "Default");
			case SHOWN -> Component.translatableWithFallback("gui.witchercraft.map.filters.shown", "Shown");
			case HIDDEN -> Component.translatableWithFallback("gui.witchercraft.map.filters.hidden", "Hidden");
		};
		int labelWidth = rowW - font.width(stateLabel) - 24;
		g.text(font, Component.literal(fit(font, label.getString(), labelWidth)), rowX + 7, rowY + 7, MapLayout.TEXT, false);
		g.text(font, stateLabel, rowX + rowW - 7 - font.width(stateLabel), rowY + 7,
			state == WorldMapPoiFilterPreferences.DisplayState.HIDDEN ? MapLayout.TEXT_DIM : MapLayout.SELECTED, false);
	}

	public boolean mouseClicked(int vx, int vy, int vw, int vh, double mouseX, double mouseY, int button) {
		if (!open)
			return false;
		if (button != 0)
			return true;
		int x = vx + (vw - MapLayout.FILTER_W) / 2;
		int y = vy + (vh - MapLayout.FILTER_H) / 2;
		if (!MapPage.inside(mouseX, mouseY, x, y, MapLayout.FILTER_W, MapLayout.FILTER_H)) {
			close();
			return true;
		}
		int rowX = x + MapLayout.FILTER_PADDING;
		int rowW = MapLayout.FILTER_W - MapLayout.FILTER_PADDING * 2;
		for (int row = 0; row < 3; row++) {
			int rowY = y + MapLayout.FILTER_ROW_Y + row * MapLayout.FILTER_ROW_GAP;
			if (MapPage.inside(mouseX, mouseY, rowX, rowY, rowW, MapLayout.FILTER_ROW_H)) {
				WorldMapPoiFilterPreferences.Filter filter = WorldMapPoiFilterPreferences.Filter.values()[row];
				WorldMapPoiFilterPreferences.toggle(WorldMapPoiClientCache.worldId(), filter, defaultShown(filter));
				return true;
			}
		}
		return true;
	}

	public boolean keyPressed(int keyCode) {
		if (!open)
			return false;
		if (keyCode == GLFW.GLFW_KEY_ESCAPE)
			close();
		return true;
	}

	private boolean defaultShown(WorldMapPoiFilterPreferences.Filter filter) {
		if (filter == WorldMapPoiFilterPreferences.Filter.PERSONAL_WAYPOINTS)
			return true;
		var player = Minecraft.getInstance().player;
		if (player == null)
			return true;
		Identifier dimension = player.level().dimension().identifier();
		boolean found = false;
		for (WorldMapPoiMarker marker : WorldMapPoiClientCache.markers(dimension)) {
			boolean wanted = filter == WorldMapPoiFilterPreferences.Filter.UNKNOWN_POIS
				? marker instanceof WorldMapPoiMarker.Unknown : marker instanceof WorldMapPoiMarker.Discovered;
			if (wanted) {
				found = true;
				if (marker.defaultVisible())
					return true;
			}
		}
		return !found;
	}

	private static String fit(Font font, String value, int width) {
		if (font.width(value) <= width)
			return value;
		String result = value;
		while (!result.isEmpty() && font.width(result + "...") > width)
			result = result.substring(0, result.offsetByCodePoints(0, result.codePointCount(0, result.length()) - 1));
		return result + "...";
	}
}
