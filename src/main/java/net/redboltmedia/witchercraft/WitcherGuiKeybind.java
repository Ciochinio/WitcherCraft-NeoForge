package net.redboltmedia.witchercraft;

import org.lwjgl.glfw.GLFW;

import net.redboltmedia.witchercraft.init.WitchercraftModKeyMappings;

import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

/**
 * Client-only keybinds that open the {@link WitcherGuiScreen} shell.
 *
 * Hand-written (no MCreator element): auto-registered via {@code @EventBusSubscriber}.
 * Unlike the MCreator GUI keybinds (which send a server message to open a
 * container menu), the shell is a plain client Screen, so these just call
 * {@code Minecraft.setScreen} directly - no networking.
 *
 * - {@link #OPEN_SHELL} (default key P) opens the shell on its default tab.
 * - One keybind per navbar page ({@link #PAGE_KEYS}, built from
 *   {@link WitcherGuiLayout#NAV}) opens the shell straight onto that tab. These
 *   are UNBOUND by default (GLFW_KEY_UNKNOWN) so nothing clashes with vanilla or
 *   other mods - assign them in the Controls menu. Adding a nav tab in the tool
 *   automatically gets it a keybind here.
 */
@EventBusSubscriber(Dist.CLIENT)
public class WitcherGuiKeybind {

	public static final KeyMapping OPEN_SHELL = new KeyMapping("key.witchercraft.open_shell", GLFW.GLFW_KEY_P, WitchercraftModKeyMappings.CATEGORY_WITCHERCRAFT);

	public static final KeyMapping[] PAGE_KEYS;
	private static final String[] PAGE_IDS;

	// Default keys per known page (tweakable in Controls). Chosen to avoid vanilla
	// keys and this mod's existing bindings (B pause-menu, Tab sign-gui, R cast,
	// P open-shell). A page not listed here (e.g. a new tab added in the tool)
	// stays UNBOUND by default.
	private static final java.util.Map<String, Integer> DEFAULT_KEYS = java.util.Map.of(
			"inventory", GLFW.GLFW_KEY_I,
			"skills", GLFW.GLFW_KEY_K,
			"alchemy", GLFW.GLFW_KEY_J,
			"bombs", GLFW.GLFW_KEY_N,
			"map", GLFW.GLFW_KEY_M,
			"glossary", GLFW.GLFW_KEY_G,
			"meditation", GLFW.GLFW_KEY_U);

	static {
		int n = WitcherGuiLayout.NAV.length;
		PAGE_KEYS = new KeyMapping[n];
		PAGE_IDS = new String[n];
		for (int i = 0; i < n; i++) {
			String pid = WitcherGuiLayout.NAV[i].pageId;
			PAGE_IDS[i] = pid;
			int def = DEFAULT_KEYS.getOrDefault(pid, GLFW.GLFW_KEY_UNKNOWN);
			PAGE_KEYS[i] = new KeyMapping("key.witchercraft.open_shell." + pid, def, WitchercraftModKeyMappings.CATEGORY_WITCHERCRAFT);
		}
	}

	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(OPEN_SHELL);
		for (KeyMapping km : PAGE_KEYS)
			event.register(km);
	}

	@EventBusSubscriber(Dist.CLIENT)
	public static class KeyEventListener {
		@SubscribeEvent
		public static void onClientTick(ClientTickEvent.Post event) {
			String target = null;
			while (OPEN_SHELL.consumeClick())
				target = WitcherGuiPages.defaultPageId();
			for (int i = 0; i < PAGE_KEYS.length; i++)
				while (PAGE_KEYS[i].consumeClick())
					target = PAGE_IDS[i];
			if (target != null && Minecraft.getInstance().screen == null && Minecraft.getInstance().player != null)
				Minecraft.getInstance().setScreen(new WitcherGuiScreen(target));
		}
	}
}
