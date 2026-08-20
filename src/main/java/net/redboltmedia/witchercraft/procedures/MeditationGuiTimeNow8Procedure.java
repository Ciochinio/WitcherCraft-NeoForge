package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

public class MeditationGuiTimeNow8Procedure {
	public static boolean execute(LevelAccessor world) {
		if ((world instanceof Level _level0 ? _level0.getDefaultClockTime() : 0) >= 1500 && (world instanceof Level _level1 ? _level1.getDefaultClockTime() : 0) <= 2500) {
			return true;
		}
		return false;
	}
}