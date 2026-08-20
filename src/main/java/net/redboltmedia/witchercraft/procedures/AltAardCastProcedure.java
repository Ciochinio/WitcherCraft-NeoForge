package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.arguments.EntityAnchorArgument;

import java.util.Comparator;

public class AltAardCastProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		double knockback = 0;
		double DifX = 0;
		double DifZ = 0;
		double DifY = 0;
		double root = 0;
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal("ALT AARD"), false);
		knockback = -2;
		{
			final Vec3 _center = new Vec3(x, y, z);
			for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(10 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList()) {
				if (entityiterator.is(EntityTypeTags.UNDEAD)) {
					entityiterator.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())));
					entityiterator.setDeltaMovement(new Vec3((Math.sin(entity.getYRot() * (-1) * 0.017453292) * Math.cos(entity.getXRot() * 0.017453292) * knockback), (Math.sin(entity.getXRot() * 0.17453292 * (-1)) * knockback),
							(Math.sin(entity.getYRot() * (-1) * 0.017453292) * Math.cos(entity.getXRot() * 0.017453292) * knockback)));
				}
			}
		}
	}
}