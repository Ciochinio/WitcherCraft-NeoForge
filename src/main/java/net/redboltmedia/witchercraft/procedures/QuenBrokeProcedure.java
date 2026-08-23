package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.minecraft.world.entity.LivingEntity;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class QuenBrokeProcedure {
	// NOTE: code-locked (locked_code=true) so MCreator preserves the color styling.
	// The Blockly cannot represent per-player action-bar color; edit this Java directly.
	// Fires from QUEN_EFFECT's onExpired, which WitchercraftModMobEffects dispatches from
	// BOTH MobEffectEvent.Remove (shattered) and MobEffectEvent.Expired (timed out).
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		// Raising the Active Shield removes QUEN_EFFECT to take ownership of the pool, which
		// fires this procedure. Bail out so the handover neither zeroes the pool the shield
		// just inherited nor prints a break message for a shield that is still up.
		if (entity instanceof LivingEntity _livEntShield && _livEntShield.hasEffect(WitchercraftModMobEffects.QUEN_ACTIVE_SHIELD))
			return;
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal("Quen broke!").withStyle(ChatFormatting.RED), true);
		// Clear the pool here rather than only in QuenBlock. QuenBlock zeroes it when the
		// shield is drained by hits, but a shield that times out with damage left keeps a
		// stale value, which leaves the HUD bar on screen after the effect is gone.
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftQuenShield = 0;
			_vars.markSyncDirty();
		}
	}
}
