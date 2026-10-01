package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

@EventBusSubscriber
public class CharacterExperienceCalculatorProcedure {
	@SubscribeEvent
	public static void onPlayerXPChange(PlayerXpEvent.XpChange event) {
		if (event.getEntity() != null) {
			execute(event, event.getEntity(), event.getAmount());
		}
	}

	public static void execute(Entity entity, double amount) {
		execute(null, entity, amount);
	}

	private static void execute(@Nullable Event event, Entity entity, double amount) {
		if (entity == null)
			return;
		double LevelCap = 0;
		double StepLevels1To10 = 0;
		double StepLevels11To20 = 0;
		double StepAfterLevel20 = 0;
		LevelCap = 60;
		StepLevels1To10 = 25;
		StepLevels11To20 = 50;
		StepAfterLevel20 = 10;
		if (amount > 0 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPlayerLevel < LevelCap) {
			if (entity instanceof ServerPlayer _player)
				_player.sendSystemMessage(Component.literal(("xp  " + amount)), false);
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftPlayerExperience = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPlayerExperience + amount;
				_vars.markSyncDirty();
			}
			while (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPlayerExperience >= entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPlayerExperienceRequirement
					&& entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPlayerLevel < LevelCap) {
				{
					WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
					_vars.witchercraftPlayerExperience = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPlayerExperience - entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPlayerExperienceRequirement;
					_vars.markSyncDirty();
				}
				{
					WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
					_vars.witchercraftPlayerLevel = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPlayerLevel + 1;
					_vars.markSyncDirty();
				}
				if (entity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal(("poziom  " + entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPlayerLevel)), false);
				if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPlayerLevel >= 21) {
					{
						WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
						_vars.witchercraftPlayerExperienceRequirement = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPlayerExperienceRequirement + StepAfterLevel20;
						_vars.markSyncDirty();
					}
				} else if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPlayerLevel > 10) {
					{
						WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
						_vars.witchercraftPlayerExperienceRequirement = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPlayerExperienceRequirement + StepLevels11To20;
						_vars.markSyncDirty();
					}
				} else {
					{
						WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
						_vars.witchercraftPlayerExperienceRequirement = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPlayerExperienceRequirement + StepLevels1To10;
						_vars.markSyncDirty();
					}
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPlayerLevel >= LevelCap) {
				{
					WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
					_vars.witchercraftPlayerExperience = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPlayerExperienceRequirement;
					_vars.markSyncDirty();
				}
			}
		}
	}
}
