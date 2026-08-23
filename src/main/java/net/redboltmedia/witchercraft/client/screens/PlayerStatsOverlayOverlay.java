package net.redboltmedia.witchercraft.client.screens;

import net.redboltmedia.witchercraft.procedures.*;

import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;

@EventBusSubscriber(Dist.CLIENT)
public class PlayerStatsOverlayOverlay {
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void eventHandler(RenderGuiEvent.Pre event) {
		int w = event.getGuiGraphics().guiWidth();
		int h = event.getGuiGraphics().guiHeight();
		Level world = null;
		double x = 0;
		double y = 0;
		double z = 0;
		Player entity = Minecraft.getInstance().player;
		if (entity != null) {
			world = entity.level();
			x = entity.getX();
			y = entity.getY();
			z = entity.getZ();
		}
		if (PlayerStatsOverlayOnProcedure.execute(entity)) {
			event.getGuiGraphics().text(Minecraft.getInstance().font, Component.translatable("gui.witchercraft.player_stats_overlay.label_movementspeed"), w / 2 + -207, h / 2 + -112, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font, Component.translatable("gui.witchercraft.player_stats_overlay.label_attackspeed"), w / 2 + -207, h / 2 + -103, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font, Component.translatable("gui.witchercraft.player_stats_overlay.label_critchance"), w / 2 + -207, h / 2 + -94, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font, Component.translatable("gui.witchercraft.player_stats_overlay.label_critdamage"), w / 2 + -207, h / 2 + -85, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font, Component.translatable("gui.witchercraft.player_stats_overlay.label_health"), w / 2 + -207, h / 2 + -76, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font, Component.translatable("gui.witchercraft.player_stats_overlay.label_healthregen"), w / 2 + -207, h / 2 + -67, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font, Component.translatable("gui.witchercraft.player_stats_overlay.label_staminaregen"), w / 2 + -207, h / 2 + -58, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font,

					CharacterGuiMovementSpeedProcedure.execute(entity), w / 2 + -108, h / 2 + -112, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font,

					CharacterGuiAttackSpeedProcedure.execute(entity), w / 2 + -108, h / 2 + -103, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font,

					CharacterGuiCritChanceProcedure.execute(entity), w / 2 + -108, h / 2 + -94, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font,

					CharacterGuiCritDamageProcedure.execute(entity), w / 2 + -108, h / 2 + -85, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font,

					CharacterGuiHealthProcedure.execute(entity), w / 2 + -108, h / 2 + -76, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font,

					CharacterGuiPassiveHealthRegenProcedure.execute(entity), w / 2 + -108, h / 2 + -67, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font,

					CharacterGuiPassiveStaminaRegenProcedure.execute(entity), w / 2 + -108, h / 2 + -58, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font, Component.translatable("gui.witchercraft.player_stats_overlay.label_toxicity"), w / 2 + -207, h / 2 + -49, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font, Component.translatable("gui.witchercraft.player_stats_overlay.label_maxtoxicity"), w / 2 + -207, h / 2 + -40, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font,

					CharacterGuiToxicityProcedure.execute(entity), w / 2 + -108, h / 2 + -49, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font,

					CharacterGuiToxicityOverdoseThresholdProcedure.execute(entity), w / 2 + -108, h / 2 + -40, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font, Component.translatable("gui.witchercraft.player_stats_overlay.label_additionaldamage"), w / 2 + -207, h / 2 + -22, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font,

					CharacterGuIncreasedDamageProcedure.execute(entity), w / 2 + -108, h / 2 + -22, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font, Component.translatable("gui.witchercraft.player_stats_overlay.label_additionaldamgeflat"), w / 2 + -207, h / 2 + -31, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font,

					CharacterGuiAdditionalDamageProcedure.execute(entity), w / 2 + -108, h / 2 + -31, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font, Component.translatable("gui.witchercraft.player_stats_overlay.label_dodge_chance"), w / 2 + -207, h / 2 + -13, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font,

					CharacterGuiDodgeChanceProcedure.execute(entity), w / 2 + -108, h / 2 + -13, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font, Component.translatable("gui.witchercraft.player_stats_overlay.label_instantkillchance"), w / 2 + -207, h / 2 + -4, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font,

					CharacterGuiInstantKillChanceProcedure.execute(entity), w / 2 + -108, h / 2 + -4, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font, Component.translatable("gui.witchercraft.player_stats_overlay.label_potion_duration"), w / 2 + -207, h / 2 + 5, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font,

					CharacterGuiPotionDurationProcedure.execute(entity), w / 2 + -108, h / 2 + 5, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font, Component.translatable("gui.witchercraft.player_stats_overlay.label_sign_intensity"), w / 2 + -207, h / 2 + 14, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font,

					CharacterGuiSignIntensityProcedure.execute(entity), w / 2 + -108, h / 2 + 14, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font, Component.translatable("gui.witchercraft.player_stats_overlay.label_oildamage"), w / 2 + -207, h / 2 + 23, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font,

					CharacterGuiOilDamageProcedure.execute(entity), w / 2 + -108, h / 2 + 23, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font, Component.translatable("gui.witchercraft.player_stats_overlay.label_life_steal"), w / 2 + -207, h / 2 + 32, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font,

					CharacterGuiLifeStealProcedure.execute(entity), w / 2 + -108, h / 2 + 32, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font, Component.translatable("gui.witchercraft.player_stats_overlay.label_reflectdamage"), w / 2 + -207, h / 2 + 41, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font,

					CharacterGuiReflectDamageProcedure.execute(entity), w / 2 + -108, h / 2 + 41, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font, Component.translatable("gui.witchercraft.player_stats_overlay.label_signhold"), w / 2 + -207, h / 2 + 50, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font,

					SignKeyHoldTimeProcedure.execute(entity), w / 2 + -108, h / 2 + 50, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font, Component.translatable("gui.witchercraft.player_stats_overlay.label_signholdcost"), w / 2 + -207, h / 2 + 59, -1, false);
			event.getGuiGraphics().text(Minecraft.getInstance().font,

					SignCastHoldCostProcedure.execute(entity), w / 2 + -108, h / 2 + 59, -1, false);
		}
	}
}