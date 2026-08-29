package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.level.LevelAccessor;

/**
 * HAND-MAINTAINED (locked_code procedure, ~/Meditation2). Cancels the active
 * meditation session: clears the session state so MeditationTick stops advancing
 * the clock. The world clock is left wherever the spin had reached - stopping
 * meditation keeps the time you have already passed, it does not rewind.
 *
 * Called when the initiator cancels (the Cancel button, or closing the GUI mid
 * spin) via MeditationGuiButtonMessage buttonID 2000.
 */
public class MeditationStopProcedure {
	public static void execute(LevelAccessor world) {
		WitchercraftModVariables.meditationState = 0;
	}
}
