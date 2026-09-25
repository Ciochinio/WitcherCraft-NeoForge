package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModBlocks;

import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

public class FastTravelSignExplodedProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		if ((world.getBlockState(BlockPos.containing(x, y - 1, z))) == (WitchercraftModBlocks.FAST_TRAVEL_SIGN.get().getStateDefinition().getProperty("upper") instanceof BooleanProperty _withbp1
				? WitchercraftModBlocks.FAST_TRAVEL_SIGN.get().defaultBlockState().setValue(_withbp1, false)
				: WitchercraftModBlocks.FAST_TRAVEL_SIGN.get().defaultBlockState())) {
			world.setBlock(BlockPos.containing(x, y - 1, z), Blocks.AIR.defaultBlockState(), 3);
		}
		if (FastTravelSignRemoveProcedure.execute(world, x, y, z)) {
			if (FastTravelSignDropsItemProcedure.execute()) {
				if (world instanceof ServerLevel _level) {
					ItemEntity entityToSpawn = new ItemEntity(_level, (x + 0.5), (y + 0.5), (z + 0.5), new ItemStack(WitchercraftModBlocks.FAST_TRAVEL_SIGN.get()));
					entityToSpawn.setPickUpDelay(10);
					_level.addFreshEntity(entityToSpawn);
				}
			}
		}
	}
}
