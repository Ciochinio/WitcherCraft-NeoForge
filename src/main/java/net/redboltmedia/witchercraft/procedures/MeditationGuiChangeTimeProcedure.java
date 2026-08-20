package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModMenus;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.Holder;

import java.util.Optional;

public class MeditationGuiChangeTimeProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		if (((entity instanceof Player _entity0 && _entity0.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu0) ? _menu0.getMenuState(2, "MeditationTime", 0.0) : 0.0) == 1) {
			if (world instanceof ServerLevel _level) {
				ServerClockManager _clockManager = _level.getServer().clockManager();
				Optional<Holder<WorldClock>> _clock = _level.dimensionType().defaultClock();
				if (_clock.isPresent())
					_clockManager.setTotalTicks(_clock.get(), 19000);
			}
		} else if (((entity instanceof Player _entity2 && _entity2.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu2) ? _menu2.getMenuState(2, "MeditationTime", 0.0) : 0.0) == 2) {
			if (world instanceof ServerLevel _level) {
				ServerClockManager _clockManager = _level.getServer().clockManager();
				Optional<Holder<WorldClock>> _clock = _level.dimensionType().defaultClock();
				if (_clock.isPresent())
					_clockManager.setTotalTicks(_clock.get(), 20000);
			}
		} else if (((entity instanceof Player _entity4 && _entity4.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu4) ? _menu4.getMenuState(2, "MeditationTime", 0.0) : 0.0) == 3) {
			if (world instanceof ServerLevel _level) {
				ServerClockManager _clockManager = _level.getServer().clockManager();
				Optional<Holder<WorldClock>> _clock = _level.dimensionType().defaultClock();
				if (_clock.isPresent())
					_clockManager.setTotalTicks(_clock.get(), 21000);
			}
		} else if (((entity instanceof Player _entity6 && _entity6.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu6) ? _menu6.getMenuState(2, "MeditationTime", 0.0) : 0.0) == 4) {
			if (world instanceof ServerLevel _level) {
				ServerClockManager _clockManager = _level.getServer().clockManager();
				Optional<Holder<WorldClock>> _clock = _level.dimensionType().defaultClock();
				if (_clock.isPresent())
					_clockManager.setTotalTicks(_clock.get(), 22000);
			}
		} else if (((entity instanceof Player _entity8 && _entity8.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu8) ? _menu8.getMenuState(2, "MeditationTime", 0.0) : 0.0) == 5) {
			if (world instanceof ServerLevel _level) {
				ServerClockManager _clockManager = _level.getServer().clockManager();
				Optional<Holder<WorldClock>> _clock = _level.dimensionType().defaultClock();
				if (_clock.isPresent())
					_clockManager.setTotalTicks(_clock.get(), 23000);
			}
		} else if (((entity instanceof Player _entity10 && _entity10.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu10) ? _menu10.getMenuState(2, "MeditationTime", 0.0) : 0.0) == 6) {
			if (world instanceof ServerLevel _level) {
				ServerClockManager _clockManager = _level.getServer().clockManager();
				Optional<Holder<WorldClock>> _clock = _level.dimensionType().defaultClock();
				if (_clock.isPresent())
					_clockManager.setTotalTicks(_clock.get(), 5);
			}
		} else if (((entity instanceof Player _entity12 && _entity12.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu12) ? _menu12.getMenuState(2, "MeditationTime", 0.0) : 0.0) == 7) {
			if (world instanceof ServerLevel _level) {
				ServerClockManager _clockManager = _level.getServer().clockManager();
				Optional<Holder<WorldClock>> _clock = _level.dimensionType().defaultClock();
				if (_clock.isPresent())
					_clockManager.setTotalTicks(_clock.get(), 1000);
			}
		} else if (((entity instanceof Player _entity14 && _entity14.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu14) ? _menu14.getMenuState(2, "MeditationTime", 0.0) : 0.0) == 8) {
			if (world instanceof ServerLevel _level) {
				ServerClockManager _clockManager = _level.getServer().clockManager();
				Optional<Holder<WorldClock>> _clock = _level.dimensionType().defaultClock();
				if (_clock.isPresent())
					_clockManager.setTotalTicks(_clock.get(), 2000);
			}
		} else if (((entity instanceof Player _entity16 && _entity16.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu16) ? _menu16.getMenuState(2, "MeditationTime", 0.0) : 0.0) == 9) {
			if (world instanceof ServerLevel _level) {
				ServerClockManager _clockManager = _level.getServer().clockManager();
				Optional<Holder<WorldClock>> _clock = _level.dimensionType().defaultClock();
				if (_clock.isPresent())
					_clockManager.setTotalTicks(_clock.get(), 3000);
			}
		} else if (((entity instanceof Player _entity18 && _entity18.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu18) ? _menu18.getMenuState(2, "MeditationTime", 0.0) : 0.0) == 10) {
			if (world instanceof ServerLevel _level) {
				ServerClockManager _clockManager = _level.getServer().clockManager();
				Optional<Holder<WorldClock>> _clock = _level.dimensionType().defaultClock();
				if (_clock.isPresent())
					_clockManager.setTotalTicks(_clock.get(), 4000);
			}
		} else if (((entity instanceof Player _entity20 && _entity20.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu20) ? _menu20.getMenuState(2, "MeditationTime", 0.0) : 0.0) == 11) {
			if (world instanceof ServerLevel _level) {
				ServerClockManager _clockManager = _level.getServer().clockManager();
				Optional<Holder<WorldClock>> _clock = _level.dimensionType().defaultClock();
				if (_clock.isPresent())
					_clockManager.setTotalTicks(_clock.get(), 5000);
			}
		} else if (((entity instanceof Player _entity22 && _entity22.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu22) ? _menu22.getMenuState(2, "MeditationTime", 0.0) : 0.0) == 12) {
			if (world instanceof ServerLevel _level) {
				ServerClockManager _clockManager = _level.getServer().clockManager();
				Optional<Holder<WorldClock>> _clock = _level.dimensionType().defaultClock();
				if (_clock.isPresent())
					_clockManager.setTotalTicks(_clock.get(), 6000);
			}
		} else if (((entity instanceof Player _entity24 && _entity24.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu24) ? _menu24.getMenuState(2, "MeditationTime", 0.0) : 0.0) == 13) {
			if (world instanceof ServerLevel _level) {
				ServerClockManager _clockManager = _level.getServer().clockManager();
				Optional<Holder<WorldClock>> _clock = _level.dimensionType().defaultClock();
				if (_clock.isPresent())
					_clockManager.setTotalTicks(_clock.get(), 7000);
			}
		} else if (((entity instanceof Player _entity26 && _entity26.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu26) ? _menu26.getMenuState(2, "MeditationTime", 0.0) : 0.0) == 14) {
			if (world instanceof ServerLevel _level) {
				ServerClockManager _clockManager = _level.getServer().clockManager();
				Optional<Holder<WorldClock>> _clock = _level.dimensionType().defaultClock();
				if (_clock.isPresent())
					_clockManager.setTotalTicks(_clock.get(), 8000);
			}
		} else if (((entity instanceof Player _entity28 && _entity28.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu28) ? _menu28.getMenuState(2, "MeditationTime", 0.0) : 0.0) == 15) {
			if (world instanceof ServerLevel _level) {
				ServerClockManager _clockManager = _level.getServer().clockManager();
				Optional<Holder<WorldClock>> _clock = _level.dimensionType().defaultClock();
				if (_clock.isPresent())
					_clockManager.setTotalTicks(_clock.get(), 9000);
			}
		} else if (((entity instanceof Player _entity30 && _entity30.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu30) ? _menu30.getMenuState(2, "MeditationTime", 0.0) : 0.0) == 16) {
			if (world instanceof ServerLevel _level) {
				ServerClockManager _clockManager = _level.getServer().clockManager();
				Optional<Holder<WorldClock>> _clock = _level.dimensionType().defaultClock();
				if (_clock.isPresent())
					_clockManager.setTotalTicks(_clock.get(), 10000);
			}
		} else if (((entity instanceof Player _entity32 && _entity32.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu32) ? _menu32.getMenuState(2, "MeditationTime", 0.0) : 0.0) == 17) {
			if (world instanceof ServerLevel _level) {
				ServerClockManager _clockManager = _level.getServer().clockManager();
				Optional<Holder<WorldClock>> _clock = _level.dimensionType().defaultClock();
				if (_clock.isPresent())
					_clockManager.setTotalTicks(_clock.get(), 11000);
			}
		} else if (((entity instanceof Player _entity34 && _entity34.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu34) ? _menu34.getMenuState(2, "MeditationTime", 0.0) : 0.0) == 18) {
			if (world instanceof ServerLevel _level) {
				ServerClockManager _clockManager = _level.getServer().clockManager();
				Optional<Holder<WorldClock>> _clock = _level.dimensionType().defaultClock();
				if (_clock.isPresent())
					_clockManager.setTotalTicks(_clock.get(), 12000);
			}
		} else if (((entity instanceof Player _entity36 && _entity36.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu36) ? _menu36.getMenuState(2, "MeditationTime", 0.0) : 0.0) == 19) {
			if (world instanceof ServerLevel _level) {
				ServerClockManager _clockManager = _level.getServer().clockManager();
				Optional<Holder<WorldClock>> _clock = _level.dimensionType().defaultClock();
				if (_clock.isPresent())
					_clockManager.setTotalTicks(_clock.get(), 13000);
			}
		} else if (((entity instanceof Player _entity38 && _entity38.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu38) ? _menu38.getMenuState(2, "MeditationTime", 0.0) : 0.0) == 20) {
			if (world instanceof ServerLevel _level) {
				ServerClockManager _clockManager = _level.getServer().clockManager();
				Optional<Holder<WorldClock>> _clock = _level.dimensionType().defaultClock();
				if (_clock.isPresent())
					_clockManager.setTotalTicks(_clock.get(), 14000);
			}
		} else if (((entity instanceof Player _entity40 && _entity40.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu40) ? _menu40.getMenuState(2, "MeditationTime", 0.0) : 0.0) == 21) {
			if (world instanceof ServerLevel _level) {
				ServerClockManager _clockManager = _level.getServer().clockManager();
				Optional<Holder<WorldClock>> _clock = _level.dimensionType().defaultClock();
				if (_clock.isPresent())
					_clockManager.setTotalTicks(_clock.get(), 15000);
			}
		} else if (((entity instanceof Player _entity42 && _entity42.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu42) ? _menu42.getMenuState(2, "MeditationTime", 0.0) : 0.0) == 22) {
			if (world instanceof ServerLevel _level) {
				ServerClockManager _clockManager = _level.getServer().clockManager();
				Optional<Holder<WorldClock>> _clock = _level.dimensionType().defaultClock();
				if (_clock.isPresent())
					_clockManager.setTotalTicks(_clock.get(), 16000);
			}
		} else if (((entity instanceof Player _entity44 && _entity44.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu44) ? _menu44.getMenuState(2, "MeditationTime", 0.0) : 0.0) == 23) {
			if (world instanceof ServerLevel _level) {
				ServerClockManager _clockManager = _level.getServer().clockManager();
				Optional<Holder<WorldClock>> _clock = _level.dimensionType().defaultClock();
				if (_clock.isPresent())
					_clockManager.setTotalTicks(_clock.get(), 17000);
			}
		} else if (((entity instanceof Player _entity46 && _entity46.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu46) ? _menu46.getMenuState(2, "MeditationTime", 0.0) : 0.0) == 24) {
			if (world instanceof ServerLevel _level) {
				ServerClockManager _clockManager = _level.getServer().clockManager();
				Optional<Holder<WorldClock>> _clock = _level.dimensionType().defaultClock();
				if (_clock.isPresent())
					_clockManager.setTotalTicks(_clock.get(), 18000);
			}
		}
	}
}