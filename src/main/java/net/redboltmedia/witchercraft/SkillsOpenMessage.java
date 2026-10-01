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
 * SERVERBOUND: open the Skills tab. The shell routes every way of reaching the
 * Skills tab (nav click, P/K keys, pause menu) through
 * {@link WitcherGuiPages#open}, which sends this. The server opens
 * {@link SkillsMenu}; the client then shows {@link SkillsScreen} in place of
 * whatever screen was open.
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber
public record SkillsOpenMessage() implements CustomPacketPayload {
	public static final SkillsOpenMessage INSTANCE = new SkillsOpenMessage();
	public static final Type<SkillsOpenMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "skills_open"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SkillsOpenMessage> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	private static final MenuProvider PROVIDER = new MenuProvider() {
		@Override
		public Component getDisplayName() {
			return Component.translatable("gui.witchercraft.shell.nav.skills");
		}

		@Override
		public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
			return new SkillsMenu(containerId, inventory, (ServerPlayer) player);
		}

		// The screen being replaced (shell, pause menu) is swapped straight for the
		// Skills screen. A client-side close first would drop to the world for a
		// frame and re-centre the cursor.
		@Override
		public boolean shouldTriggerClientSideContainerClosingOnOpen() {
			return false;
		}
	};

	@Override
	public Type<SkillsOpenMessage> type() {
		return TYPE;
	}

	public static void handleData(SkillsOpenMessage message, IPayloadContext context) {
		if (context.flow() != PacketFlow.SERVERBOUND)
			return;
		context.enqueueWork(() -> {
			if (!(context.player() instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator())
				return;
			if (player.containerMenu instanceof SkillsMenu)
				return;
			player.openMenu(PROVIDER);
		});
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, SkillsOpenMessage::handleData);
	}
}
