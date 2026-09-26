package net.redboltmedia.witchercraft;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * HAND-MAINTAINED (locked code element, ~/Admin/EnemyNearby). The "monsters nearby" rule shared by
 * meditation and fast travel. It is exactly the vanilla bed rule: a monster within 8 blocks horizontally
 * and 5 vertically blocks the action when its {@link Monster#isPreventingPlayerRest} says so. That is true
 * for every monster except zombified piglins, which only count while angry at the player. Mods can
 * override it for their own monsters.
 */
public final class NearbyMonsters {
	public static final double HORIZONTAL_RANGE = 8.0;
	public static final double VERTICAL_RANGE = 5.0;

	private NearbyMonsters() {
	}

	/** True when a monster near {@code center} would stop {@code player} from sleeping there. */
	public static boolean preventRest(ServerLevel level, Player player, Vec3 center) {
		AABB box = new AABB(center.x - HORIZONTAL_RANGE, center.y - VERTICAL_RANGE, center.z - HORIZONTAL_RANGE,
			center.x + HORIZONTAL_RANGE, center.y + VERTICAL_RANGE, center.z + HORIZONTAL_RANGE);
		return !level.getEntitiesOfClass(Monster.class, box, monster -> monster.isAlive() && monster.isPreventingPlayerRest(level, player)).isEmpty();
	}

	/** The same area for a non-player entity: any living monster counts. */
	public static boolean anyMonster(ServerLevel level, Vec3 center) {
		AABB box = new AABB(center.x - HORIZONTAL_RANGE, center.y - VERTICAL_RANGE, center.z - HORIZONTAL_RANGE,
			center.x + HORIZONTAL_RANGE, center.y + VERTICAL_RANGE, center.z + HORIZONTAL_RANGE);
		return !level.getEntitiesOfClass(Monster.class, box, Entity::isAlive).isEmpty();
	}
}
