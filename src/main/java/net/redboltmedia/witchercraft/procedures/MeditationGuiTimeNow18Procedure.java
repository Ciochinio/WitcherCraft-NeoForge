package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

public class MeditationGuiTimeNow18Procedure {
	public static boolean execute(LevelAccessor world) {
		if ((world instanceof Level _level0 ? _level0.getDefaultClockTime() : 0) >= 11500 && (world instanceof Level _level1 ? _level1.getDefaultClockTime() : 0) <= 12500) {
			return true;
		}
		return false;
	}
}