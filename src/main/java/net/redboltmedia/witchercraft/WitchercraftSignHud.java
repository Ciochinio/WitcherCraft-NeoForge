package net.redboltmedia.witchercraft;

import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Holder;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.AttackIndicatorStatus;

import java.util.List;

/**
 * The selected sign as a slot beside the hotbar, mirroring the offhand slot: on the main-hand side (right for
 * right-handed, left for left-handed), pushed further out when the vanilla attack indicator is set to Hotbar,
 * since that sits on the same side. The icon is the sign effect's own icon. The sign effects themselves are hidden
 * from the top-right icons by their MCreator elements ("render status in HUD" off); NeoForge allows only one client
 * extension per effect, so don't register another one here.
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber(Dist.CLIENT)
public class WitchercraftSignHud {
	private static final Identifier SLOT_RIGHT = Identifier.withDefaultNamespace("hud/hotbar_offhand_right");
	private static final Identifier SLOT_LEFT = Identifier.withDefaultNamespace("hud/hotbar_offhand_left");
	/** Extra distance from the hotbar when the Hotbar attack indicator takes the spot next to it. */
	private static final int ATTACK_INDICATOR_SHIFT = 26;

	private static List<Holder<MobEffect>> signs() {
		return List.of(WitchercraftModMobEffects.AARD_SIGN, WitchercraftModMobEffects.IGNI_SIGN, WitchercraftModMobEffects.YRDEN_SIGN, WitchercraftModMobEffects.QUEN_SIGN, WitchercraftModMobEffects.AXII_SIGN);
	}

	@SubscribeEvent
	public static void onRenderHotbar(RenderGuiLayerEvent.Post event) {
		if (!event.getName().equals(VanillaGuiLayers.HOTBAR))
			return;
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if (player == null || mc.options.hideGui || player.isSpectator())
			return;
		Holder<MobEffect> sign = null;
		for (Holder<MobEffect> s : signs()) {
			if (player.hasEffect(s)) {
				sign = s;
				break;
			}
		}
		if (sign == null)
			return;
		GuiGraphicsExtractor g = event.getGuiGraphics();
		int center = g.guiWidth() / 2;
		int shift = mc.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR ? ATTACK_INDICATOR_SHIFT : 0;
		int y = g.guiHeight() - 23;
		// Offsets match vanilla's offhand slot drawn on that side (29x24 frame, item 16 px at +10 / +3).
		if (player.getMainArm() == HumanoidArm.RIGHT) {
			int x = center + 91 + shift;
			g.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_RIGHT, x, y, 29, 24);
			g.blitSprite(RenderPipelines.GUI_TEXTURED, Gui.getMobEffectSprite(sign), x + 9, y + 3, 18, 18);
		} else {
			int x = center - 91 - 29 - shift;
			g.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_LEFT, x, y, 29, 24);
			g.blitSprite(RenderPipelines.GUI_TEXTURED, Gui.getMobEffectSprite(sign), x + 2, y + 3, 18, 18);
		}
	}
}
