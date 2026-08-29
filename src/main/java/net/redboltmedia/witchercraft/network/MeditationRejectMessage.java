package net.redboltmedia.witchercraft.network;

import net.redboltmedia.witchercraft.WitchercraftMod;
import net.redboltmedia.witchercraft.client.gui.shell.MeditationPage;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * CLIENTBOUND: the server refused to start a meditation (the safety gate failed:
 * monsters nearby, or not enough open space). The client shows the reason on the
 * action bar and closes the meditation GUI - so a failed Meditate click gives
 * feedback instead of silently doing nothing.
 *
 * {@code reason} is one of {@link net.redboltmedia.witchercraft.procedures.MeditationCanStartProcedure}
 * BLOCKED_* codes. HAND-MAINTAINED (no MCreator element), registered like the
 * shell's other packets.
 */
@EventBusSubscriber
public record MeditationRejectMessage(int reason) implements CustomPacketPayload {
	public static final Type<MeditationRejectMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "meditation_reject"));
	public static final StreamCodec<RegistryFriendlyByteBuf, MeditationRejectMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, MeditationRejectMessage message) -> {
		buffer.writeInt(message.reason);
	}, (RegistryFriendlyByteBuf buffer) -> new MeditationRejectMessage(buffer.readInt()));

	@Override
	public Type<MeditationRejectMessage> type() {
		return TYPE;
	}

	public static void handleData(final MeditationRejectMessage message, final IPayloadContext context) {
		if (context.flow() == PacketFlow.CLIENTBOUND) {
			// Reference to the client-only page is only reached on the client, where
			// this branch runs; the dedicated server never executes it.
			context.enqueueWork(() -> MeditationPage.rejectAndClose(message.reason));
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(MeditationRejectMessage.TYPE, MeditationRejectMessage.STREAM_CODEC, MeditationRejectMessage::handleData);
	}
}
