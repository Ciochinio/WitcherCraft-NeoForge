/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.redboltmedia.witchercraft.init;

import net.redboltmedia.witchercraft.client.gui.SignGuiScreen;
import net.redboltmedia.witchercraft.client.gui.PauseMenuGuiScreen;
import net.redboltmedia.witchercraft.client.gui.CharacterGuiScreen;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

@EventBusSubscriber(Dist.CLIENT)
public class WitchercraftModScreens {
	@SubscribeEvent
	public static void clientLoad(RegisterMenuScreensEvent event) {
		event.register(WitchercraftModMenus.PAUSE_MENU_GUI.get(), PauseMenuGuiScreen::new);
		event.register(WitchercraftModMenus.CHARACTER_GUI.get(), CharacterGuiScreen::new);
		event.register(WitchercraftModMenus.SIGN_GUI.get(), SignGuiScreen::new);
	}

	public interface ScreenAccessor {
		void updateMenuState(int elementType, String name, Object elementState);
	}
}