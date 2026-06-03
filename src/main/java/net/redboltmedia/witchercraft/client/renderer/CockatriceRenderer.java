package net.redboltmedia.witchercraft.client.renderer;

import net.redboltmedia.witchercraft.entity.CockatriceEntity;
import net.redboltmedia.witchercraft.client.model.animations.CockatriceAnimationWingBash;
import net.redboltmedia.witchercraft.client.model.animations.CockatriceAnimationWalk;
import net.redboltmedia.witchercraft.client.model.animations.CockatriceAnimationIdle;
import net.redboltmedia.witchercraft.client.model.animations.CockatriceAnimationHeadBash;
import net.redboltmedia.witchercraft.client.model.animations.CockatriceAnimationFlying;
import net.redboltmedia.witchercraft.client.model.ModelCockatrice;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.animation.AnimationDefinition;

import java.util.Map;

public class CockatriceRenderer extends MobRenderer<CockatriceEntity, LivingEntityRenderState, ModelCockatrice> {
	private CockatriceEntity entity = null;
	private final ResourceLocation entityTexture = ResourceLocation.parse("witchercraft:textures/entities/cockatrice.png");

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
		this.entity = entity;
		if (this.model instanceof AnimatedModel) {
			((AnimatedModel) this.model).setEntity(entity);
		}
	}

	@Override
	public ResourceLocation getTextureLocation(LivingEntityRenderState state) {
		return entityTexture;
	}

	private static final class AnimatedModel extends ModelCockatrice {
		private CockatriceEntity entity = null;
		private final KeyframeAnimation keyframeAnimation0;
		private final KeyframeAnimation keyframeAnimation1;
		private final KeyframeAnimation keyframeAnimation2;
		private final KeyframeAnimation keyframeAnimation3;
		private final KeyframeAnimation keyframeAnimation4;

		public AnimatedModel(ModelPart root) {
			super(root);
			this.keyframeAnimation0 = safeBake(CockatriceAnimationHeadBash.head_bash);
			this.keyframeAnimation1 = safeBake(CockatriceAnimationWingBash.wing_bash);
			this.keyframeAnimation2 = safeBake(CockatriceAnimationFlying.flying);
			this.keyframeAnimation3 = safeBake(CockatriceAnimationWalk.walk);
			this.keyframeAnimation4 = safeBake(CockatriceAnimationIdle.idle);
		}

		private KeyframeAnimation safeBake(AnimationDefinition source) {
			try {
				return source.bake(root);
			} catch (IllegalArgumentException e) {
				return new AnimationDefinition(0, false, Map.of()).bake(root);
			}
		}

		public void setEntity(CockatriceEntity entity) {
			this.entity = entity;
		}

		@Override
		public void setupAnim(LivingEntityRenderState state) {
			this.root().getAllParts().forEach(ModelPart::resetPose);
			this.keyframeAnimation0.apply(entity.animationState0, state.ageInTicks, 1f);
			this.keyframeAnimation1.apply(entity.animationState1, state.ageInTicks, 1f);
			this.keyframeAnimation2.apply(entity.animationState2, state.ageInTicks, 1f);
			this.keyframeAnimation3.apply(entity.animationState3, state.ageInTicks, 1f);
			this.keyframeAnimation4.apply(entity.animationState4, state.ageInTicks, 1f);
			super.setupAnim(state);
		}
	}
}