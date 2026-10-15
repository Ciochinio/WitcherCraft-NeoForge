package net.redboltmedia.witchercraft;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import net.redboltmedia.witchercraft.mixin.LivingEntityRendererAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;

/**
 * The Glossary tab, drawn as an open book (the old bestiary's {@code book.png}) with three
 * sets of bookmark ribbons. Nothing scrolls and nothing needs page turning to navigate:
 *
 * - right-edge ribbons switch between Bestiary and Tutorials;
 * - top ribbons pick a monster category ({@link GlossaryBestiary}, one per category tag,
 *   with its oil as the icon) or a Tutorials group;
 * - left ribbons pick a creature (spawn egg + name) or a topic, tinted like the top ribbon;
 * - Bestiary: the left page shows the doll (fitted to its box, drag to rotate), the oils it
 *   is weak to and its stats; the right page its name and description.
 * - Tutorials: the title, the topic's icon and its text run from the left page onto the
 *   right one; a topic with its own picture shows it on the left page instead.
 *
 * Text too long for its pages gets page arrows in the right page's footer; a category with
 * more creatures than fit gets arrows under the left ribbons. Geometry, art and colours are
 * {@link GlossaryLayout} (tools/glossary-layout-creator.html). Pure client state; the
 * selection is remembered for the session.
 */
public class GlossaryPage implements GuiPage {

	private enum SubTab {
		BESTIARY("bestiary"), TUTORIALS("tutorials");

		private final String key;

		SubTab(String key) {
			this.key = key;
		}

		Component title() {
			return Component.translatable("glossary.witchercraft.tab." + key);
		}
	}

	/** One bookmark: its label (tooltip, or the text on a left ribbon), its icon and its colour. */
	private record Ribbon(Component label, ItemStack item, String texture, int texW, int texH, int color) {
	}

	/** A page footer: prev button, "page / pages", next button. */
	private record Pager(int prevX, int nextX, int y, int labelX, Component label, int page, int pages) {
	}

	/**
	 * A posed model's extents in blocks, feet at y 0: its lowest and highest point, and the
	 * largest distance of any point from the vertical axis the doll turns around (so the
	 * fitted doll stays inside its box at every angle).
	 */
	private record DollFit(float minY, float maxY, float radius) {
	}

	// Where book.png's cover edges are, as fractions of the texture (638x445): ribbons are
	// only clickable where they show from under the cover.
	private static final float COVER_LEFT = 22f / 638f, COVER_RIGHT = 605f / 638f, COVER_TOP = 25f / 445f;

