package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

@EventBusSubscriber
public class HealthProcedure {
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		execute(event, event.getEntity());
	}

	public static void execute(Entity entity) {
		execute(null, entity);
	}

	private static void execute(@Nullable Event event, Entity entity) {
		if (entity == null)
			return;
		double sumHealth = 0;
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesSurvivalInstinct == true) {
			if (entity instanceof LivingEntity _entity) {
				AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:survivalinstinct"), 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
				if (!_entity.getAttribute(Attributes.MAX_HEALTH).hasModifier(modifier.id())) {
					_entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(modifier);
				}
			}
		} else {
			if (entity instanceof LivingEntity _entity) {
				_entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(Identifier.parse("witchercraft:survivalinstinct"));
			}
		}
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesDefence == true) {
			if (entity instanceof LivingEntity _entity) {
				AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:defence"), 4, AttributeModifier.Operation.ADD_VALUE);
				if (!_entity.getAttribute(Attributes.MAX_HEALTH).hasModifier(modifier.id())) {
					_entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(modifier);
				}
			}
		} else {
			if (entity instanceof LivingEntity _entity) {
				_entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(Identifier.parse("witchercraft:defence"));
			}
		}
		if (entity instanceof LivingEntity _livEnt4 && _livEnt4.hasEffect(WitchercraftModMobEffects.FULL_MOON_EFFECT)) {
			if (entity instanceof LivingEntity _entity) {
				AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:fullmoon"), 4, AttributeModifier.Operation.ADD_VALUE);
				if (!_entity.getAttribute(Attributes.MAX_HEALTH).hasModifier(modifier.id())) {
					_entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(modifier);
				}
			}
		} else {
			if (entity instanceof LivingEntity _entity) {
				_entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(Identifier.parse("witchercraft:fullmoon"));
			}
		}
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftHealth = Math.round(entity instanceof LivingEntity _livingEntity7 && _livingEntity7.getAttributes().hasAttribute(Attributes.MAX_HEALTH) ? _livingEntity7.getAttribute(Attributes.MAX_HEALTH).getValue() : 0);
			_vars.markSyncDirty();
		}
	}
}