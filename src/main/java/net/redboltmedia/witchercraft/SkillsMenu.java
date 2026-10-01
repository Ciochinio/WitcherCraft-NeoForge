package net.redboltmedia.witchercraft;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Server-side container behind the Skills tab: the four mutagen sockets and the
 * player inventory. The perk tree and equip grid are not slots; they still go
 * through {@link PerkEquipGuiButtonMessage}. Opened from the shell by
 * {@link SkillsOpenMessage}.
 *
 * On the server the sockets are {@link MutagenSlots#container}, the player's
 * stored mutagens: a change re-applies their effects, and closing the menu
 * returns nothing because the mutagens stay equipped. On the client they are a
 * plain container the menu sync fills.
 *
 * Slot positions are placeholders: {@link SkillsScreen} moves every slot onto
 * the scaled Skills layout (Slot.x / y are made writable by the access
 * transformer). The inventory slots are only active on the client while the
 * Mutagens sub-tab is shown ({@link #inventoryShown}); the server keeps them
 * active so shift-click and the sync work on every sub-tab.
 *
 * HAND-MAINTAINED: locked code element. The MenuType is registered here, not
 * through an MCreator gui element; {@link #MENUS} is attached to the mod bus in
 * WitchercraftMod's "mod init" user code block.
 */
public class SkillsMenu extends AbstractContainerMenu {
	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, WitchercraftMod.MODID);
	public static final DeferredHolder<MenuType<?>, MenuType<SkillsMenu>> TYPE = MENUS.register("skills", () -> new MenuType<>(SkillsMenu::new, FeatureFlags.VANILLA_SET));

	public static final int MUTAGEN_SLOTS = MutagenSlots.SLOTS;
	public static final int INVENTORY_START = MUTAGEN_SLOTS;
	public static final int HOTBAR_START = INVENTORY_START + 27;
	public static final int SLOT_END = HOTBAR_START + 9;

	/** Client only: whether the Mutagens sub-tab (and so the inventory) is shown. */
	public boolean inventoryShown = true;

	/** Client constructor (from the open packet). */
	public SkillsMenu(int containerId, Inventory inventory) {
		this(containerId, inventory, new SimpleContainer(MUTAGEN_SLOTS));
	}

	/** Server constructor. */
	public SkillsMenu(int containerId, Inventory inventory, ServerPlayer player) {
		this(containerId, inventory, MutagenSlots.container(player));
	}

	private SkillsMenu(int containerId, Inventory inventory, Container mutagens) {
		super(TYPE.get(), containerId);
		for (int i = 0; i < MUTAGEN_SLOTS; i++)
			this.addSlot(new MutagenSlot(mutagens, i));
		for (int row = 0; row < 3; row++)
			for (int col = 0; col < 9; col++)
				this.addSlot(new InventorySlot(inventory, col + (row + 1) * 9));
		for (int col = 0; col < 9; col++)
			this.addSlot(new InventorySlot(inventory, col));
	}

	/** One mutagen per socket; only mutagen items. */
	private static class MutagenSlot extends Slot {
		MutagenSlot(Container container, int index) {
			super(container, index, 0, 0);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return MutagenEffects.isMutagen(stack);
		}

		@Override
		public int getMaxStackSize() {
			return 1;
		}
	}

	private class InventorySlot extends Slot {
		InventorySlot(Container container, int index) {
			super(container, index, 0, 0);
		}

		@Override
		public boolean isActive() {
			return inventoryShown;
		}
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
		ItemStack moved = ItemStack.EMPTY;
		Slot slot = this.slots.get(slotIndex);
		if (slot == null || !slot.hasItem())
			return moved;

		ItemStack stack = slot.getItem();
		moved = stack.copy();
		if (slotIndex < MUTAGEN_SLOTS) {
			// socket -> inventory, hotbar first
			if (!this.moveItemStackTo(stack, INVENTORY_START, SLOT_END, true))
				return ItemStack.EMPTY;
		} else if (!MutagenEffects.isMutagen(stack) || !this.moveItemStackTo(stack, 0, MUTAGEN_SLOTS, false)) {
			// not a mutagen or every socket full: swap between inventory and hotbar, like vanilla
			if (slotIndex < HOTBAR_START) {
				if (!this.moveItemStackTo(stack, HOTBAR_START, SLOT_END, false))
					return ItemStack.EMPTY;
			} else if (!this.moveItemStackTo(stack, INVENTORY_START, HOTBAR_START, false)) {
				return ItemStack.EMPTY;
			}
		}

		if (stack.isEmpty())
			slot.setByPlayer(ItemStack.EMPTY);
		else
			slot.setChanged();
		if (stack.getCount() == moved.getCount())
			return ItemStack.EMPTY;
		slot.onTake(player, stack);
		return moved;
	}

	@Override
	public boolean stillValid(Player player) {
		return player.isAlive();
	}
}
