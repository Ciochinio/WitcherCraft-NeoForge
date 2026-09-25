package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModBlocks;

import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.BlockPos;

public class FastTravelSignNeighbourChangedProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, BlockState blockstate) {
		if (!(getPropertyByName(blockstate, "upper") instanceof BooleanProperty _getbp1 && blockstate.getValue(_getbp1))
				&& !((world.getBlockState(BlockPos.containing(x, y + 1, z))) == (WitchercraftModBlocks.FAST_TRAVEL_SIGN.get().getStateDefinition().getProperty("upper") instanceof BooleanProperty _withbp2
						? WitchercraftModBlocks.FAST_TRAVEL_SIGN.get().defaultBlockState().setValue(_withbp2, true)
						: WitchercraftModBlocks.FAST_TRAVEL_SIGN.get().defaultBlockState()))) {
			world.setBlock(BlockPos.containing(x, y, z), Blocks.AIR.defaultBlockState(), 3);
			FastTravelSignRemoveProcedure.execute(world, x, y, z);
		}
	}

	private static Property<?> getPropertyByName(BlockState state, String name) {
		return state.getBlock().getStateDefinition().getProperty(name);
	}
}
