package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;

public class PerkModifiersProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkGriffinSchool == true) {
			if (entity instanceof LivingEntity _entity) {
				AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:perk_griffin_school"), 20, AttributeModifier.Operation.ADD_VALUE);
				if (!_entity.getAttribute(WitchercraftModAttributes.POTION_DURATION).hasModifier(modifier.id())) {
					_entity.getAttribute(WitchercraftModAttributes.POTION_DURATION).addTransientModifier(modifier);
				}
			}
		} else {
			if (entity instanceof LivingEntity _entity) {
				_entity.getAttribute(WitchercraftModAttributes.POTION_DURATION).removeModifier(Identifier.parse("witchercraft:perk_griffin_school"));
			}
		}
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkGourmet == true) {
			if (entity instanceof LivingEntity _entity) {
				AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:perk_gourmet"), 0.3333, AttributeModifier.Operation.ADD_VALUE);
				if (!_entity.getAttribute(WitchercraftModAttributes.PASSIVE_STAMINA_REGEN).hasModifier(modifier.id())) {
					_entity.getAttribute(WitchercraftModAttributes.PASSIVE_STAMINA_REGEN).addTransientModifier(modifier);
				}
			}
		} else {
			if (entity instanceof LivingEntity _entity) {
				_entity.getAttribute(WitchercraftModAttributes.PASSIVE_STAMINA_REGEN).removeModifier(Identifier.parse("witchercraft:perk_gourmet"));
			}
		}
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkSurvivalInstinct == true) {
			if (entity instanceof LivingEntity _entity) {
				AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:perk_survival_instinct"), 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
				if (!_entity.getAttribute(Attributes.MAX_HEALTH).hasModifier(modifier.id())) {
					_entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(modifier);
				}
			}
		} else {
			if (entity instanceof LivingEntity _entity) {
				_entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(Identifier.parse("witchercraft:perk_survival_instinct"));
			}
		}
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkFleetFooted == true) {
			if (entity instanceof LivingEntity _entity) {
				AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:perk_fleet_footed"), 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
				if (!_entity.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(modifier.id())) {
					_entity.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(modifier);
				}
			}
		} else {
			if (entity instanceof LivingEntity _entity) {
				_entity.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(Identifier.parse("witchercraft:perk_fleet_footed"));
			}
		}
	}
}