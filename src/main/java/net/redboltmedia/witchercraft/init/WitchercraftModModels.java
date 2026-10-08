/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.redboltmedia.witchercraft.init;

import net.redboltmedia.witchercraft.client.model.ModelWolvenArmor;
import net.redboltmedia.witchercraft.client.model.ModelUrsineArmor;
import net.redboltmedia.witchercraft.client.model.ModelGhoul;
import net.redboltmedia.witchercraft.client.model.ModelCockatrice;
import net.redboltmedia.witchercraft.client.model.ModelAlghoul;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

@EventBusSubscriber(Dist.CLIENT)
public class WitchercraftModModels {
	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(ModelUrsineArmor.LAYER_LOCATION, ModelUrsineArmor::createBodyLayer);
		event.registerLayerDefinition(ModelCockatrice.LAYER_LOCATION, ModelCockatrice::createBodyLayer);
		event.registerLayerDefinition(ModelGhoul.LAYER_LOCATION, ModelGhoul::createBodyLayer);
		event.registerLayerDefinition(ModelAlghoul.LAYER_LOCATION, ModelAlghoul::createBodyLayer);
		event.registerLayerDefinition(ModelWolvenArmor.LAYER_LOCATION, ModelWolvenArmor::createBodyLayer);
	}
}