/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.redboltmedia.witchercraft.init;

import net.redboltmedia.witchercraft.world.inventory.SignGuiMenu;
import net.redboltmedia.witchercraft.world.inventory.PauseMenuGuiMenu;
import net.redboltmedia.witchercraft.world.inventory.CharacterGuiMenu;
import net.redboltmedia.witchercraft.network.MenuStateUpdateMessage;
import net.redboltmedia.witchercraft.WitchercraftMod;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.client.Minecraft;

import java.util.Map;

public class WitchercraftModMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, WitchercraftMod.MODID);
	public static final DeferredHolder<MenuType<?>, MenuType<PauseMenuGuiMenu>> PAUSE_MENU_GUI = REGISTRY.register("pause_menu_gui", () -> IMenuTypeExtension.create(PauseMenuGuiMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<CharacterGuiMenu>> CHARACTER_GUI = REGISTRY.register("character_gui", () -> IMenuTypeExtension.create(CharacterGuiMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<SignGuiMenu>> SIGN_GUI = REGISTRY.register("sign_gui", () -> IMenuTypeExtension.create(SignGuiMenu::new));

	public interface MenuAccessor {
		Map<String, Object> getMenuState();

		Map<Integer, Slot> getSlots();

		default void sendMenuStateUpdate(Player player, int elementType, String name, Object elementState, boolean needClientUpdate) {
			getMenuState().put(elementType + ":" + name, elementState);
			if (player instanceof ServerPlayer serverPlayer) {
				PacketDistributor.sendToPlayer(serverPlayer, new MenuStateUpdateMessage(elementType, name, elementState));
			} else if (player.level().isClientSide()) {
				if (Minecraft.getInstance().screen instanceof WitchercraftModScreens.ScreenAccessor accessor && needClientUpdate)
					accessor.updateMenuState(elementType, name, elementState);
				ClientPacketDistributor.sendToServer(new MenuStateUpdateMessage(elementType, name, elementState));
			}
		}

		default <T> T getMenuState(int elementType, String name, T defaultValue) {
			try {
				return (T) getMenuState().getOrDefault(elementType + ":" + name, defaultValue);
			} catch (ClassCastException e) {
				return defaultValue;
			}
		}
	}
}