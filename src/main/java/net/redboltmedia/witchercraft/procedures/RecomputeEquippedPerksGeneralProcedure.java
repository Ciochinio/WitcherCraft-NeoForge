package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class RecomputeEquippedPerksGeneralProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftEquippedPerkBearSchool = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 401 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 401
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 401 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 401
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 401 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 401
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 401 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 401
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 401 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 401
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 401 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 401;
			_vars.witchercraftEquippedPerkCatSchool = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 402 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 402
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 402 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 402
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 402 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 402
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 402 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 402
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 402 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 402
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 402 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 402;
			_vars.witchercraftEquippedPerkGourmet = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 403 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 403
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 403 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 403
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 403 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 403
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 403 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 403
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 403 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 403
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 403 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 403;
			_vars.witchercraftEquippedPerkGriffinSchool = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 404 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 404
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 404 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 404
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 404 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 404
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 404 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 404
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 404 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 404
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 404 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 404;
			_vars.witchercraftEquippedPerkSunAndStars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 405 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 405
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 405 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 405
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 405 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 405
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 405 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 405
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 405 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 405
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 405 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 405;
			_vars.witchercraftEquippedPerkSurvivalInstinct = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 406 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 406
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 406 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 406
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 406 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 406
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 406 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 406
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 406 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 406
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 406 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 406;
			_vars.markSyncDirty();
		}
	}
}