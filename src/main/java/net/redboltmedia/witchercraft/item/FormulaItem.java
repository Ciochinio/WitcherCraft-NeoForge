package net.redboltmedia.witchercraft.item;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import net.redboltmedia.witchercraft.AlchemyFormulas;

/**
 * The generic alchemy formula. Its stack names the recipe it teaches; all
 * behaviour lives in {@link AlchemyFormulas}.
 *
 * HAND-MAINTAINED: the Formula item element has locked code, so MCreator does
 * not regenerate this class or its model files.
 */
public class FormulaItem extends Item {
	public FormulaItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public Component getName(ItemStack itemStack) {
		Component name = AlchemyFormulas.name(itemStack);
		return name != null ? name : super.getName(itemStack);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		super.appendHoverText(itemStack, context, display, builder, flag);
		AlchemyFormulas.tooltip(itemStack, builder);
	}

	@Override
	public InteractionResult use(Level world, Player entity, InteractionHand hand) {
		return AlchemyFormulas.read(world, entity, hand);
	}
}
