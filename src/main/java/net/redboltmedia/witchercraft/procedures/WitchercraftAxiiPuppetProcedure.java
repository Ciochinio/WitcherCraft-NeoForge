package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;
import net.redboltmedia.witchercraft.WitchercraftAxiiAim;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

// Locked: Alt Axii (Puppet). Same aim as base Axii; returns whether there was a target so AltAxiiCast can refund.
// Every target gets AxiiCharmed (Precise). Targets not in #witchercraft:axii_immune also become your puppet
// (AxiiPuppet, driven by WitchercraftAxiiPuppetEvents). One puppet per player: a new puppet releases the old one.
public class WitchercraftAxiiPuppetProcedure {
	private static final TagKey<EntityType<?>> AXII_IMMUNE = TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("witchercraft:axii_immune"));
	private static final int BASE_TICKS = 160;
	private static final String PUPPET_KEY = "witchercraftAxiiPuppet";

	public static boolean execute(Entity entity) {
		if (!(entity instanceof Player player))
			return false;
		LivingEntity target = WitchercraftAxiiAim.target(player, 1.0F);
		if (target == null)
			return false;
		if (player.level().isClientSide())
			return true;
		double intensity = player.getAttributes().hasAttribute(WitchercraftModAttributes.SIGN_INTENSITY) ? player.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).getValue() : 0;
		int ticks = (int) Math.round(BASE_TICKS * (1 + intensity * 0.01));
		target.addEffect(new MobEffectInstance(WitchercraftModMobEffects.AXII_CHARMED, ticks, 0, false, false));
		if (target.is(AXII_IMMUNE))
			return true;
		Entity old = player.level().getEntity(player.getPersistentData().getIntOr(PUPPET_KEY, -1));
		if (old instanceof LivingEntity oldPuppet && oldPuppet != target && oldPuppet.hasEffect(WitchercraftModMobEffects.AXII_PUPPET)) {
			oldPuppet.removeEffect(WitchercraftModMobEffects.AXII_PUPPET);
			oldPuppet.removeEffect(WitchercraftModMobEffects.AXII_CHARMED);
		}
		target.removeEffect(WitchercraftModMobEffects.AXII_STUNNED);
		target.addEffect(new MobEffectInstance(WitchercraftModMobEffects.AXII_PUPPET, ticks, 0, false, false));
		player.getPersistentData().putInt(PUPPET_KEY, target.getId());
		return true;
	}
}
