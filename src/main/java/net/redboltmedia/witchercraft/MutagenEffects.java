package net.redboltmedia.witchercraft;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.redboltmedia.witchercraft.procedures.MutagenBlueEffectProcedure;
import net.redboltmedia.witchercraft.procedures.MutagenGreenEffectProcedure;
import net.redboltmedia.witchercraft.procedures.MutagenRedEffectProcedure;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * What a mutagen item is, and applying the equipped mutagens' effects.
 *
 * DATA: an item is a mutagen when it is in one type tag
 * ({@code #witchercraft:mutagen/red|green|blue}) and one level tag
 * ({@code #witchercraft:mutagen/level_1..3}). Adding, removing or re-levelling a
 * mutagen item is a tag edit; no code. Tags sync to the client, so the slots
 * predict placement and the Skills page colours its sockets without a packet.
 *
 * EFFECTS: each type runs its Mutagen<Type>Effect Blockly procedure with
 * {@code entity}, {@code level} (1-3) and {@code synergy} (perks of the type's
 * branch colour in the three perk slots above that socket). The procedure adds
 * attribute modifiers with plain fixed names. {@link #recompute} then renames
 * whatever each call added to a per-socket id ({@code mutagen_slot<N>/...}), so
 * two mutagens of the same type in different sockets both apply, and the next
 * recompute can strip them all.
 *
 * Adding a NEW type: a type tag, a Blockly procedure, and one {@link Type} entry.
 *
 * HAND-MAINTAINED: locked code element.
 */
public final class MutagenEffects {
	private MutagenEffects() {
	}

	/** Signature of a Mutagen<Type>Effect procedure, as MCreator generates it. */
	@FunctionalInterface
	public interface Effect {
		void apply(Entity entity, double level, double synergy);
	}

	/** Mutagen types. {@code branch} is the perk branch colour it synergises with (PerkRegistry.COLOR_*). */
	public enum Type {
		RED("red", PerkRegistry.COLOR_RED, MutagenRedEffectProcedure::execute),
		GREEN("green", PerkRegistry.COLOR_GREEN, MutagenGreenEffectProcedure::execute),
		BLUE("blue", PerkRegistry.COLOR_BLUE, MutagenBlueEffectProcedure::execute);

		public final TagKey<Item> tag;
		public final int branch;
		final Effect effect;

		Type(String id, int branch, Effect effect) {
			this.tag = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "mutagen/" + id));
			this.branch = branch;
			this.effect = effect;
		}

		/** The type's GUI tint (the branch colour). */
		public int tint() {
			return PerkRegistry.tint(branch);
		}
	}

	public static final int MAX_LEVEL = 3;
	private static final List<TagKey<Item>> LEVEL_TAGS = new ArrayList<>();

	static {
		for (int level = 1; level <= MAX_LEVEL; level++)
			LEVEL_TAGS.add(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "mutagen/level_" + level)));
	}

	/** Prefix of every modifier id this class owns; anything under it is stripped on recompute. */
	private static final String ID_PREFIX = "mutagen_";

	// ---- item queries (both sides) ------------------------------------------------------

	/** The stack's mutagen type, or null if it is not a mutagen. */
	public static Type typeOf(ItemStack stack) {
		if (stack.isEmpty())
			return null;
		for (Type type : Type.values())
			if (stack.is(type.tag))
				return type;
		return null;
	}

	/** The stack's mutagen level (highest level tag it is in), or 0 if none. */
	public static int levelOf(ItemStack stack) {
		for (int level = MAX_LEVEL; level >= 1; level--)
			if (stack.is(LEVEL_TAGS.get(level - 1)))
				return level;
		return 0;
	}

	/** A socket accepts the stack: it has a type and a level. */
	public static boolean isMutagen(ItemStack stack) {
		return typeOf(stack) != null && levelOf(stack) > 0;
	}

	/** Perks of {@code type}'s branch colour in the three perk slots above socket {@code slot}. */
	public static int synergy(Player player, int slot, Type type) {
		int count = 0;
		for (int s = slot * 3; s < slot * 3 + 3; s++) {
			int id = PerkEquipVars.getPerkSocket(player, s);
			if (id > 0 && PerkRegistry.color(id) == type.branch)
				count++;
		}
		return count;
	}

	// ---- applying (server) ------------------------------------------------------------------

	/**
	 * Strip every mutagen modifier, then run each filled socket's effect procedure
	 * and move what it added to that socket's ids. Server only. Current health is
	 * kept when Max Health is removed and re-added, so a recompute never costs the
	 * player the health a mutagen grants.
	 */
	public static void recompute(Player player) {
		if (player.level().isClientSide())
			return;
		float health = player.getHealth();

		List<AttributeInstance> instances = new ArrayList<>();
		for (Holder<Attribute> attribute : BuiltInRegistries.ATTRIBUTE.listElements().toList()) {
			AttributeInstance instance = player.getAttributes().getInstance(attribute);
			if (instance != null)
				instances.add(instance);
		}

		for (AttributeInstance instance : instances)
			for (AttributeModifier modifier : List.copyOf(instance.getModifiers()))
				if (isOwned(modifier.id()))
					instance.removeModifier(modifier.id());

		for (int slot = 0; slot < MutagenSlots.SLOTS; slot++) {
			ItemStack stack = MutagenSlots.get(player, slot);
			Type type = typeOf(stack);
			int level = levelOf(stack);
			if (type == null || level == 0)
				continue;

			List<Set<Identifier>> before = new ArrayList<>(instances.size());
			for (AttributeInstance instance : instances) {
				Set<Identifier> ids = new HashSet<>();
				for (AttributeModifier modifier : instance.getModifiers())
					ids.add(modifier.id());
				before.add(ids);
			}

			type.effect.apply(player, level, synergy(player, slot, type));

			for (int i = 0; i < instances.size(); i++) {
				AttributeInstance instance = instances.get(i);
				for (AttributeModifier modifier : List.copyOf(instance.getModifiers())) {
					if (before.get(i).contains(modifier.id()))
						continue;
					instance.removeModifier(modifier.id());
					instance.addTransientModifier(new AttributeModifier(slotId(slot, modifier.id()), modifier.amount(), modifier.operation()));
				}
			}
		}

		if (player.isAlive())
			player.setHealth(Math.min(health, player.getMaxHealth()));
	}

	// ---- reading what a socket gives (both sides) -----------------------------------------

	/**
	 * What socket {@code slot} currently gives, one line per modifier it applied,
	 * e.g. "+15 Increased Damage" or "+10% Sign Intensity". Read from the player's
	 * live attribute modifiers (the {@code mutagen_slot<N>/} ids), which sync to the
	 * client, so this shows whatever the effect procedure did - level, synergy and
	 * perk buffs included - without a packet. Effects that are not attribute
	 * modifiers do not appear.
	 */
	public static List<Component> bonusLines(Player player, int slot) {
		String prefix = ID_PREFIX + "slot" + (slot + 1) + "/";
		List<Component> lines = new ArrayList<>();
		for (Holder<Attribute> attribute : BuiltInRegistries.ATTRIBUTE.listElements().toList()) {
			AttributeInstance instance = player.getAttributes().getInstance(attribute);
			if (instance == null)
				continue;
			for (AttributeModifier modifier : instance.getModifiers()) {
				Identifier id = modifier.id();
				if (!id.getNamespace().equals(WitchercraftMod.MODID) || !id.getPath().startsWith(prefix))
					continue;
				boolean percent = modifier.operation() != AttributeModifier.Operation.ADD_VALUE;
				double amount = percent ? modifier.amount() * 100 : modifier.amount();
				String value = (amount >= 0 ? "+" : "") + NUMBER.format(amount) + (percent ? "%" : "");
				lines.add(Component.literal(value + " ").append(Component.translatable(attribute.value().getDescriptionId())));
			}
		}
		return lines;
	}

	private static final java.text.DecimalFormat NUMBER = new java.text.DecimalFormat("0.##");

	private static boolean isOwned(Identifier id) {
		return id.getNamespace().equals(WitchercraftMod.MODID) && id.getPath().startsWith(ID_PREFIX);
	}

	/** {@code witchercraft:mutagen_slot<N>/<namespace>/<path>} for the modifier an effect added. */
	private static Identifier slotId(int slot, Identifier added) {
		return Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, ID_PREFIX + "slot" + (slot + 1) + "/" + added.getNamespace() + "/" + added.getPath());
	}
}
