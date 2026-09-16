package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.entity.AlghoulEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class AlghoulCombatTickProcedure {
	public static void execute(Entity entity) {
		if (!(entity instanceof AlghoulEntity alghoul) || alghoul.level().isClientSide())
			return;

		LivingEntity target = alghoul.getTarget();
		boolean inCombat = target != null && target.isAlive();
		boolean spikesSuppressed = alghoul.hasEffect(MobEffects.NIGHT_VISION);
		boolean spikesOut = alghoul.getEntityData().get(AlghoulEntity.DATA_SpikesOut);
		int combatTicks = alghoul.getEntityData().get(AlghoulEntity.DATA_CombatTicks);
		int stanceAnimation = alghoul.getEntityData().get(AlghoulEntity.DATA_StanceAnimation);

		if (spikesSuppressed) {
			alghoul.getEntityData().set(AlghoulEntity.DATA_CombatTicks, 0);
			if (spikesOut || stanceAnimation == 1) {
				alghoul.getEntityData().set(AlghoulEntity.DATA_SpikesOut, false);
				alghoul.getEntityData().set(AlghoulEntity.DATA_StanceAnimation, 2);
				alghoul.getEntityData().set(AlghoulEntity.DATA_StanceTicks, 15);
				alghoul.getEntityData().set(AlghoulEntity.DATA_AttackAnimation, 0);
				alghoul.getEntityData().set(AlghoulEntity.DATA_AttackTicks, 0);
			}
		} else if (inCombat && !spikesOut && stanceAnimation == 0) {
			if (combatTicks < 50) {
				alghoul.getEntityData().set(AlghoulEntity.DATA_CombatTicks, combatTicks + 1);
			} else {
				alghoul.getEntityData().set(AlghoulEntity.DATA_StanceAnimation, 1);
				alghoul.getEntityData().set(AlghoulEntity.DATA_StanceTicks, 15);
				alghoul.getEntityData().set(AlghoulEntity.DATA_AttackAnimation, 0);
				alghoul.getEntityData().set(AlghoulEntity.DATA_AttackTicks, 0);
			}
		} else if (!inCombat) {
			alghoul.getEntityData().set(AlghoulEntity.DATA_CombatTicks, 0);
		}
		if (!spikesSuppressed && !inCombat && (spikesOut || stanceAnimation == 1)) {
			alghoul.getEntityData().set(AlghoulEntity.DATA_SpikesOut, false);
			alghoul.getEntityData().set(AlghoulEntity.DATA_StanceAnimation, 2);
			alghoul.getEntityData().set(AlghoulEntity.DATA_StanceTicks, 15);
			alghoul.getEntityData().set(AlghoulEntity.DATA_AttackAnimation, 0);
			alghoul.getEntityData().set(AlghoulEntity.DATA_AttackTicks, 0);
		}

		int stanceTicks = alghoul.getEntityData().get(AlghoulEntity.DATA_StanceTicks);
		if (stanceTicks > 0) {
			stanceTicks--;
			alghoul.getEntityData().set(AlghoulEntity.DATA_StanceTicks, stanceTicks);
			if (stanceTicks == 0) {
				if (alghoul.getEntityData().get(AlghoulEntity.DATA_StanceAnimation) == 1 && inCombat && !spikesSuppressed)
					alghoul.getEntityData().set(AlghoulEntity.DATA_SpikesOut, true);
				alghoul.getEntityData().set(AlghoulEntity.DATA_StanceAnimation, 0);
			}
		}

		boolean swinging = alghoul.swinging;
		boolean wasSwinging = alghoul.getEntityData().get(AlghoulEntity.DATA_WasSwinging);
		if (swinging && !wasSwinging && alghoul.getEntityData().get(AlghoulEntity.DATA_StanceAnimation) == 0) {
			alghoul.getEntityData().set(AlghoulEntity.DATA_AttackAnimation, 1);
			alghoul.getEntityData().set(AlghoulEntity.DATA_AttackTicks, 20);
		}
		int attackTicks = alghoul.getEntityData().get(AlghoulEntity.DATA_AttackTicks);
		if (attackTicks > 0) {
			attackTicks--;
			alghoul.getEntityData().set(AlghoulEntity.DATA_AttackTicks, attackTicks);
			if (attackTicks == 0)
				alghoul.getEntityData().set(AlghoulEntity.DATA_AttackAnimation, 0);
		}
		alghoul.getEntityData().set(AlghoulEntity.DATA_WasSwinging, swinging);
	}
}
