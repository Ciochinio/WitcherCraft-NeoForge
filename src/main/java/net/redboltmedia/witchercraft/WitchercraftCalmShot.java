/*
 * The code of this mod element is always locked.
 *
 * HAND-MAINTAINED (locked code element WitchercraftCalmShot, ~/Admin/Calc). Cold Blood (GDD 5): marks an arrow
 * fired by a player while no hostile is near, so the bonus is decided at the release, not when it lands (the
 * target itself, or a mob that walked up during the flight, must not cancel it). Blockly can't read a
 * projectile's owner on spawn, hence Java. WitchercraftArrowBonus reads the mark.
 */

package net.redboltmedia.witchercraft;

import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;

@EventBusSubscriber
public final class WitchercraftCalmShot {
	/** Arrow NBT, 1 = fired with no hostile within CALM_RANGE of the shooter. */
	public static final String CALM_SHOT = "witchercraftCalmShot";
	public static final double CALM_RANGE = 5.0;

	private WitchercraftCalmShot() {
	}

	@SubscribeEvent
	public static void onJoin(EntityJoinLevelEvent event) {
		if (event.loadedFromDisk() || !(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof AbstractArrow arrow) || !(arrow.getOwner() instanceof Player player))
			return;
		boolean hostileNear = !level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(CALM_RANGE),
				e -> e instanceof Enemy && e.isAlive() && e.distanceToSqr(player) <= CALM_RANGE * CALM_RANGE).isEmpty();
		if (!hostileNear)
			arrow.getPersistentData().putDouble(CALM_SHOT, 1);
	}
}
