package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.Mth;
import net.minecraft.tags.TagKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;

@EventBusSubscriber
public class QuenBlockProcedure {
	@SubscribeEvent
	public static void onEntityAttacked(LivingIncomingDamageEvent event) {
		if (event.getEntity() != null) {
			execute(event, event.getEntity().level(), event.getEntity(), event.getSource().getEntity(), event.getAmount(), event.getSource());
		}
	}

	public static void execute(LevelAccessor world, Entity entity, Entity sourceentity, double amount, DamageSource damagesource) {
		execute(null, world, entity, sourceentity, amount, damagesource);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, Entity entity, Entity sourceentity, double amount, DamageSource damagesource) {
		if (entity == null)
			return;
		double overflow = 0;
		if (((entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(WitchercraftModMobEffects.QUEN_EFFECT))
				|| (entity instanceof LivingEntity _livEntShield && _livEntShield.hasEffect(WitchercraftModMobEffects.QUEN_ACTIVE_SHIELD)))
				&& entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftQuenShield > 0
				&& !(damagesource.is(TagKey.create(Registries.DAMAGE_TYPE, Identifier.parse("minecraft:is_fall")))
						|| damagesource.is(TagKey.create(Registries.DAMAGE_TYPE, Identifier.parse("minecraft:is_freezing")))
						|| damagesource.is(TagKey.create(Registries.DAMAGE_TYPE, Identifier.parse("minecraft:is_drowning")))
						|| damagesource.is(TagKey.create(Registries.DAMAGE_TYPE, Identifier.parse("minecraft:bypasses_invulnerability")))
						|| damagesource.is(DamageTypes.STARVE))) {
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftQuenShield >= amount) {
				// fully absorbed
				if (event instanceof ICancellableEvent _cancellable) {
					_cancellable.setCanceled(true);
				}
				{
					WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
					_vars.witchercraftQuenShield = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftQuenShield - amount;
					_vars.markSyncDirty();
				}
				if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
					_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.DAMAGE_BLOCKED, 3, 0));
				if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftQuenShield <= 0) {
					if (entity instanceof LivingEntity _entity)
						_entity.removeEffect(WitchercraftModMobEffects.QUEN_EFFECT);
				}
				if (sourceentity instanceof Mob || sourceentity instanceof Player) {
					entity.push(Mth.clamp((entity.getX() - sourceentity.getX()) * 0.4, -0.5, 0.5), 0.15, Mth.clamp((entity.getZ() - sourceentity.getZ()) * 0.4, -0.5, 0.5));
					if (world instanceof Level _level && !_level.isClientSide()) {
						_level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("minecraft:item.shield.block")),
								SoundSource.PLAYERS, 1, 1);
					}
				}
			} else {
				// partial - shield breaks, overflow leaks through with natural feedback
				if (event instanceof ICancellableEvent _cancellable) {
					_cancellable.setCanceled(true);
				}
				overflow = amount - entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftQuenShield;
				{
					WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
					_vars.witchercraftQuenShield = 0;
					_vars.markSyncDirty();
				}
				if (entity instanceof LivingEntity _entity)
					_entity.removeEffect(WitchercraftModMobEffects.QUEN_EFFECT);
				if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
					_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.DAMAGE_BLOCKED, 3, 0));
				{
					Entity _ent = entity;
					if (_ent.level() instanceof ServerLevel _serverLevel) {
						_ent.hurtServer(_serverLevel, damagesource, (float) overflow);
					}
				}
			}
		}
	}
}
