package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class RecomputeEquippedPerksAlchemyProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftEquippedPerkClusterBombs = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 201 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 201
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 201 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 201
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 201 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 201
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 201 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 201
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 201 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 201
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 201 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 201;
			_vars.witchercraftEquippedPerkDelayedRecovery = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 202 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 202
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 202 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 202
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 202 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 202
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 202 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 202
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 202 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 202
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 202 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 202;
			_vars.witchercraftEquippedPerkEfficiency = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 203 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 203
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 203 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 203
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 203 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 203
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 203 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 203
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 203 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 203
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 203 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 203;
			_vars.witchercraftEquippedPerkHunterInstinct = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 204 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 204
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 204 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 204
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 204 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 204
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 204 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 204
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 204 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 204
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 204 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 204;
			_vars.witchercraftEquippedPerkPoisonedBlades = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 205 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 205
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 205 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 205
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 205 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 205
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 205 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 205
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 205 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 205
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 205 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 205;
			_vars.witchercraftEquippedPerkProtectiveCoating = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 206 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 206
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 206 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 206
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 206 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 206
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 206 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 206
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 206 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 206
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 206 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 206;
			_vars.witchercraftEquippedPerkPyrotechnics = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 207 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 207
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 207 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 207
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 207 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 207
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 207 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 207
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 207 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 207
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 207 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 207;
			_vars.witchercraftEquippedPerkRefreshment = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 208 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 208
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 208 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 208
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 208 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 208
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 208 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 208
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 208 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 208
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 208 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 208;
			_vars.witchercraftEquippedPerkSideEffects = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 209 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 209
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 209 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 209
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 209 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 209
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 209 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 209
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 209 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 209
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 209 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 209;
			_vars.markSyncDirty();
		}
	}
}