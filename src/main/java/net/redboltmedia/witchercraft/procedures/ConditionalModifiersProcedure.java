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
		if (!(entity instanceof LivingEntity _entity) || _entity.getAttribute(attribute) == null)
			return;
		if (active) {
			AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:" + id), amount, AttributeModifier.Operation.ADD_VALUE);
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
	}
}
