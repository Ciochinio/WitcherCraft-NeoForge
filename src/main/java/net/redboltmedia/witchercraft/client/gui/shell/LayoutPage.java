package net.redboltmedia.witchercraft.client.gui.shell;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Generic, data-driven page: renders the {@link WitcherGuiLayout} boxes tagged
 * with its pageId. This is what makes the "lower section" interchangeable - the
 * whole page is defined by the tool-edited SECTIONS array, no code per tab.
 *
 * Placeholder rendering (fill + label) so it shows immediately with no art. A
 * Section with a non-empty texture blits it; otherwise a coloured box + caption
 * stands in. Swap in real widgets/slots later without touching the shell.
 */
public class LayoutPage implements GuiPage {

	// Placeholder palette (matches the tools' dark theme).
	private static final int BOX_BORDER = 0xFF4A4A52;
	private static final int BOX_INNER = 0xFF1B1B20;
	private static final int SLOT_BORDER = 0xFF8A6D3B;
	private static final int SLOT_INNER = 0xFF23201A;
	private static final int IMG_BORDER = 0xFF3A5A7A;
	private static final int IMG_INNER = 0xFF15202B;
	private static final int TEXT_DIM = 0xFF9A9AA2;
	private static final int LABEL = 0xFFCFC7B0;

	private final String pageId;

	public LayoutPage(String pageId) {
		this.pageId = pageId;
	}

	@Override
	public String id() {
		return pageId;
	}

	@Override
	public Component navLabel() {
		return Component.literal(pageId);
	}

	@Override
	public void render(GuiGraphicsExtractor g, int ox, int oy, int mouseX, int mouseY, float partial) {
		var font = Minecraft.getInstance().font;
		List<WitcherGuiLayout.Section> sections = WitcherGuiLayout.sectionsFor(pageId);
		if (sections.isEmpty()) {
			g.text(font, Component.translatable("gui.witchercraft.shell.empty_page"), ox + 12, oy + 12, TEXT_DIM, false);
			return;
		}
		for (WitcherGuiLayout.Section s : sections) {
			int x = ox + s.x, y = oy + s.y;
			if (s.texture != null && !s.texture.isEmpty()) {
				g.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, Identifier.parse(s.texture), x, y, 0, 0, s.w, s.h, s.w, s.h);
				continue;
			}
			switch (s.type) {
				case WitcherGuiLayout.TYPE_SLOT:
					box(g, x, y, s.w, s.h, SLOT_BORDER, SLOT_INNER);
					centeredCaption(g, font, s.text, x, y, s.w, s.h, LABEL);
					break;
				case WitcherGuiLayout.TYPE_IMAGE:
					box(g, x, y, s.w, s.h, IMG_BORDER, IMG_INNER);
					centeredCaption(g, font, s.text, x, y, s.w, s.h, TEXT_DIM);
					break;
				case WitcherGuiLayout.TYPE_TEXT:
				default:
					g.text(font, resolve(s.text), x, y, LABEL, false);
					break;
			}
		}
	}

	private static void box(GuiGraphicsExtractor g, int x, int y, int w, int h, int border, int inner) {
		g.fill(x, y, x + w, y + h, border);
		g.fill(x + 1, y + 1, x + w - 1, y + h - 1, inner);
	}

	private static void centeredCaption(GuiGraphicsExtractor g, net.minecraft.client.gui.Font font, String text, int x, int y, int w, int h, int color) {
		Component c = resolve(text);
		int tw = font.width(c);
		g.text(font, c, x + (w - tw) / 2, y + h / 2 - 4, color, false);
	}

	/** A dotted string is treated as a lang key; anything else as a literal. */
	private static Component resolve(String text) {
		if (text == null || text.isEmpty())
			return Component.empty();
		return text.indexOf('.') >= 0 ? Component.translatable(text) : Component.literal(text);
	}
}
