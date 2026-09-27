package net.redboltmedia.witchercraft;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * SERVERBOUND: open the Alchemy tab. The shell routes every way of reaching the
 * Alchemy tab (nav click, P/J keys, pause menu) through
 * {@link WitcherGuiPages#open}, which sends this. The server opens
 * {@link AlchemyMenu}; the client then shows {@link AlchemyScreen} in place of
 * whatever screen was open.
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber
public record AlchemyOpenMessage() implements CustomPacketPayload {
	public static final AlchemyOpenMessage INSTANCE = new AlchemyOpenMessage();
	public static final Type<AlchemyOpenMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "alchemy_open"));
	public static final StreamCodec<RegistryFriendlyByteBuf, AlchemyOpenMessage> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	private static final MenuProvider PROVIDER = new MenuProvider() {
		@Override
		public Component getDisplayName() {
			return Component.translatable("gui.witchercraft.shell.nav.alchemy");
		}

		@Override
		public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
			return new AlchemyMenu(containerId, inventory);
		}

		// The screen being replaced (shell, pause menu) is swapped straight for the
		// Alchemy screen. A client-side close first would drop to the world for a
		// frame and re-centre the cursor.
		@Override
		public boolean shouldTriggerClientSideContainerClosingOnOpen() {
			return false;
		}
	};

	@Override
	public Type<AlchemyOpenMessage> type() {
		return TYPE;
	}

	public static void handleData(AlchemyOpenMessage message, IPayloadContext context) {
		if (context.flow() != PacketFlow.SERVERBOUND)
			return;
		context.enqueueWork(() -> {
			if (!(context.player() instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator())
				return;
			if (player.containerMenu instanceof AlchemyMenu)
				return;
			player.openMenu(PROVIDER);
		});
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, AlchemyOpenMessage::handleData);
	}
}
