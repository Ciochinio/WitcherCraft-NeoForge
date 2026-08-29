/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.redboltmedia.witchercraft.init;

import net.redboltmedia.witchercraft.world.inventory.*;
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
	public static final DeferredHolder<MenuType<?>, MenuType<AlchemyGuiMenu>> ALCHEMY_GUI = REGISTRY.register("alchemy_gui", () -> IMenuTypeExtension.create(AlchemyGuiMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<GlossaryMenuGuiMenu>> GLOSSARY_MENU_GUI = REGISTRY.register("glossary_menu_gui", () -> IMenuTypeExtension.create(GlossaryMenuGuiMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<BestiaryMenuGuiMenu>> BESTIARY_MENU_GUI = REGISTRY.register("bestiary_menu_gui", () -> IMenuTypeExtension.create(BestiaryMenuGuiMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<CharacterGuiMenu>> CHARACTER_GUI = REGISTRY.register("character_gui", () -> IMenuTypeExtension.create(CharacterGuiMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<SignGuiMenu>> SIGN_GUI = REGISTRY.register("sign_gui", () -> IMenuTypeExtension.create(SignGuiMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<DrownerGuiMenu>> DROWNER_GUI = REGISTRY.register("drowner_gui", () -> IMenuTypeExtension.create(DrownerGuiMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<RotfiendGuiMenu>> ROTFIEND_GUI = REGISTRY.register("rotfiend_gui", () -> IMenuTypeExtension.create(RotfiendGuiMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<FogletGuiMenu>> FOGLET_GUI = REGISTRY.register("foglet_gui", () -> IMenuTypeExtension.create(FogletGuiMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<BruxaGuiMenu>> BRUXA_GUI = REGISTRY.register("bruxa_gui", () -> IMenuTypeExtension.create(BruxaGuiMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<GraveirGuiMenu>> GRAVEIR_GUI = REGISTRY.register("graveir_gui", () -> IMenuTypeExtension.create(GraveirGuiMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<HigherVampireGuiMenu>> HIGHER_VAMPIRE_GUI = REGISTRY.register("higher_vampire_gui", () -> IMenuTypeExtension.create(HigherVampireGuiMenu::new));

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