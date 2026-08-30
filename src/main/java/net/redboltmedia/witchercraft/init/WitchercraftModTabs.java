/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.redboltmedia.witchercraft.init;

import net.redboltmedia.witchercraft.WitchercraftMod;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

public class WitchercraftModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WitchercraftMod.MODID);
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> WITCHER_CRAFT = REGISTRY.register("witcher_craft",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.witchercraft.witcher_craft")).icon(() -> new ItemStack(WitchercraftModItems.SWALLOW.get())).displayItems((parameters, tabData) -> {
				tabData.accept(WitchercraftModItems.WHITE_HONEY.get());
				tabData.accept(WitchercraftModItems.BLIZZARD.get());
				tabData.accept(WitchercraftModItems.CAT.get());
				tabData.accept(WitchercraftModItems.SWALLOW.get());
				tabData.accept(WitchercraftModItems.FULL_MOON.get());
				tabData.accept(WitchercraftModItems.THUNDERBOLT.get());
				tabData.accept(WitchercraftModItems.WHITE_RAFFARDS_DECOCTION.get());
				tabData.accept(WitchercraftModItems.TAWNY_OWL.get());
				tabData.accept(WitchercraftModItems.KILLER_WHALE.get());
				tabData.accept(WitchercraftModItems.PETRIS_PHILTER.get());
				tabData.accept(WitchercraftModItems.DEVILS_PUFFBALL.get());
				tabData.accept(WitchercraftModItems.DANCING_STAR.get());
				tabData.accept(WitchercraftModItems.GRAPESHOT.get());
				tabData.accept(WitchercraftModItems.NORTHERN_WIND.get());
				tabData.accept(WitchercraftModItems.SAMUM.get());
				tabData.accept(WitchercraftModItems.DIMERITIUM_BOMB.get());
				tabData.accept(WitchercraftModItems.INSECTOID_OIL.get());
				tabData.accept(WitchercraftModItems.NECROPHAGE_OIL.get());
				tabData.accept(WitchercraftModItems.HANGED_MANS_VENOM.get());
				tabData.accept(WitchercraftModItems.SPECTER_OIL.get());
				tabData.accept(WitchercraftModItems.OGROID_OIL.get());
				tabData.accept(WitchercraftModItems.BEAST_OIL.get());
				tabData.accept(WitchercraftModItems.DRACONID_OIL.get());
				tabData.accept(WitchercraftModItems.AETHER.get());
				tabData.accept(WitchercraftModItems.HYDRAGENUM.get());
				tabData.accept(WitchercraftModItems.QUEBRITH.get());
				tabData.accept(WitchercraftModItems.REBIS.get());
				tabData.accept(WitchercraftModItems.VERMILION.get());
				tabData.accept(WitchercraftModItems.VITRIOL.get());
				tabData.accept(WitchercraftModItems.WHITE_GULL.get());
				tabData.accept(WitchercraftModItems.SALTPETER.get());
				tabData.accept(WitchercraftModItems.TALLOW.get());
				tabData.accept(WitchercraftModItems.RELICT_OIL.get());
				tabData.accept(WitchercraftModItems.CURSED_OIL.get());
				tabData.accept(WitchercraftModItems.VAMPIRE_OIL.get());
				tabData.accept(WitchercraftModItems.GOLDEN_ORIOLE.get());
				tabData.accept(WitchercraftModItems.FELINE_ARMOR_HELMET.get());
				tabData.accept(WitchercraftModItems.FELINE_ARMOR_CHESTPLATE.get());
				tabData.accept(WitchercraftModItems.FELINE_ARMOR_LEGGINGS.get());
				tabData.accept(WitchercraftModItems.FELINE_ARMOR_BOOTS.get());
				tabData.accept(WitchercraftModItems.WOLVEN_ARMOR_HELMET.get());
				tabData.accept(WitchercraftModItems.WOLVEN_ARMOR_CHESTPLATE.get());
				tabData.accept(WitchercraftModItems.WOLVEN_ARMOR_LEGGINGS.get());
				tabData.accept(WitchercraftModItems.WOLVEN_ARMOR_BOOTS.get());
				tabData.accept(WitchercraftModItems.URSINE_ARMOR_HELMET.get());
				tabData.accept(WitchercraftModItems.URSINE_ARMOR_CHESTPLATE.get());
				tabData.accept(WitchercraftModItems.URSINE_ARMOR_LEGGINGS.get());
				tabData.accept(WitchercraftModItems.URSINE_ARMOR_BOOTS.get());
				tabData.accept(WitchercraftModItems.VIPER_ARMOR_HELMET.get());
				tabData.accept(WitchercraftModItems.VIPER_ARMOR_CHESTPLATE.get());
				tabData.accept(WitchercraftModItems.VIPER_ARMOR_LEGGINGS.get());
				tabData.accept(WitchercraftModItems.VIPER_ARMOR_BOOTS.get());
				tabData.accept(WitchercraftModItems.MANTICORE_ARMOR_HELMET.get());
				tabData.accept(WitchercraftModItems.MANTICORE_ARMOR_CHESTPLATE.get());
				tabData.accept(WitchercraftModItems.MANTICORE_ARMOR_LEGGINGS.get());
				tabData.accept(WitchercraftModItems.MANTICORE_ARMOR_BOOTS.get());
				tabData.accept(WitchercraftModItems.GRIFFIN_ARMOR_HELMET.get());
				tabData.accept(WitchercraftModItems.GRIFFIN_ARMOR_CHESTPLATE.get());
				tabData.accept(WitchercraftModItems.GRIFFIN_ARMOR_LEGGINGS.get());
				tabData.accept(WitchercraftModItems.GRIFFIN_ARMOR_BOOTS.get());
				tabData.accept(WitchercraftModItems.WATER_HAG_DECOCTION.get());
				tabData.accept(WitchercraftModItems.EKIMMARA_DECOCTION.get());
				tabData.accept(WitchercraftModItems.KATAKAN_DECOCTION.get());
				tabData.accept(WitchercraftModItems.LESHEN_DECOCTION.get());
				tabData.accept(WitchercraftModItems.BLACK_BLOOD.get());
				tabData.accept(WitchercraftModItems.FOGLET_DECOCTION.get());
				tabData.accept(WitchercraftModItems.NEKKER_WARRIOR_DECOCTION.get());
				tabData.accept(WitchercraftModItems.TROLL_DECOCTION.get());
				tabData.accept(WitchercraftModItems.WRAITH_DECOCTION.get());
				tabData.accept(WitchercraftModItems.WEREWOLF_DECOCTION.get());
				tabData.accept(WitchercraftModItems.WYVERN_DECOCTION.get());
				tabData.accept(WitchercraftModItems.SUCCUBUS_DECOCTION.get());
				tabData.accept(WitchercraftModItems.GRAVE_HAG_DECOCTION.get());
				tabData.accept(WitchercraftModItems.COCKATRICE_SPAWN_EGG.get());
			}).build());
}