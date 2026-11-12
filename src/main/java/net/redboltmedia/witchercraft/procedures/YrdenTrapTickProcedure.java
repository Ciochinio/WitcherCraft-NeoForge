package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;

import javax.annotation.Nullable;

import java.util.Comparator;

@EventBusSubscriber
public class YrdenTrapTickProcedure {
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
		if (!world.isClientSide() && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftYrdenTicks > 0) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftYrdenTicks = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftYrdenTicks - 1;
				_vars.markSyncDirty();
			}
			double tx = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftYrdenX;
			double ty = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftYrdenY;
			double tz = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftYrdenZ;
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftYrdenMode == 0) {
				{
					final Vec3 _center = new Vec3(tx, (ty - 1), tz);
					for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(12 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
							.toList()) {
						if (!(entityiterator instanceof Player) && !(entityiterator instanceof TamableAnimal _tamIsTamedBy && entity instanceof LivingEntity _livEnt && _tamIsTamedBy.isOwnedBy(_livEnt))
								&& Math.sqrt((entityiterator.getX() - tx) * (entityiterator.getX() - tx) + (entityiterator.getZ() - tz) * (entityiterator.getZ() - tz)) <= 3.5 && entityiterator.getY() >= ty - 7
								&& entityiterator.getY() <= ty + 5) {
							if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide())
								_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.YRDEN_TRAPPED, 20, 0, false, false));
						}
					}
				}
			} else {
				if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftYrdenTicks % 40 < 1) {
					WitchercraftYrdenTurretShootProcedure.execute(entity);
				}
			}
		}
	}
}
