/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.redboltmedia.witchercraft.init;

import net.redboltmedia.witchercraft.item.*;
import net.redboltmedia.witchercraft.WitchercraftMod;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

import net.minecraft.world.item.Item;

import java.util.function.Function;

public class WitchercraftModItems {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(WitchercraftMod.MODID);
	public static final DeferredItem<Item> WHITE_HONEY;
	public static final DeferredItem<Item> BLIZZARD;
	public static final DeferredItem<Item> CAT;
	public static final DeferredItem<Item> SWALLOW;
	public static final DeferredItem<Item> FULL_MOON;
	public static final DeferredItem<Item> THUNDERBOLT;
	public static final DeferredItem<Item> WHITE_RAFFARDS_DECOCTION;
	public static final DeferredItem<Item> TAWNY_OWL;
	public static final DeferredItem<Item> KILLER_WHALE;
	public static final DeferredItem<Item> PETRIS_PHILTER;
	public static final DeferredItem<Item> DEVILS_PUFFBALL;
	public static final DeferredItem<Item> DANCING_STAR;
	public static final DeferredItem<Item> GRAPESHOT;
	public static final DeferredItem<Item> NORTHERN_WIND;
	public static final DeferredItem<Item> SAMUM;
	public static final DeferredItem<Item> DIMERITIUM_BOMB;
	public static final DeferredItem<Item> INSECTOID_OIL;
	public static final DeferredItem<Item> NECROPHAGE_OIL;
	public static final DeferredItem<Item> HANGED_MANS_VENOM;
	public static final DeferredItem<Item> SPECTER_OIL;
	public static final DeferredItem<Item> OGROID_OIL;
	public static final DeferredItem<Item> BEAST_OIL;
	public static final DeferredItem<Item> DRACONID_OIL;
	public static final DeferredItem<Item> AETHER;
	public static final DeferredItem<Item> HYDRAGENUM;
	public static final DeferredItem<Item> QUEBRITH;
	public static final DeferredItem<Item> REBIS;
	public static final DeferredItem<Item> VERMILION;
	public static final DeferredItem<Item> VITRIOL;
	public static final DeferredItem<Item> WHITE_GULL;
	public static final DeferredItem<Item> SALETPETER;
	public static final DeferredItem<Item> TALLOW;
	public static final DeferredItem<Item> RELICT_OIL;
	public static final DeferredItem<Item> CURSED_OIL;
	public static final DeferredItem<Item> VAMPIRE_OIL;
	public static final DeferredItem<Item> GOLDEN_ORIOLE;
	public static final DeferredItem<Item> FELINE_ARMOR_HELMET;
	public static final DeferredItem<Item> FELINE_ARMOR_CHESTPLATE;
	public static final DeferredItem<Item> FELINE_ARMOR_LEGGINGS;
	public static final DeferredItem<Item> FELINE_ARMOR_BOOTS;
	public static final DeferredItem<Item> WOLVEN_ARMOR_HELMET;
	public static final DeferredItem<Item> WOLVEN_ARMOR_CHESTPLATE;
	public static final DeferredItem<Item> WOLVEN_ARMOR_LEGGINGS;
	public static final DeferredItem<Item> WOLVEN_ARMOR_BOOTS;
	public static final DeferredItem<Item> URSINE_ARMOR_HELMET;
	public static final DeferredItem<Item> URSINE_ARMOR_CHESTPLATE;
	public static final DeferredItem<Item> URSINE_ARMOR_LEGGINGS;
	public static final DeferredItem<Item> URSINE_ARMOR_BOOTS;
	public static final DeferredItem<Item> VIPER_ARMOR_HELMET;
	public static final DeferredItem<Item> VIPER_ARMOR_CHESTPLATE;
	public static final DeferredItem<Item> VIPER_ARMOR_LEGGINGS;
	public static final DeferredItem<Item> VIPER_ARMOR_BOOTS;
	public static final DeferredItem<Item> MANTICORE_ARMOR_HELMET;
	public static final DeferredItem<Item> MANTICORE_ARMOR_CHESTPLATE;
	public static final DeferredItem<Item> MANTICORE_ARMOR_LEGGINGS;
	public static final DeferredItem<Item> MANTICORE_ARMOR_BOOTS;
	public static final DeferredItem<Item> GRIFFIN_ARMOR_HELMET;
	public static final DeferredItem<Item> GRIFFIN_ARMOR_CHESTPLATE;
	public static final DeferredItem<Item> GRIFFIN_ARMOR_LEGGINGS;
	public static final DeferredItem<Item> GRIFFIN_ARMOR_BOOTS;
	public static final DeferredItem<Item> WATER_HAG_DECOCTION;
	public static final DeferredItem<Item> EKIMMARA_DECOCTION;
	public static final DeferredItem<Item> KATAKAN_DECOCTION;
	public static final DeferredItem<Item> LESHEN_DECOCTION;
	public static final DeferredItem<Item> BLACK_BLOOD;
	public static final DeferredItem<Item> FOGLET_DECOCTION;
	public static final DeferredItem<Item> NEKKER_WARRIOR_DECOCTION;
	public static final DeferredItem<Item> TROLL_DECOCTION;
	public static final DeferredItem<Item> WRAITH_DECOCTION;
	public static final DeferredItem<Item> WEREWOLF_DECOCTION;
	public static final DeferredItem<Item> WYVERN_DECOCTION;
	public static final DeferredItem<Item> SUCCUBUS_DECOCTION;
	public static final DeferredItem<Item> GRAVE_HAG_DECOCTION;
	static {
		WHITE_HONEY = register("white_honey", WhiteHoneyItem::new);
		BLIZZARD = register("blizzard", BlizzardItem::new);
		CAT = register("cat", CatItem::new);
		SWALLOW = register("swallow", SwallowItem::new);
		FULL_MOON = register("full_moon", FullMoonItem::new);
		THUNDERBOLT = register("thunderbolt", ThunderboltItem::new);
		WHITE_RAFFARDS_DECOCTION = register("white_raffards_decoction", WhiteRaffardsDecoctionItem::new);
		TAWNY_OWL = register("tawny_owl", TawnyOwlItem::new);
		KILLER_WHALE = register("killer_whale", KillerWhaleItem::new);
		PETRIS_PHILTER = register("petris_philter", PetrisPhilterItem::new);
		DEVILS_PUFFBALL = register("devils_puffball", DevilsPuffballItem::new);
		DANCING_STAR = register("dancing_star", DancingStarItem::new);
		GRAPESHOT = register("grapeshot", GrapeshotItem::new);
		NORTHERN_WIND = register("northern_wind", NorthernWindItem::new);
		SAMUM = register("samum", SamumItem::new);
		DIMERITIUM_BOMB = register("dimeritium_bomb", DimeritiumBombItem::new);
		INSECTOID_OIL = register("insectoid_oil", InsectoidOilItem::new);
		NECROPHAGE_OIL = register("necrophage_oil", NecrophageOilItem::new);
		HANGED_MANS_VENOM = register("hanged_mans_venom", HangedMansVenomItem::new);
		SPECTER_OIL = register("specter_oil", SpecterOilItem::new);
		OGROID_OIL = register("ogroid_oil", OgroidOilItem::new);
		BEAST_OIL = register("beast_oil", BeastOilItem::new);
		DRACONID_OIL = register("draconid_oil", DraconidOilItem::new);
		AETHER = register("aether", AetherItem::new);
		HYDRAGENUM = register("hydragenum", HydragenumItem::new);
		QUEBRITH = register("quebrith", QuebrithItem::new);
		REBIS = register("rebis", RebisItem::new);
		VERMILION = register("vermilion", VermilionItem::new);
		VITRIOL = register("vitriol", VitriolItem::new);
		WHITE_GULL = register("white_gull", WhiteGullItem::new);
		SALETPETER = register("saletpeter", SaletpeterItem::new);
		TALLOW = register("tallow", TallowItem::new);
		RELICT_OIL = register("relict_oil", RelictOilItem::new);
		CURSED_OIL = register("cursed_oil", CursedOilItem::new);
		VAMPIRE_OIL = register("vampire_oil", VampireOilItem::new);
		GOLDEN_ORIOLE = register("golden_oriole", GoldenOrioleItem::new);
		FELINE_ARMOR_HELMET = register("feline_armor_helmet", FelineArmorItem.Helmet::new);
		FELINE_ARMOR_CHESTPLATE = register("feline_armor_chestplate", FelineArmorItem.Chestplate::new);
		FELINE_ARMOR_LEGGINGS = register("feline_armor_leggings", FelineArmorItem.Leggings::new);
		FELINE_ARMOR_BOOTS = register("feline_armor_boots", FelineArmorItem.Boots::new);
		WOLVEN_ARMOR_HELMET = register("wolven_armor_helmet", WolvenArmorItem.Helmet::new);
		WOLVEN_ARMOR_CHESTPLATE = register("wolven_armor_chestplate", WolvenArmorItem.Chestplate::new);
		WOLVEN_ARMOR_LEGGINGS = register("wolven_armor_leggings", WolvenArmorItem.Leggings::new);
		WOLVEN_ARMOR_BOOTS = register("wolven_armor_boots", WolvenArmorItem.Boots::new);
		URSINE_ARMOR_HELMET = register("ursine_armor_helmet", UrsineArmorItem.Helmet::new);
		URSINE_ARMOR_CHESTPLATE = register("ursine_armor_chestplate", UrsineArmorItem.Chestplate::new);
		URSINE_ARMOR_LEGGINGS = register("ursine_armor_leggings", UrsineArmorItem.Leggings::new);
		URSINE_ARMOR_BOOTS = register("ursine_armor_boots", UrsineArmorItem.Boots::new);
		VIPER_ARMOR_HELMET = register("viper_armor_helmet", ViperArmorItem.Helmet::new);
		VIPER_ARMOR_CHESTPLATE = register("viper_armor_chestplate", ViperArmorItem.Chestplate::new);
		VIPER_ARMOR_LEGGINGS = register("viper_armor_leggings", ViperArmorItem.Leggings::new);
		VIPER_ARMOR_BOOTS = register("viper_armor_boots", ViperArmorItem.Boots::new);
		MANTICORE_ARMOR_HELMET = register("manticore_armor_helmet", ManticoreArmorItem.Helmet::new);
		MANTICORE_ARMOR_CHESTPLATE = register("manticore_armor_chestplate", ManticoreArmorItem.Chestplate::new);
		MANTICORE_ARMOR_LEGGINGS = register("manticore_armor_leggings", ManticoreArmorItem.Leggings::new);
		MANTICORE_ARMOR_BOOTS = register("manticore_armor_boots", ManticoreArmorItem.Boots::new);
		GRIFFIN_ARMOR_HELMET = register("griffin_armor_helmet", GriffinArmorItem.Helmet::new);
		GRIFFIN_ARMOR_CHESTPLATE = register("griffin_armor_chestplate", GriffinArmorItem.Chestplate::new);
		GRIFFIN_ARMOR_LEGGINGS = register("griffin_armor_leggings", GriffinArmorItem.Leggings::new);
		GRIFFIN_ARMOR_BOOTS = register("griffin_armor_boots", GriffinArmorItem.Boots::new);
		WATER_HAG_DECOCTION = register("water_hag_decoction", WaterHagDecoctionItem::new);
		EKIMMARA_DECOCTION = register("ekimmara_decoction", EkimmaraDecoctionItem::new);
		KATAKAN_DECOCTION = register("katakan_decoction", KatakanDecoctionItem::new);
		LESHEN_DECOCTION = register("leshen_decoction", LeshenDecoctionItem::new);
		BLACK_BLOOD = register("black_blood", BlackBloodItem::new);
		FOGLET_DECOCTION = register("foglet_decoction", FogletDecoctionItem::new);
		NEKKER_WARRIOR_DECOCTION = register("nekker_warrior_decoction", NekkerWarriorDecoctionItem::new);
		TROLL_DECOCTION = register("troll_decoction", TrollDecoctionItem::new);
		WRAITH_DECOCTION = register("wraith_decoction", WraithDecoctionItem::new);
		WEREWOLF_DECOCTION = register("werewolf_decoction", WerewolfDecoctionItem::new);
		WYVERN_DECOCTION = register("wyvern_decoction", WyvernDecoctionItem::new);
		SUCCUBUS_DECOCTION = register("succubus_decoction", SuccubusDecoctionItem::new);
		GRAVE_HAG_DECOCTION = register("grave_hag_decoction", GraveHagDecoctionItem::new);
	}

	// Start of user code block custom items
	// End of user code block custom items
	private static <I extends Item> DeferredItem<I> register(String name, Function<Item.Properties, ? extends I> supplier) {
		return REGISTRY.registerItem(name, supplier, Item.Properties::new);
	}
}