package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.entity.GhoulEntity;
import net.minecraft.world.entity.Entity;

public class GhoulAttackTickProcedure {
	public static void execute(Entity entity) {
		if (!(entity instanceof GhoulEntity ghoul) || ghoul.level().isClientSide())
			return;

		boolean swinging = ghoul.swinging;
		boolean wasSwinging = ghoul.getEntityData().get(GhoulEntity.DATA_WasSwinging);
		if (swinging && !wasSwinging) {
			ghoul.getEntityData().set(GhoulEntity.DATA_AttackAnimation, 1);
			ghoul.getEntityData().set(GhoulEntity.DATA_AttackTicks, 20);
		}

		int attackTicks = ghoul.getEntityData().get(GhoulEntity.DATA_AttackTicks);
		if (attackTicks > 0) {
			attackTicks--;
			ghoul.getEntityData().set(GhoulEntity.DATA_AttackTicks, attackTicks);
			if (attackTicks == 0)
				ghoul.getEntityData().set(GhoulEntity.DATA_AttackAnimation, 0);
		}
		ghoul.getEntityData().set(GhoulEntity.DATA_WasSwinging, swinging);
	}
}
