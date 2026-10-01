package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModMenus;
import net.redboltmedia.witchercraft.init.WitchercraftModItems;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;

public class CraftOilVampireProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu0 ? _menu0.getSlots().get(1).getItem() : ItemStack.EMPTY).getItem() == WitchercraftModItems.VERMILION.get()
				&& (entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu2 ? _menu2.getSlots().get(2).getItem() : ItemStack.EMPTY).getItem() == Blocks.AIR.asItem()
				&& (entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu4 ? _menu4.getSlots().get(3).getItem() : ItemStack.EMPTY).getItem() == WitchercraftModItems.VITRIOL.get()
				&& getAmountInGUISlot(entity, 4) >= 2
				&& (entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu7 ? _menu7.getSlots().get(4).getItem() : ItemStack.EMPTY).getItem() == WitchercraftModItems.QUEBRITH.get()
				&& (entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu9 ? _menu9.getSlots().get(5).getItem() : ItemStack.EMPTY).getItem() == Blocks.AIR.asItem()
				&& (entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu11 ? _menu11.getSlots().get(6).getItem() : ItemStack.EMPTY).getItem() == WitchercraftModItems.HYDRAGENUM.get()
				&& (entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu13 ? _menu13.getSlots().get(0).getItem() : ItemStack.EMPTY).getItem() == WitchercraftModItems.TALLOW.get()
				&& (entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu15 ? _menu15.getSlots().get(7).getItem() : ItemStack.EMPTY).getItem() == Blocks.AIR.asItem()) {
			if (entity instanceof Player _player && _player.containerMenu instanceof WitchercraftModMenus.MenuAccessor _menu) {
				_menu.getSlots().get(1).remove(1);
				_menu.getSlots().get(3).remove(1);
				_menu.getSlots().get(4).remove(2);
				_menu.getSlots().get(6).remove(1);
				_menu.getSlots().get(0).remove(1);
				ItemStack _setstack22 = new ItemStack(WitchercraftModItems.VAMPIRE_OIL.get()).copy();
				_setstack22.setCount(3);
				_menu.getSlots().get(7).set(_setstack22);
				_player.containerMenu.broadcastChanges();
			}
		}
	}

	private static int getAmountInGUISlot(Entity entity, int sltid) {
		if (entity instanceof Player player && player.containerMenu instanceof WitchercraftModMenus.MenuAccessor menuAccessor) {
			ItemStack stack = menuAccessor.getSlots().get(sltid).getItem();
			if (stack != null)
				return stack.getCount();
		}
		return 0;
	}
}