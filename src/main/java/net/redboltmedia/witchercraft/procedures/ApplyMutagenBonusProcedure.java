package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.MutagenEffects;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;

/**
 * HAND-MAINTAINED (locked_code procedure, ~/Character Abilities/Perk Calc).
 * Re-applies the equipped mutagens' effects. RecomputeEquippedPerks calls it
 * from Blockly; the work happens in {@link MutagenEffects#recompute}, which runs
 * one Mutagen<Type>Effect Blockly procedure per filled mutagen slot.
 *
 * The Blockly stub only keeps the element's entity dependency; it is never
 * regenerated into this file.
 */
public class ApplyMutagenBonusProcedure {
	public static void execute(Entity entity) {
		if (entity instanceof Player player)
			MutagenEffects.recompute(player);
	}
}
