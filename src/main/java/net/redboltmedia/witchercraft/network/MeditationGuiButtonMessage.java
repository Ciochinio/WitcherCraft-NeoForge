package net.redboltmedia.witchercraft.network;

import net.redboltmedia.witchercraft.procedures.MeditationStartProcedure;
import net.redboltmedia.witchercraft.procedures.MeditationStopProcedure;
import net.redboltmedia.witchercraft.WitchercraftMod;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.SectionPos;

@EventBusSubscriber
public record MeditationGuiButtonMessage(int buttonID, int x, int y, int z) implements CustomPacketPayload {
	public static final Type<MeditationGuiButtonMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "meditation_gui_buttons"));
	public static final StreamCodec<RegistryFriendlyByteBuf, MeditationGuiButtonMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, MeditationGuiButtonMessage message) -> {
		buffer.writeInt(message.buttonID);
		buffer.writeInt(message.x);
		buffer.writeInt(message.y);
		buffer.writeInt(message.z);
	}, (RegistryFriendlyByteBuf buffer) -> new MeditationGuiButtonMessage(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt()));

	@Override
	public Type<MeditationGuiButtonMessage> type() {
		return TYPE;
	}

	public static void handleData(final MeditationGuiButtonMessage message, final IPayloadContext context) {
		if (context.flow() == PacketFlow.SERVERBOUND) {
			context.enqueueWork(() -> handleButtonAction(context.player(), message.buttonID, message.x, message.y, message.z)).exceptionally(e -> {
				context.connection().disconnect(Component.literal(e.getMessage()));
				return null;
			});
		}
	}

	public static void handleButtonAction(Player entity, int buttonID, int x, int y, int z) {
		Level world = entity.level();
		// security measure to prevent arbitrary chunk generation
		if (!world.getChunkSource().hasChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z)))
			return;
		// buttonID 0/1 (old container's back + slider-commit) retired in slice 4.
		// New dial: buttonID = 1000 + targetHour -> start an accelerated meditation
		// to that hour. Server-authoritative; re-validated in the procedure.
		if (buttonID >= 1000 && buttonID < 1024) {
			MeditationStartProcedure.execute(world, x, y, z, entity, buttonID - 1000);
		}
		// Cancel an in-progress meditation (Cancel button / GUI closed mid spin).
		if (buttonID == 2000) {
			MeditationStopProcedure.execute(world);
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(MeditationGuiButtonMessage.TYPE, MeditationGuiButtonMessage.STREAM_CODEC, MeditationGuiButtonMessage::handleData);
	}
}