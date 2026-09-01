package net.redboltmedia.witchercraft;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

/**
 * Closes the opaque Witcher GUI shell after real damage from a hostile mob or
 * another player. Environmental damage leaves the shell open.
 *
 * HAND-MAINTAINED: tracked by the locked WitcherGuiScreen code element.
 */
@EventBusSubscriber
public final class WitcherGuiDamageInterrupt {
	private WitcherGuiDamageInterrupt() {
	}

	@SubscribeEvent
	public static void afterDamage(LivingDamageEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || event.getHealthDamage() <= 0f)
			return;

		Entity attacker = event.getSource().getEntity();
		if (attacker instanceof Enemy || attacker instanceof Player)
			PacketDistributor.sendToPlayer(player, CloseMessage.INSTANCE);
	}

	public record CloseMessage() implements CustomPacketPayload {
		private static final CloseMessage INSTANCE = new CloseMessage();
		private static final Type<CloseMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "witcher_gui_damage_interrupt"));
		private static final StreamCodec<RegistryFriendlyByteBuf, CloseMessage> STREAM_CODEC = StreamCodec.unit(INSTANCE);

		@Override
		public Type<CloseMessage> type() {
			return TYPE;
		}

		private static void handle(CloseMessage message, IPayloadContext context) {
			if (context.flow() == PacketFlow.CLIENTBOUND) {
				context.enqueueWork(() -> {
					Minecraft minecraft = Minecraft.getInstance();
					if (minecraft.screen instanceof WitcherGuiScreen screen)
						screen.onClose();
				});
			}
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(CloseMessage.TYPE, CloseMessage.STREAM_CODEC, CloseMessage::handle);
	}
}
