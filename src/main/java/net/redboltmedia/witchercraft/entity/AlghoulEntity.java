package net.redboltmedia.witchercraft.entity;

import net.redboltmedia.witchercraft.procedures.*;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.core.registries.BuiltInRegistries;

public class AlghoulEntity extends Monster {
	public static final EntityDataAccessor<Integer> DATA_CombatTicks = SynchedEntityData.defineId(AlghoulEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Boolean> DATA_SpikesOut = SynchedEntityData.defineId(AlghoulEntity.class, EntityDataSerializers.BOOLEAN);
	public static final EntityDataAccessor<Integer> DATA_StanceAnimation = SynchedEntityData.defineId(AlghoulEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> DATA_StanceTicks = SynchedEntityData.defineId(AlghoulEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> DATA_AttackAnimation = SynchedEntityData.defineId(AlghoulEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> DATA_AttackTicks = SynchedEntityData.defineId(AlghoulEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Boolean> DATA_WasSwinging = SynchedEntityData.defineId(AlghoulEntity.class, EntityDataSerializers.BOOLEAN);
	public final AnimationState animationState0 = new AnimationState();
	public final AnimationState animationState1 = new AnimationState();
	public final AnimationState animationState2 = new AnimationState();
	public final AnimationState animationState3 = new AnimationState();
	public final AnimationState animationState4 = new AnimationState();
	public final AnimationState animationState5 = new AnimationState();

	public AlghoulEntity(EntityType<AlghoulEntity> type, Level world) {
		super(type, world);
		xpReward = 0;
		setNoAi(false);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_CombatTicks, 0);
		builder.define(DATA_SpikesOut, false);
		builder.define(DATA_StanceAnimation, 0);
		builder.define(DATA_StanceTicks, 0);
		builder.define(DATA_AttackAnimation, 0);
		builder.define(DATA_AttackTicks, 0);
		builder.define(DATA_WasSwinging, false);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false) {
			@Override
			protected boolean canPerformAttack(LivingEntity target) {
				return this.isTimeToAttack() && this.mob.distanceToSqr(target) < (this.mob.getBbWidth() * this.mob.getBbWidth() + target.getBbWidth()) && this.mob.getSensing().hasLineOfSight(target);
			}
		});
		this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1));
		this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
		this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
		this.goalSelector.addGoal(5, new FloatGoal(this));
	}

	@Override
	public SoundEvent getHurtSound(DamageSource source) {
		return BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.generic.hurt"));
	}

	@Override
	public SoundEvent getDeathSound() {
		return BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.generic.death"));
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		AlghoulRetaliateProcedure.execute(this.level(), this, source.getEntity());
		return super.hurtServer(level, source, amount);
	}

	@Override
	public void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("DataCombatTicks", this.entityData.get(DATA_CombatTicks));
		output.putBoolean("DataSpikesOut", this.entityData.get(DATA_SpikesOut));
		output.putInt("DataStanceAnimation", this.entityData.get(DATA_StanceAnimation));
		output.putInt("DataStanceTicks", this.entityData.get(DATA_StanceTicks));
		output.putInt("DataAttackAnimation", this.entityData.get(DATA_AttackAnimation));
		output.putInt("DataAttackTicks", this.entityData.get(DATA_AttackTicks));
		output.putBoolean("DataWasSwinging", this.entityData.get(DATA_WasSwinging));
	}

	@Override
	public void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.entityData.set(DATA_CombatTicks, input.getIntOr("DataCombatTicks", 0));
		this.entityData.set(DATA_SpikesOut, input.getBooleanOr("DataSpikesOut", false));
		this.entityData.set(DATA_StanceAnimation, input.getIntOr("DataStanceAnimation", 0));
		this.entityData.set(DATA_StanceTicks, input.getIntOr("DataStanceTicks", 0));
		this.entityData.set(DATA_AttackAnimation, input.getIntOr("DataAttackAnimation", 0));
		this.entityData.set(DATA_AttackTicks, input.getIntOr("DataAttackTicks", 0));
		this.entityData.set(DATA_WasSwinging, input.getBooleanOr("DataWasSwinging", false));
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			this.animationState0.animateWhen(AlghoulSpikesOnAnimationConditionProcedure.execute(this), this.tickCount);
			this.animationState1.animateWhen(AlghoulSpikesOffAnimationConditionProcedure.execute(this), this.tickCount);
			this.animationState2.animateWhen(AlghoulSlapSpikesAnimationConditionProcedure.execute(this), this.tickCount);
			this.animationState3.animateWhen(AlghoulSlapNoSpikesAnimationConditionProcedure.execute(this), this.tickCount);
			this.animationState4.animateWhen(AlghoulIdleSpikesAnimationConditionProcedure.execute(this), this.tickCount);
			this.animationState5.animateWhen(AlghoulIdleNoSpikesAnimationConditionProcedure.execute(this), this.tickCount);
		}
	}

	@Override
	public void baseTick() {
		super.baseTick();
		AlghoulCombatTickProcedure.execute(this);
	}

	public static void init(RegisterSpawnPlacementsEvent event) {
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.27).add(Attributes.MAX_HEALTH, 50).add(Attributes.ARMOR, 0)
				.add(Attributes.ATTACK_DAMAGE, 5).add(Attributes.FOLLOW_RANGE, 20).add(Attributes.STEP_HEIGHT, 0.6);
	}
}
