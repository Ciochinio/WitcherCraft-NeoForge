package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

public class MeditationGuiTimeNow6Procedure {
	public static boolean execute(LevelAccessor world) {
		if ((world instanceof Level _level0 ? _level0.getDefaultClockTime() : 0) >= 23500 && (world instanceof Level _level1 ? _level1.getDefaultClockTime() : 0) <= 23999
				|| (world instanceof Level _level2 ? _level2.getDefaultClockTime() : 0) >= 1 && (world instanceof Level _level3 ? _level3.getDefaultClockTime() : 0) <= 500) {
			return true;
		}
		return false;
	}
}