package net.redboltmedia.witchercraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Modal manager for the connection-scoped copy of personal waypoints. */
public final class WorldMapWaypointManagerOverlay {
	private static final int ICON_SIZE = 22;
	private static final int ACTION_GAP = 3;
	private static final int[] ACTION_WIDTHS = {38, 34, 32, 35};

	private final Host host;
	private boolean open;
	private String search = "";
	private int scroll;
	private UUID editingId;
	private String editName = "";
	private int editIcon;
	private UUID deleteId;
	private int pendingRequest;
	private PendingKind pendingKind;
	private Component error;

	public WorldMapWaypointManagerOverlay(Host host) {
		this.host = host;
	}

	public boolean isOpen() {
		return open;
	}

	public void open() {
		open = true;
		search = "";
		scroll = 0;
		editingId = null;
		deleteId = null;
		pendingRequest = 0;
		pendingKind = null;
		error = null;
		WorldMapWaypointClientCache.requestSnapshot();
	}

	public void close() {
		open = false;
		editingId = null;
		deleteId = null;
		pendingRequest = 0;
		pendingKind = null;
		error = null;
	}

	public void render(GuiGraphicsExtractor g, int vx, int vy, int vw, int vh, int mouseX, int mouseY) {
		if (!open)
			return;
		checkResult();
		Font font = Minecraft.getInstance().font;
		g.fill(vx, vy, vx + vw, vy + vh, MapLayout.OVERLAY_DIM);
		if (editingId != null) {
			drawEditor(g, font, vx, vy, vw, vh, mouseX, mouseY);
			return;
		}
		int ox = vx + (vw - MapLayout.MANAGER_W) / 2;
		int oy = vy + (vh - MapLayout.MANAGER_H) / 2;
		g.fill(ox, oy, ox + MapLayout.MANAGER_W, oy + MapLayout.MANAGER_H, MapLayout.OVERLAY_BG);
		MapPage.drawBorder(g, ox, oy, MapLayout.MANAGER_W, MapLayout.MANAGER_H, MapLayout.OVERLAY_BORDER);
		g.text(font, Component.translatableWithFallback("gui.witchercraft.map.waypoint.manager", "Waypoints"), ox + MapLayout.MANAGER_PADDING, oy + 10, MapLayout.TEXT, false);
		MapPage.drawButton(g, font, ox + MapLayout.MANAGER_W - 30, oy + 5, 22, Component.literal("X"), mouseX, mouseY, true);

		int fieldX = ox + MapLayout.MANAGER_PADDING;
		int fieldW = MapLayout.MANAGER_W - MapLayout.MANAGER_PADDING * 2;
		g.fill(fieldX, oy + MapLayout.MANAGER_SEARCH_Y, fieldX + fieldW, oy + MapLayout.MANAGER_SEARCH_Y + MapLayout.MANAGER_SEARCH_H, MapLayout.FIELD_BG);
		MapPage.drawBorder(g, fieldX, oy + MapLayout.MANAGER_SEARCH_Y, fieldW, MapLayout.MANAGER_SEARCH_H, MapLayout.VIEW_BORDER);
		String query = search.isEmpty() ? Component.translatableWithFallback("gui.witchercraft.map.waypoint.search", "Search...").getString()
				: search + (((System.currentTimeMillis() / 500L & 1L) == 0L && pendingRequest == 0) ? "_" : "");
		g.text(font, Component.literal(fitSuffix(font, query, fieldW - 10)), fieldX + 5, oy + MapLayout.MANAGER_SEARCH_Y + 5, search.isEmpty() ? MapLayout.TEXT_DIM : MapLayout.TEXT, false);

		List<WorldMapWaypoints.Waypoint> filtered = filteredWaypoints();
		clampScroll(filtered.size());
		int end = Math.min(filtered.size(), scroll + MapLayout.MANAGER_VISIBLE_ROWS);
		for (int row = scroll; row < end; row++)
			drawRow(g, font, filtered.get(row), ox, oy + MapLayout.MANAGER_LIST_Y + (row - scroll) * MapLayout.MANAGER_ROW_H, mouseX, mouseY);
		if (filtered.isEmpty())
			g.text(font, Component.translatableWithFallback("gui.witchercraft.map.waypoint.empty", "No matching waypoints"), fieldX, oy + MapLayout.MANAGER_LIST_Y + 8, MapLayout.TEXT_DIM, false);
		Component count = Component.literal(filtered.isEmpty() ? "0" : (scroll + 1) + "-" + end + " / " + filtered.size());
		g.text(font, count, ox + MapLayout.MANAGER_W - MapLayout.MANAGER_PADDING - font.width(count), oy + MapLayout.MANAGER_H - 13, MapLayout.TEXT_DIM, false);
		if (error != null)
			g.text(font, error, fieldX, oy + MapLayout.MANAGER_H - 13, MapLayout.ERROR, false);
		if (deleteId != null)
			drawDeleteConfirmation(g, font, ox, oy, mouseX, mouseY);
	}

