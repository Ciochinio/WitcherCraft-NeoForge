package net.redboltmedia.witchercraft.client.gui.shell;

import net.redboltmedia.witchercraft.client.gui.MeditationLayout;
import net.redboltmedia.witchercraft.network.MeditationGuiButtonMessage;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * The meditation clock, a {@link GuiPage} inside {@link WitcherGuiScreen}.
 *
 * A smooth 24-hour dial: click anywhere on the ring and the nearest hour becomes
 * the target (no per-hour buttons, no slider). A dim hand shows the CURRENT world
 * time (live), a bright hand shows the picked TARGET, and a "Meditate" button
 * commits. Midnight (00:00) sits at the top, 06:00 right, 12:00 bottom, 18:00
 * left - a full 24h dial.
 *
 * All geometry + colours live in {@link MeditationLayout} (edited visually with
 * tools/meditation-dial-creator.html), which this page fit-scales into the shell
 * content region exactly like the perk page maps PerkEquipLayout. So this class
 * is only the dial surface + input; the gameplay stays in MCreator procedures.
 *
 * SLICE 1: rendering + input only. "Meditate" just records the pick; no server
 * action, no time change yet (that arrives with the meditation procedures + the
 * repurposed MeditationGuiButtonMessage in slice 2).
 */
public class MeditationPage implements GuiPage {

	private static final double TAU = Math.PI * 2.0;

	private final String pageId;

	// Client-only selection: the target hour (0-23). Defaults to the next hour.
	private int targetHour = -1;

	// Client spin state, set by MeditationSpinMessage when a session commits. The
	// client self-times the translucent overlay (no per-tick stop packet): the
	// world shows through while the server sweeps the real clock. See
	// beginClientSpin / isClientSpinning / wantsWorldVisible.
	private static volatile long clientSpinStartMs = 0L;
	private static volatile int clientSpinDurationMs = 0;
	private static volatile int clientSpinTargetHour = 0;
	// While spinning we force the vanilla HUD off for the F1-clean look; remember
	// the prior state so restoring never clobbers a user-set F1.
	private static boolean hudHidden = false;
	private static boolean prevHideGui = false;

	/** Called on the client (via MeditationSpinMessage) when a spin begins. */
	public static void beginClientSpin(int targetHour, int durationTicks) {
		clientSpinTargetHour = targetHour;
		clientSpinDurationMs = Math.max(0, durationTicks) * 50;
		clientSpinStartMs = System.currentTimeMillis();
		Minecraft mc = Minecraft.getInstance();
		if (!hudHidden)
			prevHideGui = mc.options.hideGui;
		hudHidden = true;
		mc.options.hideGui = true;
	}

	private static boolean isClientSpinning() {
		return System.currentTimeMillis() - clientSpinStartMs < clientSpinDurationMs;
	}

	/** End the client spin immediately (cancel) and restore the HUD. */
	private static void stopClientSpin() {
		clientSpinStartMs = 0L;
		clientSpinDurationMs = 0;
		restoreHud();
	}

	private static void restoreHud() {
		if (hudHidden) {
			Minecraft.getInstance().options.hideGui = prevHideGui;
			hudHidden = false;
		}
	}

	// Per-frame panel->screen mapping (set at the top of render), reused by input.
	private float scale;
	private float px, py;
	private Font font;

	public MeditationPage(String pageId) {
		this.pageId = pageId;
	}

	@Override
	public String id() {
		return pageId;
	}

	@Override
	public Component navLabel() {
		return Component.translatable("gui.witchercraft.shell.nav.meditation");
	}

	@Override
	public boolean wantsWorldVisible() {
		// during the spin, drop the shell background so the real sky shows through
		return isClientSpinning();
	}

	private static Player player() {
		return Minecraft.getInstance().player;
	}

