package net.redboltmedia.witchercraft.client.renderer;

import net.redboltmedia.witchercraft.procedures.*;
import net.redboltmedia.witchercraft.entity.AlghoulEntity;
import net.redboltmedia.witchercraft.client.model.animations.AlghoulAnimation;
import net.redboltmedia.witchercraft.client.model.ModelAlghoul;
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

public class AlghoulRenderer extends MobRenderer<AlghoulEntity, LivingEntityRenderState, ModelAlghoul> {
	private static final Identifier TEXTURE = Identifier.parse("witchercraft:textures/entities/alghoul.png");

	public AlghoulRenderer(EntityRendererProvider.Context context) {
		super(context, new AnimatedModel(context.bakeLayer(ModelAlghoul.LAYER_LOCATION)), 0.8f);
	}

	@Override
	public LivingEntityRenderState createRenderState() {
		return new LivingEntityRenderState();
	}

	@Override
	public void extractRenderState(AlghoulEntity entity, LivingEntityRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
	}

	@Override
	public Identifier getTextureLocation(LivingEntityRenderState state) {
		return TEXTURE;
	}

	private static final class AnimatedModel extends ModelAlghoul {
		private final KeyframeAnimation spikesOn;
		private final KeyframeAnimation spikesOff;
		private final KeyframeAnimation slapSpikes;
		private final KeyframeAnimation slapNoSpikes;
		private final KeyframeAnimation idleSpikes;
		private final KeyframeAnimation idleNoSpikes;
		private final KeyframeAnimation walkSpikes;
		private final KeyframeAnimation walkNoSpikes;

		private AnimatedModel(ModelPart root) {
			super(root);
			this.spikesOn = safeBake(AlghoulAnimation.spikes_on);
			this.spikesOff = safeBake(AlghoulAnimation.spikes_off);
			this.slapSpikes = safeBake(AlghoulAnimation.slap_spikes);
			this.slapNoSpikes = safeBake(AlghoulAnimation.slap_no_spikes);
			this.idleSpikes = safeBake(AlghoulAnimation.idle_spikes);
			this.idleNoSpikes = safeBake(AlghoulAnimation.idle_no_spikes);
			this.walkSpikes = safeBake(AlghoulAnimation.walk_spikes);
			this.walkNoSpikes = safeBake(AlghoulAnimation.walk_no_spikes);
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
			AlghoulEntity entity = state.getRenderData(ENTITY_KEY);
			this.spikesOn.apply(entity.animationState0, state.ageInTicks, 1f);
			this.spikesOff.apply(entity.animationState1, state.ageInTicks, 1f);
			this.slapSpikes.apply(entity.animationState2, state.ageInTicks, 1f);
			this.slapNoSpikes.apply(entity.animationState3, state.ageInTicks, 1f);
			this.idleSpikes.apply(entity.animationState4, state.ageInTicks, 1f);
			this.idleNoSpikes.apply(entity.animationState5, state.ageInTicks, 1f);
			if (AlghoulWalkSpikesAnimationConditionProcedure.execute(entity))
			this.walkSpikes.applyWalk(state.walkAnimationPos, state.walkAnimationSpeed, 1f, 8f);
			if (AlghoulWalkNoSpikesAnimationConditionProcedure.execute(entity))
			this.walkNoSpikes.applyWalk(state.walkAnimationPos, state.walkAnimationSpeed, 1f, 8f);
			super.setupAnim(state);
		}
	}

	public static final ContextKey<AlghoulEntity> ENTITY_KEY = new ContextKey<>(Identifier.parse("witchercraft:alghoul_entity"));

	@EventBusSubscriber(Dist.CLIENT)
	public static class EntityStateAdder {
		@SubscribeEvent
		private static void registerRenderStateModifiersEvent(RegisterRenderStateModifiersEvent event) {
			event.registerEntityModifier(AlghoulRenderer.class, (entity, state) -> state.setRenderData(ENTITY_KEY, entity));
		}
	}
}
