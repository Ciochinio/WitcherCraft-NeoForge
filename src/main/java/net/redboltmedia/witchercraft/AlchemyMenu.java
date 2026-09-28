package net.redboltmedia.witchercraft;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

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
 * Server-side container behind the Alchemy tab: the alchemy circle (one base
 * slot in the centre, five ingredient slots around it), an output slot, and the
 * player inventory. Portable: not bound to a block, opened from the shell by
 * {@link AlchemyOpenMessage}.
 *
 * Grid slots hold ONE item each. Closing the menu (Esc, E, switching tabs,
 * damage interrupt, death, disconnect) returns every grid slot, output included,
 * to the inventory, or drops it if the player is gone.
 *
 * The base slot accepts only {@link AlchemyRecipes#isBase}; ingredient slots only
 * items on the ingredient allowlist, checked against {@link AlchemyRecipes} on
 * the server and the synced {@link AlchemyClientIndex} on the client, so both
 * sides predict placement identically. The output slot is take-only; Brew fills
 * it through {@link #brew}.
 *
 * HAND-MAINTAINED: locked code element. The MenuType is registered here, not
 * through an MCreator gui element; {@link #MENUS} is attached to the mod bus in
 * WitchercraftMod's "mod init" user code block.
 */
public class AlchemyMenu extends AbstractContainerMenu {
	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, WitchercraftMod.MODID);
	public static final DeferredHolder<MenuType<?>, MenuType<AlchemyMenu>> TYPE = MENUS.register("alchemy", () -> new MenuType<>(AlchemyMenu::new, FeatureFlags.VANILLA_SET));

	public static final int BASE_SLOT = 0;
	public static final int FIRST_INGREDIENT_SLOT = 1;
	public static final int INGREDIENT_SLOTS = 5;
	public static final int OUTPUT_SLOT = 6;
	public static final int GRID_SIZE = 7;

	private static final int INVENTORY_START = GRID_SIZE;
	private static final int HOTBAR_START = INVENTORY_START + 27;
	private static final int SLOT_END = HOTBAR_START + 9;

	private final Container grid = new SimpleContainer(GRID_SIZE);

	public AlchemyMenu(int containerId, Inventory inventory) {
		super(TYPE.get(), containerId);

		// the server checks the real recipe index; the client checks its synced copy
		Predicate<ItemStack> isIngredient = inventory.player instanceof ServerPlayer serverPlayer
				? stack -> AlchemyRecipes.isIngredient(serverPlayer.level().getServer(), stack)
				: AlchemyClientIndex::isIngredient;

		this.addSlot(new GridSlot(grid, BASE_SLOT, AlchemyLayout.BASE_X, AlchemyLayout.BASE_Y, AlchemyRecipes::isBase));
		for (int i = 0; i < INGREDIENT_SLOTS; i++)
			this.addSlot(new GridSlot(grid, FIRST_INGREDIENT_SLOT + i, AlchemyLayout.INGREDIENT_X[i], AlchemyLayout.INGREDIENT_Y[i], isIngredient));
		this.addSlot(new Slot(grid, OUTPUT_SLOT, AlchemyLayout.OUTPUT_X, AlchemyLayout.OUTPUT_Y) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}
		});

		for (int row = 0; row < 3; row++)
			for (int col = 0; col < 9; col++)
				this.addSlot(new Slot(inventory, col + (row + 1) * 9, AlchemyLayout.INVENTORY_X + col * 18, AlchemyLayout.INVENTORY_Y + row * 18));
		for (int col = 0; col < 9; col++)
			this.addSlot(new Slot(inventory, col, AlchemyLayout.INVENTORY_X + col * 18, AlchemyLayout.HOTBAR_Y));
	}

	/** A base or ingredient slot: one item per slot, accepting only what {@code accepts} allows. */
	private static class GridSlot extends Slot {
		private final Predicate<ItemStack> accepts;

		GridSlot(Container container, int index, int x, int y, Predicate<ItemStack> accepts) {
			super(container, index, x, y);
			this.accepts = accepts;
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return accepts.test(stack);
		}

		@Override
		public int getMaxStackSize() {
			return 1;
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
		if (slotIndex < GRID_SIZE) {
			// grid -> inventory, hotbar first
			if (!this.moveItemStackTo(stack, INVENTORY_START, SLOT_END, true))
				return ItemStack.EMPTY;
		} else if (!this.moveItemStackTo(stack, BASE_SLOT, OUTPUT_SLOT, false)) {
			// grid full or item refused: swap between inventory and hotbar, like vanilla
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

	/** The grid as a recipe input: the base slot and the five ingredient slots. */
	public AlchemyInput recipeInput() {
		List<ItemStack> ingredients = new ArrayList<>(INGREDIENT_SLOTS);
		for (int i = 0; i < INGREDIENT_SLOTS; i++)
			ingredients.add(grid.getItem(FIRST_INGREDIENT_SLOT + i));
		return new AlchemyInput(grid.getItem(BASE_SLOT), ingredients);
	}

	/**
	 * Consume the whole grid (every slot holds one item, so this is the base and
	 * every ingredient) and put the result in the output slot. Server only; the
	 * caller has already checked that the output slot is empty and a recipe matched.
	 */
	public void brew(ItemStack result) {
		for (int i = BASE_SLOT; i < OUTPUT_SLOT; i++)
			grid.setItem(i, ItemStack.EMPTY);
		grid.setItem(OUTPUT_SLOT, result);
		this.broadcastChanges();
	}

	@Override
	public boolean stillValid(Player player) {
		return player.isAlive();
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		if (!player.level().isClientSide())
			this.clearContainer(player, grid);
	}
}