	public boolean mouseClicked(int vx, int vy, int vw, int vh, double mouseX, double mouseY, int button) {
		if (!open)
			return false;
		if (button != 0)
			return true;
		if (editingId != null)
			return handleEditorClick(vx, vy, vw, vh, mouseX, mouseY);
		int ox = vx + (vw - MapLayout.MANAGER_W) / 2;
		int oy = vy + (vh - MapLayout.MANAGER_H) / 2;
		if (deleteId != null)
			return handleDeleteClick(ox, oy, mouseX, mouseY);
		if (MapPage.inside(mouseX, mouseY, ox + MapLayout.MANAGER_W - 30, oy + 5, 22, MapLayout.BUTTON_H)) {
			close();
			return true;
		}
		List<WorldMapWaypoints.Waypoint> filtered = filteredWaypoints();
		int localRow = (int) ((mouseY - (oy + MapLayout.MANAGER_LIST_Y)) / MapLayout.MANAGER_ROW_H);
		if (localRow < 0 || localRow >= MapLayout.MANAGER_VISIBLE_ROWS)
			return true;
		int index = scroll + localRow;
		if (index >= filtered.size())
			return true;
		WorldMapWaypoints.Waypoint waypoint = filtered.get(index);
		int actionX = ox + MapLayout.MANAGER_ACTION_X;
		for (int action = 0; action < ACTION_WIDTHS.length; action++) {
			if (MapPage.inside(mouseX, mouseY, actionX, oy + MapLayout.MANAGER_LIST_Y + localRow * MapLayout.MANAGER_ROW_H + 4, ACTION_WIDTHS[action], MapLayout.BUTTON_H)) {
				handleAction(action, waypoint);
				return true;
			}
			actionX += ACTION_WIDTHS[action] + ACTION_GAP;
		}
		return true;
	}

	public boolean mouseScrolled(double scrollY) {
		if (!open)
			return false;
		if (editingId == null && deleteId == null && scrollY != 0) {
			scroll -= (int) Math.signum(scrollY);
			clampScroll(filteredWaypoints().size());
		}
		return true;
	}

