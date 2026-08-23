package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class WhiteHoneyUsedProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _entity)
			_entity.removeEffect(WitchercraftModMobEffects.GRAVE_HAG_DECOCTION_EFFECT);
		if (entity instanceof LivingEntity _entity)
			_entity.removeEffect(WitchercraftModMobEffects.WATER_HAG_DECOCTION_EFFECT);
		if (entity instanceof LivingEntity _entity)
			_entity.removeEffect(WitchercraftModMobEffects.EKIMMARA_DECOCTION_EFFECT);
		if (entity instanceof LivingEntity _entity)
			_entity.removeEffect(WitchercraftModMobEffects.KATAKAN_DECOCTION_EFFECT);
		if (entity instanceof LivingEntity _entity)
			_entity.removeEffect(WitchercraftModMobEffects.LESHEN_DECOCTION_EFFECT);
		if (entity instanceof LivingEntity _entity)
			_entity.removeEffect(WitchercraftModMobEffects.FOGLET_DECOCTION_EFFECT);
		if (entity instanceof LivingEntity _entity)
			_entity.removeEffect(WitchercraftModMobEffects.NEKKER_WARRIOR_DECOCTION_EFFECT);
		if (entity instanceof LivingEntity _entity)
			_entity.removeEffect(WitchercraftModMobEffects.SUCCUBUS_DECOCTION_EFFECT);
		if (entity instanceof LivingEntity _entity)
			_entity.removeEffect(WitchercraftModMobEffects.TROLL_DECOCTION_EFFECT);
		if (entity instanceof LivingEntity _entity)
			_entity.removeEffect(WitchercraftModMobEffects.WRAITH_DECOCTION_EFFECT);
		if (entity instanceof LivingEntity _entity)
			_entity.removeEffect(WitchercraftModMobEffects.WEREWOLF_DECOCTION_EFFECT);
		if (entity instanceof LivingEntity _entity)
			_entity.removeEffect(WitchercraftModMobEffects.WYVERN_DECOCTION_EFFECT);
		if (entity instanceof LivingEntity _entity)
			_entity.removeEffect(WitchercraftModMobEffects.GOLDEN_ORIOLE_EFFECT);
		if (entity instanceof LivingEntity _entity)
			_entity.removeEffect(WitchercraftModMobEffects.BLIZZARD_EFFECT);
		if (entity instanceof LivingEntity _entity)
			_entity.removeEffect(WitchercraftModMobEffects.CAT_EFFECT);
		if (entity instanceof LivingEntity _entity)
			_entity.removeEffect(WitchercraftModMobEffects.SWALLOW_EFFECT);
		if (entity instanceof LivingEntity _entity)
			_entity.removeEffect(WitchercraftModMobEffects.FULL_MOON_EFFECT);
		if (entity instanceof LivingEntity _entity)
			_entity.removeEffect(WitchercraftModMobEffects.THUNDERBOLT_EFFECT);
		if (entity instanceof LivingEntity _entity)
			_entity.removeEffect(WitchercraftModMobEffects.WHITE_RAFFARDS_DECOCTION_EFFECT);
		if (entity instanceof LivingEntity _entity)
			_entity.removeEffect(WitchercraftModMobEffects.TAWNY_OWL_EFFECT);
		if (entity instanceof LivingEntity _entity)
			_entity.removeEffect(WitchercraftModMobEffects.KILLER_WHALE_EFFECT);
		if (entity instanceof LivingEntity _entity)
			_entity.removeEffect(WitchercraftModMobEffects.BLACK_BLOOD_EFFECT);
		if (entity instanceof LivingEntity _entity)
			_entity.removeEffect(WitchercraftModMobEffects.PETRIS_PHILTER_EFFECT);
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftToxicity = 0;
			_vars.markSyncDirty();
		}
		PotionUsedProcedure.execute(entity);
	}
}