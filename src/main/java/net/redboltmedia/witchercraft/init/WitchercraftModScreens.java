/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.redboltmedia.witchercraft.init;

import net.redboltmedia.witchercraft.client.gui.*;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

@EventBusSubscriber(Dist.CLIENT)
public class WitchercraftModScreens {
	@SubscribeEvent
	public static void clientLoad(RegisterMenuScreensEvent event) {
		event.register(WitchercraftModMenus.PAUSE_MENU_GUI.get(), PauseMenuGuiScreen::new);
		event.register(WitchercraftModMenus.MEDITATION_GUI.get(), MeditationGuiScreen::new);
		event.register(WitchercraftModMenus.ALCHEMY_GUI.get(), AlchemyGuiScreen::new);
		event.register(WitchercraftModMenus.GLOSSARY_MENU_GUI.get(), GlossaryMenuGuiScreen::new);
		event.register(WitchercraftModMenus.BESTIARY_MENU_GUI.get(), BestiaryMenuGuiScreen::new);
		event.register(WitchercraftModMenus.CHARACTER_GUI.get(), CharacterGuiScreen::new);
		event.register(WitchercraftModMenus.SIGN_GUI.get(), SignGuiScreen::new);
		event.register(WitchercraftModMenus.CHARACTER_ABILITIES_GENERAL_GUI.get(), CharacterAbilitiesGeneralGuiScreen::new);
		event.register(WitchercraftModMenus.CHARACTER_ABILITIES_COMBAT_GUI.get(), CharacterAbilitiesCombatGuiScreen::new);
		event.register(WitchercraftModMenus.CHARACTER_ABILITIES_ALCHEMY_GUI.get(), CharacterAbilitiesAlchemyGuiScreen::new);
		event.register(WitchercraftModMenus.CHARACTER_ABILITIES_SIGNS_GUI.get(), CharacterAbilitiesSignsGuiScreen::new);
		event.register(WitchercraftModMenus.DROWNER_GUI.get(), DrownerGuiScreen::new);
		event.register(WitchercraftModMenus.ROTFIEND_GUI.get(), RotfiendGuiScreen::new);
		event.register(WitchercraftModMenus.FOGLET_GUI.get(), FogletGuiScreen::new);
		event.register(WitchercraftModMenus.BRUXA_GUI.get(), BruxaGuiScreen::new);
		event.register(WitchercraftModMenus.GRAVEIR_GUI.get(), GraveirGuiScreen::new);
		event.register(WitchercraftModMenus.HIGHER_VAMPIRE_GUI.get(), HigherVampireGuiScreen::new);
	}

	public interface ScreenAccessor {
		void updateMenuState(int elementType, String name, Object elementState);
	}
}