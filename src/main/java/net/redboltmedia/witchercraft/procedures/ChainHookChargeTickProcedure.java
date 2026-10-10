package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.ChainHookEntity;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;

public class ChainHookChargeTickProcedure {
	// NOTE: code-locked (locked_code=true). Called every tick while the Chain Hook is
	// being charged; spin sounds live in the ChainHookEntity code element.
	public static void execute(LevelAccessor world, Entity entity, ItemStack itemstack) {
		if (entity == null || world.isClientSide() || !(entity instanceof Player player))
			return;
		ChainHookEntity.chargeTick(player);
	}
}
