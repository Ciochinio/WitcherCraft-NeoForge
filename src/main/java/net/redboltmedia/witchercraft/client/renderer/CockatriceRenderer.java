package net.redboltmedia.witchercraft.client.renderer;

import net.redboltmedia.witchercraft.procedures.CockatriceWalkAnimationConditionProcedure;
import net.redboltmedia.witchercraft.entity.CockatriceEntity;
import net.redboltmedia.witchercraft.client.model.animations.CockatriceAnimation;
import net.redboltmedia.witchercraft.client.model.ModelCockatrice;

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

import java.util.Map;

public class CockatriceRenderer extends MobRenderer<CockatriceEntity, LivingEntityRenderState, ModelCockatrice> {
	private final Identifier entityTexture = Identifier.parse("witchercraft:textures/entities/cockatrice.png");

	public CockatriceRenderer(EntityRendererProvider.Context context) {
		super(context, new AnimatedModel(context.bakeLayer(ModelCockatrice.LAYER_LOCATION)), 1.8f);
	}

	@Override
	public LivingEntityRenderState createRenderState() {
		return new LivingEntityRenderState();
	}

	@Override
	public void extractRenderState(CockatriceEntity entity, LivingEntityRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
	}

	@Override
	public Identifier getTextureLocation(LivingEntityRenderState state) {
		return entityTexture;
	}

	private static final class AnimatedModel extends ModelCockatrice {
		private final KeyframeAnimation keyframeAnimation0;
		private final KeyframeAnimation keyframeAnimation1;
		private final KeyframeAnimation keyframeAnimation2;
		private final KeyframeAnimation keyframeAnimation3;
		private final KeyframeAnimation keyframeAnimation4;
		private final KeyframeAnimation keyframeAnimation5;

		public AnimatedModel(ModelPart root) {
			super(root);
			this.keyframeAnimation0 = safeBake(CockatriceAnimation.wing_ground);
			this.keyframeAnimation1 = safeBake(CockatriceAnimation.head_bash);
			this.keyframeAnimation2 = safeBake(CockatriceAnimation.walk);
			this.keyframeAnimation3 = safeBake(CockatriceAnimation.idle);
			this.keyframeAnimation4 = safeBake(CockatriceAnimation.flying);
			this.keyframeAnimation5 = safeBake(CockatriceAnimation.wing_bash);
		}

		private KeyframeAnimation safeBake(AnimationDefinition source) {
			try {
				return source.bake(root);
			} catch (IllegalArgumentException e) {
				return new AnimationDefinition(0, false, Map.of()).bake(root);
			}
		}

		@Override
		public void setupAnim(LivingEntityRenderState state) {
			this.root().getAllParts().forEach(ModelPart::resetPose);
			CockatriceEntity entity = state.getRenderData(ENTITY_KEY);
			this.keyframeAnimation0.apply(entity.animationState0, state.ageInTicks, 1f);
			this.keyframeAnimation1.apply(entity.animationState1, state.ageInTicks, 1f);
			if (CockatriceWalkAnimationConditionProcedure.execute(entity))
				this.keyframeAnimation2.applyWalk(state.walkAnimationPos, state.walkAnimationSpeed, 1f, 1f);
			this.keyframeAnimation3.apply(entity.animationState3, state.ageInTicks, 1f);
			this.keyframeAnimation4.apply(entity.animationState4, state.ageInTicks, 1f);
			this.keyframeAnimation5.apply(entity.animationState5, state.ageInTicks, 1f);
			super.setupAnim(state);
		}
	}

	public static final ContextKey<CockatriceEntity> ENTITY_KEY = new ContextKey<>(Identifier.parse("witchercraft:cockatrice_entity"));

	@EventBusSubscriber(Dist.CLIENT)
	public static class EntityStateAdder {
		@SubscribeEvent
		private static void registerRenderStateModifiersEvent(RegisterRenderStateModifiersEvent event) {
			event.registerEntityModifier(CockatriceRenderer.class, (entity, state) -> state.setRenderData(ENTITY_KEY, entity));
		}
	}
}