	private static final DecimalFormat NUMBER = new DecimalFormat("0.##");
	private static final int DOLL_DRAG_DEGREES_PER_PX = 2;
	private static final Identifier HEART_SPRITE = Identifier.withDefaultNamespace("hud/heart/full");
	private static final Identifier ARMOR_SPRITE = Identifier.withDefaultNamespace("hud/armor_full");
	private static final Identifier ATTACK_ICON = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "textures/screens/attack_icon.png");

	// --- session state (static: survives closing and reopening the menu) -----
	private static SubTab subTab = SubTab.BESTIARY;
	private static GlossaryBestiary.Category category;
	private static EntityType<?> selectedType;
	private static String groupId;
	private static String topicId;
	private static int sidePage;
	private static int textPage;
	private static float dollYaw = GlossaryLayout.DOLL_YAW;

	private final String pageId;
	private boolean draggingDoll;
	// page count of the text drawn last frame, so clicks and the wheel know the bounds
	private int lastTextPages = 1;
	private List<Component> pendingTooltip;
	private final Map<Identifier, Boolean> imageExists = new HashMap<>();
	private final Set<EntityType<?>> brokenDolls = new HashSet<>();
	private final Map<EntityType<?>, DollFit> dollFits = new HashMap<>();

	public GlossaryPage(String pageId) {
		this.pageId = pageId;
	}

	@Override
	public String id() {
		return pageId;
	}

	@Override
	public Component navLabel() {
		return Component.translatable("gui.witchercraft.shell.nav.glossary");
	}

	@Override
	public void onShown() {
		GlossaryBestiary.refresh();
		imageExists.clear();
		dollFits.clear();
		draggingDoll = false;
	}

	@Override
	public void onHidden() {
		draggingDoll = false;
	}

	@Override
	public List<Component> pollTooltip() {
		List<Component> tip = pendingTooltip;
		pendingTooltip = null;
		return tip;
	}

	// ==========================================================================
	// what the ribbons hold
	// ==========================================================================

	private static List<Ribbon> topRibbons() {
		List<Ribbon> out = new ArrayList<>();
		if (subTab == SubTab.BESTIARY) {
			for (GlossaryBestiary.Category c : GlossaryBestiary.Category.values())
				out.add(new Ribbon(c.title(), c.oil(), null, 0, 0, color(GlossaryLayout.CATEGORY_COLORS, c.ordinal())));
		} else {
			for (int i = 0; i < GlossaryTutorials.GROUPS.size(); i++) {
				GlossaryTutorials.Group group = GlossaryTutorials.GROUPS.get(i);
				GlossaryTutorials.Topic icon = group.topics().get(0);
				out.add(new Ribbon(group.title(), icon.itemIcon(), icon.texture(), icon.textureW(), icon.textureH(), color(GlossaryLayout.GROUP_COLORS, i)));
			}
		}
		return out;
	}

	private static int selectedTop() {
		return subTab == SubTab.BESTIARY ? currentCategory().ordinal() : GlossaryTutorials.GROUPS.indexOf(selectedGroup());
	}

	private static List<Ribbon> sideRibbons() {
		List<Ribbon> out = new ArrayList<>();
		int color = topRibbons().get(selectedTop()).color();
		if (subTab == SubTab.BESTIARY) {
			for (GlossaryBestiary.Entry e : GlossaryBestiary.entries(currentCategory()))
				out.add(new Ribbon(e.name(), e.icon(), null, 0, 0, color));
		} else {
			for (GlossaryTutorials.Topic t : selectedGroup().topics())
				out.add(new Ribbon(t.title(), t.itemIcon(), t.texture(), t.textureW(), t.textureH(), color));
		}
		return out;
	}

	/** Index of the selected left ribbon in the full list, or -1. */
	private static int selectedSide() {
		if (subTab == SubTab.BESTIARY) {
			GlossaryBestiary.Entry entry = selectedEntry();
			return entry == null ? -1 : GlossaryBestiary.entries(currentCategory()).indexOf(entry);
		}
		GlossaryTutorials.Topic topic = selectedTopic();
		return topic == null ? -1 : selectedGroup().topics().indexOf(topic);
	}

	private static int color(int[] colors, int i) {
		return colors.length == 0 ? 0xFF808080 : colors[Math.floorMod(i, colors.length)];
	}

	// ==========================================================================
	// rendering
	// ==========================================================================

	@Override
	public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, int mouseX, int mouseY, float partial) {
		Font font = Minecraft.getInstance().font;
		pendingTooltip = null;

		// ribbons first: their inner ends tuck under the book's cover
		drawTabRibbons(g, font, x, y, mouseX, mouseY);
		drawTopRibbons(g, x, y, mouseX, mouseY);
		drawSideRibbons(g, font, x, y, mouseX, mouseY);
		blit(g, Identifier.parse(GlossaryLayout.BOOK_TEX), x + GlossaryLayout.BOOK_X, y + GlossaryLayout.BOOK_Y, GlossaryLayout.BOOK_W, GlossaryLayout.BOOK_H, -1);

		if (subTab == SubTab.BESTIARY)
			renderBestiary(g, font, x, y, mouseX, mouseY);
		else
			renderTutorials(g, font, x, y, mouseX, mouseY);
	}

	// ---- ribbons -----------------------------------------------------------------

	private static int coverLeft(int x) {
		return x + GlossaryLayout.BOOK_X + Math.round(GlossaryLayout.BOOK_W * COVER_LEFT);
	}

	private static int coverRight(int x) {
		return x + GlossaryLayout.BOOK_X + Math.round(GlossaryLayout.BOOK_W * COVER_RIGHT);
	}

	private static int coverTop(int y) {
		return y + GlossaryLayout.BOOK_Y + Math.round(GlossaryLayout.BOOK_H * COVER_TOP);
	}

	private static int tabRibbonX(int x, boolean selected) {
		return x + GlossaryLayout.RIGHT_X - (selected ? 0 : GlossaryLayout.SIDE_SHIFT);
	}

	private static int tabRibbonY(int y, int i) {
		return y + GlossaryLayout.RIGHT_Y + i * GlossaryLayout.RIGHT_STEP;
	}

	private static int topRibbonX(int x, int i) {
		return x + GlossaryLayout.TOP_X + i * GlossaryLayout.TOP_STEP;
	}

	private static int topRibbonY(int y, boolean selected) {
		return y + GlossaryLayout.TOP_Y + (selected ? 0 : GlossaryLayout.TOP_LIFT);
	}

	private static int sideRibbonX(int x, boolean selected) {
		return x + GlossaryLayout.SIDE_X + (selected ? 0 : GlossaryLayout.SIDE_SHIFT);
	}

	private static int sideRibbonY(int y, int row) {
		return y + GlossaryLayout.SIDE_Y + row * GlossaryLayout.SIDE_STEP;
	}

	private static int sidePages(int count) {
		return Math.max(1, (count + GlossaryLayout.SIDE_ROWS - 1) / GlossaryLayout.SIDE_ROWS);
	}

	/** A ribbon texture tinted: full colour when selected, a little lighter on hover, darker otherwise. */
	private static void ribbon(GuiGraphicsExtractor g, String texture, int x, int y, int w, int h, int color, boolean selected, boolean hover) {
		int tint = selected ? color : shade(color, hover ? (GlossaryLayout.RIBBON_IDLE_PERCENT + 100) / 2 : GlossaryLayout.RIBBON_IDLE_PERCENT);
		blit(g, Identifier.parse(texture), x, y, w, h, tint);
	}

	private void drawTabRibbons(GuiGraphicsExtractor g, Font font, int x, int y, int mouseX, int mouseY) {
		for (SubTab tab : SubTab.values()) {
			boolean selected = tab == subTab;
			int rx = tabRibbonX(x, selected), ry = tabRibbonY(y, tab.ordinal());
			boolean hover = MapPage.inside(mouseX, mouseY, coverRight(x), ry, rx + GlossaryLayout.RIGHT_W - coverRight(x), GlossaryLayout.RIGHT_H);
			ribbon(g, GlossaryLayout.RIBBON_RIGHT_TEX, rx, ry, GlossaryLayout.RIGHT_W, GlossaryLayout.RIGHT_H, color(GlossaryLayout.TAB_COLORS, tab.ordinal()), selected, hover);
			Component label = tab.title();
			g.text(font, label, rx + GlossaryLayout.RIGHT_W - GlossaryLayout.RIGHT_TEXT_INSET - font.width(label), ry + (GlossaryLayout.RIGHT_H - 8) / 2,
					GlossaryLayout.RIBBON_TEXT, true);
		}
	}

	private void drawTopRibbons(GuiGraphicsExtractor g, int x, int y, int mouseX, int mouseY) {
		List<Ribbon> ribbons = topRibbons();
		int selected = selectedTop();
		for (int i = 0; i < ribbons.size(); i++) {
			Ribbon r = ribbons.get(i);
			boolean isSelected = i == selected;
			int rx = topRibbonX(x, i), ry = topRibbonY(y, isSelected);
			boolean hover = MapPage.inside(mouseX, mouseY, rx, ry, GlossaryLayout.TOP_W, coverTop(y) - ry);
			ribbon(g, GlossaryLayout.RIBBON_TOP_TEX, rx, ry, GlossaryLayout.TOP_W, GlossaryLayout.TOP_H, r.color(), isSelected, hover);
			drawIcon(g, r, rx + (GlossaryLayout.TOP_W - 16) / 2, ry + GlossaryLayout.TOP_ICON_Y);
			if (hover)
				pendingTooltip = List.of(r.label());
		}
	}

	private void drawSideRibbons(GuiGraphicsExtractor g, Font font, int x, int y, int mouseX, int mouseY) {
		List<Ribbon> ribbons = sideRibbons();
		int selected = selectedSide();
		int pages = sidePages(ribbons.size());
		sidePage = clamp(sidePage, pages);
		int first = sidePage * GlossaryLayout.SIDE_ROWS;
		for (int i = first; i < ribbons.size() && i < first + GlossaryLayout.SIDE_ROWS; i++) {
			Ribbon r = ribbons.get(i);
			boolean isSelected = i == selected;
			int rx = sideRibbonX(x, isSelected), ry = sideRibbonY(y, i - first);
			boolean hover = MapPage.inside(mouseX, mouseY, rx, ry, coverLeft(x) - rx, GlossaryLayout.SIDE_H);
			ribbon(g, GlossaryLayout.RIBBON_LEFT_TEX, rx, ry, GlossaryLayout.SIDE_W, GlossaryLayout.SIDE_H, r.color(), isSelected, hover);
			drawIcon(g, r, rx + GlossaryLayout.SIDE_ICON_X, ry + (GlossaryLayout.SIDE_H - 16) / 2);
			String name = r.label().getString();
			String shown = fit(font, name, GlossaryLayout.SIDE_TEXT_W);
			g.text(font, Component.literal(shown), rx + GlossaryLayout.SIDE_TEXT_X, ry + (GlossaryLayout.SIDE_H - 8) / 2, GlossaryLayout.RIBBON_TEXT, true);
			if (hover && !shown.equals(name))
				pendingTooltip = List.of(r.label());
		}
		if (ribbons.isEmpty())
			g.text(font, Component.translatable("glossary.witchercraft.empty_category"), x + GlossaryLayout.SIDE_X + GlossaryLayout.SIDE_SHIFT, sideRibbonY(y, 0) + 6,
					GlossaryLayout.RIBBON_TEXT, true);
		if (pages > 1)
			drawPager(g, font, sidePager(font, x, y, pages), mouseX, mouseY, false);
	}

	private Pager sidePager(Font font, int x, int y, int pages) {
		int left = x + GlossaryLayout.SIDE_X + GlossaryLayout.SIDE_SHIFT;
		int fy = sideRibbonY(y, GlossaryLayout.SIDE_ROWS);
		return pager(font, left, fy, sidePage, pages);
	}

	private static void drawIcon(GuiGraphicsExtractor g, Ribbon r, int ix, int iy) {
		if (!r.item().isEmpty()) {
			g.fakeItem(r.item(), ix, iy);
		} else if (r.texture() != null) {
			float fit = 16f / Math.max(r.texW(), r.texH());
			int tw = Math.round(r.texW() * fit), th = Math.round(r.texH() * fit);
			blit(g, Identifier.parse(r.texture()), ix + (16 - tw) / 2, iy + (16 - th) / 2, tw, th, -1);
		}
	}

	// ---- Bestiary pages ------------------------------------------------------------

	private void renderBestiary(GuiGraphicsExtractor g, Font font, int x, int y, int mouseX, int mouseY) {
		GlossaryBestiary.Entry entry = selectedEntry();
		if (entry == null) {
			drawTitle(g, font, currentCategory().title(), x + GlossaryLayout.RIGHT_PAGE_X, y + GlossaryLayout.RIGHT_PAGE_Y, GlossaryLayout.RIGHT_PAGE_W);
			g.text(font, Component.translatable("glossary.witchercraft.empty_category"), x + GlossaryLayout.RIGHT_PAGE_X, y + GlossaryLayout.RIGHT_PAGE_Y + GlossaryLayout.TITLE_H,
					GlossaryLayout.INK_DIM, false);
			lastTextPages = 1;
			return;
		}

		// left page: doll, weaknesses, stats
		int dx = x + GlossaryLayout.DOLL_X, dy = y + GlossaryLayout.DOLL_Y, dw = GlossaryLayout.DOLL_W, dh = GlossaryLayout.DOLL_H;
		LivingEntity doll = brokenDolls.contains(entry.type()) ? null : GlossaryBestiary.doll(entry.type());
		if (doll != null) {
			drawDoll(g, doll, entry.type(), dx, dy, dw, dh);
		} else {
			// not a living creature (or its renderer failed): its spawn egg, large
			ItemStack icon = entry.icon();
			if (!icon.isEmpty())
				drawScaledItem(g, icon, dx + dw / 2, dy + dh / 2, GlossaryLayout.TOPIC_ICON_SCALE);
		}
		if (MapPage.inside(mouseX, mouseY, dx, dy, dw, dh) || draggingDoll) {
			Component hint = Component.translatable("glossary.witchercraft.drag_hint");
			g.text(font, hint, dx + (dw - font.width(hint)) / 2, dy + dh - 9, GlossaryLayout.INK_DIM, false);
		}
		drawWeaknesses(g, font, x, y, entry, mouseX, mouseY);
		if (doll != null)
			drawStats(g, font, x, y, doll, mouseX, mouseY);

		// right page: name and description
		int px = x + GlossaryLayout.RIGHT_PAGE_X, py = y + GlossaryLayout.RIGHT_PAGE_Y;
		drawTitle(g, font, entry.name(), px, py, GlossaryLayout.RIGHT_PAGE_W);
		String key = entry.descriptionKey();
		Component description = I18n.exists(key) ? Component.translatable(key) : Component.translatable("glossary.witchercraft.no_notes").withColor(GlossaryLayout.INK_DIM);
		List<FormattedCharSequence> lines = font.split(description, GlossaryLayout.RIGHT_PAGE_W);
		int perPage = rightPageLines(GlossaryLayout.TITLE_H);
		int pages = pageCount(lines.size(), perPage);
		textPage = clamp(textPage, pages);
		drawLines(g, font, lines, textPage * perPage, perPage, px, py + GlossaryLayout.TITLE_H);
		drawTextPager(g, font, x, y, pages, mouseX, mouseY);
	}

	private void drawWeaknesses(GuiGraphicsExtractor g, Font font, int x, int y, GlossaryBestiary.Entry entry, int mouseX, int mouseY) {
		List<Ribbon> weak = weaknesses(entry);
		if (weak.isEmpty())
			return;
		int cx = x + GlossaryLayout.LEFT_PAGE_X + GlossaryLayout.LEFT_PAGE_W / 2, wy = y + GlossaryLayout.WEAK_Y;
		Component label = Component.translatable("glossary.witchercraft.weak_to");
		g.text(font, label, cx - font.width(label) / 2, wy, GlossaryLayout.INK_TITLE, false);
		// one row, centred; cells shrink (down to 17) when more than fit the page
		int n = weak.size(), size = GlossaryLayout.WEAK_CELL, gap = GlossaryLayout.WEAK_GAP;
		if (n * size + (n - 1) * gap > GlossaryLayout.LEFT_PAGE_W) {
			gap = 2;
			size = Math.max(17, (GlossaryLayout.LEFT_PAGE_W - (n - 1) * gap) / n);
		}
		int total = n * size + (n - 1) * gap;
		for (int i = 0; i < n; i++) {
			int ox = cx - total / 2 + i * (size + gap), oy = wy + 11;
			boolean hover = MapPage.inside(mouseX, mouseY, ox, oy, size, size);
			g.fill(ox, oy, ox + size, oy + size, GlossaryLayout.CELL_INNER);
			MapPage.drawBorder(g, ox, oy, size, size, hover ? GlossaryLayout.INK_TITLE : GlossaryLayout.CELL_BORDER);
			drawIcon(g, weak.get(i), ox + (size - 16) / 2, oy + (size - 16) / 2);
			if (hover)
				pendingTooltip = List.of(weak.get(i).label());
		}
	}

	/** Health, damage and armor read live from the doll: a 9x9 icon and the number each, centred on the left page. */
	private void drawStats(GuiGraphicsExtractor g, Font font, int x, int y, LivingEntity doll, int mouseX, int mouseY) {
		record Stat(String key, Holder<Attribute> attribute, int icon) {
		}
		List<Stat> stats = List.of(new Stat("health", Attributes.MAX_HEALTH, 0), new Stat("damage", Attributes.ATTACK_DAMAGE, 1), new Stat("armor", Attributes.ARMOR, 2));
		List<Stat> shown = new ArrayList<>();
		List<String> values = new ArrayList<>();
		int total = 0;
		for (Stat s : stats) {
			double value = GlossaryBestiary.stat(doll, s.attribute());
			if (value < 0)
				continue;
			String text = NUMBER.format(value);
			shown.add(s);
			values.add(text);
			total += 9 + 2 + font.width(text);
		}
		if (shown.isEmpty())
			return;
		total += (shown.size() - 1) * GlossaryLayout.STATS_GAP;
		int sx = x + GlossaryLayout.LEFT_PAGE_X + (GlossaryLayout.LEFT_PAGE_W - total) / 2, sy = y + GlossaryLayout.STATS_Y;
		for (int i = 0; i < shown.size(); i++) {
			Stat s = shown.get(i);
			switch (s.icon()) {
				case 0 -> g.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_SPRITE, sx, sy, 9, 9);
				case 1 -> blit(g, ATTACK_ICON, sx, sy, 9, 9, -1);
				default -> g.blitSprite(RenderPipelines.GUI_TEXTURED, ARMOR_SPRITE, sx, sy, 9, 9);
			}
			int w = 9 + 2 + font.width(values.get(i));
			g.text(font, values.get(i), sx + 11, sy + 1, GlossaryLayout.INK, false);
			if (MapPage.inside(mouseX, mouseY, sx, sy - 1, w, 11))
				pendingTooltip = List.of(Component.translatable("glossary.witchercraft.stat." + s.key()));
			sx += w + GlossaryLayout.STATS_GAP;
		}
	}

	/**
	 * The creature's 3D model, scaled so its whole posed model (not just its hitbox, which a
	 * horse's head or a spider's legs reach well past) fits DOLL_FILL_PERCENT of the box at
	 * any angle, and centred in it. Entities are drawn as a picture-in-picture in SCREEN
	 * pixels, so the box is mapped through the shell's design->screen transform first.
	 */
	private void drawDoll(GuiGraphicsExtractor g, LivingEntity doll, EntityType<?> type, int bx, int by, int bw, int bh) {
		Vector2f a = g.pose().transformPosition(bx, by, new Vector2f());
		Vector2f b = g.pose().transformPosition(bx + bw, by + bh, new Vector2f());
		int x0 = Math.round(a.x), y0 = Math.round(a.y), x1 = Math.round(b.x), y1 = Math.round(b.y);
		try {
			EntityRenderer<? super LivingEntity, ?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(doll);
			EntityRenderState state = renderer.createRenderState(doll, 1.0F);
			state.shadowPieces.clear();
			state.outlineColor = 0;
			if (state instanceof LivingEntityRenderState living) {
				living.bodyRot = 180.0F + dollYaw;
				living.yRot = 0.0F;
				living.xRot = 0.0F;
				living.boundingBoxWidth = living.boundingBoxWidth / living.scale;
				living.boundingBoxHeight = living.boundingBoxHeight / living.scale;
				living.scale = 1.0F;
			}
			DollFit fit = dollFits.computeIfAbsent(type, t -> measure(renderer, state, doll));
			float fill = GlossaryLayout.DOLL_FILL_PERCENT / 100f;
			float height = Math.max(fit.maxY() - fit.minY(), 0.1f), width = Math.max(2 * fit.radius(), 0.1f);
			float size = fill * Math.min((y1 - y0) / height, (x1 - x0) / width);
			Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
			Vector3f translation = new Vector3f(0.0F, (fit.minY() + fit.maxY()) / 2.0F, 0.0F);
			g.entity(state, size, translation, rotation, new Quaternionf(), x0, y0, x1, y1);
		} catch (RuntimeException e) {
			brokenDolls.add(type);
			WitchercraftMod.LOGGER.warn("Glossary could not draw the doll of {}", type, e);
		}
	}

	/**
	 * Measure the posed model the way {@code LivingEntityRenderer} draws it (flip, the renderer's
	 * own scale, the 1.501 lift), through vanilla's {@code getExtentsForGui}. Layers (saddles,
	 * armour, held items) are not counted. Falls back to the hitbox for non-model renderers.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static DollFit measure(EntityRenderer<?, ?> renderer, EntityRenderState state, LivingEntity doll) {
		if (renderer instanceof LivingEntityRenderer living && state instanceof LivingEntityRenderState livingState) {
			try {
				EntityModel model = living.getModel();
				model.setupAnim(livingState);
				PoseStack pose = new PoseStack();
				pose.scale(-1.0F, -1.0F, 1.0F);
				((LivingEntityRendererAccessor) living).witchercraft$scale(livingState, pose);
				pose.translate(0.0F, -1.501F, 0.0F);
				float[] e = {Float.MAX_VALUE, -Float.MAX_VALUE, 0.0F};
				model.root().getExtentsForGui(pose, p -> {
					e[0] = Math.min(e[0], p.y());
					e[1] = Math.max(e[1], p.y());
					e[2] = Math.max(e[2], (float) Math.sqrt(p.x() * p.x() + p.z() * p.z()));
				});
				if (e[1] > e[0])
					return new DollFit(e[0], e[1], e[2]);
			} catch (RuntimeException ex) {
				WitchercraftMod.LOGGER.warn("Glossary could not measure the model of {}", doll.getType(), ex);
			}
		}
		return new DollFit(0.0F, doll.getBbHeight(), doll.getBbWidth() * 0.71F);
	}

	/**
	 * What the creature is weak to: the oil of every category it is tagged into, then its extra
	 * weaknesses (Signs, bombs) from the {@code witchercraft:weak_to_<id>} tags.
	 */
	private static List<Ribbon> weaknesses(GlossaryBestiary.Entry entry) {
		List<Ribbon> out = new ArrayList<>();
		for (GlossaryBestiary.Category c : GlossaryBestiary.Category.values()) {
			if (GlossaryBestiary.entries(c).contains(entry)) {
				ItemStack oil = c.oil();
				out.add(new Ribbon(oil.getHoverName(), oil, null, 0, 0, 0));
			}
		}
		for (GlossaryBestiary.Weakness w : GlossaryBestiary.weaknessesOf(entry.type()))
			out.add(new Ribbon(w.name(), w.itemIcon(), w.texture(), 64, 64, 0));
		return out;
	}

	// ---- Tutorials pages -------------------------------------------------------------

	private void renderTutorials(GuiGraphicsExtractor g, Font font, int x, int y, int mouseX, int mouseY) {
		GlossaryTutorials.Topic topic = selectedTopic();
		if (topic == null) {
			lastTextPages = 1;
			return;
		}
		int lx = x + GlossaryLayout.LEFT_PAGE_X, ly = y + GlossaryLayout.LEFT_PAGE_Y, lw = GlossaryLayout.LEFT_PAGE_W;
		int rx = x + GlossaryLayout.RIGHT_PAGE_X, ry = y + GlossaryLayout.RIGHT_PAGE_Y;
		List<FormattedCharSequence> lines = font.split(topic.text(), GlossaryLayout.RIGHT_PAGE_W);
		int rightLines = rightPageLines(0);

		Identifier image = topic.image();
		if (imageExists.computeIfAbsent(image, id -> Minecraft.getInstance().getResourceManager().getResource(id).isPresent())) {
			// the topic's own picture fills the left page; title and text on the right
			blit(g, image, lx, ly, lw, GlossaryLayout.LEFT_PAGE_H, -1);
			drawTitle(g, font, topic.title(), rx, ry, GlossaryLayout.RIGHT_PAGE_W);
			int perPage = rightPageLines(GlossaryLayout.TITLE_H);
			int pages = pageCount(lines.size(), perPage);
			textPage = clamp(textPage, pages);
			drawLines(g, font, lines, textPage * perPage, perPage, rx, ry + GlossaryLayout.TITLE_H);
			drawTextPager(g, font, x, y, pages, mouseX, mouseY);
			return;
		}

		// title, icon and the first lines on the left page, the rest running on to the right
		drawTitle(g, font, topic.title(), lx, ly, lw);
		int iconSize = 16 * GlossaryLayout.TOPIC_ICON_SCALE;
		int iconY = ly + GlossaryLayout.TITLE_H;
		if (!topic.itemIcon().isEmpty()) {
			drawScaledItem(g, topic.itemIcon(), lx + lw / 2, iconY + iconSize / 2, GlossaryLayout.TOPIC_ICON_SCALE);
		} else if (topic.texture() != null) {
			float fit = (float) iconSize / Math.max(topic.textureW(), topic.textureH());
			int tw = Math.round(topic.textureW() * fit), th = Math.round(topic.textureH() * fit);
			blit(g, Identifier.parse(topic.texture()), lx + (lw - tw) / 2, iconY + (iconSize - th) / 2, tw, th, -1);
		}
		int leftTop = GlossaryLayout.TITLE_H + iconSize + GlossaryLayout.TOPIC_ICON_GAP;
		int leftLines = Math.max(0, (GlossaryLayout.LEFT_PAGE_H - leftTop) / GlossaryLayout.LINE_H);
		int perSpread = leftLines + rightLines;
		int pages = pageCount(lines.size(), perSpread);
		textPage = clamp(textPage, pages);
		int first = textPage * perSpread;
		drawLines(g, font, lines, first, leftLines, lx, ly + leftTop);
		drawLines(g, font, lines, first + leftLines, rightLines, rx, ry);
		drawTextPager(g, font, x, y, pages, mouseX, mouseY);
	}

	// ---- text -------------------------------------------------------------------------

	/** How many lines fit on the right page below {@code top}, leaving room for the footer. */
	private static int rightPageLines(int top) {
		return Math.max(1, (GlossaryLayout.RIGHT_PAGE_H - top - GlossaryLayout.FOOTER_H) / GlossaryLayout.LINE_H);
	}

	private static int pageCount(int lines, int perPage) {
		return Math.max(1, (lines + Math.max(1, perPage) - 1) / Math.max(1, perPage));
	}

	private static void drawLines(GuiGraphicsExtractor g, Font font, List<FormattedCharSequence> lines, int first, int count, int tx, int ty) {
		for (int i = first; i < lines.size() && i < first + count; i++)
			g.text(font, lines.get(i), tx, ty + (i - first) * GlossaryLayout.LINE_H, GlossaryLayout.INK, false);
	}

	private void drawTextPager(GuiGraphicsExtractor g, Font font, int x, int y, int pages, int mouseX, int mouseY) {
		textPage = clamp(textPage, pages);
		lastTextPages = pages;
		if (pages > 1)
			drawPager(g, font, textPager(font, x, y, pages), mouseX, mouseY, true);
	}

	private Pager textPager(Font font, int x, int y, int pages) {
		int centre = x + GlossaryLayout.RIGHT_PAGE_X + GlossaryLayout.RIGHT_PAGE_W / 2;
		int fy = y + GlossaryLayout.RIGHT_PAGE_Y + GlossaryLayout.RIGHT_PAGE_H - GlossaryLayout.FOOTER_H;
		return pager(font, centre - pagerWidth(font, pages) / 2, fy, textPage, pages);
	}

	private static void drawTitle(GuiGraphicsExtractor g, Font font, Component title, int px, int py, int pw) {
		String shown = fit(font, title.getString(), pw);
		g.text(font, Component.literal(shown), px, py, GlossaryLayout.INK_TITLE, false);
		g.fill(px, py + 11, px + pw, py + 12, GlossaryLayout.RULE);
	}

	// ---- pager ------------------------------------------------------------------------

	private static Component pagerLabel(int page, int pages) {
		return Component.literal((page + 1) + " / " + pages);
	}

	private static int pagerWidth(Font font, int pages) {
		// sized for the widest label so the buttons do not jump between pages
		return 2 * GlossaryLayout.PAGE_BUTTON + 12 + font.width(pagerLabel(pages - 1, pages));
	}

	private static Pager pager(Font font, int left, int footerY, int page, int pages) {
		int b = GlossaryLayout.PAGE_BUTTON;
		int width = pagerWidth(font, pages);
		int y = footerY + (GlossaryLayout.FOOTER_H - b) / 2;
		Component label = pagerLabel(page, pages);
		int labelX = left + (width - font.width(label)) / 2;
		return new Pager(left, left + width - b, y, labelX, label, page, pages);
	}

	/** On the parchment ({@code onPage}) in ink; off it (under the left ribbons) in ribbon text. */
	private static void drawPager(GuiGraphicsExtractor g, Font font, Pager p, int mouseX, int mouseY, boolean onPage) {
		drawPageButton(g, font, p.prevX(), p.y(), "<", p.page() > 0, mouseX, mouseY, onPage);
		drawPageButton(g, font, p.nextX(), p.y(), ">", p.page() < p.pages() - 1, mouseX, mouseY, onPage);
		g.text(font, p.label(), p.labelX(), p.y() + (GlossaryLayout.PAGE_BUTTON - 8) / 2 + 1, onPage ? GlossaryLayout.INK_DIM : GlossaryLayout.RIBBON_TEXT, !onPage);
	}

	private static void drawPageButton(GuiGraphicsExtractor g, Font font, int bx, int by, String glyph, boolean enabled, int mouseX, int mouseY, boolean onPage) {
		int b = GlossaryLayout.PAGE_BUTTON;
		boolean hover = enabled && MapPage.inside(mouseX, mouseY, bx, by, b, b);
		if (hover)
			g.fill(bx, by, bx + b, by + b, GlossaryLayout.BUTTON_HOVER);
		int ink = onPage ? GlossaryLayout.INK : GlossaryLayout.RIBBON_TEXT;
		int dim = onPage ? GlossaryLayout.CELL_BORDER : shade(GlossaryLayout.RIBBON_TEXT, 50);
		MapPage.drawBorder(g, bx, by, b, b, enabled ? ink : dim);
		g.text(font, glyph, bx + (b - font.width(glyph)) / 2 + 1, by + (b - 8) / 2 + 1, enabled ? ink : dim, false);
	}

	// ---- small helpers ------------------------------------------------------------------

	private static void drawScaledItem(GuiGraphicsExtractor g, ItemStack stack, int centreX, int centreY, int scale) {
		g.pose().pushMatrix();
		g.pose().translate(centreX - 8 * scale, centreY - 8 * scale);
		g.pose().scale(scale, scale);
		g.fakeItem(stack, 0, 0);
		g.pose().popMatrix();
	}

	/** The whole texture stretched over the rect, multiplied by {@code color} (-1 = untinted). */
	private static void blit(GuiGraphicsExtractor g, Identifier texture, int x, int y, int w, int h, int color) {
		g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, w, h, w, h, color);
	}

	/** An ARGB colour with its RGB scaled to {@code percent} (alpha kept). */
	private static int shade(int argb, int percent) {
		int r = Math.min(255, ((argb >> 16) & 0xFF) * percent / 100);
		int gr = Math.min(255, ((argb >> 8) & 0xFF) * percent / 100);
		int b = Math.min(255, (argb & 0xFF) * percent / 100);
		return (argb & 0xFF000000) | (r << 16) | (gr << 8) | b;
	}

	/** The text cut to fit a width, with "..." when cut. */
	private static String fit(Font font, String text, int width) {
		if (font.width(text) <= width)
			return text;
		return font.plainSubstrByWidth(text, width - font.width("...")) + "...";
	}

	private static int clamp(int page, int pages) {
		return Math.max(0, Math.min(page, pages - 1));
	}

	// ==========================================================================
	// state
	// ==========================================================================

	/** The selected category; on first open, the first one holding a creature from this mod. */
	private static GlossaryBestiary.Category currentCategory() {
		if (category != null)
			return category;
		GlossaryBestiary.Category firstFilled = null;
		for (GlossaryBestiary.Category c : GlossaryBestiary.Category.values()) {
			List<GlossaryBestiary.Entry> entries = GlossaryBestiary.entries(c);
			for (GlossaryBestiary.Entry e : entries)
				if (WitchercraftMod.MODID.equals(e.id().getNamespace()))
					return category = c;
			if (firstFilled == null && !entries.isEmpty())
				firstFilled = c;
		}
		return category = firstFilled != null ? firstFilled : GlossaryBestiary.Category.values()[0];
	}

	/** The selected creature, falling back to the category's first one (null for an empty category). */
	private static GlossaryBestiary.Entry selectedEntry() {
		List<GlossaryBestiary.Entry> entries = GlossaryBestiary.entries(currentCategory());
		for (GlossaryBestiary.Entry e : entries)
			if (e.type() == selectedType)
				return e;
		if (entries.isEmpty())
			return null;
		selectedType = entries.get(0).type();
		textPage = 0;
		return entries.get(0);
	}

	private static GlossaryTutorials.Group selectedGroup() {
		for (GlossaryTutorials.Group group : GlossaryTutorials.GROUPS)
			if (group.id().equals(groupId))
				return group;
		GlossaryTutorials.Group first = GlossaryTutorials.GROUPS.get(0);
		groupId = first.id();
		return first;
	}

	private static GlossaryTutorials.Topic selectedTopic() {
		List<GlossaryTutorials.Topic> topics = selectedGroup().topics();
		for (GlossaryTutorials.Topic t : topics)
			if (t.id().equals(topicId))
				return t;
		if (topics.isEmpty())
			return null;
		topicId = topics.get(0).id();
		textPage = 0;
		return topics.get(0);
	}

	/** Select top ribbon i: a category, or a Tutorials group. Its first entry or topic follows. */
	private static void selectTop(int i) {
		if (i == selectedTop())
			return;
		if (subTab == SubTab.BESTIARY) {
			category = GlossaryBestiary.Category.values()[i];
			List<GlossaryBestiary.Entry> entries = GlossaryBestiary.entries(category);
			selectedType = entries.isEmpty() ? null : entries.get(0).type();
		} else {
			GlossaryTutorials.Group group = GlossaryTutorials.GROUPS.get(i);
			groupId = group.id();
			topicId = group.topics().isEmpty() ? null : group.topics().get(0).id();
		}
		sidePage = 0;
		textPage = 0;
	}

	/** Select left ribbon i (index in the full list). */
	private static void selectSide(int i) {
		if (i == selectedSide())
			return;
		if (subTab == SubTab.BESTIARY)
			selectedType = GlossaryBestiary.entries(currentCategory()).get(i).type();
		else
			topicId = selectedGroup().topics().get(i).id();
		textPage = 0;
	}

	// ==========================================================================
	// input
	// ==========================================================================

	@Override
	public boolean mouseClicked(int x, int y, int w, int h, double mouseX, double mouseY, int button) {
		if (button != 0)
			return false;
		Font font = Minecraft.getInstance().font;

		// right ribbons: Bestiary / Tutorials
		for (SubTab tab : SubTab.values()) {
			int rx = tabRibbonX(x, tab == subTab), ry = tabRibbonY(y, tab.ordinal());
			if (MapPage.inside(mouseX, mouseY, coverRight(x), ry, rx + GlossaryLayout.RIGHT_W - coverRight(x), GlossaryLayout.RIGHT_H)) {
				if (tab != subTab) {
					subTab = tab;
					sidePage = 0;
					textPage = 0;
				}
				return true;
			}
		}

		// top ribbons
		int selectedTop = selectedTop();
		List<Ribbon> top = topRibbons();
		for (int i = 0; i < top.size(); i++) {
			int rx = topRibbonX(x, i), ry = topRibbonY(y, i == selectedTop);
			if (MapPage.inside(mouseX, mouseY, rx, ry, GlossaryLayout.TOP_W, coverTop(y) - ry)) {
				selectTop(i);
				return true;
			}
		}

		// left ribbons and their pager
		List<Ribbon> side = sideRibbons();
		int selectedSide = selectedSide();
		int first = sidePage * GlossaryLayout.SIDE_ROWS;
		for (int i = first; i < side.size() && i < first + GlossaryLayout.SIDE_ROWS; i++) {
			int rx = sideRibbonX(x, i == selectedSide), ry = sideRibbonY(y, i - first);
			if (MapPage.inside(mouseX, mouseY, rx, ry, coverLeft(x) - rx, GlossaryLayout.SIDE_H)) {
				selectSide(i);
				return true;
			}
		}
		int sidePages = sidePages(side.size());
		if (sidePages > 1) {
			int turn = pagerClick(sidePager(font, x, y, sidePages), mouseX, mouseY);
			if (turn != 0) {
				sidePage = clamp(sidePage + turn, sidePages);
				return true;
			}
		}

		// the doll: start a drag-to-rotate
		if (subTab == SubTab.BESTIARY && MapPage.inside(mouseX, mouseY, x + GlossaryLayout.DOLL_X, y + GlossaryLayout.DOLL_Y, GlossaryLayout.DOLL_W, GlossaryLayout.DOLL_H)) {
			draggingDoll = true;
			return true;
		}

		// the text pager
		if (lastTextPages > 1) {
			int turn = pagerClick(textPager(font, x, y, lastTextPages), mouseX, mouseY);
			if (turn != 0) {
				textPage = clamp(textPage + turn, lastTextPages);
				return true;
			}
		}

		// clicks on the book itself are swallowed, like the other tabs do
		return MapPage.inside(mouseX, mouseY, x + GlossaryLayout.BOOK_X, y + GlossaryLayout.BOOK_Y, GlossaryLayout.BOOK_W, GlossaryLayout.BOOK_H);
	}

	/** -1 for the prev button, +1 for next, 0 for neither. */
	private static int pagerClick(Pager p, double mouseX, double mouseY) {
		int b = GlossaryLayout.PAGE_BUTTON;
		if (MapPage.inside(mouseX, mouseY, p.prevX(), p.y(), b, b))
			return -1;
		if (MapPage.inside(mouseX, mouseY, p.nextX(), p.y(), b, b))
			return 1;
		return 0;
	}

	@Override
	public boolean mouseDragged(int x, int y, int w, int h, double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (!draggingDoll || button != 0)
			return false;
		// dragging right turns the creature's front to the right
		dollYaw = (float) ((dollYaw - dragX * DOLL_DRAG_DEGREES_PER_PX) % 360.0);
		return true;
	}

	@Override
	public boolean mouseReleased(int x, int y, int w, int h, double mouseX, double mouseY, int button) {
		if (!draggingDoll || button != 0)
			return false;
		draggingDoll = false;
		return true;
	}

	@Override
	public boolean mouseScrolled(int x, int y, int w, int h, double mouseX, double mouseY, double scrollX, double scrollY) {
		if (scrollY == 0)
			return false;
		int turn = scrollY < 0 ? 1 : -1;
		// over the top ribbons: the next or previous category / group
		if (mouseY < coverTop(y) && mouseX >= x + GlossaryLayout.TOP_X && mouseX < coverRight(x)) {
			int count = topRibbons().size();
			selectTop(Math.floorMod(selectedTop() + turn, count));
			return true;
		}
		// over the left ribbons: their pages
		if (mouseX < coverLeft(x)) {
			sidePage = clamp(sidePage + turn, sidePages(sideRibbons().size()));
			return true;
		}
		// over the book: the text's pages
		if (MapPage.inside(mouseX, mouseY, x + GlossaryLayout.BOOK_X, y + GlossaryLayout.BOOK_Y, GlossaryLayout.BOOK_W, GlossaryLayout.BOOK_H)) {
			textPage = clamp(textPage + turn, lastTextPages);
			return true;
		}
		return false;
	}
}
