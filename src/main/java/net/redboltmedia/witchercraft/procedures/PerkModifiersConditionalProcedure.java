package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

@EventBusSubscriber
public class PerkModifiersConditionalProcedure {
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
		if (!world.isClientSide()) {
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkSunAndStars == true && world instanceof Level _lvl17 && _lvl17.isBrightOutside()) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:perk_sun_and_stars_day"), 0.3333, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.PASSIVE_HEALTH_REGEN).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.PASSIVE_HEALTH_REGEN).addTransientModifier(modifier);
					}
				}
			} else {
				if (entity instanceof LivingEntity _entity) {
					_entity.getAttribute(WitchercraftModAttributes.PASSIVE_HEALTH_REGEN).removeModifier(Identifier.parse("witchercraft:perk_sun_and_stars_day"));
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkSunAndStars == true && !(world instanceof Level _lvl20 && _lvl20.isBrightOutside())) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:perk_sun_and_stars_night"), 0.3333, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.PASSIVE_STAMINA_REGEN).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.PASSIVE_STAMINA_REGEN).addTransientModifier(modifier);
					}
				}
			} else {
				if (entity instanceof LivingEntity _entity) {
					_entity.getAttribute(WitchercraftModAttributes.PASSIVE_STAMINA_REGEN).removeModifier(Identifier.parse("witchercraft:perk_sun_and_stars_night"));
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkMuscleMemory && !(entity.getPersistentData().getDoubleOr("witchercraftLastHurtTick", 0) > 0 && (entity.tickCount - entity.getPersistentData().getDoubleOr("witchercraftLastHurtTick", 0) >= 0 && entity.tickCount - entity.getPersistentData().getDoubleOr("witchercraftLastHurtTick", 0) <= 59))) {
				if (!(entity instanceof LivingEntity _livEnt21 && _livEnt21.hasEffect(WitchercraftModMobEffects.MUSCLE_MEMORY_ACTIVE))) {
					if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
						_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.MUSCLE_MEMORY_ACTIVE, -1, 0, true, true));
				}
			} else {
				if (entity instanceof LivingEntity _livEnt22 && _livEnt22.hasEffect(WitchercraftModMobEffects.MUSCLE_MEMORY_ACTIVE)) {
					if (entity instanceof LivingEntity _entity)
						_entity.removeEffect(WitchercraftModMobEffects.MUSCLE_MEMORY_ACTIVE);
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkBloodyRage
					&& (entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) < (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) * 0.3) {
				if (!(entity instanceof LivingEntity _livEnt23 && _livEnt23.hasEffect(WitchercraftModMobEffects.BLOODY_RAGE_ACTIVE))) {
					if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
						_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.BLOODY_RAGE_ACTIVE, -1, 0, true, true));
				}
			} else {
				if (entity instanceof LivingEntity _livEnt24 && _livEnt24.hasEffect(WitchercraftModMobEffects.BLOODY_RAGE_ACTIVE)) {
					if (entity instanceof LivingEntity _entity)
						_entity.removeEffect(WitchercraftModMobEffects.BLOODY_RAGE_ACTIVE);
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkFleetFooted
					&& !(entity instanceof LivingEntity _livEnt25 && _livEnt25.hasEffect(WitchercraftModMobEffects.IN_COMBAT))) {
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
}
