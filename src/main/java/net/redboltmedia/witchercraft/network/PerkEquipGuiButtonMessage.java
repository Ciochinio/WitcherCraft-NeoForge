package net.redboltmedia.witchercraft.network;

import net.redboltmedia.witchercraft.WitchercraftMod;
import net.redboltmedia.witchercraft.client.gui.PerkEquipLayout;
import net.redboltmedia.witchercraft.client.gui.PerkTree;
import net.redboltmedia.witchercraft.procedures.*;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.SectionPos;

/**
 * Perk equip screen button/action packet.
 *
 * HAND-MAINTAINED, locked_code=true. The buttonID encodes the action so the
 * fixed MCreator packet shape can carry all of them (the screen is client-only
 * for selection; only these state changes are server-authoritative):
 *   0                              reserved / no-op
 *   1000000 + slotIdx*1000 + perkId   place perkId into perk slot slotIdx (0-11)
 *   2000000 + slotIdx                 clear perk slot slotIdx
 *   3000000 + group                   cycle mutagen colour of group (0-3)
 *   4000000                           medallion click (placeholder button)
 *   5000000 + perkId                  learn a tree node (prereqs + points enforced)
 * The server re-validates every action; it never trusts the client's view.
 */
@EventBusSubscriber
public record PerkEquipGuiButtonMessage(int buttonID, int x, int y, int z) implements CustomPacketPayload {
	public static final Type<PerkEquipGuiButtonMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "perk_equip_gui_buttons"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PerkEquipGuiButtonMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, PerkEquipGuiButtonMessage message) -> {
		buffer.writeInt(message.buttonID);
		buffer.writeInt(message.x);
		buffer.writeInt(message.y);
		buffer.writeInt(message.z);
	}, (RegistryFriendlyByteBuf buffer) -> new PerkEquipGuiButtonMessage(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt()));

	@Override
	public Type<PerkEquipGuiButtonMessage> type() {
		return TYPE;
	}

	public static void handleData(final PerkEquipGuiButtonMessage message, final IPayloadContext context) {
		if (context.flow() == PacketFlow.SERVERBOUND) {
			context.enqueueWork(() -> handleButtonAction(context.player(), message.buttonID, message.x, message.y, message.z)).exceptionally(e -> {
				context.connection().disconnect(Component.literal(e.getMessage()));
				return null;
			});
		}
	}

	public static void handleButtonAction(Player entity, int buttonID, int x, int y, int z) {
		Level world = entity.level();
		// security measure to prevent arbitrary chunk generation
		if (!world.getChunkSource().hasChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z)))
			return;
		if (buttonID >= 5000000) { // learn a tree node (perkId), prereqs + points enforced
			tryLearn(entity, buttonID - 5000000);
		} else if (buttonID >= 4000000) { // medallion click (placeholder button)
			if (PerkEquipLayout.MEDALLION_ENABLED)
				MutagenMedallionClickProcedure.execute(entity);
		} else if (buttonID >= 3000000) { // cycle mutagen colour: empty -> red -> green -> blue -> empty
			int group = buttonID - 3000000;
			if (group < 0 || group >= PerkEquipVars.MUTAGEN_GROUPS)
				return;
			int next = (PerkEquipVars.getMutagenSocket(entity, group) + 1) % 4;
			PerkEquipVars.setMutagenSocket(entity, group, next);
		} else if (buttonID >= 2000000) { // clear a perk slot
			int slot = buttonID - 2000000;
			if (slot < 0 || slot >= PerkEquipVars.PERK_SLOTS)
				return;
			PerkEquipVars.setPerkSocket(entity, slot, 0);
		} else if (buttonID >= 1000000) { // place / move a perk into a slot
			int a = buttonID - 1000000;
			int slot = a / 1000;
			int perkId = a % 1000;
			if (slot < 0 || slot >= PerkEquipVars.PERK_SLOTS)
				return;
			if (perkId <= 0)
				return;
			// swap semantics: placing into an occupied slot overwrites the
			// occupant, which returns to the unequipped pool automatically (equipped
			// state is derived from the sockets on recompute - no extra action).
			// move semantics: if this perk already sits in another slot, vacate
			// it first so re-slotting an equipped perk moves it (never duplicates).
			for (int i = 0; i < PerkEquipVars.PERK_SLOTS; i++) {
				if (i != slot && PerkEquipVars.getPerkSocket(entity, i) == perkId)
					PerkEquipVars.setPerkSocket(entity, i, 0);
			}
			PerkEquipVars.setPerkSocket(entity, slot, perkId);
		}
		// buttonID 0: reserved no-op. Any state-changing action recomputes the
		// active perk effects here (server-authoritative). This replaces the old
		// PerkEquipGuiMenu.removed() hook, retired with the container screen when
		// the perk UI became a page in WitcherGuiScreen.
		if (buttonID != 0)
			RecomputeEquippedPerksProcedure.execute(entity);
	}

	// Learn a tree node: only if not already learned and all prerequisites are
	// learned. The actual point-check / learned-flag / point-spend is delegated
	// to the perk's existing <Perk>Effect buy procedure, so learning stays in one
	// place. (Dispatch covers the slice-2a Combat test nodes; extends to 45.)
	private static void tryLearn(Player entity, int perkId) {
		if (PerkLearnedVars.isLearned(entity, perkId))
			return;
		PerkTree.Node n = PerkTree.byId(perkId);
		if (n == null)
			return;
		for (int pre : n.prereqs)
			if (!PerkLearnedVars.isLearned(entity, pre))
				return;
		switch (perkId) {
			case 101 : AnatomicalKnowledgeEffectProcedure.execute(entity); break;
			case 102 : ColdBloodEffectProcedure.execute(entity); break;
			case 103 : CripplingShotEffectProcedure.execute(entity); break;
			case 104 : CripplingStrikesEffectProcedure.execute(entity); break;
			case 105 : CrushingBlowsEffectProcedure.execute(entity); break;
			case 106 : DeadlyPrecisionEffectProcedure.execute(entity); break;
			case 107 : DefenceEffectProcedure.execute(entity); break;
			case 108 : FleetFootedEffectProcedure.execute(entity); break;
			case 109 : FloodOfAngerEffectProcedure.execute(entity); break;
			case 110 : MuscleMemoryEffectProcedure.execute(entity); break;
			case 111 : PreciseBlowsEffectProcedure.execute(entity); break;
			case 112 : RazorFocusEffectProcedure.execute(entity); break;
			case 113 : StrengthTrainingEffectProcedure.execute(entity); break;
			case 114 : SunderArmorEffectProcedure.execute(entity); break;
			case 115 : UndyingEffectProcedure.execute(entity); break;
			case 201 : ClusterBombsEffectProcedure.execute(entity); break;
			case 202 : DelayedRecoveryEffectProcedure.execute(entity); break;
			case 203 : EfficiencyEffectProcedure.execute(entity); break;
			case 204 : HunterInstinctEffectProcedure.execute(entity); break;
			case 205 : PoisonedBladesEffectProcedure.execute(entity); break;
			case 206 : ProtectiveCoatingEffectProcedure.execute(entity); break;
			case 207 : PyrotechnicsEffectProcedure.execute(entity); break;
			case 208 : RefreshmentEffectProcedure.execute(entity); break;
			case 209 : SideEffectsEffectProcedure.execute(entity); break;
			case 301 : AardIntensityEffectProcedure.execute(entity); break;
			case 302 : AxiiIntensityEffectProcedure.execute(entity); break;
			case 303 : DelusionEffectProcedure.execute(entity); break;
			case 304 : DominationEffectProcedure.execute(entity); break;
			case 305 : ExplodingShieldEffectProcedure.execute(entity); break;
			case 306 : FarReachingAardEffectProcedure.execute(entity); break;
			case 307 : FirestreamEffectProcedure.execute(entity); break;
			case 308 : IgniIntensityEffectProcedure.execute(entity); break;
			case 309 : MagicTrapEffectProcedure.execute(entity); break;
			case 310 : PyromaniacEffectProcedure.execute(entity); break;
			case 311 : QuenDischargeEffectProcedure.execute(entity); break;
			case 312 : QuenIntensityEffectProcedure.execute(entity); break;
			case 313 : ShockWaveEffectProcedure.execute(entity); break;
			case 314 : SustainedGlyphsEffectProcedure.execute(entity); break;
			case 315 : YrdenIntensityEffectProcedure.execute(entity); break;
			case 401 : BearSchoolEffectProcedure.execute(entity); break;
			case 402 : CatSchoolEffectProcedure.execute(entity); break;
			case 403 : GourmetEffectProcedure.execute(entity); break;
			case 404 : GriffinSchoolEffectProcedure.execute(entity); break;
			case 405 : SunAndStarsEffectProcedure.execute(entity); break;
			case 406 : SurvivalInstinctEffectProcedure.execute(entity); break;
			default : break;
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(PerkEquipGuiButtonMessage.TYPE, PerkEquipGuiButtonMessage.STREAM_CODEC, PerkEquipGuiButtonMessage::handleData);
	}
}
