package net.redboltmedia.witchercraft.item;

import net.redboltmedia.witchercraft.procedures.ChainHookUseProcedure;
import net.redboltmedia.witchercraft.procedures.ChainHookReleaseProcedure;
import net.redboltmedia.witchercraft.procedures.ChainHookChargeTickProcedure;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;

public class ChainHookItem extends Item {
	public ChainHookItem(Item.Properties properties) {
		super(properties.durability(128));
	}

	@Override
	public int getUseDuration(ItemStack itemstack, LivingEntity livingEntity) {
		return 72000;
	}

	@Override
	public InteractionResult use(Level world, Player entity, InteractionHand hand) {
		InteractionResult ar = super.use(world, entity, hand);
		entity.startUsingItem(hand);
		ChainHookUseProcedure.execute(world, entity, entity.getItemInHand(hand));
		return ar;
	}

	@Override
	public boolean releaseUsing(ItemStack itemstack, Level world, LivingEntity entity, int time) {
		ChainHookReleaseProcedure.execute(world, entity, itemstack);
		return super.releaseUsing(itemstack, world, entity, time);
	}

	@Override
	public void onUseTick(Level world, LivingEntity entity, ItemStack itemstack, int time) {
		ChainHookChargeTickProcedure.execute(world, entity, itemstack);
	}
}
