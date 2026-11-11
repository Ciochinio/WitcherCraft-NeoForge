package net.redboltmedia.witchercraft;

import net.redboltmedia.witchercraft.init.WitchercraftModItems;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.client.Minecraft;

/**
 * CLIENTBOUND: Undying just saved this player. Plays vanilla's totem pop-up with the Undying Sigil item (the
 * animation takes any item), plus the totem particle emitter, exactly like a Totem of Undying does for itself.
 * Sent by the locked procedure WitchercraftUndyingPopup.
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber
public record WitchercraftUndyingMessage() implements CustomPacketPayload {
	public static final Type<WitchercraftUndyingMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "undying_popup"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WitchercraftUndyingMessage> STREAM_CODEC = StreamCodec.unit(new WitchercraftUndyingMessage());

	@Override
	public Type<WitchercraftUndyingMessage> type() {
		return TYPE;
	}

	public static void handleData(WitchercraftUndyingMessage message, IPayloadContext context) {
		if (context.flow() != PacketFlow.CLIENTBOUND)
			return;
		context.enqueueWork(() -> {
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft.player == null)
				return;
			minecraft.particleEngine.createTrackingEmitter(minecraft.player, ParticleTypes.TOTEM_OF_UNDYING, 30);
			minecraft.gameRenderer.displayItemActivation(new ItemStack(WitchercraftModItems.UNDYING_SIGIL.get()));
		});
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, WitchercraftUndyingMessage::handleData);
	}
}
