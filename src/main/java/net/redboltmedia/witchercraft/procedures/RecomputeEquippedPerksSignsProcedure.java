package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class RecomputeEquippedPerksSignsProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftEquippedPerkAardIntensity = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 301 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 301
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 301 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 301
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 301 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 301
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 301 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 301
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 301 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 301
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 301 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 301;
			_vars.witchercraftEquippedPerkAxiiIntensity = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 302 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 302
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 302 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 302
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 302 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 302
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 302 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 302
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 302 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 302
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 302 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 302;
			_vars.witchercraftEquippedPerkDelusion = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 303 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 303
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 303 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 303
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 303 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 303
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 303 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 303
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 303 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 303
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 303 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 303;
			_vars.witchercraftEquippedPerkDomination = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 304 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 304
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 304 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 304
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 304 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 304
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 304 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 304
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 304 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 304
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 304 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 304;
			_vars.witchercraftEquippedPerkExplodingShield = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 305 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 305
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 305 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 305
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 305 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 305
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 305 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 305
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 305 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 305
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 305 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 305;
			_vars.witchercraftEquippedPerkFarReachingAard = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 306 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 306
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 306 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 306
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 306 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 306
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 306 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 306
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 306 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 306
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 306 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 306;
			_vars.witchercraftEquippedPerkFirestream = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 307 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 307
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 307 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 307
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 307 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 307
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 307 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 307
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 307 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 307
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 307 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 307;
			_vars.witchercraftEquippedPerkIgniIntensity = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 308 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 308
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 308 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 308
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 308 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 308
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 308 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 308
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 308 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 308
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 308 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 308;
			_vars.witchercraftEquippedPerkMagicTrap = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 309 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 309
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 309 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 309
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 309 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 309
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 309 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 309
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 309 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 309
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 309 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 309;
			_vars.witchercraftEquippedPerkPyromaniac = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 310 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 310
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 310 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 310
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 310 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 310
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 310 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 310
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 310 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 310
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 310 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 310;
			_vars.witchercraftEquippedPerkQuenDischarge = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 311 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 311
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 311 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 311
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 311 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 311
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 311 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 311
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 311 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 311
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 311 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 311;
			_vars.witchercraftEquippedPerkQuenIntensity = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 312 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 312
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 312 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 312
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 312 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 312
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 312 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 312
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 312 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 312
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 312 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 312;
			_vars.witchercraftEquippedPerkShockWave = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 313 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 313
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 313 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 313
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 313 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 313
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 313 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 313
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 313 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 313
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 313 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 313;
			_vars.witchercraftEquippedPerkSustainedGlyphs = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 314 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 314
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 314 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 314
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 314 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 314
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 314 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 314
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 314 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 314
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 314 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 314;
			_vars.witchercraftEquippedPerkYrdenIntensity = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 == 315 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 == 315
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 == 315 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 == 315
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 == 315 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 == 315
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 == 315 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 == 315
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 == 315 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 == 315
					|| entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 == 315 || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 == 315;
			_vars.markSyncDirty();
		}
	}
}