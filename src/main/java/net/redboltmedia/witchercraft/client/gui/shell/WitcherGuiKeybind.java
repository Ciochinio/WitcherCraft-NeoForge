package net.redboltmedia.witchercraft.client.gui.shell;

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
 * Client-only keybind that opens the {@link WitcherGuiScreen} shell.
 *
 * Hand-written (no MCreator element): auto-registered via {@code @EventBusSubscriber}.
 * Unlike the MCreator GUI keybinds (which send a server message to open a
 * container menu), the shell is a plain client Screen, so this just calls
 * {@code Minecraft.setScreen} directly - no networking. Default key P; reuses
 * the existing "witchercraft" keybind category. The user can rebind in Controls.
 */
@EventBusSubscriber(Dist.CLIENT)
public class WitcherGuiKeybind {

	public static final KeyMapping OPEN_SHELL = new KeyMapping("key.witchercraft.open_shell", GLFW.GLFW_KEY_P, WitchercraftModKeyMappings.CATEGORY_WITCHERCRAFT);

	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(OPEN_SHELL);
	}

	@EventBusSubscriber(Dist.CLIENT)
	public static class KeyEventListener {
		@SubscribeEvent
		public static void onClientTick(ClientTickEvent.Post event) {
			// Only fire when no screen is open, and consume all queued clicks.
			boolean open = false;
			while (OPEN_SHELL.consumeClick())
				open = true;
			if (open && Minecraft.getInstance().screen == null && Minecraft.getInstance().player != null)
				Minecraft.getInstance().setScreen(new WitcherGuiScreen());
		}
	}
}
