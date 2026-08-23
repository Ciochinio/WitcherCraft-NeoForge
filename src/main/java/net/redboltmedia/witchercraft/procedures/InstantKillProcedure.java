package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;
import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

import javax.annotation.Nullable;

@EventBusSubscriber
public class InstantKillProcedure {
	@SubscribeEvent
	public static void onEntityAttacked(LivingIncomingDamageEvent event) {
		if (event.getEntity() != null) {
			execute(event, event.getEntity().level(), event.getSource(), event.getEntity(), event.getSource().getEntity());
		}
	}

	public static void execute(LevelAccessor world, DamageSource damagesource, Entity entity, Entity sourceentity) {
		execute(null, world, damagesource, entity, sourceentity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, DamageSource damagesource, Entity entity, Entity sourceentity) {
		if (damagesource == null || entity == null || sourceentity == null)
			return;
		double instantKillRoll = 0;
		instantKillRoll = Mth.nextInt(RandomSource.create(), 1, 100);
		if (sourceentity instanceof LivingEntity _livEnt1 && _livEnt1.hasEffect(WitchercraftModMobEffects.DEV_LOG)) {
			if (sourceentity instanceof ServerPlayer _player)
				_player.sendSystemMessage(Component.literal(("instant kill roll>>>" + instantKillRoll)), false);
			if (sourceentity instanceof ServerPlayer _player)
				_player.sendSystemMessage(Component.literal(("instant kill chance>>>" + (sourceentity instanceof LivingEntity _livingEntityIK && _livingEntityIK.getAttributes().hasAttribute(WitchercraftModAttributes.INSTANT_KILL_CHANCE) ? _livingEntityIK.getAttribute(WitchercraftModAttributes.INSTANT_KILL_CHANCE).getValue() : 0))), false);
		}
		if (!entity.is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("minecraft:enderdragon"))) && instantKillRoll <= (sourceentity instanceof LivingEntity _livingEntityIK && _livingEntityIK.getAttributes().hasAttribute(WitchercraftModAttributes.INSTANT_KILL_CHANCE) ? _livingEntityIK.getAttribute(WitchercraftModAttributes.INSTANT_KILL_CHANCE).getValue() : 0)) {
			if (!damagesource.is(DamageTypes.ARROW)) {
				if (sourceentity instanceof LivingEntity _livEnt6 && _livEnt6.hasEffect(WitchercraftModMobEffects.DEV_LOG)) {
					if (sourceentity instanceof ServerPlayer _player)
						_player.sendSystemMessage(Component.literal("BOMBA"), false);
				}
				{
					Entity _ent = entity;
					if (_ent.level() instanceof ServerLevel _serverLevel) {
						_ent.hurtServer(_serverLevel, new DamageSource(world.holderOrThrow(DamageTypes.GENERIC)), 999);
					}
				}
			}
		}
	}
}