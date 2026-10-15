package net.redboltmedia.witchercraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Exposes a living renderer's protected {@code scale} (the per-renderer model scale,
 * e.g. the Ghoul's 0.8) so the Glossary can measure a creature's model exactly the
 * way it will be drawn and fit its doll to the doll area ({@code GlossaryPage}).
 *
 * HAND-MAINTAINED: not an MCreator element. Registered in witchercraft.mixins.json.
 */
@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererAccessor {
	@Invoker("scale")
	void witchercraft$scale(LivingEntityRenderState state, PoseStack poseStack);
}
