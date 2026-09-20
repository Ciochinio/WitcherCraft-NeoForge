package net.redboltmedia.witchercraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * Client HUD lifecycle and configurable shell for the minimap. Terrain, map rotation, markers, and
 * the final circular mask are added by the following implementation steps without starting a second
 * map-data pipeline.
 */
@EventBusSubscriber(Dist.CLIENT)
public final class WorldMapMinimapHud {
	private static final int MARGIN = 8;
	private static final int BACKGROUND = 0xD0181714;
	private static final int TEXT = 0xFFE8DDBE;
	private static final Identifier PLAYER_MARKER = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "textures/screens/map_player_arrow.png");
	private static final Identifier SQUARE_FRAME = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "textures/screens/minimap_frame_square.png");

	private WorldMapMinimapHud() {}

	@SubscribeEvent
	public static void render(RenderGuiEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!shouldRender(minecraft))
			return;

		GuiGraphicsExtractor graphics = event.getGuiGraphics();
		int size = Math.min(WorldMapClientConfig.minimapSize(), Math.min(graphics.guiWidth() - 2 * MARGIN, graphics.guiHeight() - 2 * MARGIN));
		if (size < 32)
			return;

		int x = switch (WorldMapClientConfig.minimapCorner()) {
			case TOP_LEFT, BOTTOM_LEFT -> MARGIN;
			case TOP_RIGHT, BOTTOM_RIGHT -> graphics.guiWidth() - MARGIN - size;
		};
		int y = switch (WorldMapClientConfig.minimapCorner()) {
			case TOP_LEFT, TOP_RIGHT -> MARGIN;
			case BOTTOM_LEFT, BOTTOM_RIGHT -> graphics.guiHeight() - MARGIN - size;
		};

		drawSquareShell(graphics, x, y, size);
		float yaw = minecraft.player.getViewYRot(event.getPartialTick().getGameTimeDeltaPartialTick(false));
		drawNorth(graphics, minecraft.font, x, y, size, yaw);
		drawPlayer(graphics, x + size / 2.0f, y + size / 2.0f, size, yaw);
	}

	private static boolean shouldRender(Minecraft minecraft) {
		return minecraft.player != null && minecraft.level != null && !minecraft.options.hideGui
			&& WorldMapClientConfig.minimapEnabled() && WorldMapServerConfig.minimapEnabled();
	}

	private static void drawSquareShell(GuiGraphicsExtractor graphics, int x, int y, int size) {
		graphics.fill(x, y, x + size, y + size, BACKGROUND);
		graphics.blit(RenderPipelines.GUI_TEXTURED, SQUARE_FRAME, x, y, 0.0F, 0.0F, size, size, 64, 64, 64, 64);
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
