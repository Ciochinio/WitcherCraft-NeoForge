package net.redboltmedia.witchercraft.client.gui.shell;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * A not-yet-built tab: draws a centred "&lt;Name&gt; - coming soon" in the content
 * region. Used for every navbar page that has no bespoke {@link GuiPage} yet.
 * When a real page is built (its own class + its own placer tool, like Skills),
 * register it in {@link WitcherGuiPages} and this stops being used for that id.
 */
public class PlaceholderPage implements GuiPage {

	private static final int TITLE = 0xFFCFC7B0;
	private static final int SUB = 0xFF7A7A84;

	private final String pageId;

	public PlaceholderPage(String pageId) {
		this.pageId = pageId;
	}

	@Override
	public String id() {
		return pageId;
	}

	@Override
	public Component navLabel() {
		return Component.literal(cap(pageId));
	}

	@Override
	public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, int mouseX, int mouseY, float partial) {
		Font font = Minecraft.getInstance().font;
		Component title = Component.literal(cap(pageId));
		Component sub = Component.translatable("gui.witchercraft.shell.coming_soon");
		int cx = x + w / 2, cy = y + h / 2;
		g.text(font, title, cx - font.width(title) / 2, cy - 8, TITLE, false);
		g.text(font, sub, cx - font.width(sub) / 2, cy + 4, SUB, false);
	}

	private static String cap(String s) {
		if (s == null || s.isEmpty())
			return "";
		return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
	}
}
