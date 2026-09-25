package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModBlocks;

import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;

public class FastTravelSignCanSurviveProcedure {
	public static boolean execute(LevelAccessor world, double x, double y, double z, BlockState blockstate) {
		if (getPropertyByName(blockstate, "upper") instanceof BooleanProperty _getbp1 && blockstate.getValue(_getbp1)) {
			return (world.getBlockState(BlockPos.containing(x, y - 1, z))) == (WitchercraftModBlocks.FAST_TRAVEL_SIGN.get().getStateDefinition().getProperty("upper") instanceof BooleanProperty _withbp2
					? WitchercraftModBlocks.FAST_TRAVEL_SIGN.get().defaultBlockState().setValue(_withbp2, false)
					: WitchercraftModBlocks.FAST_TRAVEL_SIGN.get().defaultBlockState());
		}
		return (world instanceof Level _lvl ? _lvl.dimension() : (world instanceof WorldGenLevel _wgl ? _wgl.getLevel().dimension() : Level.OVERWORLD)) == Level.OVERWORLD
				&& (world.isEmptyBlock(BlockPos.containing(x, y + 1, z))
						|| (world.getBlockState(BlockPos.containing(x, y + 1, z))) == (WitchercraftModBlocks.FAST_TRAVEL_SIGN.get().getStateDefinition().getProperty("upper") instanceof BooleanProperty _withbp3
								? WitchercraftModBlocks.FAST_TRAVEL_SIGN.get().defaultBlockState().setValue(_withbp3, true)
								: WitchercraftModBlocks.FAST_TRAVEL_SIGN.get().defaultBlockState()));
	}

	private static Property<?> getPropertyByName(BlockState state, String name) {
		return state.getBlock().getStateDefinition().getProperty(name);
	}
}
