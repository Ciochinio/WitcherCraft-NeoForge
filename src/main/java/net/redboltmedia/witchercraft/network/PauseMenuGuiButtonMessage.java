package net.redboltmedia.witchercraft.network;

import net.redboltmedia.witchercraft.procedures.*;
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
public record PauseMenuGuiButtonMessage(int buttonID, int x, int y, int z) implements CustomPacketPayload {
	public static final Type<PauseMenuGuiButtonMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "pause_menu_gui_buttons"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PauseMenuGuiButtonMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, PauseMenuGuiButtonMessage message) -> {
		buffer.writeInt(message.buttonID);
		buffer.writeInt(message.x);
		buffer.writeInt(message.y);
		buffer.writeInt(message.z);
	}, (RegistryFriendlyByteBuf buffer) -> new PauseMenuGuiButtonMessage(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt()));

	@Override
	public Type<PauseMenuGuiButtonMessage> type() {
		return TYPE;
	}

	public static void handleData(final PauseMenuGuiButtonMessage message, final IPayloadContext context) {
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

			MeditationGuiOpenProcedure.execute(world, x, y, z, entity);
		}
		if (buttonID == 1) {

			CharacterGuiOpenProcedure.execute(world, x, y, z, entity);
		}
		if (buttonID == 2) {

			AlchemyGuiOpenProcedure.execute(world, x, y, z, entity);
		}
		if (buttonID == 3) {

			GlossaryMenuGuiOpenProcedure.execute(world, x, y, z, entity);
		}
		if (buttonID == 4) {

			BestiaryMenuGuiOpenProcedure.execute(world, x, y, z, entity);
		}
		// buttonID 5 ("Skill Tree", used to open the now-retired old tab GUI)
		// retired: the client no longer sends it, PauseMenuGuiScreen now opens the
		// WitcherCraft shell's Skills tab directly (client-only, no server action).
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(PauseMenuGuiButtonMessage.TYPE, PauseMenuGuiButtonMessage.STREAM_CODEC, PauseMenuGuiButtonMessage::handleData);
	}
}