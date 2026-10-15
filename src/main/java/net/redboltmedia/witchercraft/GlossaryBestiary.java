package net.redboltmedia.witchercraft;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.redboltmedia.witchercraft.init.WitchercraftModItems;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;

/**
 * The Bestiary's data: the monster categories and the creatures in each.
 *
 * A category IS a {@code witchercraft:<id>} entity type tag - the same tags the
 * oils match against - so every creature tagged into a category gets an entry,
 * mod monsters and vanilla ones alike, and the Bestiary can never disagree with
 * the oil damage. An entry's description is one lang key
 * ({@code glossary.<namespace>.<path>.description}); its stats are read live from a client-side copy of the creature (the doll), so a
 * rebalance shows up without touching the Bestiary.
 *
 * Client only (entity type tags are synced to the client with the registries).
 */
public final class GlossaryBestiary {
	private GlossaryBestiary() {
	}

	/** The categories, top to bottom in the rail. Each names its tag and the oil that hurts it. */
	public enum Category {
		BEASTS("beast", () -> WitchercraftModItems.BEAST_OIL.get()),
		CURSED("cursed", () -> WitchercraftModItems.CURSED_OIL.get()),
		DRACONIDS("draconid", () -> WitchercraftModItems.DRACONID_OIL.get()),
		INSECTOIDS("insectoid", () -> WitchercraftModItems.INSECTOID_OIL.get()),
		NECROPHAGES("necrophage", () -> WitchercraftModItems.NECROPHAGE_OIL.get()),
		OGROIDS("ogroid", () -> WitchercraftModItems.OGROID_OIL.get()),
		RELICTS("relict", () -> WitchercraftModItems.RELICT_OIL.get()),
		SPECTERS("specter", () -> WitchercraftModItems.SPECTER_OIL.get()),
		VAMPIRES("vampire", () -> WitchercraftModItems.VAMPIRE_OIL.get()),
		HUMANS("humanoid", () -> WitchercraftModItems.HANGED_MANS_VENOM.get());

		private final String id;
		private final TagKey<EntityType<?>> tag;
		private final Supplier<Item> oil;

		Category(String id, Supplier<Item> oil) {
			this.id = id;
			this.tag = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, id));
			this.oil = oil;
		}

		public Component title() {
			return Component.translatable("glossary.witchercraft.category." + id);
		}

		/** The oil whose bonus damage lands on this category. */
		public ItemStack oil() {
			return new ItemStack(oil.get());
		}
	}

	/** One creature's entry. */
	public record Entry(EntityType<?> type, Identifier id) {
		public Component name() {
			return type.getDescription();
		}

		/** Lang key of the entry's description: {@code glossary.<namespace>.<path>.description}. */
		public String descriptionKey() {
			return "glossary." + id.getNamespace() + "." + id.getPath() + ".description";
		}

		/** The list icon: the creature's spawn egg, or empty when it has none. */
		public ItemStack icon() {
			return SpawnEggItem.byId(type).map(egg -> new ItemStack(egg.value())).orElse(ItemStack.EMPTY);
		}
	}

	/**
	 * A weakness besides the category's oil: a Sign or a bomb. A creature has it when it is in the
	 * entity tag {@code witchercraft:weak_to_<id>} - the same tag the Sign's or bomb's own procedure
	 * can check ("is entity tagged as") to deal the extra damage, so the Bestiary and the damage
	 * stay in step. Shown in the "Weak to" row after the oils, in this order.
	 */
	public record Weakness(String id, TagKey<EntityType<?>> tag, Supplier<Item> item, String texture) {
		public Component name() {
			return item != null ? new ItemStack(item.get()).getHoverName() : Component.translatable("glossary.witchercraft.weakness." + id);
		}

		/** The item icon, or empty when the icon is a texture (64x64 Sign art). */
		public ItemStack itemIcon() {
			return item == null ? ItemStack.EMPTY : new ItemStack(item.get());
		}
	}

	private static Weakness sign(String id) {
		return new Weakness(id, weakTag(id), null, "witchercraft:textures/screens/" + id + ".png");
	}

	private static Weakness bomb(String id, Supplier<Item> item) {
		return new Weakness(id, weakTag(id), item, null);
	}

	private static TagKey<EntityType<?>> weakTag(String id) {
		return TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "weak_to_" + id));
	}

	public static final List<Weakness> WEAKNESSES = List.of(
			sign("aard"), sign("igni"), sign("yrden"), sign("quen"), sign("axii"),
			bomb("grapeshot", () -> WitchercraftModItems.GRAPESHOT.get()),
			bomb("dancing_star", () -> WitchercraftModItems.DANCING_STAR.get()),
			bomb("devils_puffball", () -> WitchercraftModItems.DEVILS_PUFFBALL.get()),
			bomb("northern_wind", () -> WitchercraftModItems.NORTHERN_WIND.get()),
			bomb("samum", () -> WitchercraftModItems.SAMUM.get()),
			bomb("dimeritium_bomb", () -> WitchercraftModItems.DIMERITIUM_BOMB.get()));

	/** The extra weaknesses whose {@code weak_to_<id>} tag holds this creature. */
	public static List<Weakness> weaknessesOf(EntityType<?> type) {
		Holder<EntityType<?>> holder = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type);
		List<Weakness> out = new ArrayList<>();
		for (Weakness w : WEAKNESSES)
			if (holder.is(w.tag()))
				out.add(w);
		return out;
	}

	private static final Map<Category, List<Entry>> ENTRIES = new EnumMap<>(Category.class);
	private static final Map<EntityType<?>, LivingEntity> DOLLS = new HashMap<>();
	private static Level dollLevel;

	/** Forget cached entries and dolls (tags can change on a datapack reload). */
	public static void refresh() {
		ENTRIES.clear();
		DOLLS.clear();
		dollLevel = null;
	}

	/** The creatures tagged into a category: this mod's first, then the rest, each by name. */
	public static List<Entry> entries(Category category) {
		return ENTRIES.computeIfAbsent(category, c -> {
			List<Entry> list = new ArrayList<>();
			for (Holder<EntityType<?>> holder : BuiltInRegistries.ENTITY_TYPE.getTagOrEmpty(c.tag))
				list.add(new Entry(holder.value(), BuiltInRegistries.ENTITY_TYPE.getKey(holder.value())));
			list.sort(Comparator.comparing((Entry e) -> !WitchercraftMod.MODID.equals(e.id().getNamespace()))
					.thenComparing(e -> e.name().getString()));
			return list;
		});
	}

	/** A client-side copy of the creature for the doll and its stats, or null if it is not a living entity. */
	public static LivingEntity doll(EntityType<?> type) {
		Level level = Minecraft.getInstance().level;
		if (level == null)
			return null;
		if (level != dollLevel) {
			DOLLS.clear();
			dollLevel = level;
		}
		if (!DOLLS.containsKey(type)) {
			LivingEntity living = null;
			try {
				Entity entity = type.create(level, EntitySpawnReason.LOAD);
				if (entity instanceof LivingEntity l)
					living = l;
			} catch (RuntimeException e) {
				WitchercraftMod.LOGGER.warn("Glossary could not create a doll for {}", BuiltInRegistries.ENTITY_TYPE.getKey(type), e);
			}
			DOLLS.put(type, living);
		}
		return DOLLS.get(type);
	}

	/** An attribute's current value on the doll, or -1 when the creature does not have it. */
	public static double stat(LivingEntity doll, Holder<Attribute> attribute) {
		AttributeInstance instance = doll.getAttribute(attribute);
		return instance == null ? -1 : instance.getValue();
	}
}
