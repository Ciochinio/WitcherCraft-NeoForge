/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.redboltmedia.witchercraft.init;

import net.redboltmedia.witchercraft.client.renderer.CockatriceRenderer;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;

@EventBusSubscriber(Dist.CLIENT)
public class WitchercraftModEntityRenderers {
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(WitchercraftModEntities.GRAPESHOT_PROJECTILE.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(WitchercraftModEntities.DEVILS_PUFFBALL_PROJECTILE.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(WitchercraftModEntities.DANCING_STAR_PROJECTILE.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(WitchercraftModEntities.NORTHERN_WIND_PROJECTILE.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(WitchercraftModEntities.SAMUM_PROJECTILE.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(WitchercraftModEntities.DIMERITIUM_BOMB_PROJECTILE.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(WitchercraftModEntities.IGNI_PARTICLES.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(WitchercraftModEntities.AARD_PARTICLES.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(WitchercraftModEntities.COCKATRICE.get(), CockatriceRenderer::new);
	}
}