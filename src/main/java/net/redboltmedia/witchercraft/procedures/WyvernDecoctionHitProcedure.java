package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

@EventBusSubscriber
public class WyvernDecoctionHitProcedure {
	@SubscribeEvent
	public static void onEntityAttacked(LivingIncomingDamageEvent event) {
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
		if (sourceentity instanceof LivingEntity _livingEntity0 && _livingEntity0.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE)) {
			if (sourceentity instanceof LivingEntity _livEnt1 && _livEnt1.hasEffect(WitchercraftModMobEffects.WYVERN_DECOCTION_EFFECT)) {
				if (sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftWyvernDecoctionHit < 10) {
					{
						WitchercraftModVariables.PlayerVariables _vars = sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
						_vars.witchercraftWyvernDecoctionHit = sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftWyvernDecoctionHit + 1;
						_vars.markSyncDirty();
					}
				}
			} else {
				{
					WitchercraftModVariables.PlayerVariables _vars = sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
					_vars.witchercraftWyvernDecoctionHit = 0;
					_vars.markSyncDirty();
				}
			}
			if (sourceentity instanceof LivingEntity _entity) {
				_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).removeModifier(Identifier.parse("witchercraft:effect_wyvern_hits"));
			}
			if (sourceentity instanceof LivingEntity _livEnt3 && _livEnt3.hasEffect(WitchercraftModMobEffects.WYVERN_DECOCTION_EFFECT)) {
				if (sourceentity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:effect_wyvern_hits"), sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftWyvernDecoctionHit,
							AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).addTransientModifier(modifier);
					}
				}
			}
		}
	}
}