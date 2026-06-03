package net.redboltmedia.witchercraft.network;

import net.redboltmedia.witchercraft.procedures.GraveirGuiOpenProcedure;
import net.redboltmedia.witchercraft.procedures.FogletGuiOpenProcedure;
import net.redboltmedia.witchercraft.procedures.BestiaryMenuGuiOpenProcedure;
import net.redboltmedia.witchercraft.WitchercraftMod;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.SectionPos;

@EventBusSubscriber
public record BruxaGuiButtonMessage(int buttonID, int x, int y, int z) implements CustomPacketPayload {
	public static final Type<BruxaGuiButtonMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(WitchercraftMod.MODID, "bruxa_gui_buttons"));
	public static final StreamCodec<RegistryFriendlyByteBuf, BruxaGuiButtonMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, BruxaGuiButtonMessage message) -> {
		buffer.writeInt(message.buttonID);
		buffer.writeInt(message.x);
		buffer.writeInt(message.y);
		buffer.writeInt(message.z);
	}, (RegistryFriendlyByteBuf buffer) -> new BruxaGuiButtonMessage(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt()));

	@Override
	public Type<BruxaGuiButtonMessage> type() {
		return TYPE;
	}

	public static void handleData(final BruxaGuiButtonMessage message, final IPayloadContext context) {
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

			GraveirGuiOpenProcedure.execute(world, x, y, z, entity);
		}
		if (buttonID == 1) {

			BestiaryMenuGuiOpenProcedure.execute(world, x, y, z, entity);
		}
		if (buttonID == 5) {

			FogletGuiOpenProcedure.execute(world, x, y, z, entity);
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(BruxaGuiButtonMessage.TYPE, BruxaGuiButtonMessage.STREAM_CODEC, BruxaGuiButtonMessage::handleData);
	}
}