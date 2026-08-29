package net.redboltmedia.witchercraft.client.gui.shell;

import net.redboltmedia.witchercraft.client.gui.MeditationLayout;
import net.redboltmedia.witchercraft.network.MeditationGuiButtonMessage;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

/**
 * The meditation clock, a {@link GuiPage} inside {@link WitcherGuiScreen}.
 *
 * A smooth 24-hour dial: click anywhere on the ring and the nearest hour becomes
 * the target (no per-hour buttons, no slider). A dim hand shows the CURRENT world
 * time (live), a bright hand shows the picked TARGET, and a "Meditate" button
 * commits. The dial reads day-up: NOON (12:00) sits at the top, 18:00 right,
 * midnight (00:00) bottom, 06:00 left - a full 24h dial.
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
	// Half-turn so the dial reads day-up / night-down: NOON sits at the top, 00:00 at
	// the bottom (without this, hour 0 = midnight lands at the top). Every hour->angle
	// conversion goes through hourAngle(); mouseClicked() inverts it.
	private static final double DIAL_OFFSET = Math.PI;

	/** Screen rotation (radians, 0 = up, clockwise) for a dial hour, with the day-up offset. */
	private static double hourAngle(double hour) {
		return hour / 24.0 * TAU + DIAL_OFFSET;
	}

	// Time-of-day orb textures (parsed once); positions/sizes come from MeditationLayout.
	private static final Identifier ORB_DAWN = Identifier.parse(MeditationLayout.ORB_DAWN_TEX);
	private static final Identifier ORB_NOON = Identifier.parse(MeditationLayout.ORB_NOON_TEX);
	private static final Identifier ORB_DUSK = Identifier.parse(MeditationLayout.ORB_DUSK_TEX);
	private static final Identifier ORB_NIGHT = Identifier.parse(MeditationLayout.ORB_NIGHT_TEX);

	private final String pageId;

	// Client-only selection: the target hour (0-23). Defaults to the next hour.
	private int targetHour = -1;

	// Client spin state, armed by MeditationSpinMessage when a session commits. The
	// client self-times the whole transition (no per-tick packets): a short fade in
	// from black, the world spinning with the world + dial visible, then a fade out
	// to black and auto-close (like waking from a bed). Timeline helpers below:
	// spinFadeAlpha / requestsClose / wantsWorldVisible.
	private static final int FADE_MS = 400;
	private static volatile long spinStartMs = 0L;
	private static volatile int spinMs = 0; // server spin length; the fade-out follows it
	private static volatile int spinTargetHour = 0;
	private static volatile boolean spinActive = false; // overlay owns the screen (world visible, dial locked)
	private static volatile boolean pendingClose = false; // natural finish -> the shell should close
	// While spinning we force the vanilla HUD off for the F1-clean look; remember
	// the prior state so restoring never clobbers a user-set F1.
	private static boolean hudHidden = false;
	private static boolean prevHideGui = false;

	/** Called on the client (via MeditationSpinMessage) when a spin begins. */
	public static void beginClientSpin(int targetHour, int durationTicks) {
		spinTargetHour = targetHour;
		spinMs = Math.max(0, durationTicks) * 50;
		spinStartMs = System.currentTimeMillis();
		spinActive = true;
		pendingClose = false;
		Minecraft mc = Minecraft.getInstance();
		if (!hudHidden)
			prevHideGui = mc.options.hideGui;
		hudHidden = true;
		mc.options.hideGui = true;
	}

	/**
	 * Called on the client (via MeditationRejectMessage) when the server refuses a
	 * Meditate: show the reason on the action bar and close the meditation GUI, so a
	 * blocked click gives feedback instead of silently doing nothing. {@code reason}
	 * is a {@link net.redboltmedia.witchercraft.procedures.MeditationCanStartProcedure}
	 * BLOCKED_* code (compile-time constant, so no server class loads on the client).
	 */
	public static void rejectAndClose(int reason) {
		Minecraft mc = Minecraft.getInstance();
		String key = reason == net.redboltmedia.witchercraft.procedures.MeditationCanStartProcedure.BLOCKED_MONSTER
				? "gui.witchercraft.shell.meditation.blocked_monster"
				: "gui.witchercraft.shell.meditation.blocked_space";
		if (mc.player != null)
			mc.gui.setOverlayMessage(Component.translatable(key), false); // action bar
		if (mc.screen instanceof WitcherGuiScreen)
			mc.setScreen(null);
	}

	private static long spinElapsed() {
		return System.currentTimeMillis() - spinStartMs;
	}

	/** True while the meditation overlay owns the screen (world visible, dial locked). */
	private static boolean spinning() {
		return spinActive;
	}

	/** True only while the spin can still be cancelled (before the finishing fade-out). */
	private static boolean cancellable() {
		return spinActive && !pendingClose;
	}

	/** Drop the overlay and restore the HUD (used by cancel + close + finish). */
	private static void endSpin() {
		spinActive = false;
		pendingClose = false;
		restoreHud();
	}

	private static void restoreHud() {
		if (hudHidden) {
			Minecraft.getInstance().options.hideGui = prevHideGui;
			hudHidden = false;
		}
	}

	/** Fullscreen black overlay alpha (0..1): fades IN from black at the very start only. */
	private static float spinFadeAlphaValue() {
		if (!spinActive)
			return 0f;
		long e = spinElapsed();
		if (e < FADE_MS)
			return 1f - e / (float) FADE_MS; // fade in from black
		return 0f; // the finish is handled by fading the dial itself, not a black flash
	}

	/**
	 * The dial's own opacity (0..1). Full while spinning; in the last FADE_MS it
	 * fades the clock (ticks, hands, text, button - everything this page draws)
	 * down to fully transparent, so what remains at the end is just the plain
	 * world (the HUD is already hidden) - not a black flash. The shell then closes
	 * once this reaches 0, which is visually seamless.
	 */
	private static float contentAlphaValue() {
		if (!spinActive)
			return 1f;
		long e = spinElapsed();
		if (e < spinMs)
			return 1f;
		if (e < spinMs + FADE_MS)
			return 1f - (e - spinMs) / (float) FADE_MS;
		return 0f;
	}

	// Per-frame panel->screen mapping (set at the top of render), reused by input.
	private float scale;
	private float px, py;
	private Font font;
	// This frame's dial opacity (see contentAlphaValue) - all draw calls go through
	// fillA/textA below, which apply it to every colour's alpha channel.
	private float contentAlpha = 1f;

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
		return spinning();
	}

	@Override
	public float spinFadeAlpha() {
		return spinFadeAlphaValue();
	}

	@Override
	public boolean requestsClose() {
		return pendingClose;
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

		// natural finish: after the spin + fade-out, ask the shell to close (bed-wake)
		if (spinActive && spinElapsed() >= spinMs + FADE_MS)
			pendingClose = true;

		this.contentAlpha = contentAlphaValue();

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
		drawOrbs(g);
		drawArc(g, nowHour, targetHour);
		drawArrow(g, nowHour, MeditationLayout.HAND_CURRENT_LEN, MeditationLayout.HAND_CURRENT_HALF, MeditationLayout.COL_HAND_CURRENT);
		drawArrow(g, targetHour, MeditationLayout.HAND_TARGET_LEN, MeditationLayout.HAND_TARGET_HALF, MeditationLayout.COL_HAND_TARGET);
		// centre hub
		int hub = MeditationLayout.HUB_HALF;
		fillA(g, MeditationLayout.CX - hub, MeditationLayout.CY - hub, MeditationLayout.CX + hub + 1, MeditationLayout.CY + hub + 1, MeditationLayout.COL_HUB);

		// readouts (centred on CX)
		Component now = Component.translatable("gui.witchercraft.shell.meditation.now", clock(nowHour));
		Component tgt = Component.translatable("gui.witchercraft.shell.meditation.target", clockHour(targetHour));
		centeredText(g, now, MeditationLayout.CX, MeditationLayout.NOW_Y, MeditationLayout.COL_TEXT_SUB);
		centeredText(g, tgt, MeditationLayout.CX, MeditationLayout.TARGET_Y, MeditationLayout.COL_TEXT_TITLE);

		// Meditate button
		int bx = MeditationLayout.BTN_X, by = MeditationLayout.BTN_Y, bw = MeditationLayout.BTN_W, bh = MeditationLayout.BTN_H;
		boolean hover = lmx >= bx && lmx < bx + bw && lmy >= by && lmy < by + bh;
		fillA(g, bx, by, bx + bw, by + bh, hover ? MeditationLayout.COL_BTN_BG_HOVER : MeditationLayout.COL_BTN_BG);
		fillA(g, bx, by, bx + bw, by + 1, MeditationLayout.COL_BTN_BORDER);
		fillA(g, bx, by + bh - 1, bx + bw, by + bh, MeditationLayout.COL_BTN_BORDER);
		// while spinning the same button becomes Cancel (stop the meditation)
		Component btnLabel = Component.translatable(cancellable() ? "gui.witchercraft.shell.meditation.cancel" : "gui.witchercraft.shell.meditation.button");
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
			if (major && MeditationLayout.SHOW_NUMBERS != 0) {
				double a = hourAngle(hHour);
				int lx = MeditationLayout.CX + (int) (Math.sin(a) * MeditationLayout.LABEL_RADIUS);
				int ly = MeditationLayout.CY - (int) (Math.cos(a) * MeditationLayout.LABEL_RADIUS);
				String s = String.valueOf(hHour);
				textA(g, Component.literal(s), lx - font.width(s) / 2, ly - 4, MeditationLayout.COL_TICK_MAJOR);
			}
		}
	}

	/**
	 * A clock hand drawn as an ARROW at {@code hour}: a shaft from the hub out to the
	 * arrowhead base, then a triangular head tapering to the tip at {@code len}. Drawn
	 * in the rotated frame (up = towards the hour), the head is a stack of 1px rows so
	 * it works with axis-aligned fills.
	 */
	private void drawArrow(GuiGraphicsExtractor g, double hour, int len, int half, int col) {
		int headLen = Math.min(MeditationLayout.HAND_HEAD_LEN, len - MeditationLayout.HUB_HALF);
		int headHalf = Math.max(half, MeditationLayout.HAND_HEAD_HALF);
		int baseY = -(len - headLen); // where shaft meets the arrowhead (up = negative)
		g.pose().pushMatrix();
		g.pose().translate(MeditationLayout.CX, MeditationLayout.CY);
		g.pose().rotate((float) hourAngle(hour));
		// shaft: hub -> arrowhead base
		fillA(g, -half, baseY, half, MeditationLayout.HUB_HALF, col);
		// arrowhead: rows from the base (full width) up to the tip (zero width)
		for (int i = 0; i < headLen; i++) {
			int hw = Math.round(headHalf * (1f - i / (float) headLen));
			int y = baseY - i;
			fillA(g, -hw, y - 1, hw + 1, y, col);
		}
		g.pose().popMatrix();
	}

	/** A tick mark: a bar between two radii at the given hour (rotated into place). */
	private void drawRadial(GuiGraphicsExtractor g, double hour, int rOuter, int rInner, int half, int col) {
		g.pose().pushMatrix();
		g.pose().translate(MeditationLayout.CX, MeditationLayout.CY);
		g.pose().rotate((float) hourAngle(hour));
		fillA(g, -half, -rOuter, half, -rInner, col);
		g.pose().popMatrix();
	}

	/** The four time-of-day orbs at their fixed hours (dawn/noon/dusk/night). */
	private void drawOrbs(GuiGraphicsExtractor g) {
		drawOrb(g, MeditationLayout.ORB_DAWN_HOUR, ORB_DAWN);
		drawOrb(g, MeditationLayout.ORB_NOON_HOUR, ORB_NOON);
		drawOrb(g, MeditationLayout.ORB_DUSK_HOUR, ORB_DUSK);
		drawOrb(g, MeditationLayout.ORB_NIGHT_HOUR, ORB_NIGHT);
	}

	/** Blit one ORB_SIZE orb centred on the ring at {@code hour}, faded with the dial. */
	private void drawOrb(GuiGraphicsExtractor g, int hour, Identifier tex) {
		double a = hourAngle(hour);
		float cx = MeditationLayout.CX + (float) (Math.sin(a) * MeditationLayout.ORB_RADIUS);
		float cy = MeditationLayout.CY - (float) (Math.cos(a) * MeditationLayout.ORB_RADIUS);
		float s = MeditationLayout.ORB_SIZE / (float) MeditationLayout.ORB_SRC;
		int tint = withAlpha(0xFFFFFFFF, contentAlpha); // white tint, alpha = dial fade
		g.pose().pushMatrix();
		g.pose().translate(cx - MeditationLayout.ORB_SIZE / 2f, cy - MeditationLayout.ORB_SIZE / 2f);
		g.pose().scale(s, s);
		g.blit(RenderPipelines.GUI_TEXTURED, tex, 0, 0, 0, 0, MeditationLayout.ORB_SRC, MeditationLayout.ORB_SRC, MeditationLayout.ORB_SRC, MeditationLayout.ORB_SRC, tint);
		g.pose().popMatrix();
	}

	/**
	 * A thin progress arc sweeping FORWARD from the current hour to the target hour
	 * (the span of world time the meditation will skip), plotted as small dots along
	 * ARC_RADIUS. A full-day pick (delta 0) shows the whole ring.
	 */
	private void drawArc(GuiGraphicsExtractor g, double nowHour, int target) {
		double delta = (((target - nowHour) % 24.0) + 24.0) % 24.0;
		if (spinning()) {
			// During the spin the live clock advances to the target and STOPS there, so
			// delta shrinks smoothly to ~0 - draw nothing once it arrives (also guards the
			// float boundary where now nudges just past target and delta wraps to ~24, the
			// old full-ring "pop"). Otherwise the arc stays live and shrinks as time passes.
			if (delta < 0.02 || delta > 23.98)
				return;
		} else if (delta < 1e-3) {
			delta = 24.0; // picking the current hour = advance a full day
		}
		// The arc is the 24-gon's edges from the current hour to the target: a partial
		// leading segment (now -> next whole hour) then one straight segment per hour.
		// As the current hand advances the leading segment shrinks then whole edges drop,
		// so the arc "eats" itself edge by edge instead of flickering pixels.
		double end = nowHour + delta;
		double h = nowHour;
		while (h < end - 1e-9) {
			double next = Math.min(Math.floor(h) + 1.0, end);
			drawArcSegment(g, h, next);
			h = next;
		}
	}

	/** One straight arc edge between two (fractional) hours, drawn as a rotated bar. */
	private void drawArcSegment(GuiGraphicsExtractor g, double h1, double h2) {
		double a1 = hourAngle(h1), a2 = hourAngle(h2);
		float x1 = MeditationLayout.CX + (float) (Math.sin(a1) * MeditationLayout.ARC_RADIUS);
		float y1 = MeditationLayout.CY - (float) (Math.cos(a1) * MeditationLayout.ARC_RADIUS);
		float x2 = MeditationLayout.CX + (float) (Math.sin(a2) * MeditationLayout.ARC_RADIUS);
		float y2 = MeditationLayout.CY - (float) (Math.cos(a2) * MeditationLayout.ARC_RADIUS);
		float len = (float) Math.hypot(x2 - x1, y2 - y1);
		int half = MeditationLayout.ARC_HALF;
		g.pose().pushMatrix();
		g.pose().translate(x1, y1);
		g.pose().rotate((float) Math.atan2(y2 - y1, x2 - x1));
		fillA(g, 0, -half, Math.round(len), half + 1, MeditationLayout.COL_ARC);
		g.pose().popMatrix();
	}

	private void centeredText(GuiGraphicsExtractor g, Component c, int centerX, int topY, int col) {
		textA(g, c, centerX - font.width(c) / 2, topY, col);
	}

	// ---- alpha-aware drawing primitives ---------------------------------------
	// Every draw in this page goes through these two, so contentAlpha (the
	// finishing fade-to-transparent) applies uniformly without touching each
	// call site's colour math.

	private void fillA(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int argb) {
		g.fill(x1, y1, x2, y2, withAlpha(argb, contentAlpha));
	}

	private void textA(GuiGraphicsExtractor g, Component c, int x, int y, int argb) {
		g.text(font, c, x, y, withAlpha(argb, contentAlpha), false);
	}

	private static int withAlpha(int argb, float mul) {
		int a = (argb >>> 24) & 0xFF;
		int newA = Math.round(a * Math.max(0f, Math.min(1f, mul)));
		return (newA << 24) | (argb & 0x00FFFFFF);
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
			if (spinning()) {
				if (cancellable())
					sendCancel();
			} else {
				onMeditate();
			}
			return true;
		}
		if (spinning())
			return true; // dial is locked while the world spins
		// dial: any click inside the ring picks the nearest hour
		double dx = lx - MeditationLayout.CX, dy = ly - MeditationLayout.CY;
		double dist = Math.sqrt(dx * dx + dy * dy);
		if (dist <= MeditationLayout.RADIUS * 1.25) {
			double a = Math.atan2(dx, -dy) - DIAL_OFFSET; // up = 0, clockwise; undo the day-up offset
			a = ((a % TAU) + TAU) % TAU;
			targetHour = ((int) Math.round(a / TAU * 24.0)) % 24;
			return true;
		}
		return false;
	}

	// Send the confirm to the server, which re-validates and starts the session.
	// buttonID = 1000 + targetHour (see MeditationGuiButtonMessage).
	private void onMeditate() {
		Player p = player();
		if (p == null || spinning())
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
		endSpin();
	}

	@Override
	public void onClose() {
		// closing mid-spin cancels it (time stops where it reached); a natural
		// finish already ran server-side, so that path just drops the overlay.
		if (cancellable())
			sendCancel();
		else
			endSpin();
	}
}
