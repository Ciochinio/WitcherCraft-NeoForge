package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

@EventBusSubscriber
public class PerkModifiersProcedure {
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		execute(event, event.getEntity().level(), event.getEntity());
	}

	public static void execute(LevelAccessor world, Entity entity) {
		execute(null, world, entity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		if (!(world.isClientSide())) {
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesDeadlyPrecision == true) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:perk_deadly_precision"), 1, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.INSTANT_KILL_CHANCE).hasModifier(modifier.id()))
						_entity.getAttribute(WitchercraftModAttributes.INSTANT_KILL_CHANCE).addTransientModifier(modifier);
				}
			} else {
				if (entity instanceof LivingEntity _entity)
					_entity.getAttribute(WitchercraftModAttributes.INSTANT_KILL_CHANCE).removeModifier(Identifier.parse("witchercraft:perk_deadly_precision"));
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesGriffinSchool == true) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:perk_griffin_school"), 20, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.POTION_DURATION).hasModifier(modifier.id()))
						_entity.getAttribute(WitchercraftModAttributes.POTION_DURATION).addTransientModifier(modifier);
				}
			} else {
				if (entity instanceof LivingEntity _entity)
					_entity.getAttribute(WitchercraftModAttributes.POTION_DURATION).removeModifier(Identifier.parse("witchercraft:perk_griffin_school"));
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesRazorFocus == true) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:perk_razor_focus"), 10, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.DODGE_CHANCE).hasModifier(modifier.id()))
						_entity.getAttribute(WitchercraftModAttributes.DODGE_CHANCE).addTransientModifier(modifier);
				}
			} else {
				if (entity instanceof LivingEntity _entity)
					_entity.getAttribute(WitchercraftModAttributes.DODGE_CHANCE).removeModifier(Identifier.parse("witchercraft:perk_razor_focus"));
			}
		}
	}
}
