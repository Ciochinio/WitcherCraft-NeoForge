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
public class AttackSpeedProcedure {
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
		double sumAttackSpeed = 0;
		double sumMovementSpeed = 0;
		if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(WitchercraftModMobEffects.BLIZZARD_EFFECT)) {
			if (entity instanceof LivingEntity _entity) {
				AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:blizzard"), 0.55, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
				if (!_entity.getAttribute(Attributes.ATTACK_SPEED).hasModifier(modifier.id())) {
					_entity.getAttribute(Attributes.ATTACK_SPEED).addTransientModifier(modifier);
				}
			}
		} else {
			if (entity instanceof LivingEntity _entity) {
				_entity.getAttribute(Attributes.ATTACK_SPEED).removeModifier(Identifier.parse("witchercraft:blizzard"));
			}
		}
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftAttackSpeed = entity instanceof LivingEntity _livingEntity3 && _livingEntity3.getAttributes().hasAttribute(Attributes.ATTACK_SPEED) ? _livingEntity3.getAttribute(Attributes.ATTACK_SPEED).getValue() : 0;
			_vars.markSyncDirty();
		}
	}
}