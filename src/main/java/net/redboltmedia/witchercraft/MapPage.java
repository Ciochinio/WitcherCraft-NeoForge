package net.redboltmedia.witchercraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** Interactive diagnostic map shell. Terrain and marker data arrive in later milestones. */
public final class MapPage implements GuiPage {
	private static final double MIN_ZOOM = 0.25;
	private static final double MAX_ZOOM = 16.0;
	private static final double ZOOM_STEP = 1.25;

	private double centerX;
	private double centerZ;
	private double zoom = 1.0;
	private boolean dragging;

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
		centerOnPlayer();
		WorldMapClientTileCache.markViewDirty();
	}

	@Override
	public boolean pausesGame() {
		return WorldMapClientTileCache.canPause();
	}

	@Override
	public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, int mouseX, int mouseY, float partial) {
		Font font = Minecraft.getInstance().font;
		int vx = x + MapLayout.VIEW_X, vy = y + MapLayout.VIEW_Y;
		int vw = Math.min(MapLayout.VIEW_W, w - MapLayout.VIEW_X);
		int vh = Math.min(MapLayout.VIEW_H, h - MapLayout.VIEW_Y);
		g.fill(vx, vy, vx + vw, vy + vh, MapLayout.VIEW_BG);
		g.enableScissor(vx, vy, vx + vw, vy + vh);
		WorldMapClientTileCache.renderAndRequest(g, vx, vy, vw, vh, centerX, centerZ, zoom);
		drawPlayer(g, vx, vy, vw, vh);
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
			setZoom(zoom / ZOOM_STEP, vx + MapLayout.VIEW_W / 2.0, vy + MapLayout.VIEW_H / 2.0, vx, vy);
			return true;
		}
		if (inside(mouseX, mouseY, bx + MapLayout.ZOOM_IN_X, by, MapLayout.ZOOM_W, MapLayout.BUTTON_H)) {
			setZoom(zoom * ZOOM_STEP, vx + MapLayout.VIEW_W / 2.0, vy + MapLayout.VIEW_H / 2.0, vx, vy);
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
		int vx = x + MapLayout.VIEW_X, vy = y + MapLayout.VIEW_Y;
		if (!inside(mouseX, mouseY, vx, vy, MapLayout.VIEW_W, MapLayout.VIEW_H) || scrollY == 0)
			return false;
		setZoom(zoom * Math.pow(ZOOM_STEP, scrollY), mouseX, mouseY, vx, vy);
		return true;
	}

	@Override
	public void onClose() {
		dragging = false;
	}

	private void setZoom(double requested, double mouseX, double mouseY, int vx, int vy) {
		double next = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, requested));
		double anchorX = centerX + (mouseX - (vx + MapLayout.VIEW_W / 2.0)) / zoom;
		double anchorZ = centerZ + (mouseY - (vy + MapLayout.VIEW_H / 2.0)) / zoom;
		centerX = anchorX - (mouseX - (vx + MapLayout.VIEW_W / 2.0)) / next;
		centerZ = anchorZ - (mouseY - (vy + MapLayout.VIEW_H / 2.0)) / next;
		zoom = next;
		clampToWorldBorder();
		WorldMapClientTileCache.markViewDirty();
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
		if (Minecraft.getInstance().player == null)
			return;
		int px = (int) Math.round(x + w / 2.0 + (Minecraft.getInstance().player.getX() - centerX) * zoom);
		int py = (int) Math.round(y + h / 2.0 + (Minecraft.getInstance().player.getZ() - centerZ) * zoom);
		g.fill(px - 3, py - 1, px + 4, py + 2, MapLayout.PLAYER);
		g.fill(px - 1, py - 3, px + 2, py + 4, MapLayout.PLAYER);
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
