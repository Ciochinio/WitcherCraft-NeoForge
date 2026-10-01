package net.redboltmedia.witchercraft;

import java.util.List;
import java.util.function.Supplier;

import com.mojang.serialization.MapCodec;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * The player's four equipped mutagens, one per mutagen socket (socket i sits
 * under perk slots 3i..3i+2). Stored on the player as an attachment, separate
 * from the inventory, and kept through death and respawn like perks.
 *
 * {@link SkillsMenu} edits the slots through {@link #container}; every change
 * re-applies the mutagen effects ({@link MutagenEffects#recompute}).
 *
 * HAND-MAINTAINED: locked code element. {@link #ATTACHMENT_TYPES} is attached to
 * the mod bus in WitchercraftMod's "mod init" user code block.
 */
public final class MutagenSlots {
	private MutagenSlots() {
	}

	public static final int SLOTS = PerkEquipVars.MUTAGEN_GROUPS;

	/** Mutable: the container edits the list in place, and the player is saved with it. */
	public static final class Data {
		final NonNullList<ItemStack> stacks = NonNullList.withSize(SLOTS, ItemStack.EMPTY);

		static final MapCodec<Data> MAP_CODEC = ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("slots", List.of()).xmap(list -> {
			Data data = new Data();
			for (int i = 0; i < list.size() && i < SLOTS; i++)
				data.stacks.set(i, list.get(i));
			return data;
		}, data -> List.copyOf(data.stacks));
	}

	public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, WitchercraftMod.MODID);
	public static final Supplier<AttachmentType<Data>> DATA = ATTACHMENT_TYPES.register("mutagen_slots",
			() -> AttachmentType.builder(Data::new).serialize(Data.MAP_CODEC).copyOnDeath().build());

	/** The mutagen in a socket (0-3); empty if none. Server side: the client does not receive the attachment. */
	public static ItemStack get(Player player, int slot) {
		if (slot < 0 || slot >= SLOTS)
			return ItemStack.EMPTY;
		return player.getData(DATA).stacks.get(slot);
	}

	/** The sockets as a Container for a server-side menu. Any change re-applies the mutagen effects. */
	public static Container container(ServerPlayer player) {
		return new SlotsContainer(player);
	}

	private static final class SlotsContainer implements Container {
		private final ServerPlayer player;

		SlotsContainer(ServerPlayer player) {
			this.player = player;
		}

		private NonNullList<ItemStack> stacks() {
			return player.getData(DATA).stacks;
		}

		@Override
		public int getContainerSize() {
			return SLOTS;
		}

		@Override
		public boolean isEmpty() {
			for (ItemStack stack : stacks())
				if (!stack.isEmpty())
					return false;
			return true;
		}

		@Override
		public ItemStack getItem(int slot) {
			return stacks().get(slot);
		}

		@Override
		public ItemStack removeItem(int slot, int count) {
			ItemStack removed = ContainerHelper.removeItem(stacks(), slot, count);
			if (!removed.isEmpty())
				setChanged();
			return removed;
		}

		@Override
		public ItemStack removeItemNoUpdate(int slot) {
			return ContainerHelper.takeItem(stacks(), slot);
		}

		@Override
		public void setItem(int slot, ItemStack stack) {
			stacks().set(slot, stack);
			setChanged();
		}

		@Override
		public int getMaxStackSize() {
			return 1;
		}

		@Override
		public void setChanged() {
			MutagenEffects.recompute(player);
		}

		@Override
		public boolean stillValid(Player player) {
			return player == this.player && player.isAlive();
		}

		@Override
		public void clearContent() {
			stacks().clear();
			setChanged();
		}
	}
}
