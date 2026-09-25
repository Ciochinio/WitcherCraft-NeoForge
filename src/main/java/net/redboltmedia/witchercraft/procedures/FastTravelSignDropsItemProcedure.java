package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.FastTravelSigns;

/**
 * HAND-MAINTAINED (locked_code procedure, ~/World Map/Fast Travel). The "Sign drops as item"
 * world setting, for the signpost's Blockly destroy procedures.
 */
public class FastTravelSignDropsItemProcedure {
	public static boolean execute() {
		return FastTravelSigns.dropsItem();
	}
}
