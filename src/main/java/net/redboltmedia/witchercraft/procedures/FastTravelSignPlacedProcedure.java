package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModBlocks;

import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;

public class FastTravelSignPlacedProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, BlockState blockstate, Entity entity) {
		if (entity == null)
			return;
		if (!(getPropertyByName(blockstate, "upper") instanceof BooleanProperty _getbp1 && blockstate.getValue(_getbp1))) {
			if (FastTravelSignRegisterProcedure.execute(world, x, y, z, entity)) {
				world.setBlock(BlockPos.containing(x, y + 1, z), (WitchercraftModBlocks.FAST_TRAVEL_SIGN.get().getStateDefinition().getProperty("upper") instanceof BooleanProperty _withbp2
						? WitchercraftModBlocks.FAST_TRAVEL_SIGN.get().defaultBlockState().setValue(_withbp2, true)
						: WitchercraftModBlocks.FAST_TRAVEL_SIGN.get().defaultBlockState()), 3);
			} else {
				world.setBlock(BlockPos.containing(x, y, z), Blocks.AIR.defaultBlockState(), 3);
				if (!(entity instanceof Player _plr && _plr.getAbilities().instabuild)) {
					if (entity instanceof Player _player) {
						ItemStack _setstack = new ItemStack(WitchercraftModBlocks.FAST_TRAVEL_SIGN.get()).copy();
						_setstack.setCount(1);
						_player.getInventory().placeItemBackInInventory(_setstack);
					}
				}
			}
		}
	}

	private static Property<?> getPropertyByName(BlockState state, String name) {
		return state.getBlock().getStateDefinition().getProperty(name);
	}
}
