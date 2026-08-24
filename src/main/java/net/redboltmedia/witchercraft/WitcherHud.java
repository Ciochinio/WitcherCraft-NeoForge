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
import net.redboltmedia.witchercraft.procedures.MedallionVariantProcedure;
import net.redboltmedia.witchercraft.procedures.MedallionStepNearProcedure;
import net.redboltmedia.witchercraft.procedures.MedallionStepMidProcedure;
import net.redboltmedia.witchercraft.procedures.MedallionShowProcedure;
import net.redboltmedia.witchercraft.procedures.MedallionSenseRangeProcedure;
import net.redboltmedia.witchercraft.procedures.MedallionSenseAllHostilesProcedure;

import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;

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

	private static final Identifier MEDALLION = Identifier.parse("witchercraft:textures/screens/medallion.png");

	/**
	 * medallion.png is a single column of square MEDALLION_W x MEDALLION_H cells, one row per
	 * medallion variant, counted from the top. Adding a medallion is: append a row to the PNG,
	 * bump MEDALLION_VARIANTS, and return the new row index from MedallionVariant.
	 *
	 * There is one cell per variant, not an animation strip - the shake is pure translation
	 * (see drawMedallion), so a new medallion costs exactly one drawing.
	 */
	private static final int MEDALLION_W = 20;
	private static final int MEDALLION_H = 20;
	private static final int MEDALLION_VARIANTS = 2;

	/**
	 * Where the medallion sits. x is an offset from screen centre; y counts up from the bottom
	 * edge on the same origin the vanilla cursors use, so 39 is the row health and hunger start
	 * on and the medallion's base sits level with the top of that row.
	 *
	 * These two are the whole layout. Nudge them if another mod claims the same centre channel
	 * (Tough As Nails puts an icon there). The channel is only 20 pixels wide, between the right
	 * edge of the health row and the left edge of the hunger row, so MEDALLION_W cannot grow
	 * past 20 without overlapping them. Height is unconstrained; nothing else draws in that column.
	 */
	private static final int MEDALLION_X_OFFSET = 0;
	private static final int MEDALLION_Y_ORIGIN = 39;

	/** Ticks between proximity scans. The medallion animates every frame; it senses on this clock. */
	private static final int MEDALLION_SCAN_INTERVAL = 10;

	/**
	 * The shake, as x/y pixel offsets from rest. Deliberately not in ring order: stepping around a
	 * circle reads as an orbit, this order reads as a vibration.
	 *
	 * Amplitude is one pixel in every direction, which is why the art has to stay inside the middle
	 * 18 columns of its cell - at full lean the sprite would otherwise cross into the health or
	 * hunger row. Vertical has no such limit; nothing else draws in this column.
	 *
	 * Frame duration is indexed by intensity, so index 0 is unused (a resting medallion is still).
	 * Tune by the frame time, not by the cycle: because the order is scrambled rather than a smooth
	 * orbit, what you perceive is how often the sprite jumps, so 35ms reads as 29 moves a second.
	 * Around 30ms is the floor worth trying - at 60fps a new frame only arrives every 16.7ms, and
	 * below that the shake starts aliasing against the framerate instead of getting faster.
	 */
	private static final int[][] MEDALLION_SHAKE = { { -1, -1 }, { 1, 0 }, { 0, 1 }, { -1, 1 }, { 1, -1 }, { 0, -1 }, { -1, 0 }, { 1, 1 } };
	private static final long[] MEDALLION_FRAME_MS = { 0L, 180L, 120L, 70L };

	/**
	 * What the medallion reacts to. MEDALLION_SENSES is the explicit opt-in list;
	 * MedallionSenseAllHostiles additionally pulls in anything whose entity type declares
	 * MobCategory.MONSTER, which is how mod-added monsters are picked up without either mod
	 * knowing about the other. MEDALLION_IGNORES subtracts from both and ships empty - populate
	 * it if you decide the medallion should stay quiet around, say, illagers.
	 */
	private static final TagKey<EntityType<?>> MEDALLION_SENSES = TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("witchercraft:medallion_senses"));
	private static final TagKey<EntityType<?>> MEDALLION_IGNORES = TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("witchercraft:medallion_ignores"));

	/**
	 * Client-only sense state. Single player instance per client, so plain statics are fine.
	 *
	 * Seeded one full interval in the past so the very first frame scans. Do NOT reach for
	 * Integer.MIN_VALUE as the "never scanned" sentinel: tickCount - MIN_VALUE overflows int and
	 * wraps negative, the interval check then passes forever, and the medallion silently never
	 * senses anything.
	 */
	private static int medallionIntensity = 0;
	private static int medallionLastScan = -MEDALLION_SCAN_INTERVAL;

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
			drawBar(graphics, TOXICITY_BAR, TOXICITY_TEX_ROWS, xRight, graphics.guiHeight() - gui.rightHeight, fillRow, ToxicityHudFillProcedure.execute(player), true);
			gui.rightHeight += ROW;
		}
	}

	/**
	 * The medallion is not a bar and deliberately does not take part in the cursor protocol. It
	 * lives in the 20 pixel channel between the health and hunger columns, at a fixed height, so
	 * it never moves and never collides with anything stacking on either cursor.
	 *
	 * Because its y is a constant rather than a cursor read, it does not technically need the
	 * canHurtPlayer() gate the bars need - it would land correctly in creative. It uses that gate
	 * anyway, for looks rather than correctness: in creative the whole bar cluster is gone, and a
	 * lone medallion hovering over empty ground reads as a bug. Creative, spectator and F1 all hide
	 * it, exactly like the bars.
	 */
	@SubscribeEvent
	public static void onRenderMedallion(RenderGuiLayerEvent.Post event) {
		if (!event.getName().equals(VanillaGuiLayers.AIR_LEVEL))
			return;

		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		if (player == null || minecraft.options.hideGui || minecraft.gameMode == null || !minecraft.gameMode.canHurtPlayer())
			return;
		if (!MedallionShowProcedure.execute(player))
			return;

		refreshMedallionSense(player);
		// MedallionVariant reads witchercraftMedallion, which is set server side and reaches the
		// client through the usual player variable sync - the same route QuenHudShow relies on.
		// Because this runs every frame, the sprite swaps the instant the synced value changes.
		drawMedallion(event.getGuiGraphics(), medallionIntensity, (int) MedallionVariantProcedure.execute(player));
	}

	/**
	 * Rescans for monsters on MEDALLION_SCAN_INTERVAL and caches the result as an intensity step.
	 *
	 * This is client side on purpose. The client already tracks every entity the server sends it,
	 * so the scan costs nothing and needs no player variable and no packet - which also means it
	 * cannot go through markSyncDirty and drag all 86 variables across the wire.
	 *
	 * Steps rather than a continuous value: a wobble that varies smoothly with distance reads as
	 * noise, three states read as information.
	 */
	private static void refreshMedallionSense(Player player) {
		// tickCount restarts at 0 on respawn and on a dimension change, so treat any backwards
		// jump as due for a scan rather than waiting out a stale interval.
		if (player.tickCount >= medallionLastScan && player.tickCount - medallionLastScan < MEDALLION_SCAN_INTERVAL)
			return;
		medallionLastScan = player.tickCount;

		double range = MedallionSenseRangeProcedure.execute();
		double near = MedallionStepNearProcedure.execute();
		double mid = MedallionStepMidProcedure.execute();
		boolean allHostiles = MedallionSenseAllHostilesProcedure.execute();

		AABB box = player.getBoundingBox().inflate(range);
		double nearestSqr = Double.MAX_VALUE;
		for (Entity other : player.level().getEntities(player, box, e -> medallionSenses(e, allHostiles))) {
			double distanceSqr = other.distanceToSqr(player);
			if (distanceSqr < nearestSqr)
				nearestSqr = distanceSqr;
		}

		// The scan box is a cube; the range is a sphere. Without this the medallion reaches about
		// 1.7x further diagonally than it does straight ahead.
		if (nearestSqr > range * range) {
			medallionIntensity = 0;
			return;
		}

		double nearest = Math.sqrt(nearestSqr);
		medallionIntensity = nearest < near ? 3 : (nearest < mid ? 2 : 1);
	}

	private static boolean medallionSenses(Entity other, boolean allHostiles) {
		if (!(other instanceof LivingEntity living) || !living.isAlive())
			return false;
		if (other.is(MEDALLION_IGNORES))
			return false;
		return other.is(MEDALLION_SENSES) || (allHostiles && other.getType().getCategory() == MobCategory.MONSTER);
	}

	/**
	 * Shifts the whole sprite around its rest position by whole pixels, stepping the offset off the
	 * wall clock rather than off tick count so the shake stays smooth and independent of the 10 tick
	 * sense clock.
	 *
	 * Translation rather than rotation, and whole pixels rather than fractional: GUI textures sample
	 * nearest neighbour, so a rotated or sub-pixel 20 pixel sprite lands source pixels unevenly on
	 * screen pixels and shimmers while it moves. Every frame here is pixel-exact.
	 *
	 * The row is clamped because it comes from a procedure. An out of range value would otherwise
	 * blit past the end of the texture.
	 */
	private static void drawMedallion(GuiGraphicsExtractor graphics, int intensity, int variant) {
		int shakeX = 0;
		int shakeY = 0;
		if (intensity > 0) {
			int phase = (int) ((Util.getMillis() / MEDALLION_FRAME_MS[intensity]) % MEDALLION_SHAKE.length);
			shakeX = MEDALLION_SHAKE[phase][0];
			shakeY = MEDALLION_SHAKE[phase][1];
		}

		int row = Math.max(0, Math.min(variant, MEDALLION_VARIANTS - 1));
		int x = graphics.guiWidth() / 2 - MEDALLION_W / 2 + MEDALLION_X_OFFSET + shakeX;
		int y = graphics.guiHeight() - MEDALLION_Y_ORIGIN - MEDALLION_H + shakeY;
		graphics.blit(RenderPipelines.GUI_TEXTURED, MEDALLION, x, y, 0, row * MEDALLION_H, MEDALLION_W, MEDALLION_H, MEDALLION_W,
				MEDALLION_VARIANTS * MEDALLION_H);
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
	 *
	 * anchorRight grows the fill from the right edge leftward instead of the reverse.
	 * Every vanilla readout is anchored at the OUTER edge of its column and moves toward
	 * the centre (hearts and armor from xLeft, food and air from xRight), so a bar in the
	 * right column has to fill right to left or it reads as starting in mid-screen.
	 * Because the source crop and the destination offset are the same value, this is one
	 * extra term in both.
	 */
	private static void drawBar(GuiGraphicsExtractor graphics, Identifier texture, int texRows, int x, int y, int fillRow, double fill, boolean anchorRight) {
		int texH = texRows * BAR_H;
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, ROW_EMPTY * BAR_H, BAR_W, BAR_H, BAR_W, texH);
		int filled = (int) Math.round(Math.max(0.0, Math.min(1.0, fill)) * BAR_W);
		if (filled <= 0)
			return;
		int offset = anchorRight ? BAR_W - filled : 0;
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x + offset, y, offset, fillRow * BAR_H, filled, BAR_H, BAR_W, texH);
	}
}
