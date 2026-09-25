package net.redboltmedia.witchercraft;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import org.lwjgl.glfw.GLFW;

/**
 * HAND-MAINTAINED (locked code element, ~/World Map/Fast Travel). Popup shown after placing a
 * signpost. It uses the waypoint creation panel's look ({@link MapLayout} colors and the shared
 * {@link MapPage} button drawing). An empty field, Cancel, or Escape keeps the default name.
 * Closing the screen in any way always answers the server, which ends the naming session and makes
 * the sign discoverable.
 */
public class FastTravelSignNameScreen extends Screen {
	private static final int PANEL_W = 250;
	private static final int PANEL_H = 112;
	private static final int PADDING = 14;
	private static final int NAME_Y = 34;
	private static final int NAME_H = 18;
	private static final int HINT_Y = 60;
	private static final int ACTION_Y = 82;
	private static final int ACTION_W = 68;

	private final String defaultName;
	private String name = "";
	private boolean answered;

	private FastTravelSignNameScreen(String defaultName) {
		super(Component.translatableWithFallback("gui.witchercraft.fast_travel.sign_name.title", "Name this signpost"));
		this.defaultName = defaultName;
	}

	public static void open(String defaultName) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player != null)
			minecraft.setScreen(new FastTravelSignNameScreen(defaultName));
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
		g.fill(0, 0, width, height, MapLayout.OVERLAY_DIM);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
		super.extractRenderState(g, mouseX, mouseY, partial);
		Font font = this.font;
		int ox = panelX(), oy = panelY();
		g.fill(ox, oy, ox + PANEL_W, oy + PANEL_H, MapLayout.OVERLAY_BG);
		MapPage.drawBorder(g, ox, oy, PANEL_W, PANEL_H, MapLayout.OVERLAY_BORDER);
		g.text(font, getTitle(), ox + PADDING, oy + 9, MapLayout.TEXT, false);

		int fieldX = ox + PADDING;
		int fieldW = PANEL_W - PADDING * 2;
		g.text(font, Component.translatableWithFallback("gui.witchercraft.fast_travel.sign_name.name", "Name"), fieldX, oy + 23, MapLayout.TEXT_DIM, false);
		g.fill(fieldX, oy + NAME_Y, fieldX + fieldW, oy + NAME_Y + NAME_H, MapLayout.FIELD_BG);
		MapPage.drawBorder(g, fieldX, oy + NAME_Y, fieldW, NAME_H, MapLayout.VIEW_BORDER);
		boolean placeholder = name.isEmpty();
		String shown = placeholder ? defaultName : name;
		if ((System.currentTimeMillis() / 500L & 1L) == 0L && !placeholder)
			shown += "_";
		while (!shown.isEmpty() && font.width(shown) > fieldW - 10)
			shown = shown.substring(shown.offsetByCodePoints(0, 1));
		g.text(font, Component.literal(shown), fieldX + 5, oy + NAME_Y + 6, placeholder ? MapLayout.TEXT_DIM : MapLayout.TEXT, false);
		g.text(font, Component.translatableWithFallback("gui.witchercraft.fast_travel.sign_name.hint", "Leave empty to keep the default name."),
			fieldX, oy + HINT_Y, MapLayout.TEXT_DIM, false);

		MapPage.drawButton(g, font, fieldX, oy + ACTION_Y, ACTION_W, Component.translatableWithFallback("gui.cancel", "Cancel"), mouseX, mouseY, true);
		MapPage.drawButton(g, font, saveButtonX(), oy + ACTION_Y, ACTION_W,
			Component.translatableWithFallback("gui.witchercraft.fast_travel.sign_name.save", "Save"), mouseX, mouseY, true);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() != 0)
			return true;
		int oy = panelY();
		if (MapPage.inside(event.x(), event.y(), panelX() + PADDING, oy + ACTION_Y, ACTION_W, MapLayout.BUTTON_H)) {
			onClose();
			return true;
		}
		if (MapPage.inside(event.x(), event.y(), saveButtonX(), oy + ACTION_Y, ACTION_W, MapLayout.BUTTON_H)) {
			submit();
			return true;
		}
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		int key = InputConstants.getKey(event).getValue();
		if (key == GLFW.GLFW_KEY_ESCAPE) {
			onClose();
			return true;
		}
		if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
			submit();
			return true;
		}
		if (key == GLFW.GLFW_KEY_BACKSPACE && !name.isEmpty()) {
			name = name.substring(0, name.offsetByCodePoints(name.length(), -1));
			return true;
		}
		return true;
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		int codepoint = event.codepoint();
		if (!Character.isValidCodePoint(codepoint) || Character.isISOControl(codepoint))
			return true;
		if (name.codePointCount(0, name.length()) < FastTravelSigns.MAX_NAME_CHARACTERS)
			name += Character.toString(codepoint);
		return true;
	}

	private void submit() {
		answer(name.strip());
		onClose();
	}

	/** An empty name tells the server to keep the default. */
	private void answer(String chosen) {
		if (answered)
			return;
		answered = true;
		if (Minecraft.getInstance().getConnection() != null)
			ClientPacketDistributor.sendToServer(new FastTravelSignNameMessage(chosen));
	}

	@Override
	public void removed() {
		answer("");
		super.removed();
	}

	private int panelX() {
		return (width - PANEL_W) / 2;
	}

	private int panelY() {
		return (height - PANEL_H) / 2;
	}

	private int saveButtonX() {
		return panelX() + PANEL_W - PADDING - ACTION_W;
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(null);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
