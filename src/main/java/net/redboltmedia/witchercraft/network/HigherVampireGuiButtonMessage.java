package net.redboltmedia.witchercraft.network;

import net.redboltmedia.witchercraft.procedures.GraveirGuiOpenProcedure;
import net.redboltmedia.witchercraft.procedures.DrownerGuiOpenProcedure;
import net.redboltmedia.witchercraft.procedures.BestiaryMenuGuiOpenProcedure;
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
public record HigherVampireGuiButtonMessage(int buttonID, int x, int y, int z) implements CustomPacketPayload {
	public static final Type<HigherVampireGuiButtonMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "higher_vampire_gui_buttons"));
	public static final StreamCodec<RegistryFriendlyByteBuf, HigherVampireGuiButtonMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, HigherVampireGuiButtonMessage message) -> {
		buffer.writeInt(message.buttonID);
		buffer.writeInt(message.x);
		buffer.writeInt(message.y);
		buffer.writeInt(message.z);
	}, (RegistryFriendlyByteBuf buffer) -> new HigherVampireGuiButtonMessage(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt()));

	@Override
	public Type<HigherVampireGuiButtonMessage> type() {
		return TYPE;
	}

	public static void handleData(final HigherVampireGuiButtonMessage message, final IPayloadContext context) {
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
		if (buttonID == 0) {

			DrownerGuiOpenProcedure.execute(world, x, y, z, entity);
		}
		if (buttonID == 1) {

			BestiaryMenuGuiOpenProcedure.execute(world, x, y, z, entity);
		}
		if (buttonID == 5) {

			GraveirGuiOpenProcedure.execute(world, x, y, z, entity);
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(HigherVampireGuiButtonMessage.TYPE, HigherVampireGuiButtonMessage.STREAM_CODEC, HigherVampireGuiButtonMessage::handleData);
	}
}