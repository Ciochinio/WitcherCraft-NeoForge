package net.redboltmedia.witchercraft;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * The "New recipe learned" toast, in the style of vanilla's recipe unlock toast
 * (same background sprite, 5 seconds). Recipes learned while it is showing join
 * it and cycle, instead of stacking more toasts. Shown in the Alchemy tab and,
 * for manuscripts (slice 4), anywhere else.
 *
 * Client only. HAND-MAINTAINED: locked code element.
 */
public class AlchemyToast implements Toast {
	private static final Identifier BACKGROUND_SPRITE = Identifier.withDefaultNamespace("toast/recipe");
	private static final double DISPLAY_MS = 5000.0;
	private static final int TITLE_COLOR = 0xFF500050;
	private static final int TEXT_COLOR = 0xFF000000;
	private static final Component TITLE = Component.translatable("gui.witchercraft.alchemy.toast.title");

	private final List<ItemStack> results = new ArrayList<>();
	private long lastChanged;
	private boolean changed;
	private Toast.Visibility wantedVisibility = Toast.Visibility.HIDE;
	private int shownIndex;

	private AlchemyToast() {
	}

	/** Show newly learned recipes by their results, joining a toast that is already up. */
	public static void show(List<ItemStack> learnedResults) {
		ToastManager manager = Minecraft.getInstance().getToastManager();
		AlchemyToast toast = manager.getToast(AlchemyToast.class, NO_TOKEN);
		if (toast == null) {
			toast = new AlchemyToast();
			manager.addToast(toast);
		}
		toast.results.addAll(learnedResults);
		toast.changed = true;
	}

	@Override
	public Toast.Visibility getWantedVisibility() {
		return wantedVisibility;
	}

	@Override
	public void update(ToastManager manager, long fullyVisibleForMs) {
		if (changed) {
			lastChanged = fullyVisibleForMs;
			changed = false;
		}
		double shownFor = DISPLAY_MS * manager.getNotificationDisplayTimeMultiplier();
		wantedVisibility = results.isEmpty() || fullyVisibleForMs - lastChanged >= shownFor ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
		if (!results.isEmpty())
			shownIndex = (int) (fullyVisibleForMs / Math.max(1.0, shownFor / results.size()) % results.size());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long fullyVisibleForMs) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_SPRITE, 0, 0, this.width(), this.height());
		graphics.text(font, TITLE, 30, 7, TITLE_COLOR, false);
		if (results.isEmpty())
			return;
		ItemStack result = results.get(Math.min(shownIndex, results.size() - 1));
		graphics.text(font, font.plainSubstrByWidth(result.getHoverName().getString(), this.width() - 34), 30, 18, TEXT_COLOR, false);
		graphics.fakeItem(result, 8, 8);
	}
}
