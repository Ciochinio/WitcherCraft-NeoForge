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
	public static double sekund = 0;
	public static double minut = 0;

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
		clone.witchercraftAbilitiesLearned = original.witchercraftAbilitiesLearned;
		clone.witchercraftAbiltiesGourmet = original.witchercraftAbiltiesGourmet;
		clone.witchercraftAbilitiesSurvivalInstinct = original.witchercraftAbilitiesSurvivalInstinct;
		clone.witchercraftAbilitiesSunAndStars = original.witchercraftAbilitiesSunAndStars;
		clone.witchercraftAbilitiesCatSchool = original.witchercraftAbilitiesCatSchool;
		clone.witchercraftAbilitiesGriffinSchool = original.witchercraftAbilitiesGriffinSchool;
		clone.witchercraftAbilitiesBearSchool = original.witchercraftAbilitiesBearSchool;
		clone.witchercraftAbilitiesRefreshment = original.witchercraftAbilitiesRefreshment;
		clone.witchercraftAbilitiesDelayedRecovery = original.witchercraftAbilitiesDelayedRecovery;
		clone.witchercraftAbilitiesSideEffects = original.witchercraftAbilitiesSideEffects;
		clone.witchercraftAbilitiesPoisonedBlades = original.witchercraftAbilitiesPoisonedBlades;
		clone.witchercraftAbilitiesProtectiveCoating = original.witchercraftAbilitiesProtectiveCoating;
		clone.witchercraftAbilitiesHunterInstincts = original.witchercraftAbilitiesHunterInstincts;
		clone.witchercraftAbilitiesPyrotechnics = original.witchercraftAbilitiesPyrotechnics;
		clone.witchercraftAbilitiesEfficiency = original.witchercraftAbilitiesEfficiency;
		clone.witchercraftAbilitiesClusterBombs = original.witchercraftAbilitiesClusterBombs;
		clone.witchercraftAbilitiesFarReachingAard = original.witchercraftAbilitiesFarReachingAard;
		clone.witchercraftAbilitiesAardIntensity = original.witchercraftAbilitiesAardIntensity;
		clone.witchercraftAbilitiesShockWave = original.witchercraftAbilitiesShockWave;
		clone.witchercraftAbilitiesFireStream = original.witchercraftAbilitiesFireStream;
		clone.witchercraftAbilitiesIgniIntensity = original.witchercraftAbilitiesIgniIntensity;
		clone.witchercraftAbilitiesPyromaniac = original.witchercraftAbilitiesPyromaniac;
		clone.witchercraftAbilitiesSustainedGlyphs = original.witchercraftAbilitiesSustainedGlyphs;
		clone.witchercraftAbilitiesYrdenIntensity = original.witchercraftAbilitiesYrdenIntensity;
		clone.witchercraftAbilitiesMagicTrap = original.witchercraftAbilitiesMagicTrap;
		clone.witchercraftAbilitiesExploadingShield = original.witchercraftAbilitiesExploadingShield;
		clone.witchercraftAbilitiesQuenIntensity = original.witchercraftAbilitiesQuenIntensity;
		clone.witchercraftAbilitiesQuenDischarge = original.witchercraftAbilitiesQuenDischarge;
		clone.witchercraftAbilitiesDelusion = original.witchercraftAbilitiesDelusion;
		clone.witchercraftAbilitiesAxiiIntensity = original.witchercraftAbilitiesAxiiIntensity;
		clone.witchercraftAbilitiesDomination = original.witchercraftAbilitiesDomination;
		clone.witchercraftAbilitiesMuscleMemory = original.witchercraftAbilitiesMuscleMemory;
		clone.witchercraftAbilitiesPreciseBlows = original.witchercraftAbilitiesPreciseBlows;
		clone.witchercraftAbilitiesCripplingStrikes = original.witchercraftAbilitiesCripplingStrikes;
		clone.witchercraftAbilitiesStrengthTraining = original.witchercraftAbilitiesStrengthTraining;
		clone.witchercraftAbilitiesCrushingBlows = original.witchercraftAbilitiesCrushingBlows;
		clone.witchercraftAbilitiesSunderArmor = original.witchercraftAbilitiesSunderArmor;
		clone.witchercraftAbilitiesFleetFooted = original.witchercraftAbilitiesFleetFooted;
		clone.witchercraftAbilitiesDefence = original.witchercraftAbilitiesDefence;
		clone.witchercraftAbilitiesDeadlyPresicion = original.witchercraftAbilitiesDeadlyPresicion;
		clone.witchercraftAbilitiesColdBlood = original.witchercraftAbilitiesColdBlood;
		clone.witchercraftAbilitiesAnatomicalKnowledge = original.witchercraftAbilitiesAnatomicalKnowledge;
		clone.witchercraftAbilitiesCripplingShot = original.witchercraftAbilitiesCripplingShot;
		clone.witchercraftAbilitiesFloodOfAnger = original.witchercraftAbilitiesFloodOfAnger;
		clone.witchercraftAbilitiesRazorFocus = original.witchercraftAbilitiesRazorFocus;
		clone.witchercraftAbilitiesUndying = original.witchercraftAbilitiesUndying;
		clone.wichercraftPlayerLevel = original.wichercraftPlayerLevel;
		clone.wichercraftPlayerExperience = original.wichercraftPlayerExperience;
		clone.wichercraftAbilitesExperienceRequirement = original.wichercraftAbilitesExperienceRequirement;
		clone.witchercraftAbilitiesCombatSkillPointsUsed = original.witchercraftAbilitiesCombatSkillPointsUsed;
		clone.witchercraftAbilitiesAlchemySkillPointsUsed = original.witchercraftAbilitiesAlchemySkillPointsUsed;
		clone.witchercraftAbilitiesSignsSkillPointsUsed = original.witchercraftAbilitiesSignsSkillPointsUsed;
		if (!event.isWasDeath()) {
			clone.witchercraftMovementSpeed = original.witchercraftMovementSpeed;
			clone.witchercraftHealth = original.witchercraftHealth;
			clone.witchercraftArmor = original.witchercraftArmor;
			clone.witchercraftAdditionalDamage = original.witchercraftAdditionalDamage;
			clone.witchercraftCritChance = original.witchercraftCritChance;
			clone.witchercraftCritDamage = original.witchercraftCritDamage;
			clone.witchercraftAttackSpeed = original.witchercraftAttackSpeed;
			clone.witchercraftPassiveHealthRegeneration = original.witchercraftPassiveHealthRegeneration;
			clone.witchercraftBasePassiveHealthRegeneration = original.witchercraftBasePassiveHealthRegeneration;
			clone.witchercraftPassiveStaminaRegeneration = original.witchercraftPassiveStaminaRegeneration;
			clone.witchercraftToxicity = original.witchercraftToxicity;
			clone.ToxicityOverdoseThreshold = original.ToxicityOverdoseThreshold;
			clone.witchercraftIncreasedDamage = original.witchercraftIncreasedDamage;
			clone.witchercraftEnemyNearby = original.witchercraftEnemyNearby;
			clone.witchercraftDodgeChance = original.witchercraftDodgeChance;
			clone.witchercraftQuenShield = original.witchercraftQuenShield;
			clone.witchercraftInstantKillChance = original.witchercraftInstantKillChance;
			clone.witchercraftBasePassiveStaminaRegeneration = original.witchercraftBasePassiveStaminaRegeneration;
			clone.witchercraftPotionDuration = original.witchercraftPotionDuration;
			clone.witchercraftSignIntensity = original.witchercraftSignIntensity;
			clone.witchercraftOilDamage = original.witchercraftOilDamage;
			clone.witchercraftLifeSteal = original.witchercraftLifeSteal;
			clone.witchercraftBaseCritChance = original.witchercraftBaseCritChance;
			clone.witchercraftBaseCritDamage = original.witchercraftBaseCritDamage;
			clone.witchercraftReflectDamage = original.witchercraftReflectDamage;
			clone.witchercraftWyvenDecoctionHit = original.witchercraftWyvenDecoctionHit;
			clone.witchercraftSuccubusDecoctionTick = original.witchercraftSuccubusDecoctionTick;
			clone.witchercraftGraveHagDecoctionKill = original.witchercraftGraveHagDecoctionKill;
			clone.witchercraftSignKeyHoldTime = original.witchercraftSignKeyHoldTime;
			clone.witchercraftSignKeyHold = original.witchercraftSignKeyHold;
			clone.witchercraftTick = original.witchercraftTick;
			clone.witchercraftSignNoCast = original.witchercraftSignNoCast;
			clone.witchercraftSignCastHoldCost = original.witchercraftSignCastHoldCost;
		}
		event.getEntity().setData(PLAYER_VARIABLES, clone);
	}

	public static class PlayerVariables implements ValueIOSerializable {
		boolean _syncDirty = false;
		public double witchercraftMovementSpeed = 0.0;
		public double witchercraftHealth = 0.0;
		public double witchercraftArmor = 0.0;
		public double witchercraftAdditionalDamage = 0.0;
		public double witchercraftCritChance = 0.0;
		public double witchercraftCritDamage = 0.0;
		public double witchercraftAttackSpeed = 0;
		public double witchercraftPassiveHealthRegeneration = 0;
		public double witchercraftBasePassiveHealthRegeneration = 0;
		public double witchercraftPassiveStaminaRegeneration = 0;
		public double witchercraftAbilitiesLearned = 0.0;
		public boolean witchercraftAbiltiesGourmet = false;
		public boolean witchercraftAbilitiesSurvivalInstinct = false;
		public boolean witchercraftAbilitiesSunAndStars = false;
		public boolean witchercraftAbilitiesCatSchool = false;
		public boolean witchercraftAbilitiesGriffinSchool = false;
		public boolean witchercraftAbilitiesBearSchool = false;
		public boolean witchercraftAbilitiesRefreshment = false;
		public boolean witchercraftAbilitiesDelayedRecovery = false;
		public boolean witchercraftAbilitiesSideEffects = false;
		public boolean witchercraftAbilitiesPoisonedBlades = false;
		public boolean witchercraftAbilitiesProtectiveCoating = false;
		public boolean witchercraftAbilitiesHunterInstincts = false;
		public boolean witchercraftAbilitiesPyrotechnics = false;
		public boolean witchercraftAbilitiesEfficiency = false;
		public boolean witchercraftAbilitiesClusterBombs = false;
		public boolean witchercraftAbilitiesFarReachingAard = false;
		public boolean witchercraftAbilitiesAardIntensity = false;
		public boolean witchercraftAbilitiesShockWave = false;
		public boolean witchercraftAbilitiesFireStream = false;
		public boolean witchercraftAbilitiesIgniIntensity = false;
		public boolean witchercraftAbilitiesPyromaniac = false;
		public boolean witchercraftAbilitiesSustainedGlyphs = false;
		public boolean witchercraftAbilitiesYrdenIntensity = false;
		public boolean witchercraftAbilitiesMagicTrap = false;
		public boolean witchercraftAbilitiesExploadingShield = false;
		public boolean witchercraftAbilitiesQuenIntensity = false;
		public boolean witchercraftAbilitiesQuenDischarge = false;
		public boolean witchercraftAbilitiesDelusion = false;
		public boolean witchercraftAbilitiesAxiiIntensity = false;
		public boolean witchercraftAbilitiesDomination = false;
		public boolean witchercraftAbilitiesMuscleMemory = false;
		public boolean witchercraftAbilitiesPreciseBlows = false;
		public boolean witchercraftAbilitiesCripplingStrikes = false;
		public boolean witchercraftAbilitiesStrengthTraining = false;
		public boolean witchercraftAbilitiesCrushingBlows = false;
		public boolean witchercraftAbilitiesSunderArmor = false;
		public boolean witchercraftAbilitiesFleetFooted = false;
		public boolean witchercraftAbilitiesDefence = false;
		public boolean witchercraftAbilitiesDeadlyPresicion = false;
		public boolean witchercraftAbilitiesColdBlood = false;
		public boolean witchercraftAbilitiesAnatomicalKnowledge = false;
		public boolean witchercraftAbilitiesCripplingShot = false;
		public boolean witchercraftAbilitiesFloodOfAnger = false;
		public boolean witchercraftAbilitiesRazorFocus = false;
		public boolean witchercraftAbilitiesUndying = false;
		public double wichercraftPlayerLevel = 0;
		public double wichercraftPlayerExperience = 0;
		public double wichercraftAbilitesExperienceRequirement = 0.0;
		public double witchercraftToxicity = 0;
		public double ToxicityOverdoseThreshold = 0.0;
		public double witchercraftIncreasedDamage = 0;
		public double witchercraftAbilitiesCombatSkillPointsUsed = 0;
		public double witchercraftAbilitiesAlchemySkillPointsUsed = 0;
		public double witchercraftAbilitiesSignsSkillPointsUsed = 0;
		public boolean witchercraftEnemyNearby = false;
		public double witchercraftDodgeChance = 0;
		public double witchercraftQuenShield = 0;
		public double witchercraftInstantKillChance = 0;
		public double witchercraftBasePassiveStaminaRegeneration = 0;
		public double witchercraftPotionDuration = 0;
		public double witchercraftSignIntensity = 0;
		public double witchercraftOilDamage = 0;
		public double witchercraftLifeSteal = 0;
		public double witchercraftBaseCritChance = 0;
		public double witchercraftBaseCritDamage = 0;
		public double witchercraftReflectDamage = 0;
		public double witchercraftWyvenDecoctionHit = 0;
		public double witchercraftSuccubusDecoctionTick = 0;
		public double witchercraftGraveHagDecoctionKill = 0;
		public double witchercraftSignKeyHoldTime = 0;
		public boolean witchercraftSignKeyHold = false;
		public double witchercraftTick = 0;
		public boolean witchercraftSignNoCast = false;
		public double witchercraftSignCastHoldCost = 0;

		@Override
		public void serialize(ValueOutput output) {
			output.putDouble("witchercraftMovementSpeed", witchercraftMovementSpeed);
			output.putDouble("witchercraftHealth", witchercraftHealth);
			output.putDouble("witchercraftArmor", witchercraftArmor);
			output.putDouble("witchercraftAdditionalDamage", witchercraftAdditionalDamage);
			output.putDouble("witchercraftCritChance", witchercraftCritChance);
			output.putDouble("witchercraftCritDamage", witchercraftCritDamage);
			output.putDouble("witchercraftAttackSpeed", witchercraftAttackSpeed);
			output.putDouble("witchercraftPassiveHealthRegeneration", witchercraftPassiveHealthRegeneration);
			output.putDouble("witchercraftBasePassiveHealthRegeneration", witchercraftBasePassiveHealthRegeneration);
			output.putDouble("witchercraftPassiveStaminaRegeneration", witchercraftPassiveStaminaRegeneration);
			output.putDouble("witchercraftAbilitiesLearned", witchercraftAbilitiesLearned);
			output.putBoolean("witchercraftAbiltiesGourmet", witchercraftAbiltiesGourmet);
			output.putBoolean("witchercraftAbilitiesSurvivalInstinct", witchercraftAbilitiesSurvivalInstinct);
			output.putBoolean("witchercraftAbilitiesSunAndStars", witchercraftAbilitiesSunAndStars);
			output.putBoolean("witchercraftAbilitiesCatSchool", witchercraftAbilitiesCatSchool);
			output.putBoolean("witchercraftAbilitiesGriffinSchool", witchercraftAbilitiesGriffinSchool);
			output.putBoolean("witchercraftAbilitiesBearSchool", witchercraftAbilitiesBearSchool);
			output.putBoolean("witchercraftAbilitiesRefreshment", witchercraftAbilitiesRefreshment);
			output.putBoolean("witchercraftAbilitiesDelayedRecovery", witchercraftAbilitiesDelayedRecovery);
			output.putBoolean("witchercraftAbilitiesSideEffects", witchercraftAbilitiesSideEffects);
			output.putBoolean("witchercraftAbilitiesPoisonedBlades", witchercraftAbilitiesPoisonedBlades);
			output.putBoolean("witchercraftAbilitiesProtectiveCoating", witchercraftAbilitiesProtectiveCoating);
			output.putBoolean("witchercraftAbilitiesHunterInstincts", witchercraftAbilitiesHunterInstincts);
			output.putBoolean("witchercraftAbilitiesPyrotechnics", witchercraftAbilitiesPyrotechnics);
			output.putBoolean("witchercraftAbilitiesEfficiency", witchercraftAbilitiesEfficiency);
			output.putBoolean("witchercraftAbilitiesClusterBombs", witchercraftAbilitiesClusterBombs);
			output.putBoolean("witchercraftAbilitiesFarReachingAard", witchercraftAbilitiesFarReachingAard);
			output.putBoolean("witchercraftAbilitiesAardIntensity", witchercraftAbilitiesAardIntensity);
			output.putBoolean("witchercraftAbilitiesShockWave", witchercraftAbilitiesShockWave);
			output.putBoolean("witchercraftAbilitiesFireStream", witchercraftAbilitiesFireStream);
			output.putBoolean("witchercraftAbilitiesIgniIntensity", witchercraftAbilitiesIgniIntensity);
			output.putBoolean("witchercraftAbilitiesPyromaniac", witchercraftAbilitiesPyromaniac);
			output.putBoolean("witchercraftAbilitiesSustainedGlyphs", witchercraftAbilitiesSustainedGlyphs);
			output.putBoolean("witchercraftAbilitiesYrdenIntensity", witchercraftAbilitiesYrdenIntensity);
			output.putBoolean("witchercraftAbilitiesMagicTrap", witchercraftAbilitiesMagicTrap);
			output.putBoolean("witchercraftAbilitiesExploadingShield", witchercraftAbilitiesExploadingShield);
			output.putBoolean("witchercraftAbilitiesQuenIntensity", witchercraftAbilitiesQuenIntensity);
			output.putBoolean("witchercraftAbilitiesQuenDischarge", witchercraftAbilitiesQuenDischarge);
			output.putBoolean("witchercraftAbilitiesDelusion", witchercraftAbilitiesDelusion);
			output.putBoolean("witchercraftAbilitiesAxiiIntensity", witchercraftAbilitiesAxiiIntensity);
			output.putBoolean("witchercraftAbilitiesDomination", witchercraftAbilitiesDomination);
			output.putBoolean("witchercraftAbilitiesMuscleMemory", witchercraftAbilitiesMuscleMemory);
			output.putBoolean("witchercraftAbilitiesPreciseBlows", witchercraftAbilitiesPreciseBlows);
			output.putBoolean("witchercraftAbilitiesCripplingStrikes", witchercraftAbilitiesCripplingStrikes);
			output.putBoolean("witchercraftAbilitiesStrengthTraining", witchercraftAbilitiesStrengthTraining);
			output.putBoolean("witchercraftAbilitiesCrushingBlows", witchercraftAbilitiesCrushingBlows);
			output.putBoolean("witchercraftAbilitiesSunderArmor", witchercraftAbilitiesSunderArmor);
			output.putBoolean("witchercraftAbilitiesFleetFooted", witchercraftAbilitiesFleetFooted);
			output.putBoolean("witchercraftAbilitiesDefence", witchercraftAbilitiesDefence);
			output.putBoolean("witchercraftAbilitiesDeadlyPresicion", witchercraftAbilitiesDeadlyPresicion);
			output.putBoolean("witchercraftAbilitiesColdBlood", witchercraftAbilitiesColdBlood);
			output.putBoolean("witchercraftAbilitiesAnatomicalKnowledge", witchercraftAbilitiesAnatomicalKnowledge);
			output.putBoolean("witchercraftAbilitiesCripplingShot", witchercraftAbilitiesCripplingShot);
			output.putBoolean("witchercraftAbilitiesFloodOfAnger", witchercraftAbilitiesFloodOfAnger);
			output.putBoolean("witchercraftAbilitiesRazorFocus", witchercraftAbilitiesRazorFocus);
			output.putBoolean("witchercraftAbilitiesUndying", witchercraftAbilitiesUndying);
			output.putDouble("wichercraftPlayerLevel", wichercraftPlayerLevel);
			output.putDouble("wichercraftPlayerExperience", wichercraftPlayerExperience);
			output.putDouble("wichercraftAbilitesExperienceRequirement", wichercraftAbilitesExperienceRequirement);
			output.putDouble("witchercraftToxicity", witchercraftToxicity);
			output.putDouble("ToxicityOverdoseThreshold", ToxicityOverdoseThreshold);
			output.putDouble("witchercraftIncreasedDamage", witchercraftIncreasedDamage);
			output.putDouble("witchercraftAbilitiesCombatSkillPointsUsed", witchercraftAbilitiesCombatSkillPointsUsed);
			output.putDouble("witchercraftAbilitiesAlchemySkillPointsUsed", witchercraftAbilitiesAlchemySkillPointsUsed);
			output.putDouble("witchercraftAbilitiesSignsSkillPointsUsed", witchercraftAbilitiesSignsSkillPointsUsed);
			output.putBoolean("witchercraftEnemyNearby", witchercraftEnemyNearby);
			output.putDouble("witchercraftDodgeChance", witchercraftDodgeChance);
			output.putDouble("witchercraftQuenShield", witchercraftQuenShield);
			output.putDouble("witchercraftInstantKillChance", witchercraftInstantKillChance);
			output.putDouble("witchercraftBasePassiveStaminaRegeneration", witchercraftBasePassiveStaminaRegeneration);
			output.putDouble("witchercraftPotionDuration", witchercraftPotionDuration);
			output.putDouble("witchercraftSignIntensity", witchercraftSignIntensity);
			output.putDouble("witchercraftOilDamage", witchercraftOilDamage);
			output.putDouble("witchercraftLifeSteal", witchercraftLifeSteal);
			output.putDouble("witchercraftBaseCritChance", witchercraftBaseCritChance);
			output.putDouble("witchercraftBaseCritDamage", witchercraftBaseCritDamage);
			output.putDouble("witchercraftReflectDamage", witchercraftReflectDamage);
			output.putDouble("witchercraftWyvenDecoctionHit", witchercraftWyvenDecoctionHit);
			output.putDouble("witchercraftSuccubusDecoctionTick", witchercraftSuccubusDecoctionTick);
			output.putDouble("witchercraftGraveHagDecoctionKill", witchercraftGraveHagDecoctionKill);
			output.putDouble("witchercraftSignKeyHoldTime", witchercraftSignKeyHoldTime);
			output.putBoolean("witchercraftSignKeyHold", witchercraftSignKeyHold);
			output.putDouble("witchercraftTick", witchercraftTick);
			output.putBoolean("witchercraftSignNoCast", witchercraftSignNoCast);
			output.putDouble("witchercraftSignCastHoldCost", witchercraftSignCastHoldCost);
		}

		@Override
		public void deserialize(ValueInput input) {
			witchercraftMovementSpeed = input.getDoubleOr("witchercraftMovementSpeed", 0);
			witchercraftHealth = input.getDoubleOr("witchercraftHealth", 0);
			witchercraftArmor = input.getDoubleOr("witchercraftArmor", 0);
			witchercraftAdditionalDamage = input.getDoubleOr("witchercraftAdditionalDamage", 0);
			witchercraftCritChance = input.getDoubleOr("witchercraftCritChance", 0);
			witchercraftCritDamage = input.getDoubleOr("witchercraftCritDamage", 0);
			witchercraftAttackSpeed = input.getDoubleOr("witchercraftAttackSpeed", 0);
			witchercraftPassiveHealthRegeneration = input.getDoubleOr("witchercraftPassiveHealthRegeneration", 0);
			witchercraftBasePassiveHealthRegeneration = input.getDoubleOr("witchercraftBasePassiveHealthRegeneration", 0);
			witchercraftPassiveStaminaRegeneration = input.getDoubleOr("witchercraftPassiveStaminaRegeneration", 0);
			witchercraftAbilitiesLearned = input.getDoubleOr("witchercraftAbilitiesLearned", 0);
			witchercraftAbiltiesGourmet = input.getBooleanOr("witchercraftAbiltiesGourmet", false);
			witchercraftAbilitiesSurvivalInstinct = input.getBooleanOr("witchercraftAbilitiesSurvivalInstinct", false);
			witchercraftAbilitiesSunAndStars = input.getBooleanOr("witchercraftAbilitiesSunAndStars", false);
			witchercraftAbilitiesCatSchool = input.getBooleanOr("witchercraftAbilitiesCatSchool", false);
			witchercraftAbilitiesGriffinSchool = input.getBooleanOr("witchercraftAbilitiesGriffinSchool", false);
			witchercraftAbilitiesBearSchool = input.getBooleanOr("witchercraftAbilitiesBearSchool", false);
			witchercraftAbilitiesRefreshment = input.getBooleanOr("witchercraftAbilitiesRefreshment", false);
			witchercraftAbilitiesDelayedRecovery = input.getBooleanOr("witchercraftAbilitiesDelayedRecovery", false);
			witchercraftAbilitiesSideEffects = input.getBooleanOr("witchercraftAbilitiesSideEffects", false);
			witchercraftAbilitiesPoisonedBlades = input.getBooleanOr("witchercraftAbilitiesPoisonedBlades", false);
			witchercraftAbilitiesProtectiveCoating = input.getBooleanOr("witchercraftAbilitiesProtectiveCoating", false);
			witchercraftAbilitiesHunterInstincts = input.getBooleanOr("witchercraftAbilitiesHunterInstincts", false);
			witchercraftAbilitiesPyrotechnics = input.getBooleanOr("witchercraftAbilitiesPyrotechnics", false);
			witchercraftAbilitiesEfficiency = input.getBooleanOr("witchercraftAbilitiesEfficiency", false);
			witchercraftAbilitiesClusterBombs = input.getBooleanOr("witchercraftAbilitiesClusterBombs", false);
			witchercraftAbilitiesFarReachingAard = input.getBooleanOr("witchercraftAbilitiesFarReachingAard", false);
			witchercraftAbilitiesAardIntensity = input.getBooleanOr("witchercraftAbilitiesAardIntensity", false);
			witchercraftAbilitiesShockWave = input.getBooleanOr("witchercraftAbilitiesShockWave", false);
			witchercraftAbilitiesFireStream = input.getBooleanOr("witchercraftAbilitiesFireStream", false);
			witchercraftAbilitiesIgniIntensity = input.getBooleanOr("witchercraftAbilitiesIgniIntensity", false);
			witchercraftAbilitiesPyromaniac = input.getBooleanOr("witchercraftAbilitiesPyromaniac", false);
			witchercraftAbilitiesSustainedGlyphs = input.getBooleanOr("witchercraftAbilitiesSustainedGlyphs", false);
			witchercraftAbilitiesYrdenIntensity = input.getBooleanOr("witchercraftAbilitiesYrdenIntensity", false);
			witchercraftAbilitiesMagicTrap = input.getBooleanOr("witchercraftAbilitiesMagicTrap", false);
			witchercraftAbilitiesExploadingShield = input.getBooleanOr("witchercraftAbilitiesExploadingShield", false);
			witchercraftAbilitiesQuenIntensity = input.getBooleanOr("witchercraftAbilitiesQuenIntensity", false);
			witchercraftAbilitiesQuenDischarge = input.getBooleanOr("witchercraftAbilitiesQuenDischarge", false);
			witchercraftAbilitiesDelusion = input.getBooleanOr("witchercraftAbilitiesDelusion", false);
			witchercraftAbilitiesAxiiIntensity = input.getBooleanOr("witchercraftAbilitiesAxiiIntensity", false);
			witchercraftAbilitiesDomination = input.getBooleanOr("witchercraftAbilitiesDomination", false);
			witchercraftAbilitiesMuscleMemory = input.getBooleanOr("witchercraftAbilitiesMuscleMemory", false);
			witchercraftAbilitiesPreciseBlows = input.getBooleanOr("witchercraftAbilitiesPreciseBlows", false);
			witchercraftAbilitiesCripplingStrikes = input.getBooleanOr("witchercraftAbilitiesCripplingStrikes", false);
			witchercraftAbilitiesStrengthTraining = input.getBooleanOr("witchercraftAbilitiesStrengthTraining", false);
			witchercraftAbilitiesCrushingBlows = input.getBooleanOr("witchercraftAbilitiesCrushingBlows", false);
			witchercraftAbilitiesSunderArmor = input.getBooleanOr("witchercraftAbilitiesSunderArmor", false);
			witchercraftAbilitiesFleetFooted = input.getBooleanOr("witchercraftAbilitiesFleetFooted", false);
			witchercraftAbilitiesDefence = input.getBooleanOr("witchercraftAbilitiesDefence", false);
			witchercraftAbilitiesDeadlyPresicion = input.getBooleanOr("witchercraftAbilitiesDeadlyPresicion", false);
			witchercraftAbilitiesColdBlood = input.getBooleanOr("witchercraftAbilitiesColdBlood", false);
			witchercraftAbilitiesAnatomicalKnowledge = input.getBooleanOr("witchercraftAbilitiesAnatomicalKnowledge", false);
			witchercraftAbilitiesCripplingShot = input.getBooleanOr("witchercraftAbilitiesCripplingShot", false);
			witchercraftAbilitiesFloodOfAnger = input.getBooleanOr("witchercraftAbilitiesFloodOfAnger", false);
			witchercraftAbilitiesRazorFocus = input.getBooleanOr("witchercraftAbilitiesRazorFocus", false);
			witchercraftAbilitiesUndying = input.getBooleanOr("witchercraftAbilitiesUndying", false);
			wichercraftPlayerLevel = input.getDoubleOr("wichercraftPlayerLevel", 0);
			wichercraftPlayerExperience = input.getDoubleOr("wichercraftPlayerExperience", 0);
			wichercraftAbilitesExperienceRequirement = input.getDoubleOr("wichercraftAbilitesExperienceRequirement", 0);
			witchercraftToxicity = input.getDoubleOr("witchercraftToxicity", 0);
			ToxicityOverdoseThreshold = input.getDoubleOr("ToxicityOverdoseThreshold", 0);
			witchercraftIncreasedDamage = input.getDoubleOr("witchercraftIncreasedDamage", 0);
			witchercraftAbilitiesCombatSkillPointsUsed = input.getDoubleOr("witchercraftAbilitiesCombatSkillPointsUsed", 0);
			witchercraftAbilitiesAlchemySkillPointsUsed = input.getDoubleOr("witchercraftAbilitiesAlchemySkillPointsUsed", 0);
			witchercraftAbilitiesSignsSkillPointsUsed = input.getDoubleOr("witchercraftAbilitiesSignsSkillPointsUsed", 0);
			witchercraftEnemyNearby = input.getBooleanOr("witchercraftEnemyNearby", false);
			witchercraftDodgeChance = input.getDoubleOr("witchercraftDodgeChance", 0);
			witchercraftQuenShield = input.getDoubleOr("witchercraftQuenShield", 0);
			witchercraftInstantKillChance = input.getDoubleOr("witchercraftInstantKillChance", 0);
			witchercraftBasePassiveStaminaRegeneration = input.getDoubleOr("witchercraftBasePassiveStaminaRegeneration", 0);
			witchercraftPotionDuration = input.getDoubleOr("witchercraftPotionDuration", 0);
			witchercraftSignIntensity = input.getDoubleOr("witchercraftSignIntensity", 0);
			witchercraftOilDamage = input.getDoubleOr("witchercraftOilDamage", 0);
			witchercraftLifeSteal = input.getDoubleOr("witchercraftLifeSteal", 0);
			witchercraftBaseCritChance = input.getDoubleOr("witchercraftBaseCritChance", 0);
			witchercraftBaseCritDamage = input.getDoubleOr("witchercraftBaseCritDamage", 0);
			witchercraftReflectDamage = input.getDoubleOr("witchercraftReflectDamage", 0);
			witchercraftWyvenDecoctionHit = input.getDoubleOr("witchercraftWyvenDecoctionHit", 0);
			witchercraftSuccubusDecoctionTick = input.getDoubleOr("witchercraftSuccubusDecoctionTick", 0);
			witchercraftGraveHagDecoctionKill = input.getDoubleOr("witchercraftGraveHagDecoctionKill", 0);
			witchercraftSignKeyHoldTime = input.getDoubleOr("witchercraftSignKeyHoldTime", 0);
			witchercraftSignKeyHold = input.getBooleanOr("witchercraftSignKeyHold", false);
			witchercraftTick = input.getDoubleOr("witchercraftTick", 0);
			witchercraftSignNoCast = input.getBooleanOr("witchercraftSignNoCast", false);
			witchercraftSignCastHoldCost = input.getDoubleOr("witchercraftSignCastHoldCost", 0);
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