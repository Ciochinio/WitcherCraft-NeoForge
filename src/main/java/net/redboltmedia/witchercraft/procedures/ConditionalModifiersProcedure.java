package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

@EventBusSubscriber
public class ConditionalModifiersProcedure {
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
			if (entity instanceof LivingEntity _livEnt1 && _livEnt1.hasEffect(WitchercraftModMobEffects.THUNDERBOLT_EFFECT) && world instanceof Level _lvl2 && _lvl2.isThundering()) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:effect_thunderbolt_storm"), 100, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.CRIT_CHANCE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.CRIT_CHANCE).addTransientModifier(modifier);
					}
				}
			} else {
				if (entity instanceof LivingEntity _entity) {
					_entity.getAttribute(WitchercraftModAttributes.CRIT_CHANCE).removeModifier(Identifier.parse("witchercraft:effect_thunderbolt_storm"));
				}
			}
			if (entity instanceof LivingEntity _livEnt5 && _livEnt5.hasEffect(WitchercraftModMobEffects.WATER_HAG_DECOCTION_EFFECT)
					&& (entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) == (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1)) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:effect_waterhag_fullhp"), 40, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).addTransientModifier(modifier);
					}
				}
			} else {
				if (entity instanceof LivingEntity _entity) {
					_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).removeModifier(Identifier.parse("witchercraft:effect_waterhag_fullhp"));
				}
			}
			if (entity instanceof LivingEntity _livEnt10 && _livEnt10.hasEffect(WitchercraftModMobEffects.NEKKER_WARRIOR_DECOCTION_EFFECT) && entity.isPassenger()) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:effect_nekker_riding"), 50, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).addTransientModifier(modifier);
					}
				}
			} else {
				if (entity instanceof LivingEntity _entity) {
					_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).removeModifier(Identifier.parse("witchercraft:effect_nekker_riding"));
				}
			}
			if (entity instanceof LivingEntity _livEnt14 && _livEnt14.hasEffect(WitchercraftModMobEffects.WEREWOLF_DECOCTION_EFFECT) && !(world instanceof Level _lvl15 && _lvl15.isRaining())
					&& !(world instanceof Level _lvl16 && _lvl16.isBrightOutside())) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:effect_werewolf_night"), 0.3333, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.PASSIVE_STAMINA_REGEN).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.PASSIVE_STAMINA_REGEN).addTransientModifier(modifier);
					}
				}
			} else {
				if (entity instanceof LivingEntity _entity) {
					_entity.getAttribute(WitchercraftModAttributes.PASSIVE_STAMINA_REGEN).removeModifier(Identifier.parse("witchercraft:effect_werewolf_night"));
				}
			}
			if (entity instanceof LivingEntity _livEnt19 && _livEnt19.hasEffect(WitchercraftModMobEffects.IN_COMBAT)) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:combat_regen_penalty"), (-0.5), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
					if (!_entity.getAttribute(WitchercraftModAttributes.PASSIVE_HEALTH_REGEN).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.PASSIVE_HEALTH_REGEN).addTransientModifier(modifier);
					}
				}
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:combat_regen_penalty"), (-0.5), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
					if (!_entity.getAttribute(WitchercraftModAttributes.PASSIVE_STAMINA_REGEN).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.PASSIVE_STAMINA_REGEN).addTransientModifier(modifier);
					}
				}
			} else {
				if (entity instanceof LivingEntity _entity) {
					_entity.getAttribute(WitchercraftModAttributes.PASSIVE_HEALTH_REGEN).removeModifier(Identifier.parse("witchercraft:combat_regen_penalty"));
				}
				if (entity instanceof LivingEntity _entity) {
					_entity.getAttribute(WitchercraftModAttributes.PASSIVE_STAMINA_REGEN).removeModifier(Identifier.parse("witchercraft:combat_regen_penalty"));
				}
			}
			if (entity instanceof LivingEntity _livEnt24 && _livEnt24.hasEffect(WitchercraftModMobEffects.FOGLET_DECOCTION_EFFECT) && world instanceof Level _lvl25 && _lvl25.isRaining()) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:effect_foglet_rain"), 25, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).addTransientModifier(modifier);
					}
				}
			} else {
				if (entity instanceof LivingEntity _entity) {
					_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).removeModifier(Identifier.parse("witchercraft:effect_foglet_rain"));
				}
			}
			if (entity instanceof LivingEntity _livEnt28 && _livEnt28.hasEffect(MobEffects.LUCK)) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:effect_luck_oil"), 20, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.OIL_DAMAGE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.OIL_DAMAGE).addTransientModifier(modifier);
					}
				}
			} else {
				if (entity instanceof LivingEntity _entity) {
					_entity.getAttribute(WitchercraftModAttributes.OIL_DAMAGE).removeModifier(Identifier.parse("witchercraft:effect_luck_oil"));
				}
			}
		}
	}
}