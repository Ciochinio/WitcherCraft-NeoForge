package net.redboltmedia.witchercraft;

import java.util.List;
import java.util.function.Supplier;

import net.redboltmedia.witchercraft.init.WitchercraftModItems;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * The Glossary's Tutorials: topics about the mod's mechanics, grouped by system.
 *
 * A topic's title and text are lang keys ({@code glossary.witchercraft.tutorial.<id>.title / .text}).
 * The text's {@code %1$s}, {@code %2$s}... are the topic's keybinds, shown as whatever key the
 * player has bound, so the text stays right after a rebind.
 *
 * Its illustration is {@code witchercraft:textures/glossary/tutorials/<id>.png} when that file
 * exists (stretched over the illustration panel); otherwise the topic's icon is drawn large
 * in the panel's centre. The icon is an item or a texture (with its pixel size, so it keeps
 * its proportions).
 */
public final class GlossaryTutorials {
	private GlossaryTutorials() {
	}

	public record Topic(String id, Supplier<Item> item, String texture, int textureW, int textureH, List<String> keys) {
		public Component title() {
			return Component.translatable("glossary.witchercraft.tutorial." + id + ".title");
		}

		public Component text() {
			return Component.translatable("glossary.witchercraft.tutorial." + id + ".text", keys.stream().map(Component::keybind).toArray());
		}

		/** The optional full illustration for this topic. */
		public Identifier image() {
			return Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "textures/glossary/tutorials/" + id + ".png");
		}

		/** The item icon, or empty when the icon is a texture. */
		public ItemStack itemIcon() {
			return item == null ? ItemStack.EMPTY : new ItemStack(item.get());
		}
	}

	public record Group(String id, List<Topic> topics) {
		public Component title() {
			return Component.translatable("glossary.witchercraft.tutorial_group." + id);
		}
	}

	private static Topic item(String id, Supplier<Item> item, String... keys) {
		return new Topic(id, item, null, 0, 0, List.of(keys));
	}

	private static Topic texture(String id, String texture, int w, int h, String... keys) {
		return new Topic(id, null, texture, w, h, List.of(keys));
	}

	public static final List<Group> GROUPS = List.of(
			new Group("signs", List.of(
					texture("signs", "witchercraft:textures/screens/igni.png", 64, 64,
							"key.witchercraft.sign_gui_keybind", "key.witchercraft.sign_cast_keybind"),
					texture("sign_intensity", "witchercraft:textures/screens/aard.png", 64, 64))),
			new Group("alchemy", List.of(
					item("brewing", () -> WitchercraftModItems.SWALLOW.get(),
							"key.witchercraft.open_shell", "key.witchercraft.open_shell.alchemy"),
					item("oils", () -> WitchercraftModItems.NECROPHAGE_OIL.get()),
					item("toxicity", () -> WitchercraftModItems.WHITE_HONEY.get()))),
			new Group("hunting", List.of(
					texture("medallion", "witchercraft:textures/screens/medallion.png", 20, 40),
					item("monster_categories", () -> WitchercraftModItems.BEAST_OIL.get(), "key.witchercraft.open_shell.glossary"))),
			new Group("world", List.of(
					texture("meditation", "witchercraft:textures/screens/nav_meditation.png", 32, 32, "key.witchercraft.open_shell.meditation"),
					item("fast_travel", () -> WitchercraftModItems.FAST_TRAVEL_SIGN.get()))));

	/** Every topic in list order. */
	public static List<Topic> topics() {
		return GROUPS.stream().flatMap(g -> g.topics().stream()).toList();
	}
}
