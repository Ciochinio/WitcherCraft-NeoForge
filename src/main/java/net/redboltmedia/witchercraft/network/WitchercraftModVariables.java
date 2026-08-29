package net.redboltmedia.witchercraft.network;

import net.redboltmedia.witchercraft.WitchercraftMod;

import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.util.ProblemReporter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.function.Supplier;

@EventBusSubscriber
public class WitchercraftModVariables {
	public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, WitchercraftMod.MODID);
	public static final Supplier<AttachmentType<PlayerVariables>> PLAYER_VARIABLES = ATTACHMENT_TYPES.register("player_variables", () -> AttachmentType.serializable(PlayerVariables::new).build());
	public static double witchercraftSeconds = 0;
	public static double witchercraftMinutes = 0;
	public static double meditationState = 0;
	public static double meditationAnchorTicks = 0;
	public static double meditationDeltaTicks = 0;
	public static double meditationAnchorGametime = 0;

	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(PlayerVariablesSyncMessage.TYPE, PlayerVariablesSyncMessage.STREAM_CODEC, PlayerVariablesSyncMessage::handleData);
	}

	@SubscribeEvent
	public static void onPlayerLoggedInSyncPlayerVariables(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player)
			PacketDistributor.sendToPlayer(player, new PlayerVariablesSyncMessage(player.getData(PLAYER_VARIABLES)));
	}

	@SubscribeEvent
	public static void onPlayerRespawnedSyncPlayerVariables(PlayerEvent.PlayerRespawnEvent event) {
		if (event.getEntity() instanceof ServerPlayer player)
			PacketDistributor.sendToPlayer(player, new PlayerVariablesSyncMessage(player.getData(PLAYER_VARIABLES)));
	}

	@SubscribeEvent
	public static void onPlayerChangedDimensionSyncPlayerVariables(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player)
			PacketDistributor.sendToPlayer(player, new PlayerVariablesSyncMessage(player.getData(PLAYER_VARIABLES)));
	}

	@SubscribeEvent
	public static void onPlayerTickUpdateSyncPlayerVariables(PlayerTickEvent.Post event) {
		if (event.getEntity() instanceof ServerPlayer player && player.getData(PLAYER_VARIABLES)._syncDirty) {
			PacketDistributor.sendToPlayer(player, new PlayerVariablesSyncMessage(player.getData(PLAYER_VARIABLES)));
			player.getData(PLAYER_VARIABLES)._syncDirty = false;
		}
	}

	@SubscribeEvent
	public static void clonePlayer(PlayerEvent.Clone event) {
		PlayerVariables original = event.getOriginal().getData(PLAYER_VARIABLES);
		PlayerVariables clone = new PlayerVariables();
		clone.witchercraftPerksLearned = original.witchercraftPerksLearned;
		clone.witchercraftPerksGourmet = original.witchercraftPerksGourmet;
		clone.witchercraftPerkSocket1 = original.witchercraftPerkSocket1;
		clone.witchercraftPerkSocket2 = original.witchercraftPerkSocket2;
		clone.witchercraftPerkSocket3 = original.witchercraftPerkSocket3;
		clone.witchercraftPerkSocket4 = original.witchercraftPerkSocket4;
		clone.witchercraftPerkSocket5 = original.witchercraftPerkSocket5;
		clone.witchercraftPerkSocket6 = original.witchercraftPerkSocket6;
		clone.witchercraftPerkSocket7 = original.witchercraftPerkSocket7;
		clone.witchercraftPerkSocket8 = original.witchercraftPerkSocket8;
		clone.witchercraftPerkSocket9 = original.witchercraftPerkSocket9;
		clone.witchercraftPerkSocket10 = original.witchercraftPerkSocket10;
		clone.witchercraftPerkSocket11 = original.witchercraftPerkSocket11;
		clone.witchercraftPerkSocket12 = original.witchercraftPerkSocket12;
		clone.witchercraftMutagenSocket1 = original.witchercraftMutagenSocket1;
		clone.witchercraftMutagenSocket2 = original.witchercraftMutagenSocket2;
		clone.witchercraftMutagenSocket3 = original.witchercraftMutagenSocket3;
		clone.witchercraftMutagenSocket4 = original.witchercraftMutagenSocket4;
		clone.witchercraftMutagenOwnedRed = original.witchercraftMutagenOwnedRed;
		clone.witchercraftMutagenOwnedGreen = original.witchercraftMutagenOwnedGreen;
		clone.witchercraftMutagenOwnedBlue = original.witchercraftMutagenOwnedBlue;
		clone.witchercraftSelectedPerk = original.witchercraftSelectedPerk;
		clone.witchercraftEquippedPerkAnatomicalKnowledge = original.witchercraftEquippedPerkAnatomicalKnowledge;
		clone.witchercraftEquippedPerkColdBlood = original.witchercraftEquippedPerkColdBlood;
		clone.witchercraftEquippedPerkCripplingShot = original.witchercraftEquippedPerkCripplingShot;
		clone.witchercraftEquippedPerkCripplingStrikes = original.witchercraftEquippedPerkCripplingStrikes;
		clone.witchercraftEquippedPerkCrushingBlows = original.witchercraftEquippedPerkCrushingBlows;
		clone.witchercraftEquippedPerkDeadlyPrecision = original.witchercraftEquippedPerkDeadlyPrecision;
		clone.witchercraftEquippedPerkDefence = original.witchercraftEquippedPerkDefence;
		clone.witchercraftEquippedPerkFleetFooted = original.witchercraftEquippedPerkFleetFooted;
		clone.witchercraftEquippedPerkFloodOfAnger = original.witchercraftEquippedPerkFloodOfAnger;
		clone.witchercraftEquippedPerkMuscleMemory = original.witchercraftEquippedPerkMuscleMemory;
		clone.witchercraftEquippedPerkPreciseBlows = original.witchercraftEquippedPerkPreciseBlows;
		clone.witchercraftEquippedPerkRazorFocus = original.witchercraftEquippedPerkRazorFocus;
		clone.witchercraftEquippedPerkStrengthTraining = original.witchercraftEquippedPerkStrengthTraining;
		clone.witchercraftEquippedPerkSunderArmor = original.witchercraftEquippedPerkSunderArmor;
		clone.witchercraftEquippedPerkUndying = original.witchercraftEquippedPerkUndying;
		clone.witchercraftEquippedPerkClusterBombs = original.witchercraftEquippedPerkClusterBombs;
		clone.witchercraftEquippedPerkDelayedRecovery = original.witchercraftEquippedPerkDelayedRecovery;
		clone.witchercraftEquippedPerkEfficiency = original.witchercraftEquippedPerkEfficiency;
		clone.witchercraftEquippedPerkHunterInstinct = original.witchercraftEquippedPerkHunterInstinct;
		clone.witchercraftEquippedPerkPoisonedBlades = original.witchercraftEquippedPerkPoisonedBlades;
		clone.witchercraftEquippedPerkProtectiveCoating = original.witchercraftEquippedPerkProtectiveCoating;
		clone.witchercraftEquippedPerkPyrotechnics = original.witchercraftEquippedPerkPyrotechnics;
		clone.witchercraftEquippedPerkRefreshment = original.witchercraftEquippedPerkRefreshment;
		clone.witchercraftEquippedPerkSideEffects = original.witchercraftEquippedPerkSideEffects;
		clone.witchercraftEquippedPerkAardIntensity = original.witchercraftEquippedPerkAardIntensity;
		clone.witchercraftEquippedPerkAxiiIntensity = original.witchercraftEquippedPerkAxiiIntensity;
		clone.witchercraftEquippedPerkDelusion = original.witchercraftEquippedPerkDelusion;
		clone.witchercraftEquippedPerkDomination = original.witchercraftEquippedPerkDomination;
		clone.witchercraftEquippedPerkExplodingShield = original.witchercraftEquippedPerkExplodingShield;
		clone.witchercraftEquippedPerkFarReachingAard = original.witchercraftEquippedPerkFarReachingAard;
		clone.witchercraftEquippedPerkFirestream = original.witchercraftEquippedPerkFirestream;
		clone.witchercraftEquippedPerkIgniIntensity = original.witchercraftEquippedPerkIgniIntensity;
		clone.witchercraftEquippedPerkMagicTrap = original.witchercraftEquippedPerkMagicTrap;
		clone.witchercraftEquippedPerkPyromaniac = original.witchercraftEquippedPerkPyromaniac;
		clone.witchercraftEquippedPerkQuenDischarge = original.witchercraftEquippedPerkQuenDischarge;
		clone.witchercraftEquippedPerkQuenIntensity = original.witchercraftEquippedPerkQuenIntensity;
		clone.witchercraftEquippedPerkShockWave = original.witchercraftEquippedPerkShockWave;
		clone.witchercraftEquippedPerkSustainedGlyphs = original.witchercraftEquippedPerkSustainedGlyphs;
		clone.witchercraftEquippedPerkYrdenIntensity = original.witchercraftEquippedPerkYrdenIntensity;
		clone.witchercraftEquippedPerkBearSchool = original.witchercraftEquippedPerkBearSchool;
		clone.witchercraftEquippedPerkCatSchool = original.witchercraftEquippedPerkCatSchool;
		clone.witchercraftEquippedPerkGourmet = original.witchercraftEquippedPerkGourmet;
		clone.witchercraftEquippedPerkGriffinSchool = original.witchercraftEquippedPerkGriffinSchool;
		clone.witchercraftEquippedPerkSunAndStars = original.witchercraftEquippedPerkSunAndStars;
		clone.witchercraftEquippedPerkSurvivalInstinct = original.witchercraftEquippedPerkSurvivalInstinct;
		clone.witchercraftPerksSurvivalInstinct = original.witchercraftPerksSurvivalInstinct;
		clone.witchercraftPerksSunAndStars = original.witchercraftPerksSunAndStars;
		clone.witchercraftPerksCatSchool = original.witchercraftPerksCatSchool;
		clone.witchercraftPerksGriffinSchool = original.witchercraftPerksGriffinSchool;
		clone.witchercraftPerksBearSchool = original.witchercraftPerksBearSchool;
		clone.witchercraftPerksRefreshment = original.witchercraftPerksRefreshment;
		clone.witchercraftPerksDelayedRecovery = original.witchercraftPerksDelayedRecovery;
		clone.witchercraftPerksSideEffects = original.witchercraftPerksSideEffects;
		clone.witchercraftPerksPoisonedBlades = original.witchercraftPerksPoisonedBlades;
		clone.witchercraftPerksProtectiveCoating = original.witchercraftPerksProtectiveCoating;
		clone.witchercraftPerksHunterInstinct = original.witchercraftPerksHunterInstinct;
		clone.witchercraftPerksPyrotechnics = original.witchercraftPerksPyrotechnics;
		clone.witchercraftPerksEfficiency = original.witchercraftPerksEfficiency;
		clone.witchercraftPerksClusterBombs = original.witchercraftPerksClusterBombs;
		clone.witchercraftPerksFarReachingAard = original.witchercraftPerksFarReachingAard;
		clone.witchercraftPerksAardIntensity = original.witchercraftPerksAardIntensity;
		clone.witchercraftPerksShockWave = original.witchercraftPerksShockWave;
		clone.witchercraftPerksFirestream = original.witchercraftPerksFirestream;
		clone.witchercraftPerksIgniIntensity = original.witchercraftPerksIgniIntensity;
		clone.witchercraftPerksPyromaniac = original.witchercraftPerksPyromaniac;
		clone.witchercraftPerksSustainedGlyphs = original.witchercraftPerksSustainedGlyphs;
		clone.witchercraftPerksYrdenIntensity = original.witchercraftPerksYrdenIntensity;
		clone.witchercraftPerksMagicTrap = original.witchercraftPerksMagicTrap;
		clone.witchercraftPerksExplodingShield = original.witchercraftPerksExplodingShield;
		clone.witchercraftPerksQuenIntensity = original.witchercraftPerksQuenIntensity;
		clone.witchercraftPerksQuenDischarge = original.witchercraftPerksQuenDischarge;
		clone.witchercraftPerksDelusion = original.witchercraftPerksDelusion;
		clone.witchercraftPerksAxiiIntensity = original.witchercraftPerksAxiiIntensity;
		clone.witchercraftPerksDomination = original.witchercraftPerksDomination;
		clone.witchercraftPerksMuscleMemory = original.witchercraftPerksMuscleMemory;
		clone.witchercraftPerksPreciseBlows = original.witchercraftPerksPreciseBlows;
		clone.witchercraftPerksCripplingStrikes = original.witchercraftPerksCripplingStrikes;
		clone.witchercraftPerksStrengthTraining = original.witchercraftPerksStrengthTraining;
		clone.witchercraftPerksCrushingBlows = original.witchercraftPerksCrushingBlows;
		clone.witchercraftPerksSunderArmor = original.witchercraftPerksSunderArmor;
		clone.witchercraftPerksFleetFooted = original.witchercraftPerksFleetFooted;
		clone.witchercraftPerksDefence = original.witchercraftPerksDefence;
		clone.witchercraftPerksDeadlyPrecision = original.witchercraftPerksDeadlyPrecision;
		clone.witchercraftPerksColdBlood = original.witchercraftPerksColdBlood;
		clone.witchercraftPerksAnatomicalKnowledge = original.witchercraftPerksAnatomicalKnowledge;
		clone.witchercraftPerksCripplingShot = original.witchercraftPerksCripplingShot;
		clone.witchercraftPerksFloodOfAnger = original.witchercraftPerksFloodOfAnger;
		clone.witchercraftPerksRazorFocus = original.witchercraftPerksRazorFocus;
		clone.witchercraftPerksUndying = original.witchercraftPerksUndying;
		clone.witchercraftPlayerLevel = original.witchercraftPlayerLevel;
		clone.witchercraftMedallion = original.witchercraftMedallion;
		clone.witchercraftPlayerExperience = original.witchercraftPlayerExperience;
		clone.witchercraftPlayerExperienceRequirement = original.witchercraftPlayerExperienceRequirement;
		clone.witchercraftPerksCombatSkillPointsUsed = original.witchercraftPerksCombatSkillPointsUsed;
		clone.witchercraftPerksAlchemySkillPointsUsed = original.witchercraftPerksAlchemySkillPointsUsed;
		clone.witchercraftPerksSignsSkillPointsUsed = original.witchercraftPerksSignsSkillPointsUsed;
		if (!event.isWasDeath()) {
			clone.witchercraftToxicity = original.witchercraftToxicity;
			clone.witchercraftEnemyNearby = original.witchercraftEnemyNearby;
			clone.witchercraftQuenShield = original.witchercraftQuenShield;
			clone.witchercraftQuenShieldMax = original.witchercraftQuenShieldMax;
			clone.witchercraftWyvernDecoctionHit = original.witchercraftWyvernDecoctionHit;
			clone.witchercraftSuccubusDecoctionTick = original.witchercraftSuccubusDecoctionTick;
			clone.witchercraftGraveHagDecoctionKill = original.witchercraftGraveHagDecoctionKill;
			clone.witchercraftSignKeyHoldTime = original.witchercraftSignKeyHoldTime;
			clone.witchercraftSignKeyHold = original.witchercraftSignKeyHold;
			clone.witchercraftStaminaRegenBuffer = original.witchercraftStaminaRegenBuffer;
			clone.witchercraftSignNoCast = original.witchercraftSignNoCast;
			clone.witchercraftSignHoldCostPerSecond = original.witchercraftSignHoldCostPerSecond;
		}
		event.getEntity().setData(PLAYER_VARIABLES, clone);
	}

	public static class PlayerVariables implements ValueIOSerializable {
		boolean _syncDirty = false;
		public double witchercraftPerksLearned = 0.0;
		public boolean witchercraftPerksGourmet = false;
		public double witchercraftPerkSocket1 = 0.0;
		public double witchercraftPerkSocket2 = 0;
		public double witchercraftPerkSocket3 = 0;
		public double witchercraftPerkSocket4 = 0;
		public double witchercraftPerkSocket5 = 0;
		public double witchercraftPerkSocket6 = 0;
		public double witchercraftPerkSocket7 = 0;
		public double witchercraftPerkSocket8 = 0;
		public double witchercraftPerkSocket9 = 0;
		public double witchercraftPerkSocket10 = 0;
		public double witchercraftPerkSocket11 = 0;
		public double witchercraftPerkSocket12 = 0;
		public double witchercraftMutagenSocket1 = 0;
		public double witchercraftMutagenSocket2 = 0;
		public double witchercraftMutagenSocket3 = 0;
		public double witchercraftMutagenSocket4 = 0;
		public double witchercraftMutagenOwnedRed = 0;
		public double witchercraftMutagenOwnedGreen = 0;
		public double witchercraftMutagenOwnedBlue = 0;
		public double witchercraftSelectedPerk = 0;
		public boolean witchercraftEquippedPerkAnatomicalKnowledge = false;
		public boolean witchercraftEquippedPerkColdBlood = false;
		public boolean witchercraftEquippedPerkCripplingShot = false;
		public boolean witchercraftEquippedPerkCripplingStrikes = false;
		public boolean witchercraftEquippedPerkCrushingBlows = false;
		public boolean witchercraftEquippedPerkDeadlyPrecision = false;
		public boolean witchercraftEquippedPerkDefence = false;
		public boolean witchercraftEquippedPerkFleetFooted = false;
		public boolean witchercraftEquippedPerkFloodOfAnger = false;
		public boolean witchercraftEquippedPerkMuscleMemory = false;
		public boolean witchercraftEquippedPerkPreciseBlows = false;
		public boolean witchercraftEquippedPerkRazorFocus = false;
		public boolean witchercraftEquippedPerkStrengthTraining = false;
		public boolean witchercraftEquippedPerkSunderArmor = false;
		public boolean witchercraftEquippedPerkUndying = false;
		public boolean witchercraftEquippedPerkClusterBombs = false;
		public boolean witchercraftEquippedPerkDelayedRecovery = false;
		public boolean witchercraftEquippedPerkEfficiency = false;
		public boolean witchercraftEquippedPerkHunterInstinct = false;
		public boolean witchercraftEquippedPerkPoisonedBlades = false;
		public boolean witchercraftEquippedPerkProtectiveCoating = false;
		public boolean witchercraftEquippedPerkPyrotechnics = false;
		public boolean witchercraftEquippedPerkRefreshment = false;
		public boolean witchercraftEquippedPerkSideEffects = false;
		public boolean witchercraftEquippedPerkAardIntensity = false;
		public boolean witchercraftEquippedPerkAxiiIntensity = false;
		public boolean witchercraftEquippedPerkDelusion = false;
		public boolean witchercraftEquippedPerkDomination = false;
		public boolean witchercraftEquippedPerkExplodingShield = false;
		public boolean witchercraftEquippedPerkFarReachingAard = false;
		public boolean witchercraftEquippedPerkFirestream = false;
		public boolean witchercraftEquippedPerkIgniIntensity = false;
		public boolean witchercraftEquippedPerkMagicTrap = false;
		public boolean witchercraftEquippedPerkPyromaniac = false;
		public boolean witchercraftEquippedPerkQuenDischarge = false;
		public boolean witchercraftEquippedPerkQuenIntensity = false;
		public boolean witchercraftEquippedPerkShockWave = false;
		public boolean witchercraftEquippedPerkSustainedGlyphs = false;
		public boolean witchercraftEquippedPerkYrdenIntensity = false;
		public boolean witchercraftEquippedPerkBearSchool = false;
		public boolean witchercraftEquippedPerkCatSchool = false;
		public boolean witchercraftEquippedPerkGourmet = false;
		public boolean witchercraftEquippedPerkGriffinSchool = false;
		public boolean witchercraftEquippedPerkSunAndStars = false;
		public boolean witchercraftEquippedPerkSurvivalInstinct = false;
		public boolean witchercraftPerksSurvivalInstinct = false;
		public boolean witchercraftPerksSunAndStars = false;
		public boolean witchercraftPerksCatSchool = false;
		public boolean witchercraftPerksGriffinSchool = false;
		public boolean witchercraftPerksBearSchool = false;
		public boolean witchercraftPerksRefreshment = false;
		public boolean witchercraftPerksDelayedRecovery = false;
		public boolean witchercraftPerksSideEffects = false;
		public boolean witchercraftPerksPoisonedBlades = false;
		public boolean witchercraftPerksProtectiveCoating = false;
		public boolean witchercraftPerksHunterInstinct = false;
		public boolean witchercraftPerksPyrotechnics = false;
		public boolean witchercraftPerksEfficiency = false;
		public boolean witchercraftPerksClusterBombs = false;
		public boolean witchercraftPerksFarReachingAard = false;
		public boolean witchercraftPerksAardIntensity = false;
		public boolean witchercraftPerksShockWave = false;
		public boolean witchercraftPerksFirestream = false;
		public boolean witchercraftPerksIgniIntensity = false;
		public boolean witchercraftPerksPyromaniac = false;
		public boolean witchercraftPerksSustainedGlyphs = false;
		public boolean witchercraftPerksYrdenIntensity = false;
		public boolean witchercraftPerksMagicTrap = false;
		public boolean witchercraftPerksExplodingShield = false;
		public boolean witchercraftPerksQuenIntensity = false;
		public boolean witchercraftPerksQuenDischarge = false;
		public boolean witchercraftPerksDelusion = false;
		public boolean witchercraftPerksAxiiIntensity = false;
		public boolean witchercraftPerksDomination = false;
		public boolean witchercraftPerksMuscleMemory = false;
		public boolean witchercraftPerksPreciseBlows = false;
		public boolean witchercraftPerksCripplingStrikes = false;
		public boolean witchercraftPerksStrengthTraining = false;
		public boolean witchercraftPerksCrushingBlows = false;
		public boolean witchercraftPerksSunderArmor = false;
		public boolean witchercraftPerksFleetFooted = false;
		public boolean witchercraftPerksDefence = false;
		public boolean witchercraftPerksDeadlyPrecision = false;
		public boolean witchercraftPerksColdBlood = false;
		public boolean witchercraftPerksAnatomicalKnowledge = false;
		public boolean witchercraftPerksCripplingShot = false;
		public boolean witchercraftPerksFloodOfAnger = false;
		public boolean witchercraftPerksRazorFocus = false;
		public boolean witchercraftPerksUndying = false;
		public double witchercraftPlayerLevel = 0;
		public double witchercraftMedallion = 0;
		public double witchercraftPlayerExperience = 0;
		public double witchercraftPlayerExperienceRequirement = 0.0;
		public double witchercraftToxicity = 0;
		public double witchercraftPerksCombatSkillPointsUsed = 0;
		public double witchercraftPerksAlchemySkillPointsUsed = 0;
		public double witchercraftPerksSignsSkillPointsUsed = 0;
		public boolean witchercraftEnemyNearby = false;
		public double witchercraftQuenShield = 0;
		public double witchercraftQuenShieldMax = 0;
		public double witchercraftWyvernDecoctionHit = 0;
		public double witchercraftSuccubusDecoctionTick = 0;
		public double witchercraftGraveHagDecoctionKill = 0;
		public double witchercraftSignKeyHoldTime = 0;
		public boolean witchercraftSignKeyHold = false;
		public double witchercraftStaminaRegenBuffer = 0;
		public boolean witchercraftSignNoCast = false;
		public double witchercraftSignHoldCostPerSecond = 0;

		@Override
		public void serialize(ValueOutput output) {
			output.putDouble("witchercraftPerksLearned", witchercraftPerksLearned);
			output.putBoolean("witchercraftPerksGourmet", witchercraftPerksGourmet);
			output.putDouble("witchercraftPerkSocket1", witchercraftPerkSocket1);
			output.putDouble("witchercraftPerkSocket2", witchercraftPerkSocket2);
			output.putDouble("witchercraftPerkSocket3", witchercraftPerkSocket3);
			output.putDouble("witchercraftPerkSocket4", witchercraftPerkSocket4);
			output.putDouble("witchercraftPerkSocket5", witchercraftPerkSocket5);
			output.putDouble("witchercraftPerkSocket6", witchercraftPerkSocket6);
			output.putDouble("witchercraftPerkSocket7", witchercraftPerkSocket7);
			output.putDouble("witchercraftPerkSocket8", witchercraftPerkSocket8);
			output.putDouble("witchercraftPerkSocket9", witchercraftPerkSocket9);
			output.putDouble("witchercraftPerkSocket10", witchercraftPerkSocket10);
			output.putDouble("witchercraftPerkSocket11", witchercraftPerkSocket11);
			output.putDouble("witchercraftPerkSocket12", witchercraftPerkSocket12);
			output.putDouble("witchercraftMutagenSocket1", witchercraftMutagenSocket1);
			output.putDouble("witchercraftMutagenSocket2", witchercraftMutagenSocket2);
			output.putDouble("witchercraftMutagenSocket3", witchercraftMutagenSocket3);
			output.putDouble("witchercraftMutagenSocket4", witchercraftMutagenSocket4);
			output.putDouble("witchercraftMutagenOwnedRed", witchercraftMutagenOwnedRed);
			output.putDouble("witchercraftMutagenOwnedGreen", witchercraftMutagenOwnedGreen);
			output.putDouble("witchercraftMutagenOwnedBlue", witchercraftMutagenOwnedBlue);
			output.putDouble("witchercraftSelectedPerk", witchercraftSelectedPerk);
			output.putBoolean("witchercraftEquippedPerkAnatomicalKnowledge", witchercraftEquippedPerkAnatomicalKnowledge);
			output.putBoolean("witchercraftEquippedPerkColdBlood", witchercraftEquippedPerkColdBlood);
			output.putBoolean("witchercraftEquippedPerkCripplingShot", witchercraftEquippedPerkCripplingShot);
			output.putBoolean("witchercraftEquippedPerkCripplingStrikes", witchercraftEquippedPerkCripplingStrikes);
			output.putBoolean("witchercraftEquippedPerkCrushingBlows", witchercraftEquippedPerkCrushingBlows);
			output.putBoolean("witchercraftEquippedPerkDeadlyPrecision", witchercraftEquippedPerkDeadlyPrecision);
			output.putBoolean("witchercraftEquippedPerkDefence", witchercraftEquippedPerkDefence);
			output.putBoolean("witchercraftEquippedPerkFleetFooted", witchercraftEquippedPerkFleetFooted);
			output.putBoolean("witchercraftEquippedPerkFloodOfAnger", witchercraftEquippedPerkFloodOfAnger);
			output.putBoolean("witchercraftEquippedPerkMuscleMemory", witchercraftEquippedPerkMuscleMemory);
			output.putBoolean("witchercraftEquippedPerkPreciseBlows", witchercraftEquippedPerkPreciseBlows);
			output.putBoolean("witchercraftEquippedPerkRazorFocus", witchercraftEquippedPerkRazorFocus);
			output.putBoolean("witchercraftEquippedPerkStrengthTraining", witchercraftEquippedPerkStrengthTraining);
			output.putBoolean("witchercraftEquippedPerkSunderArmor", witchercraftEquippedPerkSunderArmor);
			output.putBoolean("witchercraftEquippedPerkUndying", witchercraftEquippedPerkUndying);
			output.putBoolean("witchercraftEquippedPerkClusterBombs", witchercraftEquippedPerkClusterBombs);
			output.putBoolean("witchercraftEquippedPerkDelayedRecovery", witchercraftEquippedPerkDelayedRecovery);
			output.putBoolean("witchercraftEquippedPerkEfficiency", witchercraftEquippedPerkEfficiency);
			output.putBoolean("witchercraftEquippedPerkHunterInstinct", witchercraftEquippedPerkHunterInstinct);
			output.putBoolean("witchercraftEquippedPerkPoisonedBlades", witchercraftEquippedPerkPoisonedBlades);
			output.putBoolean("witchercraftEquippedPerkProtectiveCoating", witchercraftEquippedPerkProtectiveCoating);
			output.putBoolean("witchercraftEquippedPerkPyrotechnics", witchercraftEquippedPerkPyrotechnics);
			output.putBoolean("witchercraftEquippedPerkRefreshment", witchercraftEquippedPerkRefreshment);
			output.putBoolean("witchercraftEquippedPerkSideEffects", witchercraftEquippedPerkSideEffects);
			output.putBoolean("witchercraftEquippedPerkAardIntensity", witchercraftEquippedPerkAardIntensity);
			output.putBoolean("witchercraftEquippedPerkAxiiIntensity", witchercraftEquippedPerkAxiiIntensity);
			output.putBoolean("witchercraftEquippedPerkDelusion", witchercraftEquippedPerkDelusion);
			output.putBoolean("witchercraftEquippedPerkDomination", witchercraftEquippedPerkDomination);
			output.putBoolean("witchercraftEquippedPerkExplodingShield", witchercraftEquippedPerkExplodingShield);
			output.putBoolean("witchercraftEquippedPerkFarReachingAard", witchercraftEquippedPerkFarReachingAard);
			output.putBoolean("witchercraftEquippedPerkFirestream", witchercraftEquippedPerkFirestream);
			output.putBoolean("witchercraftEquippedPerkIgniIntensity", witchercraftEquippedPerkIgniIntensity);
			output.putBoolean("witchercraftEquippedPerkMagicTrap", witchercraftEquippedPerkMagicTrap);
			output.putBoolean("witchercraftEquippedPerkPyromaniac", witchercraftEquippedPerkPyromaniac);
			output.putBoolean("witchercraftEquippedPerkQuenDischarge", witchercraftEquippedPerkQuenDischarge);
			output.putBoolean("witchercraftEquippedPerkQuenIntensity", witchercraftEquippedPerkQuenIntensity);
			output.putBoolean("witchercraftEquippedPerkShockWave", witchercraftEquippedPerkShockWave);
			output.putBoolean("witchercraftEquippedPerkSustainedGlyphs", witchercraftEquippedPerkSustainedGlyphs);
			output.putBoolean("witchercraftEquippedPerkYrdenIntensity", witchercraftEquippedPerkYrdenIntensity);
			output.putBoolean("witchercraftEquippedPerkBearSchool", witchercraftEquippedPerkBearSchool);
			output.putBoolean("witchercraftEquippedPerkCatSchool", witchercraftEquippedPerkCatSchool);
			output.putBoolean("witchercraftEquippedPerkGourmet", witchercraftEquippedPerkGourmet);
			output.putBoolean("witchercraftEquippedPerkGriffinSchool", witchercraftEquippedPerkGriffinSchool);
			output.putBoolean("witchercraftEquippedPerkSunAndStars", witchercraftEquippedPerkSunAndStars);
			output.putBoolean("witchercraftEquippedPerkSurvivalInstinct", witchercraftEquippedPerkSurvivalInstinct);
			output.putBoolean("witchercraftPerksSurvivalInstinct", witchercraftPerksSurvivalInstinct);
			output.putBoolean("witchercraftPerksSunAndStars", witchercraftPerksSunAndStars);
			output.putBoolean("witchercraftPerksCatSchool", witchercraftPerksCatSchool);
			output.putBoolean("witchercraftPerksGriffinSchool", witchercraftPerksGriffinSchool);
			output.putBoolean("witchercraftPerksBearSchool", witchercraftPerksBearSchool);
			output.putBoolean("witchercraftPerksRefreshment", witchercraftPerksRefreshment);
			output.putBoolean("witchercraftPerksDelayedRecovery", witchercraftPerksDelayedRecovery);
			output.putBoolean("witchercraftPerksSideEffects", witchercraftPerksSideEffects);
			output.putBoolean("witchercraftPerksPoisonedBlades", witchercraftPerksPoisonedBlades);
			output.putBoolean("witchercraftPerksProtectiveCoating", witchercraftPerksProtectiveCoating);
			output.putBoolean("witchercraftPerksHunterInstinct", witchercraftPerksHunterInstinct);
			output.putBoolean("witchercraftPerksPyrotechnics", witchercraftPerksPyrotechnics);
			output.putBoolean("witchercraftPerksEfficiency", witchercraftPerksEfficiency);
			output.putBoolean("witchercraftPerksClusterBombs", witchercraftPerksClusterBombs);
			output.putBoolean("witchercraftPerksFarReachingAard", witchercraftPerksFarReachingAard);
			output.putBoolean("witchercraftPerksAardIntensity", witchercraftPerksAardIntensity);
			output.putBoolean("witchercraftPerksShockWave", witchercraftPerksShockWave);
			output.putBoolean("witchercraftPerksFirestream", witchercraftPerksFirestream);
			output.putBoolean("witchercraftPerksIgniIntensity", witchercraftPerksIgniIntensity);
			output.putBoolean("witchercraftPerksPyromaniac", witchercraftPerksPyromaniac);
			output.putBoolean("witchercraftPerksSustainedGlyphs", witchercraftPerksSustainedGlyphs);
			output.putBoolean("witchercraftPerksYrdenIntensity", witchercraftPerksYrdenIntensity);
			output.putBoolean("witchercraftPerksMagicTrap", witchercraftPerksMagicTrap);
			output.putBoolean("witchercraftPerksExplodingShield", witchercraftPerksExplodingShield);
			output.putBoolean("witchercraftPerksQuenIntensity", witchercraftPerksQuenIntensity);
			output.putBoolean("witchercraftPerksQuenDischarge", witchercraftPerksQuenDischarge);
			output.putBoolean("witchercraftPerksDelusion", witchercraftPerksDelusion);
			output.putBoolean("witchercraftPerksAxiiIntensity", witchercraftPerksAxiiIntensity);
			output.putBoolean("witchercraftPerksDomination", witchercraftPerksDomination);
			output.putBoolean("witchercraftPerksMuscleMemory", witchercraftPerksMuscleMemory);
			output.putBoolean("witchercraftPerksPreciseBlows", witchercraftPerksPreciseBlows);
			output.putBoolean("witchercraftPerksCripplingStrikes", witchercraftPerksCripplingStrikes);
			output.putBoolean("witchercraftPerksStrengthTraining", witchercraftPerksStrengthTraining);
			output.putBoolean("witchercraftPerksCrushingBlows", witchercraftPerksCrushingBlows);
			output.putBoolean("witchercraftPerksSunderArmor", witchercraftPerksSunderArmor);
			output.putBoolean("witchercraftPerksFleetFooted", witchercraftPerksFleetFooted);
			output.putBoolean("witchercraftPerksDefence", witchercraftPerksDefence);
			output.putBoolean("witchercraftPerksDeadlyPrecision", witchercraftPerksDeadlyPrecision);
			output.putBoolean("witchercraftPerksColdBlood", witchercraftPerksColdBlood);
			output.putBoolean("witchercraftPerksAnatomicalKnowledge", witchercraftPerksAnatomicalKnowledge);
			output.putBoolean("witchercraftPerksCripplingShot", witchercraftPerksCripplingShot);
			output.putBoolean("witchercraftPerksFloodOfAnger", witchercraftPerksFloodOfAnger);
			output.putBoolean("witchercraftPerksRazorFocus", witchercraftPerksRazorFocus);
			output.putBoolean("witchercraftPerksUndying", witchercraftPerksUndying);
			output.putDouble("witchercraftPlayerLevel", witchercraftPlayerLevel);
			output.putDouble("witchercraftMedallion", witchercraftMedallion);
			output.putDouble("witchercraftPlayerExperience", witchercraftPlayerExperience);
			output.putDouble("witchercraftPlayerExperienceRequirement", witchercraftPlayerExperienceRequirement);
			output.putDouble("witchercraftToxicity", witchercraftToxicity);
			output.putDouble("witchercraftPerksCombatSkillPointsUsed", witchercraftPerksCombatSkillPointsUsed);
			output.putDouble("witchercraftPerksAlchemySkillPointsUsed", witchercraftPerksAlchemySkillPointsUsed);
			output.putDouble("witchercraftPerksSignsSkillPointsUsed", witchercraftPerksSignsSkillPointsUsed);
			output.putBoolean("witchercraftEnemyNearby", witchercraftEnemyNearby);
			output.putDouble("witchercraftQuenShield", witchercraftQuenShield);
			output.putDouble("witchercraftQuenShieldMax", witchercraftQuenShieldMax);
			output.putDouble("witchercraftWyvernDecoctionHit", witchercraftWyvernDecoctionHit);
			output.putDouble("witchercraftSuccubusDecoctionTick", witchercraftSuccubusDecoctionTick);
			output.putDouble("witchercraftGraveHagDecoctionKill", witchercraftGraveHagDecoctionKill);
			output.putDouble("witchercraftSignKeyHoldTime", witchercraftSignKeyHoldTime);
			output.putBoolean("witchercraftSignKeyHold", witchercraftSignKeyHold);
			output.putDouble("witchercraftStaminaRegenBuffer", witchercraftStaminaRegenBuffer);
			output.putBoolean("witchercraftSignNoCast", witchercraftSignNoCast);
			output.putDouble("witchercraftSignHoldCostPerSecond", witchercraftSignHoldCostPerSecond);
		}

		@Override
		public void deserialize(ValueInput input) {
			witchercraftPerksLearned = input.getDoubleOr("witchercraftPerksLearned", 0);
			witchercraftPerksGourmet = input.getBooleanOr("witchercraftPerksGourmet", false);
			witchercraftPerkSocket1 = input.getDoubleOr("witchercraftPerkSocket1", 0);
			witchercraftPerkSocket2 = input.getDoubleOr("witchercraftPerkSocket2", 0);
			witchercraftPerkSocket3 = input.getDoubleOr("witchercraftPerkSocket3", 0);
			witchercraftPerkSocket4 = input.getDoubleOr("witchercraftPerkSocket4", 0);
			witchercraftPerkSocket5 = input.getDoubleOr("witchercraftPerkSocket5", 0);
			witchercraftPerkSocket6 = input.getDoubleOr("witchercraftPerkSocket6", 0);
			witchercraftPerkSocket7 = input.getDoubleOr("witchercraftPerkSocket7", 0);
			witchercraftPerkSocket8 = input.getDoubleOr("witchercraftPerkSocket8", 0);
			witchercraftPerkSocket9 = input.getDoubleOr("witchercraftPerkSocket9", 0);
			witchercraftPerkSocket10 = input.getDoubleOr("witchercraftPerkSocket10", 0);
			witchercraftPerkSocket11 = input.getDoubleOr("witchercraftPerkSocket11", 0);
			witchercraftPerkSocket12 = input.getDoubleOr("witchercraftPerkSocket12", 0);
			witchercraftMutagenSocket1 = input.getDoubleOr("witchercraftMutagenSocket1", 0);
			witchercraftMutagenSocket2 = input.getDoubleOr("witchercraftMutagenSocket2", 0);
			witchercraftMutagenSocket3 = input.getDoubleOr("witchercraftMutagenSocket3", 0);
			witchercraftMutagenSocket4 = input.getDoubleOr("witchercraftMutagenSocket4", 0);
			witchercraftMutagenOwnedRed = input.getDoubleOr("witchercraftMutagenOwnedRed", 0);
			witchercraftMutagenOwnedGreen = input.getDoubleOr("witchercraftMutagenOwnedGreen", 0);
			witchercraftMutagenOwnedBlue = input.getDoubleOr("witchercraftMutagenOwnedBlue", 0);
			witchercraftSelectedPerk = input.getDoubleOr("witchercraftSelectedPerk", 0);
			witchercraftEquippedPerkAnatomicalKnowledge = input.getBooleanOr("witchercraftEquippedPerkAnatomicalKnowledge", false);
			witchercraftEquippedPerkColdBlood = input.getBooleanOr("witchercraftEquippedPerkColdBlood", false);
			witchercraftEquippedPerkCripplingShot = input.getBooleanOr("witchercraftEquippedPerkCripplingShot", false);
			witchercraftEquippedPerkCripplingStrikes = input.getBooleanOr("witchercraftEquippedPerkCripplingStrikes", false);
			witchercraftEquippedPerkCrushingBlows = input.getBooleanOr("witchercraftEquippedPerkCrushingBlows", false);
			witchercraftEquippedPerkDeadlyPrecision = input.getBooleanOr("witchercraftEquippedPerkDeadlyPrecision", false);
			witchercraftEquippedPerkDefence = input.getBooleanOr("witchercraftEquippedPerkDefence", false);
			witchercraftEquippedPerkFleetFooted = input.getBooleanOr("witchercraftEquippedPerkFleetFooted", false);
			witchercraftEquippedPerkFloodOfAnger = input.getBooleanOr("witchercraftEquippedPerkFloodOfAnger", false);
			witchercraftEquippedPerkMuscleMemory = input.getBooleanOr("witchercraftEquippedPerkMuscleMemory", false);
			witchercraftEquippedPerkPreciseBlows = input.getBooleanOr("witchercraftEquippedPerkPreciseBlows", false);
			witchercraftEquippedPerkRazorFocus = input.getBooleanOr("witchercraftEquippedPerkRazorFocus", false);
			witchercraftEquippedPerkStrengthTraining = input.getBooleanOr("witchercraftEquippedPerkStrengthTraining", false);
			witchercraftEquippedPerkSunderArmor = input.getBooleanOr("witchercraftEquippedPerkSunderArmor", false);
			witchercraftEquippedPerkUndying = input.getBooleanOr("witchercraftEquippedPerkUndying", false);
			witchercraftEquippedPerkClusterBombs = input.getBooleanOr("witchercraftEquippedPerkClusterBombs", false);
			witchercraftEquippedPerkDelayedRecovery = input.getBooleanOr("witchercraftEquippedPerkDelayedRecovery", false);
			witchercraftEquippedPerkEfficiency = input.getBooleanOr("witchercraftEquippedPerkEfficiency", false);
			witchercraftEquippedPerkHunterInstinct = input.getBooleanOr("witchercraftEquippedPerkHunterInstinct", false);
			witchercraftEquippedPerkPoisonedBlades = input.getBooleanOr("witchercraftEquippedPerkPoisonedBlades", false);
			witchercraftEquippedPerkProtectiveCoating = input.getBooleanOr("witchercraftEquippedPerkProtectiveCoating", false);
			witchercraftEquippedPerkPyrotechnics = input.getBooleanOr("witchercraftEquippedPerkPyrotechnics", false);
			witchercraftEquippedPerkRefreshment = input.getBooleanOr("witchercraftEquippedPerkRefreshment", false);
			witchercraftEquippedPerkSideEffects = input.getBooleanOr("witchercraftEquippedPerkSideEffects", false);
			witchercraftEquippedPerkAardIntensity = input.getBooleanOr("witchercraftEquippedPerkAardIntensity", false);
			witchercraftEquippedPerkAxiiIntensity = input.getBooleanOr("witchercraftEquippedPerkAxiiIntensity", false);
			witchercraftEquippedPerkDelusion = input.getBooleanOr("witchercraftEquippedPerkDelusion", false);
			witchercraftEquippedPerkDomination = input.getBooleanOr("witchercraftEquippedPerkDomination", false);
			witchercraftEquippedPerkExplodingShield = input.getBooleanOr("witchercraftEquippedPerkExplodingShield", false);
			witchercraftEquippedPerkFarReachingAard = input.getBooleanOr("witchercraftEquippedPerkFarReachingAard", false);
			witchercraftEquippedPerkFirestream = input.getBooleanOr("witchercraftEquippedPerkFirestream", false);
			witchercraftEquippedPerkIgniIntensity = input.getBooleanOr("witchercraftEquippedPerkIgniIntensity", false);
			witchercraftEquippedPerkMagicTrap = input.getBooleanOr("witchercraftEquippedPerkMagicTrap", false);
			witchercraftEquippedPerkPyromaniac = input.getBooleanOr("witchercraftEquippedPerkPyromaniac", false);
			witchercraftEquippedPerkQuenDischarge = input.getBooleanOr("witchercraftEquippedPerkQuenDischarge", false);
			witchercraftEquippedPerkQuenIntensity = input.getBooleanOr("witchercraftEquippedPerkQuenIntensity", false);
			witchercraftEquippedPerkShockWave = input.getBooleanOr("witchercraftEquippedPerkShockWave", false);
			witchercraftEquippedPerkSustainedGlyphs = input.getBooleanOr("witchercraftEquippedPerkSustainedGlyphs", false);
			witchercraftEquippedPerkYrdenIntensity = input.getBooleanOr("witchercraftEquippedPerkYrdenIntensity", false);
			witchercraftEquippedPerkBearSchool = input.getBooleanOr("witchercraftEquippedPerkBearSchool", false);
			witchercraftEquippedPerkCatSchool = input.getBooleanOr("witchercraftEquippedPerkCatSchool", false);
			witchercraftEquippedPerkGourmet = input.getBooleanOr("witchercraftEquippedPerkGourmet", false);
			witchercraftEquippedPerkGriffinSchool = input.getBooleanOr("witchercraftEquippedPerkGriffinSchool", false);
			witchercraftEquippedPerkSunAndStars = input.getBooleanOr("witchercraftEquippedPerkSunAndStars", false);
			witchercraftEquippedPerkSurvivalInstinct = input.getBooleanOr("witchercraftEquippedPerkSurvivalInstinct", false);
			witchercraftPerksSurvivalInstinct = input.getBooleanOr("witchercraftPerksSurvivalInstinct", false);
			witchercraftPerksSunAndStars = input.getBooleanOr("witchercraftPerksSunAndStars", false);
			witchercraftPerksCatSchool = input.getBooleanOr("witchercraftPerksCatSchool", false);
			witchercraftPerksGriffinSchool = input.getBooleanOr("witchercraftPerksGriffinSchool", false);
			witchercraftPerksBearSchool = input.getBooleanOr("witchercraftPerksBearSchool", false);
			witchercraftPerksRefreshment = input.getBooleanOr("witchercraftPerksRefreshment", false);
			witchercraftPerksDelayedRecovery = input.getBooleanOr("witchercraftPerksDelayedRecovery", false);
			witchercraftPerksSideEffects = input.getBooleanOr("witchercraftPerksSideEffects", false);
			witchercraftPerksPoisonedBlades = input.getBooleanOr("witchercraftPerksPoisonedBlades", false);
			witchercraftPerksProtectiveCoating = input.getBooleanOr("witchercraftPerksProtectiveCoating", false);
			witchercraftPerksHunterInstinct = input.getBooleanOr("witchercraftPerksHunterInstinct", false);
			witchercraftPerksPyrotechnics = input.getBooleanOr("witchercraftPerksPyrotechnics", false);
			witchercraftPerksEfficiency = input.getBooleanOr("witchercraftPerksEfficiency", false);
			witchercraftPerksClusterBombs = input.getBooleanOr("witchercraftPerksClusterBombs", false);
			witchercraftPerksFarReachingAard = input.getBooleanOr("witchercraftPerksFarReachingAard", false);
			witchercraftPerksAardIntensity = input.getBooleanOr("witchercraftPerksAardIntensity", false);
			witchercraftPerksShockWave = input.getBooleanOr("witchercraftPerksShockWave", false);
			witchercraftPerksFirestream = input.getBooleanOr("witchercraftPerksFirestream", false);
			witchercraftPerksIgniIntensity = input.getBooleanOr("witchercraftPerksIgniIntensity", false);
			witchercraftPerksPyromaniac = input.getBooleanOr("witchercraftPerksPyromaniac", false);
			witchercraftPerksSustainedGlyphs = input.getBooleanOr("witchercraftPerksSustainedGlyphs", false);
			witchercraftPerksYrdenIntensity = input.getBooleanOr("witchercraftPerksYrdenIntensity", false);
			witchercraftPerksMagicTrap = input.getBooleanOr("witchercraftPerksMagicTrap", false);
			witchercraftPerksExplodingShield = input.getBooleanOr("witchercraftPerksExplodingShield", false);
			witchercraftPerksQuenIntensity = input.getBooleanOr("witchercraftPerksQuenIntensity", false);
			witchercraftPerksQuenDischarge = input.getBooleanOr("witchercraftPerksQuenDischarge", false);
			witchercraftPerksDelusion = input.getBooleanOr("witchercraftPerksDelusion", false);
			witchercraftPerksAxiiIntensity = input.getBooleanOr("witchercraftPerksAxiiIntensity", false);
			witchercraftPerksDomination = input.getBooleanOr("witchercraftPerksDomination", false);
			witchercraftPerksMuscleMemory = input.getBooleanOr("witchercraftPerksMuscleMemory", false);
			witchercraftPerksPreciseBlows = input.getBooleanOr("witchercraftPerksPreciseBlows", false);
			witchercraftPerksCripplingStrikes = input.getBooleanOr("witchercraftPerksCripplingStrikes", false);
			witchercraftPerksStrengthTraining = input.getBooleanOr("witchercraftPerksStrengthTraining", false);
			witchercraftPerksCrushingBlows = input.getBooleanOr("witchercraftPerksCrushingBlows", false);
			witchercraftPerksSunderArmor = input.getBooleanOr("witchercraftPerksSunderArmor", false);
			witchercraftPerksFleetFooted = input.getBooleanOr("witchercraftPerksFleetFooted", false);
			witchercraftPerksDefence = input.getBooleanOr("witchercraftPerksDefence", false);
			witchercraftPerksDeadlyPrecision = input.getBooleanOr("witchercraftPerksDeadlyPrecision", false);
			witchercraftPerksColdBlood = input.getBooleanOr("witchercraftPerksColdBlood", false);
			witchercraftPerksAnatomicalKnowledge = input.getBooleanOr("witchercraftPerksAnatomicalKnowledge", false);
			witchercraftPerksCripplingShot = input.getBooleanOr("witchercraftPerksCripplingShot", false);
			witchercraftPerksFloodOfAnger = input.getBooleanOr("witchercraftPerksFloodOfAnger", false);
			witchercraftPerksRazorFocus = input.getBooleanOr("witchercraftPerksRazorFocus", false);
			witchercraftPerksUndying = input.getBooleanOr("witchercraftPerksUndying", false);
			witchercraftPlayerLevel = input.getDoubleOr("witchercraftPlayerLevel", 0);
			witchercraftMedallion = input.getDoubleOr("witchercraftMedallion", 0);
			witchercraftPlayerExperience = input.getDoubleOr("witchercraftPlayerExperience", 0);
			witchercraftPlayerExperienceRequirement = input.getDoubleOr("witchercraftPlayerExperienceRequirement", 0);
			witchercraftToxicity = input.getDoubleOr("witchercraftToxicity", 0);
			witchercraftPerksCombatSkillPointsUsed = input.getDoubleOr("witchercraftPerksCombatSkillPointsUsed", 0);
			witchercraftPerksAlchemySkillPointsUsed = input.getDoubleOr("witchercraftPerksAlchemySkillPointsUsed", 0);
			witchercraftPerksSignsSkillPointsUsed = input.getDoubleOr("witchercraftPerksSignsSkillPointsUsed", 0);
			witchercraftEnemyNearby = input.getBooleanOr("witchercraftEnemyNearby", false);
			witchercraftQuenShield = input.getDoubleOr("witchercraftQuenShield", 0);
			witchercraftQuenShieldMax = input.getDoubleOr("witchercraftQuenShieldMax", 0);
			witchercraftWyvernDecoctionHit = input.getDoubleOr("witchercraftWyvernDecoctionHit", 0);
			witchercraftSuccubusDecoctionTick = input.getDoubleOr("witchercraftSuccubusDecoctionTick", 0);
			witchercraftGraveHagDecoctionKill = input.getDoubleOr("witchercraftGraveHagDecoctionKill", 0);
			witchercraftSignKeyHoldTime = input.getDoubleOr("witchercraftSignKeyHoldTime", 0);
			witchercraftSignKeyHold = input.getBooleanOr("witchercraftSignKeyHold", false);
			witchercraftStaminaRegenBuffer = input.getDoubleOr("witchercraftStaminaRegenBuffer", 0);
			witchercraftSignNoCast = input.getBooleanOr("witchercraftSignNoCast", false);
			witchercraftSignHoldCostPerSecond = input.getDoubleOr("witchercraftSignHoldCostPerSecond", 0);
		}

		public void markSyncDirty() {
			_syncDirty = true;
		}
	}

	public record PlayerVariablesSyncMessage(PlayerVariables data) implements CustomPacketPayload {
		public static final Type<PlayerVariablesSyncMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "player_variables_sync"));
		public static final StreamCodec<RegistryFriendlyByteBuf, PlayerVariablesSyncMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, PlayerVariablesSyncMessage message) -> {
			TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, buffer.registryAccess());
			message.data.serialize(output);
			buffer.writeNbt(output.buildResult());
		}, (RegistryFriendlyByteBuf buffer) -> {
			PlayerVariablesSyncMessage message = new PlayerVariablesSyncMessage(new PlayerVariables());
			message.data.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, buffer.registryAccess(), buffer.readNbt()));
			return message;
		});

		@Override
		public Type<PlayerVariablesSyncMessage> type() {
			return TYPE;
		}

		public static void handleData(final PlayerVariablesSyncMessage message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.CLIENTBOUND && message.data != null) {
				context.enqueueWork(() -> {
					TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, context.player().registryAccess());
					message.data.serialize(output);
					context.player().getData(PLAYER_VARIABLES).deserialize(TagValueInput.create(ProblemReporter.DISCARDING, context.player().registryAccess(), output.buildResult()));
				}).exceptionally(e -> {
					context.connection().disconnect(Component.literal(e.getMessage()));
					return null;
				});
			}
		}
	}
}