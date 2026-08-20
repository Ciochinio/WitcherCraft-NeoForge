package net.redboltmedia.witchercraft.client.screens;

import net.redboltmedia.witchercraft.procedures.Tox5Procedure;
import net.redboltmedia.witchercraft.procedures.Tox4Procedure;
import net.redboltmedia.witchercraft.procedures.Tox3Procedure;
import net.redboltmedia.witchercraft.procedures.Tox2Procedure;
import net.redboltmedia.witchercraft.procedures.Tox1Procedure;

import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.Minecraft;

@EventBusSubscriber(Dist.CLIENT)
public class ToxicityOverlayOverlay {
	private static final Identifier IMAGE_0 = Identifier.parse("witchercraft:textures/screens/arrowforword.png");
	private static final Identifier IMAGE_1 = Identifier.parse("witchercraft:textures/screens/arrowback.png");
	private static final Identifier IMAGE_2 = Identifier.parse("witchercraft:textures/screens/arrowforword.png");
	private static final Identifier IMAGE_3 = Identifier.parse("witchercraft:textures/screens/arrowback.png");
	private static final Identifier IMAGE_4 = Identifier.parse("witchercraft:textures/screens/arrowforword.png");

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
		if (true) {
			if (Tox1Procedure.execute(entity)) {
				event.getGuiGraphics().blit(RenderPipelines.GUI_TEXTURED, IMAGE_0, w / 2 + -118, h / 2 + 42, 0, 0, 32, 32, 32, 32);
			}
			if (Tox2Procedure.execute(entity)) {
				event.getGuiGraphics().blit(RenderPipelines.GUI_TEXTURED, IMAGE_1, w / 2 + -61, h / 2 + 42, 0, 0, 32, 32, 32, 32);
			}
			if (Tox3Procedure.execute(entity)) {
				event.getGuiGraphics().blit(RenderPipelines.GUI_TEXTURED, IMAGE_2, w / 2 + -3, h / 2 + 39, 0, 0, 32, 32, 32, 32);
			}
			if (Tox4Procedure.execute(entity)) {
				event.getGuiGraphics().blit(RenderPipelines.GUI_TEXTURED, IMAGE_3, w / 2 + 50, h / 2 + 38, 0, 0, 32, 32, 32, 32);
			}
			if (Tox5Procedure.execute(entity)) {
				event.getGuiGraphics().blit(RenderPipelines.GUI_TEXTURED, IMAGE_4, w / 2 + 105, h / 2 + 35, 0, 0, 32, 32, 32, 32);
			}
		}
	}
}