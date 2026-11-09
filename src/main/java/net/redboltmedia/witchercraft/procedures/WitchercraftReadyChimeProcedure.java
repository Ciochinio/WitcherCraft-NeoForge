package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;

public class WitchercraftReadyChimeProcedure {
	public static void execute(LevelAccessor world, Entity sourceentity) {
		if (sourceentity == null)
			return;
		if (world instanceof Level _level) {
			if (!_level.isClientSide()) {
				_level.playSound(null, BlockPos.containing(sourceentity.getX(), sourceentity.getY(), sourceentity.getZ()), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("block.amethyst_block.chime")), SoundSource.PLAYERS, (float) 0.8, (float) 1.6);
			} else {
				_level.playLocalSound((sourceentity.getX()), (sourceentity.getY()), (sourceentity.getZ()), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("block.amethyst_block.chime")), SoundSource.PLAYERS, (float) 0.8, (float) 1.6, false);
			}
		}
	}
}