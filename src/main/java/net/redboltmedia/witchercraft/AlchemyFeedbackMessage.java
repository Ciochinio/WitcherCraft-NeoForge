package net.redboltmedia.witchercraft;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * CLIENTBOUND: a short message for the Alchemy tab ("Nothing happens.",
 * the only one today), shown below the Brew button for a moment and then faded
 * out by {@link AlchemyScreen}. Ignored if the tab is no longer open.
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber
public record AlchemyFeedbackMessage(Component text) implements CustomPacketPayload {
	public static final Type<AlchemyFeedbackMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "alchemy_feedback"));
	public static final StreamCodec<RegistryFriendlyByteBuf, AlchemyFeedbackMessage> STREAM_CODEC = StreamCodec.composite(
			ComponentSerialization.TRUSTED_STREAM_CODEC, AlchemyFeedbackMessage::text, AlchemyFeedbackMessage::new);

	@Override
	public Type<AlchemyFeedbackMessage> type() {
		return TYPE;
	}

	public static void handleData(AlchemyFeedbackMessage message, IPayloadContext context) {
		if (context.flow() != PacketFlow.CLIENTBOUND)
			return;
		context.enqueueWork(() -> {
			if (Minecraft.getInstance().screen instanceof AlchemyScreen screen)
				screen.showFeedback(message.text());
		});
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, AlchemyFeedbackMessage::handleData);
	}
}
