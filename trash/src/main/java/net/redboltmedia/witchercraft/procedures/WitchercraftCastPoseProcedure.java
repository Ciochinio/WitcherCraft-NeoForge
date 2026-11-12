package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.WitchercraftCastPoseMessage;

import net.minecraft.world.entity.Entity;

// Locked: entry point from Blockly into WitchercraftCastPoseMessage (off-hand arm out for `ticks`).
public class WitchercraftCastPoseProcedure {
	public static void execute(Entity entity, double ticks) {
		if (entity == null)
			return;
		WitchercraftCastPoseMessage.send(entity, (int) ticks);
	}
}
