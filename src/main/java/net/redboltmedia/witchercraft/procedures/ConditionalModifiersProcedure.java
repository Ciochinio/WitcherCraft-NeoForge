package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Holder;

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
		boolean thunderboltStorm = entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(WitchercraftModMobEffects.THUNDERBOLT_EFFECT) && world instanceof Level _lvl0 && _lvl0.isThundering();
		sync(entity, thunderboltStorm, "effect_thunderbolt_storm", WitchercraftModAttributes.CRIT_CHANCE, 100);
		boolean waterHagFull = entity instanceof LivingEntity _livEnt1 && _livEnt1.hasEffect(WitchercraftModMobEffects.WATER_HAG_DECOCTION_EFFECT)
				&& (entity instanceof LivingEntity _livHp ? _livHp.getHealth() : -1) == (entity instanceof LivingEntity _livHpM ? _livHpM.getMaxHealth() : -1);
		sync(entity, waterHagFull, "effect_waterhag_fullhp", WitchercraftModAttributes.INCREASED_DAMAGE, 40);
		boolean nekkerRiding = entity instanceof LivingEntity _livEnt2 && _livEnt2.hasEffect(WitchercraftModMobEffects.NEKKER_WARRIOR_DECOCTION_EFFECT) && entity.isPassenger();
		sync(entity, nekkerRiding, "effect_nekker_riding", WitchercraftModAttributes.INCREASED_DAMAGE, 50);
		boolean werewolfNight = entity instanceof LivingEntity _livEnt3 && _livEnt3.hasEffect(WitchercraftModMobEffects.WEREWOLF_DECOCTION_EFFECT) && !(world instanceof Level _lvlR && _lvlR.isRaining())
				&& !(world instanceof Level _lvlB && _lvlB.isBrightOutside());
		sync(entity, werewolfNight, "effect_werewolf_night", WitchercraftModAttributes.PASSIVE_STAMINA_REGEN, 0.3333);
		boolean inCombat = entity instanceof LivingEntity _livEnt4 && _livEnt4.hasEffect(WitchercraftModMobEffects.IN_COMBAT);
		sync(entity, inCombat, "combat_regen_penalty", WitchercraftModAttributes.PASSIVE_HEALTH_REGEN, -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		sync(entity, inCombat, "combat_regen_penalty", WitchercraftModAttributes.PASSIVE_STAMINA_REGEN, -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
	}
}
