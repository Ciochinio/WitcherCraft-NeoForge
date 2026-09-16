package net.redboltmedia.witchercraft.entity;

import net.redboltmedia.witchercraft.procedures.GhoulSlapAnimationConditionProcedure;
import net.redboltmedia.witchercraft.procedures.GhoulIdleAnimationConditionProcedure;
import net.redboltmedia.witchercraft.procedures.GhoulAttackTickProcedure;
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
import net.minecraft.resources.Identifier;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.core.registries.BuiltInRegistries;

public class GhoulEntity extends Monster {
	public static final EntityDataAccessor<Integer> DATA_AttackAnimation = SynchedEntityData.defineId(GhoulEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> DATA_AttackTicks = SynchedEntityData.defineId(GhoulEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Boolean> DATA_WasSwinging = SynchedEntityData.defineId(GhoulEntity.class, EntityDataSerializers.BOOLEAN);
	public final AnimationState animationState0 = new AnimationState();
	public final AnimationState animationState1 = new AnimationState();

	public GhoulEntity(EntityType<GhoulEntity> type, Level world) {
		super(type, world);
		xpReward = 0;
		setNoAi(false);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_AttackAnimation, 0);
		builder.define(DATA_AttackTicks, 0);
		builder.define(DATA_WasSwinging, false);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false) {
			@Override
			protected boolean canPerformAttack(LivingEntity entity) {
				return this.isTimeToAttack() && this.mob.distanceToSqr(entity) < (this.mob.getBbWidth() * this.mob.getBbWidth() + entity.getBbWidth()) && this.mob.getSensing().hasLineOfSight(entity);
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
	public void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("DataAttackAnimation", this.entityData.get(DATA_AttackAnimation));
		output.putInt("DataAttackTicks", this.entityData.get(DATA_AttackTicks));
		output.putBoolean("DataWasSwinging", this.entityData.get(DATA_WasSwinging));
	}

	@Override
	public void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.entityData.set(DATA_AttackAnimation, input.getIntOr("DataAttackAnimation", 0));
		this.entityData.set(DATA_AttackTicks, input.getIntOr("DataAttackTicks", 0));
		this.entityData.set(DATA_WasSwinging, input.getBooleanOr("DataWasSwinging", false));
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			this.animationState0.animateWhen(GhoulSlapAnimationConditionProcedure.execute(this), this.tickCount);
			this.animationState1.animateWhen(GhoulIdleAnimationConditionProcedure.execute(this), this.tickCount);
		}
	}

	@Override
	public void baseTick() {
		super.baseTick();
		GhoulAttackTickProcedure.execute(this);
	}

	public static void init(RegisterSpawnPlacementsEvent event) {
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.33).add(Attributes.MAX_HEALTH, 30).add(Attributes.ARMOR, 0)
				.add(Attributes.ATTACK_DAMAGE, 4).add(Attributes.FOLLOW_RANGE, 20).add(Attributes.STEP_HEIGHT, 0.6);
	}
}
