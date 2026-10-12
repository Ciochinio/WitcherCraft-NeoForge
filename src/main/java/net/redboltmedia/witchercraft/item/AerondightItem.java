package net.redboltmedia.witchercraft.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class AerondightItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1561, 8f, 0, 10, TagKey.create(Registries.ITEM, Identifier.parse("witchercraft:aerondight_repair_items")));

	public AerondightItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 6f, -2.4f).rarity(Rarity.EPIC));
	}
}
