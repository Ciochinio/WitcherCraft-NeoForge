/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.redboltmedia.witchercraft.init;

import net.redboltmedia.witchercraft.procedures.SuccubusDecoctionEndProcedure;
import net.redboltmedia.witchercraft.procedures.QuenBrokeProcedure;
import net.redboltmedia.witchercraft.procedures.QuenActiveShieldDropProcedure;
import net.redboltmedia.witchercraft.potion.*;
import net.redboltmedia.witchercraft.WitchercraftMod;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.registries.Registries;

@EventBusSubscriber
public class WitchercraftModMobEffects {
	public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(Registries.MOB_EFFECT, WitchercraftMod.MODID);
	public static final DeferredHolder<MobEffect, MobEffect> GOLDEN_ORIOLE_EFFECT = REGISTRY.register("golden_oriole_effect", GoldenOrioleEffectMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> BLIZZARD_EFFECT = REGISTRY.register("blizzard_effect", BlizzardEffectMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> CAT_EFFECT = REGISTRY.register("cat_effect", CatEffectMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> FULL_MOON_EFFECT = REGISTRY.register("full_moon_effect", FullMoonEffectMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> THUNDERBOLT_EFFECT = REGISTRY.register("thunderbolt_effect", ThunderboltEffectMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> WHITE_RAFFARDS_DECOCTION_EFFECT = REGISTRY.register("white_raffards_decoction_effect", WhiteRaffardsDecoctionEffectMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> KILLER_WHALE_EFFECT = REGISTRY.register("killer_whale_effect", KillerWhaleEffectMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> SWALLOW_EFFECT = REGISTRY.register("swallow_effect", SwallowEffectMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> TAWNY_OWL_EFFECT = REGISTRY.register("tawny_owl_effect", TawnyOwlEffectMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> SCHOOL_OF_THE_CAT = REGISTRY.register("school_of_the_cat", SchoolOfTheCatMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> SCHOOL_OF_THE_WOLF = REGISTRY.register("school_of_the_wolf", SchoolOfTheWolfMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> SCHOOL_OF_THE_BEAR = REGISTRY.register("school_of_the_bear", SchoolOfTheBearMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> SCHOOL_OF_THE_VIPER = REGISTRY.register("school_of_the_viper", SchoolOfTheViperMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> SCHOOL_OF_THE_MANTICORE = REGISTRY.register("school_of_the_manticore", SchoolOfTheManticoreMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> SCHOOL_OF_THE_GRIFFIN = REGISTRY.register("school_of_the_griffin", SchoolOfTheGriffinMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> IGNI_SIGN = REGISTRY.register("igni_sign", IgniSignMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> YRDEN_SIGN = REGISTRY.register("yrden_sign", YrdenSignMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> AXII_SIGN = REGISTRY.register("axii_sign", AxiiSignMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> AARD_SIGN = REGISTRY.register("aard_sign", AardSignMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> QUEN_SIGN = REGISTRY.register("quen_sign", QuenSignMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> DEV_LOG = REGISTRY.register("dev_log", DevLogMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> BLEED = REGISTRY.register("bleed", BleedMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> ENEMY_NEARBY = REGISTRY.register("enemy_nearby", EnemyNearbyMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> UNDYING_COOLDOWN = REGISTRY.register("undying_cooldown", UndyingCooldownMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> DODGE_COOLDOWN = REGISTRY.register("dodge_cooldown", DodgeCooldownMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> PETRIS_PHILTER_EFFECT = REGISTRY.register("petris_philter_effect", PetrisPhilterEffectMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> PLAYER_TRACKER = REGISTRY.register("player_tracker", PlayerTrackerMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> CORRECT_OIL = REGISTRY.register("correct_oil", CorrectOilMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> WATER_HAG_DECOCTION_EFFECT = REGISTRY.register("water_hag_decoction_effect", WaterHagDecoctionEffectMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> EKIMMARA_DECOCTION_EFFECT = REGISTRY.register("ekimmara_decoction_effect", EkimmaraDecoctionEffectMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> KATAKAN_DECOCTION_EFFECT = REGISTRY.register("katakan_decoction_effect", KatakanDecoctionEffectMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> LESHEN_DECOCTION_EFFECT = REGISTRY.register("leshen_decoction_effect", LeshenDecoctionEffectMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> BLACK_BLOOD_EFFECT = REGISTRY.register("black_blood_effect", BlackBloodEffectMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> FOGLET_DECOCTION_EFFECT = REGISTRY.register("foglet_decoction_effect", FogletDecoctionEffectMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> NEKKER_WARRIOR_DECOCTION_EFFECT = REGISTRY.register("nekker_warrior_decoction_effect", NekkerWarriorDecoctionEffectMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> TROLL_DECOCTION_EFFECT = REGISTRY.register("troll_decoction_effect", TrollDecoctionEffectMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> WRAITH_DECOCTION_EFFECT = REGISTRY.register("wraith_decoction_effect", WraithDecoctionEffectMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> WEREWOLF_DECOCTION_EFFECT = REGISTRY.register("werewolf_decoction_effect", WerewolfDecoctionEffectMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> IN_COMBAT = REGISTRY.register("in_combat", InCombatMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> WYVERN_DECOCTION_EFFECT = REGISTRY.register("wyvern_decoction_effect", WyvernDecoctionEffectMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> SUCCUBUS_DECOCTION_EFFECT = REGISTRY.register("succubus_decoction_effect", SuccubusDecoctionEffectMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> GRAVE_HAG_DECOCTION_EFFECT = REGISTRY.register("grave_hag_decoction_effect", GraveHagDecoctionEffectMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> BLINDNESS = REGISTRY.register("blindness", BlindnessMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> SIGN_COOLDOWN = REGISTRY.register("sign_cooldown", SignCooldownMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> QUEN_ACTIVE_SHIELD = REGISTRY.register("quen_active_shield", QuenActiveShieldMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> SIGN_HOLD = REGISTRY.register("sign_hold", SignHoldMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> QUEN_EFFECT = REGISTRY.register("quen_effect", QuenEffectMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> DAMAGE_BLOCKED = REGISTRY.register("damage_blocked", DamageBlockedMobEffect::new);

	@SubscribeEvent
	public static void onEffectRemoved(MobEffectEvent.Remove event) {
		MobEffectInstance effectInstance = event.getEffectInstance();
		if (effectInstance != null) {
			expireEffects(event.getEntity(), effectInstance);
		}
	}

	@SubscribeEvent
	public static void onEffectExpired(MobEffectEvent.Expired event) {
		MobEffectInstance effectInstance = event.getEffectInstance();
		if (effectInstance != null) {
			expireEffects(event.getEntity(), effectInstance);
		}
	}

	private static void expireEffects(Entity entity, MobEffectInstance effectInstance) {
		if (effectInstance.is(SUCCUBUS_DECOCTION_EFFECT)) {
			SuccubusDecoctionEndProcedure.execute(entity);
		} else if (effectInstance.is(QUEN_ACTIVE_SHIELD)) {
			QuenActiveShieldDropProcedure.execute(entity);
		} else if (effectInstance.is(QUEN_EFFECT)) {
			QuenBrokeProcedure.execute(entity);
		}
	}
}