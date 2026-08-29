package net.redboltmedia.witchercraft;


import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * CLIENTBOUND: the server tells the initiating player's client that an
 * accelerated meditation spin has begun. The client then self-times the
 * translucent overlay for {@code durationTicks} (so the world's real sky shows
 * through while the server spins the clock) - no per-tick stop packet needed.
 *
 * HAND-MAINTAINED (no MCreator element), registered like the shell's packets.
 */
@EventBusSubscriber
public record MeditationSpinMessage(int targetHour, int durationTicks) implements CustomPacketPayload {
	public static final Type<MeditationSpinMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "meditation_spin"));
	public static final StreamCodec<RegistryFriendlyByteBuf, MeditationSpinMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, MeditationSpinMessage message) -> {
		buffer.writeInt(message.targetHour);
		buffer.writeInt(message.durationTicks);
	}, (RegistryFriendlyByteBuf buffer) -> new MeditationSpinMessage(buffer.readInt(), buffer.readInt()));

	@Override
	public Type<MeditationSpinMessage> type() {
		return TYPE;
	}

	public static void handleData(final MeditationSpinMessage message, final IPayloadContext context) {
		if (context.flow() == PacketFlow.CLIENTBOUND) {
			// Reference to the client-only page is only reached on the client, where
			// this branch runs; the dedicated server never executes it.
			context.enqueueWork(() -> MeditationPage.beginClientSpin(message.targetHour, message.durationTicks));
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(MeditationSpinMessage.TYPE, MeditationSpinMessage.STREAM_CODEC, MeditationSpinMessage::handleData);
	}
}
