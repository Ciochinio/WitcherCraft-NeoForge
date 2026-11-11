package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;

@EventBusSubscriber
public class WitchercraftOnShieldBlockProcedure {
	@SubscribeEvent
	public static void whenEntityBlocksWithShield(LivingShieldBlockEvent event) {
		if (event.getEntity() != null) {
			execute(event, event.getEntity().level(), event.getEntity(), event.getDamageSource().getDirectEntity(), event.getDamageSource().getEntity());
		}
	}

	public static void execute(LevelAccessor world, Entity entity, Entity immediatesourceentity, Entity sourceentity) {
		execute(null, world, entity, immediatesourceentity, sourceentity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, Entity entity, Entity immediatesourceentity, Entity sourceentity) {
		if (entity == null || immediatesourceentity == null || sourceentity == null)
			return;
		if (entity instanceof Player && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkCounterattack
				&& (entity.getPersistentData().getDoubleOr("witchercraftPerfectParryTick", 0) == entity.tickCount && sourceentity == immediatesourceentity)) {
			if (!(entity.getPersistentData().getDoubleOr("witchercraftCounterattackTick", 0) > 0 && (entity.tickCount - entity.getPersistentData().getDoubleOr("witchercraftCounterattackTick", 0) >= 0
					&& entity.tickCount - entity.getPersistentData().getDoubleOr("witchercraftCounterattackTick", 0) <= 60))) {
				if (world instanceof Level _level) {
					if (!_level.isClientSide()) {
						_level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("block.amethyst_block.chime")), SoundSource.PLAYERS, (float) 0.8, (float) 1.6);
					} else {
						_level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("block.amethyst_block.chime")), SoundSource.PLAYERS, (float) 0.8, (float) 1.6, false);
					}
				}
			}
			entity.getPersistentData().putDouble("witchercraftCounterattackTick", (entity.tickCount));
		}
	}
}