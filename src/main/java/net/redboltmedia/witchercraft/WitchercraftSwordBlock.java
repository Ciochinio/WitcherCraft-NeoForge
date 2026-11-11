/*
 * The code of this mod element is always locked.
 *
 * HAND-MAINTAINED (locked code element WitchercraftSwordBlock, ~/Admin/Calc). Sword blocking and the
 * perfect parry (GDD 5, Blocking and parrying). Blockly cannot add a data component to vanilla items or
 * read the raise timing, so this lives in Java; the perks that react to a parry stay in Blockly and only
 * read the NBT stamp written here.
 */

package net.redboltmedia.witchercraft;

import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.HolderSet;

import java.util.Optional;
import java.util.List;

@EventBusSubscriber
public final class WitchercraftSwordBlock {
	/** Tick of the last perfect parry, on the blocker. Counterattack (WitchercraftOnShieldBlock) compares it with tickCount. */
	public static final String PERFECT_PARRY_TICK = "witchercraftPerfectParryTick";
	private static final String RAISE_TICK = "witchercraftParryRaiseTick";
	private static final String WINDOW_START = "witchercraftParryWindowStart";

	/** A hit this many ticks after the block becomes active (or sooner) is a perfect parry. */
	public static final int PERFECT_WINDOW_TICKS = 5;
	/** A raise opens a perfect window only if the previous raise started at least this long ago; stops right-click spam. */
	public static final int RERAISE_LOCKOUT_TICKS = 20;
	public static final float SWORD_BLOCK_FACTOR = 0.5f;

	private WitchercraftSwordBlock() {
	}

	/** Counterattack equipped: sword blocking, the perfect parry, stagger and the incoming-attack indicator. Without it a shield is vanilla. */
	public static boolean hasParry(Player player) {
		return player.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkCounterattack;
	}

	private static BlocksAttacks swordBlock(Level level) {
		HolderSet<DamageType> bypass = level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypeTags.BYPASSES_SHIELD);
		return new BlocksAttacks(0f, 1f, List.of(new BlocksAttacks.DamageReduction(90f, Optional.empty(), 0f, SWORD_BLOCK_FACTOR)),
				new BlocksAttacks.ItemDamageFunction(0f, 1f, 0f), Optional.of(bypass), Optional.of(SoundEvents.SHIELD_BLOCK), Optional.of(SoundEvents.SHIELD_BREAK));
	}

	/**
	 * Every blocking item (vanilla shield, modded shields) blocks on the press, like the sword, so one perfect
	 * window and one indicator timing fit all. Default components, so it can't follow a world option. LOWEST so
	 * other mods' component edits are already in.
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void noRaiseDelay(ModifyDefaultComponentsEvent event) {
		event.modifyMatching((item, components) -> {
			BlocksAttacks blocks = components.get(DataComponents.BLOCKS_ATTACKS);
			return blocks != null && blocks.blockDelaySeconds() > 0;
		}, (builder, context, item) -> {
			BlocksAttacks b = builder.get(DataComponents.BLOCKS_ATTACKS);
			if (b != null)
				builder.set(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(0f, b.disableCooldownScale(), b.damageReductions(), b.itemDamage(), b.bypassedBy(), b.blockSound(), b.disableSound()));
		});
	}

	/**
	 * The component is put on the sword stack when it is right-clicked with an empty off-hand, and taken off
	 * again when it is right-clicked while blocking is not allowed, so the off-hand item (shield, chain hook,
	 * bomb, torch) is used instead. This runs on both sides before Item.use, which is what starts the block.
	 * Swords that block by default (another mod's) are never touched.
	 */
	@SubscribeEvent
	public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
		if (event.getHand() != InteractionHand.MAIN_HAND)
			return;
		ItemStack stack = event.getItemStack();
		if (!stack.is(ItemTags.SWORDS) || stack.getPrototype().has(DataComponents.BLOCKS_ATTACKS))
			return;
		Player player = event.getEntity();
		boolean allowed = WorldMapServerConfig.swordBlocking() && player.getOffhandItem().isEmpty() && hasParry(player);
		if (allowed && !stack.has(DataComponents.BLOCKS_ATTACKS))
			stack.set(DataComponents.BLOCKS_ATTACKS, swordBlock(player.level()));
		else if (!allowed && stack.has(DataComponents.BLOCKS_ATTACKS))
			stack.remove(DataComponents.BLOCKS_ATTACKS);
	}

	/** Any block (sword or shield): open a perfect window when the raise is fresh. Server only. */
	@SubscribeEvent
	public static void onUseStart(LivingEntityUseItemEvent.Start event) {
		if (!(event.getEntity() instanceof Player player) || player.level().isClientSide())
			return;
		BlocksAttacks blocks = event.getItem().get(DataComponents.BLOCKS_ATTACKS);
		if (blocks == null)
			return;
		CompoundTag data = player.getPersistentData();
		if (!hasParry(player)) {
			data.putDouble(WINDOW_START, 0);
			return;
		}
		int now = player.tickCount;
		double last = data.getDoubleOr(RAISE_TICK, 0);
		// tickCount restarts on relog and respawn, so a stamp from before reads as negative and counts as fresh
		boolean locked = last > 0 && now - last >= 0 && now - last < RERAISE_LOCKOUT_TICKS;
		data.putDouble(WINDOW_START, locked ? 0 : Math.max(1, now + blocks.blockDelayTicks()));
		data.putDouble(RAISE_TICK, Math.max(1, now));
	}

	/**
	 * Runs before the Blockly shield-block procedure (HIGH priority), so the parry stamp is set when it reads it.
	 * Only a hit the block actually catches (in front, not bypassing) can be a perfect parry.
	 */
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onBlock(LivingShieldBlockEvent event) {
		if (!(event.getEntity() instanceof Player player) || !event.getBlocked() || event.getOriginalBlockedDamage() <= 0)
			return;
		CompoundTag data = player.getPersistentData();
		int now = player.tickCount;
		double start = data.getDoubleOr(WINDOW_START, 0);
		if (!(start > 0 && now - start >= 0 && now - start <= PERFECT_WINDOW_TICKS))
			return;
		event.setBlockedDamage(event.getDamageContainer().getNewDamage());
		data.putDouble(PERFECT_PARRY_TICK, now);
		if (player.level() instanceof ServerLevel level) {
			Vec3 at = player.getEyePosition().add(player.getLookAngle().scale(0.7)).subtract(0, 0.3, 0);
			level.playSound(null, at.x, at.y, at.z, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.45f, 1.9f);
			level.sendParticles(ParticleTypes.CRIT, at.x, at.y, at.z, 12, 0.15, 0.15, 0.15, 0.25);
		}
	}
}