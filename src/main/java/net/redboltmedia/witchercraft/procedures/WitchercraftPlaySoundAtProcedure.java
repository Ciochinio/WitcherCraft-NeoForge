package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;

// Locked: MCreator's play-sound block snaps to the block centre (BlockPos), which puts a sound cast on
// yourself up to 0.7 blocks to one side, so it pans left or right depending on where you face.
public class WitchercraftPlaySoundAtProcedure {
	public static void execute(Entity entity, String sound, double pitch) {
		if (entity == null || sound == null)
			return;
		Level level = entity.level();
		if (!level.isClientSide())
			level.playSound(null, entity.getX(), entity.getY() + 1, entity.getZ(), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse(sound)), SoundSource.PLAYERS, 1, (float) pitch);
	}
}
