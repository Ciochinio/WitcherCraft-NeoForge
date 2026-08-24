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
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Holder;

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

	private static void sync(Entity entity, boolean active, String id, Holder<Attribute> attribute, double amount) {
		sync(entity, active, id, attribute, amount, AttributeModifier.Operation.ADD_VALUE);
	}

	private static void sync(Entity entity, boolean active, String id, Holder<Attribute> attribute, double amount, AttributeModifier.Operation op) {
		if (!(entity instanceof LivingEntity _entity) || _entity.getAttribute(attribute) == null)
			return;
		if (active) {
			AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:" + id), amount, op);
			if (!_entity.getAttribute(attribute).hasModifier(modifier.id()))
				_entity.getAttribute(attribute).addTransientModifier(modifier);
		} else {
			_entity.getAttribute(attribute).removeModifier(Identifier.parse("witchercraft:" + id));
		}
	}

	private static void execute(@Nullable Event event, LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		if (world.isClientSide())
			return;
		WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
		ItemStack _hand = entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY;
		boolean bowHeld = _hand.getItem() == Items.CROSSBOW || _hand.getItem() == Items.BOW;
		sync(entity, _vars.witchercraftAbilitiesDeadlyPrecision, "perk_deadly_precision", WitchercraftModAttributes.INSTANT_KILL_CHANCE, 1);
		sync(entity, _vars.witchercraftAbilitiesGriffinSchool, "perk_griffin_school", WitchercraftModAttributes.POTION_DURATION, 20);
		sync(entity, _vars.witchercraftAbilitiesRazorFocus, "perk_razor_focus", WitchercraftModAttributes.DODGE_CHANCE, 10);
		sync(entity, _vars.witchercraftAbilitiesPreciseBlows, "perk_precise_blows", WitchercraftModAttributes.CRIT_CHANCE, 12);
		sync(entity, _vars.witchercraftAbilitiesPreciseBlows, "perk_precise_blows", WitchercraftModAttributes.CRIT_DAMAGE, 75);
		sync(entity, _vars.witchercraftAbilitiesCrushingBlows, "perk_crushing_blows", WitchercraftModAttributes.CRIT_CHANCE, 8);
		sync(entity, _vars.witchercraftAbilitiesCrushingBlows, "perk_crushing_blows", WitchercraftModAttributes.CRIT_DAMAGE, 50);
		sync(entity, _vars.witchercraftAbilitiesAnatomicalKnowledge && bowHeld, "perk_anatomical_knowledge", WitchercraftModAttributes.CRIT_CHANCE, 10);
		sync(entity, _vars.witchercraftAbilitiesCripplingShot && bowHeld, "perk_crippling_shot", WitchercraftModAttributes.CRIT_DAMAGE, 50);
		sync(entity, _vars.witchercraftAbilitiesStrengthTraining, "perk_strength_training", WitchercraftModAttributes.INCREASED_DAMAGE, 10);
		sync(entity, _vars.witchercraftAbilitiesSunderArmor, "perk_sunder_armor", WitchercraftModAttributes.INCREASED_DAMAGE, 20);
		sync(entity, _vars.witchercraftAbilitiesMuscleMemory, "perk_muscle_memory", WitchercraftModAttributes.ADDITIONAL_DAMAGE, 3);
		sync(entity, _vars.witchercraftAbilitiesColdBlood && !_vars.witchercraftEnemyNearby, "perk_cold_blood", WitchercraftModAttributes.ADDITIONAL_DAMAGE, 5);
		sync(entity, _vars.witchercraftAbilitiesFloodOfAnger && _vars.witchercraftEnemyNearby, "perk_flood_of_anger", WitchercraftModAttributes.ADDITIONAL_DAMAGE, 5);
		boolean bright = world instanceof Level _lvlD && _lvlD.isBrightOutside();
		sync(entity, _vars.witchercraftAbilitiesSunAndStars && bright, "perk_sun_and_stars_day", WitchercraftModAttributes.PASSIVE_HEALTH_REGEN, 0.3333);
		sync(entity, _vars.witchercraftAbilitiesSunAndStars && !bright, "perk_sun_and_stars_night", WitchercraftModAttributes.PASSIVE_STAMINA_REGEN, 0.3333);
		sync(entity, _vars.witchercraftAbilitiesGourmet, "perk_gourmet", WitchercraftModAttributes.PASSIVE_STAMINA_REGEN, 0.3333);
	}
}
