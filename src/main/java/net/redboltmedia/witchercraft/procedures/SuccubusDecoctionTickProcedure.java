package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;

public class SuccubusDecoctionTickProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _livingEntityID && _livingEntityID.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE)) {
			if ((entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(WitchercraftModMobEffects.SUCCUBUS_DECOCTION_EFFECT)
					? _livEnt.getEffect(WitchercraftModMobEffects.SUCCUBUS_DECOCTION_EFFECT).getDuration()
					: 0) % 40 == 0) {
				if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(WitchercraftModMobEffects.IN_COMBAT)) {
					if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftSuccubusDecoctionTick < 10) {
						{
							WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
							_vars.witchercraftSuccubusDecoctionTick = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftSuccubusDecoctionTick + 1;
							_vars.markSyncDirty();
						}
					}
				} else {
					{
						WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
						_vars.witchercraftSuccubusDecoctionTick = 0;
						_vars.markSyncDirty();
					}
				}
				if (entity instanceof LivingEntity _entity) {
					_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).removeModifier(Identifier.parse("witchercraft:effect_succubus_stacks"));
				}
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:effect_succubus_stacks"),
							entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftSuccubusDecoctionTick, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).addTransientModifier(modifier);
					}
				}
			}
		}
	}
}
