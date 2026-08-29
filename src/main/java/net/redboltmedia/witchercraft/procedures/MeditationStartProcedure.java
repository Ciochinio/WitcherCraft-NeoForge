package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.MeditationSpinMessage;
import net.redboltmedia.witchercraft.MeditationRejectMessage;

import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

import java.util.UUID;

/**
 * HAND-MAINTAINED (locked_code procedure, ~/Meditation2). Begins a meditation
 * session: converts the picked hour to a forward tick delta, arms the session
 * anchors in {@link WitchercraftModVariables}, and tells the initiator's client
 * to go translucent for the spin (the accelerated advance itself runs in
 * {@link MeditationTickProcedure} from the heartbeat).
 *
 * SLICE 2a: solo, immediate commit. The safety gate (MeditationCanStart) and the
 * campfire placement (MeditationPlaceCampfire) are wired in here in slice 2b; the
 * sleep-percentage session / join flow is slice 3.
 *
 * targetHour is 0-23. MC tick 0 = 06:00, so ticks(hour) = ((hour-6)*1000) mod
 * 24000. Picking the current hour advances a full day.
 */
public class MeditationStartProcedure {
	/**
	 * Who started the current session (server-side). MeditationTick awards this
	 * player their insomnia on completion; MeditationStop clears it on cancel.
	 * A plain static is fine - there is one session per server (slice 3 revisits).
	 */
	public static UUID initiator = null;

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, double targetHour) {
		if (entity == null || !(world instanceof ServerLevel level))
			return;
		// one session at a time (slice 3 turns this into join-the-pending-session)
		if (WitchercraftModVariables.meditationState != 0)
			return;
		// safety gate (space / mobs), re-validated server-side. On failure, tell the
		// player why on the action bar and close their GUI (MeditationRejectMessage).
		int gate = MeditationCanStartProcedure.reason(world, x, y, z, entity);
		if (gate != MeditationCanStartProcedure.OK) {
			if (entity instanceof ServerPlayer sp)
				PacketDistributor.sendToPlayer(sp, new MeditationRejectMessage(gate));
			return;
		}

		int hour = ((int) Math.round(targetHour) % 24 + 24) % 24;
		long anchor = (long) level.getDefaultClockTime();
		int nowTod = (int) (((anchor % 24000L) + 24000L) % 24000L);
		int targetTod = ((((hour - 6) * 1000) % 24000) + 24000) % 24000;
		int delta = (((targetTod - nowTod) % 24000) + 24000) % 24000;
		if (delta == 0)
			delta = 24000; // picking the current hour = advance a full day

		WitchercraftModVariables.meditationAnchorTicks = anchor;
		WitchercraftModVariables.meditationDeltaTicks = delta;
		WitchercraftModVariables.meditationAnchorGametime = level.getGameTime();
		WitchercraftModVariables.meditationState = 2;

		// place the campfire near the initiator (Blockly procedure; skips if one is
		// already nearby), then arm the client overlay.
		MeditationPlaceCampfireProcedure.execute(world, x, y, z, entity);
		if (entity instanceof ServerPlayer sp) {
			initiator = sp.getUUID();
			PacketDistributor.sendToPlayer(sp, new MeditationSpinMessage(hour, MeditationTickProcedure.spinDurationTicks(delta)));
		}
	}
}
