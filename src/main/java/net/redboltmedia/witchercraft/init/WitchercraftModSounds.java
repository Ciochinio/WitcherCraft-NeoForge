/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.redboltmedia.witchercraft.init;

import net.redboltmedia.witchercraft.WitchercraftMod;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class WitchercraftModSounds {
	public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(Registries.SOUND_EVENT, WitchercraftMod.MODID);
	public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMY_BREW = REGISTRY.register("alchemy_brew", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("witchercraft", "alchemy_brew")));
	public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMY_FAIL = REGISTRY.register("alchemy_fail", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("witchercraft", "alchemy_fail")));
}