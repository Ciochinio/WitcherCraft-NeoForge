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

// Locked: Axii's charm. Finds the target with WitchercraftAxiiAim (the same aim the marker shows) and returns
// whether there was one, so AxiiCast can refund an empty cast. Every target gets AxiiCharmed (Precise hits);
// targets not in #witchercraft:axii_immune also get AxiiStunned (can't move, can't hurt anyone).
public class WitchercraftAxiiCharmProcedure {
	private static final TagKey<EntityType<?>> AXII_IMMUNE = TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("witchercraft:axii_immune"));
	private static final int BASE_TICKS = 80;

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
		if (!target.is(AXII_IMMUNE))
			target.addEffect(new MobEffectInstance(WitchercraftModMobEffects.AXII_STUNNED, ticks, 0, false, false));
		return true;
	}
}
