package net.redboltmedia.witchercraft;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** Client-only bridge to NeoForge's generated, section-aware configuration UI. */
@EventBusSubscriber(Dist.CLIENT)
public final class WitchercraftConfigScreen {
	private WitchercraftConfigScreen() {}

	@SubscribeEvent
	public static void register(FMLClientSetupEvent event) {
		ModList.get().getModContainerById(WitchercraftMod.MODID).orElseThrow()
			.registerExtensionPoint(IConfigScreenFactory.class, (IConfigScreenFactory) ConfigurationScreen::new);
	}
}
