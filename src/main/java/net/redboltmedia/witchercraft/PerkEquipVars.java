package net.redboltmedia.witchercraft;

import net.minecraft.world.entity.player.Player;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

/**
 * Read/write helpers for the 12 perk-socket and 4 mutagen-socket player vars.
 *
 * Reads are safe on either side (vars sync to the client). Writes must run
 * server-side; they mark the var bundle dirty so the next player tick syncs it
 * down (same pattern the generated buy procedures use). Slot indices are
 * 0-based here; the underlying vars are 1-based (witchercraftPerkSocket1..12,
 * witchercraftMutagenSocket1..4).
 */
public final class PerkEquipVars {
	private PerkEquipVars() {
	}

	public static final int PERK_SLOTS = 12;
	public static final int MUTAGEN_GROUPS = 4;

	public static int getPerkSocket(Player e, int idx) {
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

	public static int getMutagenSocket(Player e, int group) {
		WitchercraftModVariables.PlayerVariables v = e.getData(WitchercraftModVariables.PLAYER_VARIABLES);
		switch (group) {
			case 0:
				return (int) v.witchercraftMutagenSocket1;
			case 1:
				return (int) v.witchercraftMutagenSocket2;
			case 2:
				return (int) v.witchercraftMutagenSocket3;
			case 3:
				return (int) v.witchercraftMutagenSocket4;
			default:
				return 0;
		}
	}

	public static void setMutagenSocket(Player e, int group, int val) {
		WitchercraftModVariables.PlayerVariables v = e.getData(WitchercraftModVariables.PLAYER_VARIABLES);
		switch (group) {
			case 0:
				v.witchercraftMutagenSocket1 = val;
				break;
			case 1:
				v.witchercraftMutagenSocket2 = val;
				break;
			case 2:
				v.witchercraftMutagenSocket3 = val;
				break;
			case 3:
				v.witchercraftMutagenSocket4 = val;
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
