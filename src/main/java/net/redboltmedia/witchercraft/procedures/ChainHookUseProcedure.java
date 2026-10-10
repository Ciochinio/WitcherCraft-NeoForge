package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.ChainHookEntity;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;

public class ChainHookUseProcedure {
	// NOTE: code-locked (locked_code=true). The Chain Hook item's right-click trigger
	// calls this on both sides; all hook logic lives in the ChainHookEntity code element.
	public static void execute(LevelAccessor world, Entity entity, ItemStack itemstack) {
		if (entity == null || !(entity instanceof Player player))
			return;
		InteractionHand hand = player.getMainHandItem() == itemstack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		ChainHookEntity.use(player, hand);
	}
}
