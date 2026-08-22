/*
 * The code of this mod element is always locked.
 *
 * WitcherCraft HUD bars.
 *
 * Both bars position themselves off NeoForge's Gui.leftHeight / Gui.rightHeight
 * cursors instead of fixed coordinates, so they stay correct with any amount of
 * armor, absorption or max health. See the GUI chapter of
 * docs/TECHNICAL_DESIGN_DOCUMENT.md before changing anything in here.
 *
 * All tunable logic lives in the QuenHud* / ToxicityHud* procedures, which are
 * ordinary block-based procedures and are meant to be edited in MCreator.
 * This file only decides WHERE a bar goes and blits it.
 */

package net.redboltmedia.witchercraft;

import net.redboltmedia.witchercraft.procedures.ToxicityHudOverdoseProcedure;
import net.redboltmedia.witchercraft.procedures.ToxicityHudFillProcedure;
import net.redboltmedia.witchercraft.procedures.QuenHudShowProcedure;
import net.redboltmedia.witchercraft.procedures.QuenHudPoolProcedure;

import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.Minecraft;

@EventBusSubscriber(Dist.CLIENT)
public class WitcherHud {

	private static final Identifier QUEN_ICONS = Identifier.parse("witchercraft:textures/screens/quen_icons.png");
	private static final Identifier TOXICITY_BAR = Identifier.parse("witchercraft:textures/screens/toxicity_bar.png");

	/** Vanilla's icon row is 10 * 8 + 1 pixels wide and sits on a 10 pixel pitch. */
	private static final int BAR_W = 81;
	private static final int BAR_H = 9;
	private static final int ROW = 10;

	/** Sprite sheet rows, counted from the top of the texture. */
	private static final int ROW_EMPTY = 0;
	private static final int TOXICITY_ROW_FILL = 1;
	private static final int TOXICITY_ROW_OVERDOSE = 2;

	private static final int TOXICITY_TEX_ROWS = 3;

	/** Quen icons: 9x9 cells on an 8 pixel step, 10 per row, same grid as hearts. */
	private static final int ICON = 9;
	private static final int ICON_STEP = 8;
	private static final int ICONS_PER_ROW = 10;
	private static final int POINTS_PER_ICON = 2;

	/** Columns in quen_icons.png. */
	private static final int QUEN_ICON_BIG = 0;
	private static final int QUEN_ICON_SMALL = 1;
	private static final int QUEN_ICON_CELLS = 2;

	@SubscribeEvent
	public static void onRenderGuiLayer(RenderGuiLayerEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;

		// These layer events fire even when the layer itself does not render (creative,
		// spectator, F1). In those cases nothing has advanced the cursors, so drawing
		// here would put the bars on top of the hotbar. Mirror vanilla's own gate.
		if (player == null || minecraft.options.hideGui || minecraft.gameMode == null || !minecraft.gameMode.canHurtPlayer())
			return;

		Gui gui = minecraft.gui;
		GuiGraphicsExtractor graphics = event.getGuiGraphics();
		int xLeft = graphics.guiWidth() / 2 - 91;
		int xRight = graphics.guiWidth() / 2 + 91 - BAR_W;

		// Left column, directly above health and armor.
		if (event.getName().equals(VanillaGuiLayers.ARMOR_LEVEL)) {
			if (!QuenHudShowProcedure.execute(player))
				return; // no icons and no cursor bump, exactly like armor when you wear none
			gui.leftHeight += drawQuenIcons(graphics, xLeft, graphics.guiHeight() - gui.leftHeight, QuenHudPoolProcedure.execute(player));
		}

		// Right column, above hunger and below oxygen. Hooked after VEHICLE_HEALTH
		// rather than FOOD_LEVEL so that mounted horse hearts are also accounted for.
		else if (event.getName().equals(VanillaGuiLayers.VEHICLE_HEALTH)) {
			int fillRow = ToxicityHudOverdoseProcedure.execute(player) ? TOXICITY_ROW_OVERDOSE : TOXICITY_ROW_FILL;
			drawBar(graphics, TOXICITY_BAR, TOXICITY_TEX_ROWS, xRight, graphics.guiHeight() - gui.rightHeight, fillRow, ToxicityHudFillProcedure.execute(player));
			gui.rightHeight += ROW;
		}
	}

	/**
	 * Draws the Quen shield as heart-style icons: one big bubble per 2 points of pool,
	 * a small bubble for a leftover odd point, 10 per row, extra rows stacking upward.
	 *
	 * This mirrors vanilla's Gui.extractHearts loop, minus the empty container pass -
	 * only the shield you actually have is drawn, so there are no empty sockets.
	 * Row pitch compresses past two rows exactly like vanilla hearts do, so a very
	 * large pool cannot walk off the top of the screen.
	 *
	 * @return how far to advance Gui.leftHeight, matching what vanilla's health layer does
	 */
	private static int drawQuenIcons(GuiGraphicsExtractor graphics, int xLeft, int yLineBase, double pool) {
		int points = (int) Math.ceil(pool); // any fraction left still earns a small bubble
		if (points <= 0)
			return 0;

		int iconCount = (points + POINTS_PER_ICON - 1) / POINTS_PER_ICON;
		int rows = (iconCount + ICONS_PER_ROW - 1) / ICONS_PER_ROW;
		int rowHeight = Math.max(10 - (rows - 2), 3);

		for (int i = iconCount - 1; i >= 0; i--) {
			int column = i % ICONS_PER_ROW;
			int row = i / ICONS_PER_ROW;
			int xo = xLeft + column * ICON_STEP;
			int yo = yLineBase - row * rowHeight;
			// The last icon is small when the pool is an odd number of points.
			int cell = (i * POINTS_PER_ICON + 1 == points) ? QUEN_ICON_SMALL : QUEN_ICON_BIG;
			graphics.blit(RenderPipelines.GUI_TEXTURED, QUEN_ICONS, xo, yo, cell * ICON, 0, ICON, ICON, QUEN_ICON_CELLS * ICON, ICON);
		}

		return (rows - 1) * rowHeight + ROW;
	}

	/**
	 * Draws the empty track, then the fill row clipped to the filled width. Same
	 * partial-width blit the vanilla experience bar uses, so the fill is smooth
	 * rather than stepped.
	 */
	private static void drawBar(GuiGraphicsExtractor graphics, Identifier texture, int texRows, int x, int y, int fillRow, double fill) {
		int texH = texRows * BAR_H;
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, ROW_EMPTY * BAR_H, BAR_W, BAR_H, BAR_W, texH);
		int filled = (int) Math.round(Math.max(0.0, Math.min(1.0, fill)) * BAR_W);
		if (filled > 0)
			graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, fillRow * BAR_H, filled, BAR_H, BAR_W, texH);
	}
}
