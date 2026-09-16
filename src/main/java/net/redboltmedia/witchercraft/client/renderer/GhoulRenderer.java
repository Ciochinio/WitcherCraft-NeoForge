package net.redboltmedia.witchercraft.client.renderer;

import net.redboltmedia.witchercraft.procedures.GhoulWalkAnimationConditionProcedure;
import net.redboltmedia.witchercraft.entity.GhoulEntity;
import net.redboltmedia.witchercraft.client.model.animations.GhoulAnimation;
import net.redboltmedia.witchercraft.client.model.ModelGhoul;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.minecraft.util.context.ContextKey;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.animation.AnimationDefinition;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;

public class GhoulRenderer extends MobRenderer<GhoulEntity, LivingEntityRenderState, ModelGhoul> {
	private static final Identifier TEXTURE = Identifier.parse("witchercraft:textures/entities/ghoul.png");

	public GhoulRenderer(EntityRendererProvider.Context context) {
		super(context, new AnimatedModel(context.bakeLayer(ModelGhoul.LAYER_LOCATION)), 0.65f);
	}

	@Override
	public LivingEntityRenderState createRenderState() {
		return new LivingEntityRenderState();
	}

	@Override
	protected void scale(LivingEntityRenderState state, PoseStack poseStack) {
		poseStack.scale(0.8f, 0.8f, 0.8f);
	}

	@Override
	public Identifier getTextureLocation(LivingEntityRenderState state) {
		return TEXTURE;
	}

	private static final class AnimatedModel extends ModelGhoul {
		private final KeyframeAnimation slap;
		private final KeyframeAnimation idle;
		private final KeyframeAnimation walk;

		private AnimatedModel(ModelPart root) {
			super(root);
			this.slap = safeBake(GhoulAnimation.slap);
			this.idle = safeBake(GhoulAnimation.idle);
			this.walk = safeBake(GhoulAnimation.walk);
		}

		private KeyframeAnimation safeBake(AnimationDefinition source) {
			try {
				return source.bake(root);
			} catch (IllegalArgumentException exception) {
				return new AnimationDefinition(0, false, Map.of()).bake(root);
			}
		}

		@Override
		public void setupAnim(LivingEntityRenderState state) {
			this.root().getAllParts().forEach(ModelPart::resetPose);
			GhoulEntity entity = state.getRenderData(ENTITY_KEY);
			this.slap.apply(entity.animationState0, state.ageInTicks, 1f);
			this.idle.apply(entity.animationState1, state.ageInTicks, 1f);
			if (GhoulWalkAnimationConditionProcedure.execute(entity))
				this.walk.applyWalk(state.walkAnimationPos, state.walkAnimationSpeed, 1f, 8f);
			super.setupAnim(state);
		}
	}

	public static final ContextKey<GhoulEntity> ENTITY_KEY = new ContextKey<>(Identifier.parse("witchercraft:ghoul_entity"));

	@EventBusSubscriber(Dist.CLIENT)
	public static class EntityStateAdder {
		@SubscribeEvent
		private static void registerRenderStateModifiersEvent(RegisterRenderStateModifiersEvent event) {
			event.registerEntityModifier(GhoulRenderer.class, (entity, state) -> state.setRenderData(ENTITY_KEY, entity));
		}
	}
}
