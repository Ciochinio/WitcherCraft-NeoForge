package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

@EventBusSubscriber
public class GraveHagDecoctionHitProcedure {
	@SubscribeEvent
	public static void onEntityDeath(LivingDeathEvent event) {
		if (event.getEntity() != null) {
			execute(event, event.getSource().getEntity());
		}
	}

	public static void execute(Entity sourceentity) {
		execute(null, sourceentity);
	}

	private static void execute(@Nullable Event event, Entity sourceentity) {
		if (sourceentity == null)
			return;
		if (sourceentity instanceof LivingEntity _livingEntity0 && _livingEntity0.getAttributes().hasAttribute(WitchercraftModAttributes.PASSIVE_HEALTH_REGEN)) {
			if (sourceentity instanceof LivingEntity _livEnt1 && _livEnt1.hasEffect(WitchercraftModMobEffects.GRAVE_HAG_DECOCTION_EFFECT) && sourceentity instanceof LivingEntity _livEnt2 && _livEnt2.hasEffect(WitchercraftModMobEffects.IN_COMBAT)) {
				if (sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftGraveHagDecoctionKill < 10) {
					{
						WitchercraftModVariables.PlayerVariables _vars = sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
						_vars.witchercraftGraveHagDecoctionKill = sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftGraveHagDecoctionKill + 1;
						_vars.markSyncDirty();
					}
				}
			}
			if (sourceentity instanceof LivingEntity _entity) {
				_entity.getAttribute(WitchercraftModAttributes.PASSIVE_HEALTH_REGEN).removeModifier(Identifier.parse("witchercraft:effect_grave_hag_kills"));
			}
			if (sourceentity instanceof LivingEntity _livEnt4 && _livEnt4.hasEffect(WitchercraftModMobEffects.GRAVE_HAG_DECOCTION_EFFECT) && sourceentity instanceof LivingEntity _livEnt5 && _livEnt5.hasEffect(WitchercraftModMobEffects.IN_COMBAT)) {
				if (sourceentity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:effect_grave_hag_kills"),
							(Math.round(sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftGraveHagDecoctionKill / 2) * 0.3333), AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.PASSIVE_HEALTH_REGEN).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.PASSIVE_HEALTH_REGEN).addTransientModifier(modifier);
					}
				}
			}
		}
	}
}