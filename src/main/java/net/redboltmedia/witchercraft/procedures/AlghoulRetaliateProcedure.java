package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.entity.AlghoulEntity;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;

public class AlghoulRetaliateProcedure {
	public static void execute(LevelAccessor world, Entity entity, Entity sourceentity) {
		if (entity == null || sourceentity == null)
			return;
		if (entity instanceof AlghoulEntity _datEntL0 && _datEntL0.getEntityData().get(AlghoulEntity.DATA_SpikesOut) && sourceentity.isAlive()) {
			{
				Entity _ent = sourceentity;
				if (_ent.level() instanceof ServerLevel _serverLevel) {
					_ent.hurtServer(_serverLevel, new DamageSource(world.holderOrThrow(DamageTypes.THORNS), entity), 2);
				}
			}
		}
	}
}