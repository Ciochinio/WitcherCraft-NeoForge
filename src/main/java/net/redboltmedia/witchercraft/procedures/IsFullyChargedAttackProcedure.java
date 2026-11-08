package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;

// LOCKED: hand-written, Blockly has no attack-strength block. NeoForge resets the attack
// strength only after the attack events, so it is still accurate inside CriticalHitEvent.
public class IsFullyChargedAttackProcedure {
	public static boolean execute(Entity sourceentity) {
		if (sourceentity == null)
			return false;
		return sourceentity instanceof Player _player && _player.getAttackStrengthScale(0.5F) > 0.9F;
	}
}
