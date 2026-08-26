package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class RecomputeEquippedPerksCombatProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftEquippedPerkAnatomicalKnowledge = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 101 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 101
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 101 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 101
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 101 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 101
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 101 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 101
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 101 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 101
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 101 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 101;
			_vars.witchercraftEquippedPerkColdBlood = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 102 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 102
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 102 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 102
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 102 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 102
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 102 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 102
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 102 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 102
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 102 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 102;
			_vars.witchercraftEquippedPerkCripplingShot = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 103 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 103
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 103 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 103
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 103 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 103
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 103 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 103
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 103 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 103
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 103 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 103;
			_vars.witchercraftEquippedPerkCripplingStrikes = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 104 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 104
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 104 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 104
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 104 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 104
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 104 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 104
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 104 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 104
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 104 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 104;
			_vars.witchercraftEquippedPerkCrushingBlows = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 105 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 105
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 105 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 105
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 105 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 105
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 105 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 105
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 105 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 105
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 105 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 105;
			_vars.witchercraftEquippedPerkDeadlyPrecision = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 106 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 106
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 106 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 106
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 106 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 106
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 106 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 106
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 106 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 106
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 106 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 106;
			_vars.witchercraftEquippedPerkDefence = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 107 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 107
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 107 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 107
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 107 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 107
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 107 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 107
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 107 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 107
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 107 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 107;
			_vars.witchercraftEquippedPerkFleetFooted = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 108 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 108
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 108 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 108
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 108 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 108
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 108 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 108
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 108 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 108
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 108 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 108;
			_vars.witchercraftEquippedPerkFloodOfAnger = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 109 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 109
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 109 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 109
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 109 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 109
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 109 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 109
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 109 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 109
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 109 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 109;
			_vars.witchercraftEquippedPerkMuscleMemory = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 110 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 110
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 110 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 110
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 110 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 110
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 110 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 110
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 110 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 110
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 110 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 110;
			_vars.witchercraftEquippedPerkPreciseBlows = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 111 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 111
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 111 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 111
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 111 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 111
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 111 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 111
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 111 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 111
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 111 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 111;
			_vars.witchercraftEquippedPerkRazorFocus = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 112 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 112
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 112 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 112
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 112 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 112
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 112 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 112
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 112 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 112
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 112 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 112;
			_vars.witchercraftEquippedPerkStrengthTraining = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 113 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 113
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 113 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 113
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 113 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 113
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 113 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 113
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 113 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 113
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 113 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 113;
			_vars.witchercraftEquippedPerkSunderArmor = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 114 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 114
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 114 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 114
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 114 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 114
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 114 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 114
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 114 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 114
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 114 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 114;
			_vars.witchercraftEquippedPerkUndying = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 115 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 115
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 115 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 115
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 115 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 115
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 115 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 115
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 115 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 115
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 115 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 115;
			_vars.markSyncDirty();
		}
	}
}