	public boolean keyPressed(int keyCode) {
		if (!open)
			return false;
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			if (deleteId != null)
				deleteId = null;
			else if (editingId != null)
				editingId = null;
			else close();
			error = null;
			return true;
		}
		if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) && editingId != null) {
			submitEdit();
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_BACKSPACE && pendingRequest == 0) {
			if (editingId != null)
				editName = removeLastCodePoint(editName);
			else if (deleteId == null)
				search = removeLastCodePoint(search);
			error = null;
			return true;
		}
		return true;
	}

	public boolean charTyped(int codepoint) {
		if (!open)
			return false;
		if (deleteId != null || pendingRequest != 0 || !Character.isValidCodePoint(codepoint) || Character.isISOControl(codepoint))
			return true;
		if (editingId != null) {
			if (editName.codePointCount(0, editName.length()) < WorldMapWaypoints.MAX_NAME_CHARACTERS)
				editName += Character.toString(codepoint);
		} else if (search.codePointCount(0, search.length()) < WorldMapWaypoints.MAX_NAME_CHARACTERS) {
			search += Character.toString(codepoint);
			scroll = 0;
		}
		error = null;
		return true;
	}

	private void drawRow(GuiGraphicsExtractor g, Font font, WorldMapWaypoints.Waypoint waypoint, int ox, int rowY, int mouseX, int mouseY) {
		int fieldX = ox + MapLayout.MANAGER_PADDING;
		g.fill(fieldX, rowY, ox + MapLayout.MANAGER_W - MapLayout.MANAGER_PADDING, rowY + MapLayout.MANAGER_ROW_H - 2, 0xB8202027);
		MapPage.drawWaypointIcon(g, waypoint.icon().atlasIndex(), fieldX + 3, rowY + 3, ICON_SIZE, MapLayout.WAYPOINT_COLOR);
		Component name = Component.literal(fit(font, waypoint.name(), 94));
		g.text(font, name, fieldX + 31, rowY + 5, MapLayout.WAYPOINT_COLOR, false);
		g.text(font, Component.literal(fit(font, rowInfo(waypoint), 150)), fieldX + 31, rowY + 16, MapLayout.TEXT_DIM, false);
		Component[] labels = {
				Component.translatableWithFallback(waypoint.visible() ? "gui.witchercraft.map.waypoint.hide" : "gui.witchercraft.map.waypoint.show", waypoint.visible() ? "Hide" : "Show"),
				Component.translatableWithFallback("gui.witchercraft.map.waypoint.edit_short", "Edit"),
				Component.translatableWithFallback("gui.witchercraft.map.waypoint.delete_short", "Del"),
				Component.translatableWithFallback("gui.witchercraft.map.waypoint.map", "Map")};
		int actionX = ox + MapLayout.MANAGER_ACTION_X;
		boolean sameDimension = isCurrentDimension(waypoint);
		for (int i = 0; i < ACTION_WIDTHS.length; i++) {
			boolean enabled = pendingRequest == 0 && (i != 3 || sameDimension);
			MapPage.drawButton(g, font, actionX, rowY + 4, ACTION_WIDTHS[i], labels[i], mouseX, mouseY, enabled);
			actionX += ACTION_WIDTHS[i] + ACTION_GAP;
		}
	}

	private void handleAction(int action, WorldMapWaypoints.Waypoint waypoint) {
		if (pendingRequest != 0)
			return;
		switch (action) {
			case 0 -> beginRequest(WorldMapWaypointClientCache.setVisible(waypoint.id(), !waypoint.visible()), PendingKind.VISIBILITY);
			case 1 -> openEditor(waypoint);
			case 2 -> deleteId = waypoint.id();
			case 3 -> {
				if (isCurrentDimension(waypoint)) {
					close();
					host.showOnMap(waypoint);
				}
			}
			default -> {
			}
		}
	}

	private void openEditor(WorldMapWaypoints.Waypoint waypoint) {
		editingId = waypoint.id();
		editName = waypoint.name();
		editIcon = waypoint.icon().ordinal();
		error = null;
	}

	private void drawEditor(GuiGraphicsExtractor g, Font font, int vx, int vy, int vw, int vh, int mouseX, int mouseY) {
		int ox = vx + (vw - MapLayout.CREATE_W) / 2;
		int oy = vy + (vh - MapLayout.CREATE_H) / 2;
		g.fill(ox, oy, ox + MapLayout.CREATE_W, oy + MapLayout.CREATE_H, MapLayout.OVERLAY_BG);
		MapPage.drawBorder(g, ox, oy, MapLayout.CREATE_W, MapLayout.CREATE_H, MapLayout.OVERLAY_BORDER);
		g.text(font, Component.translatableWithFallback("gui.witchercraft.map.waypoint.edit", "Edit waypoint"), ox + MapLayout.CREATE_PADDING, oy + 9, MapLayout.TEXT, false);
		int fieldX = ox + MapLayout.CREATE_PADDING;
		int fieldW = MapLayout.CREATE_W - MapLayout.CREATE_PADDING * 2;
		g.text(font, Component.translatableWithFallback("gui.witchercraft.map.waypoint.name", "Name"), fieldX, oy + 23, MapLayout.TEXT_DIM, false);
		g.fill(fieldX, oy + MapLayout.CREATE_NAME_Y, fieldX + fieldW, oy + MapLayout.CREATE_NAME_Y + MapLayout.CREATE_NAME_H, MapLayout.FIELD_BG);
		MapPage.drawBorder(g, fieldX, oy + MapLayout.CREATE_NAME_Y, fieldW, MapLayout.CREATE_NAME_H, error == null ? MapLayout.VIEW_BORDER : MapLayout.ERROR);
		String shownName = editName + (((System.currentTimeMillis() / 500L & 1L) == 0L && pendingRequest == 0) ? "_" : "");
		g.text(font, Component.literal(fitSuffix(font, shownName, fieldW - 10)), fieldX + 5, oy + MapLayout.CREATE_NAME_Y + 5, MapLayout.TEXT, false);
		g.text(font, Component.translatableWithFallback("gui.witchercraft.map.waypoint.icon", "Icon"), fieldX, oy + 56, MapLayout.TEXT_DIM, false);
		for (int i = 0; i < WorldMapWaypoints.WaypointIcon.values().length; i++) {
			int iconX = fieldX + i * (MapLayout.CREATE_ICON_SIZE + MapLayout.CREATE_ICON_GAP);
			if (i == editIcon)
				MapPage.drawBorder(g, iconX - 2, oy + MapLayout.CREATE_ICON_Y - 2, MapLayout.CREATE_ICON_SIZE + 4, MapLayout.CREATE_ICON_SIZE + 4, MapLayout.SELECTED);
			MapPage.drawWaypointIcon(g, WorldMapWaypoints.WaypointIcon.values()[i].atlasIndex(), iconX, oy + MapLayout.CREATE_ICON_Y, MapLayout.CREATE_ICON_SIZE, MapLayout.WAYPOINT_COLOR);
		}
		if (error != null)
			g.text(font, error, fieldX, oy + 96, MapLayout.ERROR, false);
		MapPage.drawButton(g, font, fieldX, oy + MapLayout.CREATE_ACTION_Y, MapLayout.CREATE_ACTION_W, Component.translatableWithFallback("gui.cancel", "Cancel"), mouseX, mouseY, pendingRequest == 0);
		int saveX = ox + MapLayout.CREATE_W - MapLayout.CREATE_PADDING - MapLayout.CREATE_ACTION_W;
		MapPage.drawButton(g, font, saveX, oy + MapLayout.CREATE_ACTION_Y, MapLayout.CREATE_ACTION_W, Component.translatableWithFallback("gui.witchercraft.map.waypoint.save", "Save"), mouseX, mouseY, pendingRequest == 0 && !editName.isBlank());
	}

	private boolean handleEditorClick(int vx, int vy, int vw, int vh, double mouseX, double mouseY) {
		int ox = vx + (vw - MapLayout.CREATE_W) / 2;
		int oy = vy + (vh - MapLayout.CREATE_H) / 2;
		int fieldX = ox + MapLayout.CREATE_PADDING;
		for (int i = 0; i < WorldMapWaypoints.WaypointIcon.values().length; i++) {
			int iconX = fieldX + i * (MapLayout.CREATE_ICON_SIZE + MapLayout.CREATE_ICON_GAP);
			if (MapPage.inside(mouseX, mouseY, iconX, oy + MapLayout.CREATE_ICON_Y, MapLayout.CREATE_ICON_SIZE, MapLayout.CREATE_ICON_SIZE)) {
				if (pendingRequest == 0)
					editIcon = i;
				return true;
			}
		}
		if (MapPage.inside(mouseX, mouseY, fieldX, oy + MapLayout.CREATE_ACTION_Y, MapLayout.CREATE_ACTION_W, MapLayout.BUTTON_H)) {
			if (pendingRequest == 0)
				editingId = null;
			return true;
		}
		int saveX = ox + MapLayout.CREATE_W - MapLayout.CREATE_PADDING - MapLayout.CREATE_ACTION_W;
		if (MapPage.inside(mouseX, mouseY, saveX, oy + MapLayout.CREATE_ACTION_Y, MapLayout.CREATE_ACTION_W, MapLayout.BUTTON_H)) {
			submitEdit();
			return true;
		}
		return true;
	}

	private void submitEdit() {
		if (editingId == null || pendingRequest != 0)
			return;
		String normalized = editName.strip();
		if (normalized.isEmpty()) {
			error = Component.translatableWithFallback("gui.witchercraft.map.waypoint.name_required", "Enter a waypoint name");
			return;
		}
		editName = normalized;
		beginRequest(WorldMapWaypointClientCache.edit(editingId, editName, WorldMapWaypoints.WaypointIcon.values()[editIcon]), PendingKind.EDIT);
	}

	private void drawDeleteConfirmation(GuiGraphicsExtractor g, Font font, int managerX, int managerY, int mouseX, int mouseY) {
		int w = 250;
		int h = 74;
		int x = managerX + (MapLayout.MANAGER_W - w) / 2;
		int y = managerY + (MapLayout.MANAGER_H - h) / 2;
		g.fill(x, y, x + w, y + h, MapLayout.OVERLAY_BG);
		MapPage.drawBorder(g, x, y, w, h, MapLayout.ERROR);
		g.text(font, Component.translatableWithFallback("gui.witchercraft.map.waypoint.delete_confirm", "Delete this waypoint?"), x + 12, y + 13, MapLayout.TEXT, false);
		MapPage.drawButton(g, font, x + 12, y + 42, 72, Component.translatableWithFallback("gui.cancel", "Cancel"), mouseX, mouseY, pendingRequest == 0);
		MapPage.drawButton(g, font, x + w - 84, y + 42, 72, Component.translatableWithFallback("gui.witchercraft.map.waypoint.delete", "Delete"), mouseX, mouseY, pendingRequest == 0);
	}

	private boolean handleDeleteClick(int managerX, int managerY, double mouseX, double mouseY) {
		int w = 250;
		int h = 74;
		int x = managerX + (MapLayout.MANAGER_W - w) / 2;
		int y = managerY + (MapLayout.MANAGER_H - h) / 2;
		if (MapPage.inside(mouseX, mouseY, x + 12, y + 42, 72, MapLayout.BUTTON_H)) {
			if (pendingRequest == 0)
				deleteId = null;
			return true;
		}
		if (MapPage.inside(mouseX, mouseY, x + w - 84, y + 42, 72, MapLayout.BUTTON_H) && pendingRequest == 0) {
			beginRequest(WorldMapWaypointClientCache.delete(deleteId), PendingKind.DELETE);
			return true;
		}
		return true;
	}

	private void beginRequest(int requestId, PendingKind kind) {
		if (requestId == 0) {
			error = Component.translatableWithFallback("gui.witchercraft.map.waypoint.not_connected", "Not connected to a server");
			return;
		}
		pendingRequest = requestId;
		pendingKind = kind;
		error = null;
	}

	private void checkResult() {
		if (pendingRequest == 0)
			return;
		WorldMapWaypointResultMessage result = WorldMapWaypointClientCache.takeResult(pendingRequest);
		if (result == null)
			return;
		pendingRequest = 0;
		if (result.status() == WorldMapWaypoints.Status.SUCCESS) {
			if (pendingKind == PendingKind.EDIT)
				editingId = null;
			if (pendingKind == PendingKind.DELETE)
				deleteId = null;
			error = null;
		} else {
			error = Component.translatableWithFallback("gui.witchercraft.map.waypoint.error." + result.status().name().toLowerCase(Locale.ROOT), "The server rejected this change");
		}
		pendingKind = null;
	}

	private List<WorldMapWaypoints.Waypoint> filteredWaypoints() {
		var player = Minecraft.getInstance().player;
		if (player == null)
			return List.of();
		Identifier current = player.level().dimension().identifier();
		String needle = search.strip().toLowerCase(Locale.ROOT);
		List<WorldMapWaypoints.Waypoint> result = new ArrayList<>();
		for (WorldMapWaypoints.Waypoint waypoint : WorldMapWaypointClientCache.waypoints())
			if (needle.isEmpty() || waypoint.name().toLowerCase(Locale.ROOT).contains(needle))
				result.add(waypoint);
		result.sort(Comparator.comparing((WorldMapWaypoints.Waypoint waypoint) -> !waypoint.dimension().equals(current)).thenComparing(waypoint -> waypoint.name().toLowerCase(Locale.ROOT)).thenComparing(WorldMapWaypoints.Waypoint::id));
		return result;
	}

	private String rowInfo(WorldMapWaypoints.Waypoint waypoint) {
		var player = Minecraft.getInstance().player;
		if (player != null && isCurrentDimension(waypoint)) {
			double dx = waypoint.x() - player.getX();
			double dz = waypoint.z() - player.getZ();
			return Math.round(Math.sqrt(dx * dx + dz * dz)) + " m";
		}
		return waypoint.dimension().toString();
	}

	private boolean isCurrentDimension(WorldMapWaypoints.Waypoint waypoint) {
		var player = Minecraft.getInstance().player;
		return player != null && waypoint.dimension().equals(player.level().dimension().identifier());
	}

	private void clampScroll(int size) {
		scroll = Math.max(0, Math.min(Math.max(0, size - MapLayout.MANAGER_VISIBLE_ROWS), scroll));
	}

	private static String fit(Font font, String value, int width) {
		if (font.width(value) <= width)
			return value;
		String result = value;
		while (!result.isEmpty() && font.width(result + "...") > width)
			result = result.substring(0, result.offsetByCodePoints(0, result.codePointCount(0, result.length()) - 1));
		return result + "...";
	}

	private static String fitSuffix(Font font, String value, int width) {
		String result = value;
		while (!result.isEmpty() && font.width(result) > width)
			result = result.substring(result.offsetByCodePoints(0, 1));
		return result;
	}

	private static String removeLastCodePoint(String value) {
		return value.isEmpty() ? value : value.substring(0, value.offsetByCodePoints(value.length(), -1));
	}

	private enum PendingKind {
		VISIBILITY, EDIT, DELETE
	}

	@FunctionalInterface
	public interface Host {
		void showOnMap(WorldMapWaypoints.Waypoint waypoint);
	}
}
