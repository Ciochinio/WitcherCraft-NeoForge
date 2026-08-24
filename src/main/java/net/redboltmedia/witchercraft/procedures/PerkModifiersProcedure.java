package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
		if (!world.isClientSide()) {
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesDeadlyPrecision == true) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:perk_deadly_precision"), 1, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.INSTANT_KILL_CHANCE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.INSTANT_KILL_CHANCE).addTransientModifier(modifier);
					}
				}
			} else {
				if (entity instanceof LivingEntity _entity) {
					_entity.getAttribute(WitchercraftModAttributes.INSTANT_KILL_CHANCE).removeModifier(Identifier.parse("witchercraft:perk_deadly_precision"));
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesGriffinSchool == true) {
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
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesRazorFocus == true) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:perk_razor_focus"), 10, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.DODGE_CHANCE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.DODGE_CHANCE).addTransientModifier(modifier);
					}
				}
			} else {
				if (entity instanceof LivingEntity _entity) {
					_entity.getAttribute(WitchercraftModAttributes.DODGE_CHANCE).removeModifier(Identifier.parse("witchercraft:perk_razor_focus"));
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesPreciseBlows == true) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:perk_precise_blows"), 12, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.CRIT_CHANCE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.CRIT_CHANCE).addTransientModifier(modifier);
					}
				}
			} else {
				if (entity instanceof LivingEntity _entity) {
					_entity.getAttribute(WitchercraftModAttributes.CRIT_CHANCE).removeModifier(Identifier.parse("witchercraft:perk_precise_blows"));
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesPreciseBlows == true) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:perk_precise_blows"), 75, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.CRIT_DAMAGE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.CRIT_DAMAGE).addTransientModifier(modifier);
					}
				}
			} else {
				if (entity instanceof LivingEntity _entity) {
					_entity.getAttribute(WitchercraftModAttributes.CRIT_DAMAGE).removeModifier(Identifier.parse("witchercraft:perk_precise_blows"));
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesCrushingBlows == true) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:perk_crushing_blows"), 8, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.CRIT_CHANCE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.CRIT_CHANCE).addTransientModifier(modifier);
					}
				}
			} else {
				if (entity instanceof LivingEntity _entity) {
					_entity.getAttribute(WitchercraftModAttributes.CRIT_CHANCE).removeModifier(Identifier.parse("witchercraft:perk_crushing_blows"));
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesCrushingBlows == true) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:perk_crushing_blows"), 50, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.CRIT_DAMAGE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.CRIT_DAMAGE).addTransientModifier(modifier);
					}
				}
			} else {
				if (entity instanceof LivingEntity _entity) {
					_entity.getAttribute(WitchercraftModAttributes.CRIT_DAMAGE).removeModifier(Identifier.parse("witchercraft:perk_crushing_blows"));
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesAnatomicalKnowledge == true && ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Items.CROSSBOW || (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Items.BOW)) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:perk_anatomical_knowledge"), 10, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.CRIT_CHANCE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.CRIT_CHANCE).addTransientModifier(modifier);
					}
				}
			} else {
				if (entity instanceof LivingEntity _entity) {
					_entity.getAttribute(WitchercraftModAttributes.CRIT_CHANCE).removeModifier(Identifier.parse("witchercraft:perk_anatomical_knowledge"));
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesCripplingShot == true && ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Items.CROSSBOW || (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Items.BOW)) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:perk_crippling_shot"), 50, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.CRIT_DAMAGE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.CRIT_DAMAGE).addTransientModifier(modifier);
					}
				}
			} else {
				if (entity instanceof LivingEntity _entity) {
					_entity.getAttribute(WitchercraftModAttributes.CRIT_DAMAGE).removeModifier(Identifier.parse("witchercraft:perk_crippling_shot"));
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesStrengthTraining == true) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:perk_strength_training"), 10, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).addTransientModifier(modifier);
					}
				}
			} else {
				if (entity instanceof LivingEntity _entity) {
					_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).removeModifier(Identifier.parse("witchercraft:perk_strength_training"));
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesSunderArmor == true) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:perk_sunder_armor"), 20, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).addTransientModifier(modifier);
					}
				}
			} else {
				if (entity instanceof LivingEntity _entity) {
					_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).removeModifier(Identifier.parse("witchercraft:perk_sunder_armor"));
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesMuscleMemory == true) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:perk_muscle_memory"), 3, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).addTransientModifier(modifier);
					}
				}
			} else {
				if (entity instanceof LivingEntity _entity) {
					_entity.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).removeModifier(Identifier.parse("witchercraft:perk_muscle_memory"));
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesColdBlood == true && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEnemyNearby == false) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:perk_cold_blood"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).addTransientModifier(modifier);
					}
				}
			} else {
				if (entity instanceof LivingEntity _entity) {
					_entity.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).removeModifier(Identifier.parse("witchercraft:perk_cold_blood"));
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesFloodOfAnger == true && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEnemyNearby == true) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:perk_flood_of_anger"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).addTransientModifier(modifier);
					}
				}
			} else {
				if (entity instanceof LivingEntity _entity) {
					_entity.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).removeModifier(Identifier.parse("witchercraft:perk_flood_of_anger"));
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesSunAndStars == true && world instanceof Level _lvlD && _lvlD.isBrightOutside()) {
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
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesSunAndStars == true && !(world instanceof Level _lvlN && _lvlN.isBrightOutside())) {
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
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesGourmet == true) {
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
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesSurvivalInstinct == true) {
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
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesDefence == true) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:perk_defence"), 4, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(Attributes.MAX_HEALTH).hasModifier(modifier.id())) {
						_entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(modifier);
					}
				}
			} else {
				if (entity instanceof LivingEntity _entity) {
					_entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(Identifier.parse("witchercraft:perk_defence"));
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesFleetFooted == true) {
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