	/** World time-of-day as a fractional hour in [0,24). MC tick 0 = 06:00. */
	private static double currentHourFloat() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null)
			return 0.0;
		// getDefaultClockTime(): the dimension's default WorldClock total ticks
		// (client-safe; the same accessor MCreator's "current time" block uses).
		double t = ((mc.level.getDefaultClockTime() % 24000.0) + 24000.0) % 24000.0;
		return (((t / 1000.0) + 6.0) % 24.0);
	}

	// ---- rendering -----------------------------------------------------------

	@Override
	public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, int mouseX, int mouseY, float partial) {
		this.font = Minecraft.getInstance().font;
		if (player() == null)
			return;

		// the spin ends on the client by its own timer -> restore the HUD when it does
		if (hudHidden && !isClientSpinning())
			restoreHud();

		double nowHour = currentHourFloat();
		if (targetHour < 0)
			targetHour = ((int) Math.ceil(nowHour)) % 24; // first open -> next hour

		// fit the fixed dial panel into the content region (uniform scale, centred),
		// then draw everything in panel coords through a nested pose transform.
		this.scale = MeditationLayout.fitScale(w, h);
		this.px = x + (w - MeditationLayout.PANEL_W * scale) / 2f;
		this.py = y + (h - MeditationLayout.PANEL_H * scale) / 2f;

		// mouse mapped back into panel coords (for the button hover this frame).
		int lmx = (int) ((mouseX - px) / scale), lmy = (int) ((mouseY - py) / scale);

		g.pose().pushMatrix();
		g.pose().translate(px, py);
		g.pose().scale(scale, scale);

		drawTicks(g);
		drawHand(g, nowHour, MeditationLayout.HAND_CURRENT_LEN, MeditationLayout.HAND_CURRENT_HALF, MeditationLayout.COL_HAND_CURRENT);
		drawHand(g, targetHour, MeditationLayout.HAND_TARGET_LEN, MeditationLayout.HAND_TARGET_HALF, MeditationLayout.COL_HAND_TARGET);
		drawTargetGlyph(g);
		// centre hub
		int hub = MeditationLayout.HUB_HALF;
		g.fill(MeditationLayout.CX - hub, MeditationLayout.CY - hub, MeditationLayout.CX + hub + 1, MeditationLayout.CY + hub + 1, MeditationLayout.COL_HUB);

		// readouts (centred on CX)
		Component now = Component.translatable("gui.witchercraft.shell.meditation.now", clock(nowHour));
		Component tgt = Component.translatable("gui.witchercraft.shell.meditation.target", clockHour(targetHour));
		centeredText(g, now, MeditationLayout.CX, MeditationLayout.NOW_Y, MeditationLayout.COL_TEXT_SUB);
		centeredText(g, tgt, MeditationLayout.CX, MeditationLayout.TARGET_Y, MeditationLayout.COL_TEXT_TITLE);

		// Meditate button
		int bx = MeditationLayout.BTN_X, by = MeditationLayout.BTN_Y, bw = MeditationLayout.BTN_W, bh = MeditationLayout.BTN_H;
		boolean hover = lmx >= bx && lmx < bx + bw && lmy >= by && lmy < by + bh;
		g.fill(bx, by, bx + bw, by + bh, hover ? MeditationLayout.COL_BTN_BG_HOVER : MeditationLayout.COL_BTN_BG);
		g.fill(bx, by, bx + bw, by + 1, MeditationLayout.COL_BTN_BORDER);
		g.fill(bx, by + bh - 1, bx + bw, by + bh, MeditationLayout.COL_BTN_BORDER);
		// while spinning the same button becomes Cancel (stop the meditation)
		Component btnLabel = Component.translatable(isClientSpinning() ? "gui.witchercraft.shell.meditation.cancel" : "gui.witchercraft.shell.meditation.button");
		centeredText(g, btnLabel, bx + bw / 2, by + bh / 2 - 4, MeditationLayout.COL_BTN_TEXT);

		g.pose().popMatrix();
	}

	/** 24 tick marks; every MAJOR_EVERY-th is longer + brighter and labelled. */
	private void drawTicks(GuiGraphicsExtractor g) {
		for (int hHour = 0; hHour < 24; hHour++) {
			boolean major = (hHour % MeditationLayout.MAJOR_EVERY) == 0;
			int len = major ? MeditationLayout.TICK_MAJOR_LEN : MeditationLayout.TICK_MINOR_LEN;
			int half = major ? MeditationLayout.TICK_MAJOR_HALF : MeditationLayout.TICK_MINOR_HALF;
			int col = major ? MeditationLayout.COL_TICK_MAJOR : MeditationLayout.COL_TICK_MINOR;
			drawRadial(g, hHour, MeditationLayout.RADIUS, MeditationLayout.RADIUS - len, half, col);
			if (major) {
				double a = hHour / 24.0 * TAU;
				int lx = MeditationLayout.CX + (int) (Math.sin(a) * MeditationLayout.LABEL_RADIUS);
				int ly = MeditationLayout.CY - (int) (Math.cos(a) * MeditationLayout.LABEL_RADIUS);
				String s = String.valueOf(hHour);
				g.text(font, Component.literal(s), lx - font.width(s) / 2, ly - 4, MeditationLayout.COL_TICK_MAJOR, false);
			}
		}
	}

	/** A hand from the hub outward to {@code len}, {@code half}px each side, at hour. */
	private void drawHand(GuiGraphicsExtractor g, double hour, int len, int half, int col) {
		g.pose().pushMatrix();
		g.pose().translate(MeditationLayout.CX, MeditationLayout.CY);
		g.pose().rotate((float) (hour / 24.0 * TAU));
		g.fill(-half, -len, half, MeditationLayout.HUB_HALF, col); // up = towards the hour before rotation
		g.pose().popMatrix();
	}

	/** A tick mark: a bar between two radii at the given hour (rotated into place). */
	private void drawRadial(GuiGraphicsExtractor g, double hour, int rOuter, int rInner, int half, int col) {
		g.pose().pushMatrix();
		g.pose().translate(MeditationLayout.CX, MeditationLayout.CY);
		g.pose().rotate((float) (hour / 24.0 * TAU));
		g.fill(-half, -rOuter, half, -rInner, col);
		g.pose().popMatrix();
	}

	/** Sun (day) or moon (night) marker sitting on the ring at the target hour. */
	private void drawTargetGlyph(GuiGraphicsExtractor g) {
		double a = targetHour / 24.0 * TAU;
		int gx = MeditationLayout.CX + (int) (Math.sin(a) * MeditationLayout.RADIUS);
		int gy = MeditationLayout.CY - (int) (Math.cos(a) * MeditationLayout.RADIUS);
		boolean day = targetHour >= 6 && targetHour < 18;
		int hs = MeditationLayout.GLYPH_HALF;
		g.fill(gx - hs, gy - hs, gx + hs + 1, gy + hs + 1, day ? MeditationLayout.COL_SUN : MeditationLayout.COL_MOON);
	}

	private void centeredText(GuiGraphicsExtractor g, Component c, int centerX, int topY, int col) {
		g.text(font, c, centerX - font.width(c) / 2, topY, col, false);
	}

	// ---- time formatting -----------------------------------------------------

	private static String clock(double hourFloat) {
		int hh = (int) hourFloat;
		int mm = (int) ((hourFloat - hh) * 60.0);
		return String.format("%02d:%02d", hh, mm);
	}

	private static String clockHour(int hour) {
		return String.format("%02d:00", ((hour % 24) + 24) % 24);
	}

	// ---- input ---------------------------------------------------------------

	@Override
	public boolean mouseClicked(int x, int y, int w, int h, double mouseX, double mouseY, int button) {
		if (button != 0)
			return false;
		// map the click back into panel coords through this frame's fit-scale.
		double lx = (mouseX - px) / scale, ly = (mouseY - py) / scale;

		// the button works in both states: Meditate when idle, Cancel while spinning
		if (lx >= MeditationLayout.BTN_X && lx < MeditationLayout.BTN_X + MeditationLayout.BTN_W
				&& ly >= MeditationLayout.BTN_Y && ly < MeditationLayout.BTN_Y + MeditationLayout.BTN_H) {
			if (isClientSpinning())
				sendCancel();
			else
				onMeditate();
			return true;
		}
		if (isClientSpinning())
			return true; // dial is locked while the world spins
		// dial: any click inside the ring picks the nearest hour
		double dx = lx - MeditationLayout.CX, dy = ly - MeditationLayout.CY;
		double dist = Math.sqrt(dx * dx + dy * dy);
		if (dist <= MeditationLayout.RADIUS * 1.25) {
			double a = Math.atan2(dx, -dy); // up = 0, clockwise positive
			if (a < 0)
				a += TAU;
			targetHour = ((int) Math.round(a / TAU * 24.0)) % 24;
			return true;
		}
		return false;
	}

	// Send the confirm to the server, which re-validates and starts the session.
	// buttonID = 1000 + targetHour (see MeditationGuiButtonMessage).
	private void onMeditate() {
		Player p = player();
		if (p == null || isClientSpinning())
			return;
		ClientPacketDistributor.sendToServer(new MeditationGuiButtonMessage(1000 + (((targetHour % 24) + 24) % 24), (int) p.getX(), (int) p.getY(), (int) p.getZ()));
	}

	// Cancel an in-progress meditation: tell the server to stop advancing and end
	// the client overlay now (buttonID 2000). Used by the Cancel button and by
	// closing the GUI mid-spin.
	private void sendCancel() {
		Player p = player();
		if (p != null)
			ClientPacketDistributor.sendToServer(new MeditationGuiButtonMessage(2000, (int) p.getX(), (int) p.getY(), (int) p.getZ()));
		stopClientSpin();
	}

	@Override
	public void onClose() {
		// closing the GUI during meditation cancels it (time stops where it reached)
		if (isClientSpinning())
			sendCancel();
		restoreHud();
	}
}
