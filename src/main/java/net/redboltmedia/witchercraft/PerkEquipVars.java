package net.redboltmedia.witchercraft;

import net.minecraft.world.entity.player.Player;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

/**
 * Read/write helpers for the 12 perk-socket player vars. (Mutagens are items in
 * {@link MutagenSlots}; MUTAGEN_GROUPS is the number of mutagen sockets.)
 *
 * Reads are safe on either side (vars sync to the client). Writes must run
 * server-side; they mark the var bundle dirty so the next player tick syncs it
 * down (same pattern the generated buy procedures use). Slot indices are
 * 0-based here; the underlying vars are 1-based (witchercraftPerkSocket1..12).
 * Mutagen socket i covers perk slots 3i..3i+2.
 */
public final class PerkEquipVars {
	private PerkEquipVars() {
	}

	public static final int PERK_SLOTS = 12;
	public static final int MUTAGEN_GROUPS = 4;

	// Witcher level each perk slot opens at, by slot index. Slots fill one
	// mutagen group (column of 3) at a time. Mutagen sockets are never gated.
	public static final int[] SLOT_UNLOCK_LEVEL = {1, 2, 4, 6, 8, 10, 13, 16, 19, 22, 25, 28};

	public static int slotUnlockLevel(int idx) {
		return idx >= 0 && idx < SLOT_UNLOCK_LEVEL.length ? SLOT_UNLOCK_LEVEL[idx] : Integer.MAX_VALUE;
	}

	public static boolean isSlotUnlocked(Player e, int idx) {
		return e.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPlayerLevel >= slotUnlockLevel(idx);
	}

	/** The perk in slot idx, or 0. A perk whose tree slot is hidden reads as 0 (empty). */
	public static int getPerkSocket(Player e, int idx) {
		int id = rawPerkSocket(e, idx);
		return id > 0 && PerkTree.byId(id) != null ? id : 0;
	}

	private static int rawPerkSocket(Player e, int idx) {
		WitchercraftModVariables.PlayerVariables v = e.getData(WitchercraftModVariables.PLAYER_VARIABLES);
		switch (idx) {
			case 0:
				return (int) v.witchercraftPerkSocket1;
			case 1:
				return (int) v.witchercraftPerkSocket2;
			case 2:
				return (int) v.witchercraftPerkSocket3;
			case 3:
				return (int) v.witchercraftPerkSocket4;
			case 4:
				return (int) v.witchercraftPerkSocket5;
			case 5:
				return (int) v.witchercraftPerkSocket6;
			case 6:
				return (int) v.witchercraftPerkSocket7;
			case 7:
				return (int) v.witchercraftPerkSocket8;
			case 8:
				return (int) v.witchercraftPerkSocket9;
			case 9:
				return (int) v.witchercraftPerkSocket10;
			case 10:
				return (int) v.witchercraftPerkSocket11;
			case 11:
				return (int) v.witchercraftPerkSocket12;
			default:
				return 0;
		}
	}

	public static void setPerkSocket(Player e, int idx, int val) {
		WitchercraftModVariables.PlayerVariables v = e.getData(WitchercraftModVariables.PLAYER_VARIABLES);
		switch (idx) {
			case 0:
				v.witchercraftPerkSocket1 = val;
				break;
			case 1:
				v.witchercraftPerkSocket2 = val;
				break;
			case 2:
				v.witchercraftPerkSocket3 = val;
				break;
			case 3:
				v.witchercraftPerkSocket4 = val;
				break;
			case 4:
				v.witchercraftPerkSocket5 = val;
				break;
			case 5:
				v.witchercraftPerkSocket6 = val;
				break;
			case 6:
				v.witchercraftPerkSocket7 = val;
				break;
			case 7:
				v.witchercraftPerkSocket8 = val;
				break;
			case 8:
				v.witchercraftPerkSocket9 = val;
				break;
			case 9:
				v.witchercraftPerkSocket10 = val;
				break;
			case 10:
				v.witchercraftPerkSocket11 = val;
				break;
			case 11:
				v.witchercraftPerkSocket12 = val;
				break;
			default:
				return;
		}
		v.markSyncDirty();
	}

	/** true if this perk id already occupies any of the 12 slots. */
	public static boolean isPerkSocketed(Player e, int perkId) {
		if (perkId <= 0)
			return false;
		for (int i = 0; i < PERK_SLOTS; i++)
			if (getPerkSocket(e, i) == perkId)
				return true;
		return false;
	}
}
