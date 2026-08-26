/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.redboltmedia.witchercraft.init;

import org.lwjgl.glfw.GLFW;

import net.redboltmedia.witchercraft.network.SignGuiKeybindMessage;
import net.redboltmedia.witchercraft.network.SignCastKeybindMessage;
import net.redboltmedia.witchercraft.network.PauseMenuKeybindPressMessage;
import net.redboltmedia.witchercraft.network.DebugRecomputePerksKeybindMessage;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;

@EventBusSubscriber(Dist.CLIENT)
public class WitchercraftModKeyMappings {
	public static final KeyMapping.Category CATEGORY_WITCHERCRAFT = new KeyMapping.Category(Identifier.parse("witchercraft:witchercraft"));
	public static final KeyMapping PAUSE_MENU_KEYBIND_PRESS = new KeyMapping("key.witchercraft.pause_menu_keybind_press", GLFW.GLFW_KEY_B, KeyMapping.Category.MISC) {
		private boolean isDownOld = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown) {
				ClientPacketDistributor.sendToServer(new PauseMenuKeybindPressMessage(0, 0));
				PauseMenuKeybindPressMessage.pressAction(Minecraft.getInstance().player, 0, 0);
			}
			isDownOld = isDown;
		}
	};
	public static final KeyMapping SIGN_GUI_KEYBIND = new KeyMapping("key.witchercraft.sign_gui_keybind", GLFW.GLFW_KEY_TAB, CATEGORY_WITCHERCRAFT) {
		private boolean isDownOld = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown) {
				ClientPacketDistributor.sendToServer(new SignGuiKeybindMessage(0, 0));
				SignGuiKeybindMessage.pressAction(Minecraft.getInstance().player, 0, 0);
			}
			isDownOld = isDown;
		}
	};
	public static final KeyMapping SIGN_CAST_KEYBIND = new KeyMapping("key.witchercraft.sign_cast_keybind", GLFW.GLFW_KEY_R, CATEGORY_WITCHERCRAFT) {
		private boolean isDownOld = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown) {
				ClientPacketDistributor.sendToServer(new SignCastKeybindMessage(0, 0));
				SignCastKeybindMessage.pressAction(Minecraft.getInstance().player, 0, 0);
				SIGN_CAST_KEYBIND_LASTPRESS = System.currentTimeMillis();
			} else if (isDownOld != isDown && !isDown) {
				int dt = (int) (System.currentTimeMillis() - SIGN_CAST_KEYBIND_LASTPRESS);
				ClientPacketDistributor.sendToServer(new SignCastKeybindMessage(1, dt));
				SignCastKeybindMessage.pressAction(Minecraft.getInstance().player, 1, dt);
			}
			isDownOld = isDown;
		}
	};
	public static final KeyMapping DEBUG_RECOMPUTE_PERKS_KEYBIND = new KeyMapping("key.witchercraft.debug_recompute_perks_keybind", GLFW.GLFW_KEY_P, CATEGORY_WITCHERCRAFT) {
		private boolean isDownOld = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown) {
				ClientPacketDistributor.sendToServer(new DebugRecomputePerksKeybindMessage(0, 0));
				DebugRecomputePerksKeybindMessage.pressAction(Minecraft.getInstance().player, 0, 0);
			}
			isDownOld = isDown;
		}
	};
	private static long SIGN_CAST_KEYBIND_LASTPRESS = 0;

	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.registerCategory(CATEGORY_WITCHERCRAFT);
		event.register(PAUSE_MENU_KEYBIND_PRESS);
		event.register(SIGN_GUI_KEYBIND);
		event.register(SIGN_CAST_KEYBIND);
		event.register(DEBUG_RECOMPUTE_PERKS_KEYBIND);
	}

	@EventBusSubscriber(Dist.CLIENT)
	public static class KeyEventListener {
		@SubscribeEvent
		public static void onClientTick(ClientTickEvent.Post event) {
			if (Minecraft.getInstance().screen == null) {
				PAUSE_MENU_KEYBIND_PRESS.consumeClick();
				SIGN_GUI_KEYBIND.consumeClick();
				SIGN_CAST_KEYBIND.consumeClick();
				DEBUG_RECOMPUTE_PERKS_KEYBIND.consumeClick();
			}
		}
	}
}