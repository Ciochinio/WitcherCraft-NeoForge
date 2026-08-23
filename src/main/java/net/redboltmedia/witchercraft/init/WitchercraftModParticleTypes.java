/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.redboltmedia.witchercraft.init;

import net.redboltmedia.witchercraft.WitchercraftMod;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleType;

public class WitchercraftModParticleTypes {
	public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(Registries.PARTICLE_TYPE, WitchercraftMod.MODID);
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> YRDEN_PARTICLES = REGISTRY.register("yrden_particles", () -> new SimpleParticleType(true));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> QUEN_HOLD_PARTICLES = REGISTRY.register("quen_hold_particles", () -> new SimpleParticleType(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> QUEN_PARTICLES = REGISTRY.register("quen_particles", () -> new SimpleParticleType(false));
}