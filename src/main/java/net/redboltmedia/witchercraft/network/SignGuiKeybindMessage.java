package net.redboltmedia.witchercraft.network;

import net.redboltmedia.witchercraft.procedures.SignGuiKeyOpenProcedure;
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
public record SignGuiKeybindMessage(int eventType, int pressedms) implements CustomPacketPayload {
	public static final Type<SignGuiKeybindMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "key_sign_gui_keybind"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SignGuiKeybindMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, SignGuiKeybindMessage message) -> {
		buffer.writeInt(message.eventType);
		buffer.writeInt(message.pressedms);
	}, (RegistryFriendlyByteBuf buffer) -> new SignGuiKeybindMessage(buffer.readInt(), buffer.readInt()));

	@Override
	public Type<SignGuiKeybindMessage> type() {
		return TYPE;
	}

	public static void handleData(final SignGuiKeybindMessage message, final IPayloadContext context) {
		if (context.flow() == PacketFlow.SERVERBOUND) {
			context.enqueueWork(() -> {
				pressAction(context.player(), message.eventType, message.pressedms);
			}).exceptionally(e -> {
				context.connection().disconnect(Component.literal(e.getMessage()));
				return null;
			});
		}
	}

	public static void pressAction(Player entity, int type, int pressedms) {
		Level world = entity.level();
		double x = entity.getX();
		double y = entity.getY();
		double z = entity.getZ();
		// security measure to prevent arbitrary chunk generation
		if (!world.getChunkSource().hasChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z)))
			return;
		if (type == 0) {

			SignGuiKeyOpenProcedure.execute(world, x, y, z, entity);
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(SignGuiKeybindMessage.TYPE, SignGuiKeybindMessage.STREAM_CODEC, SignGuiKeybindMessage::handleData);
	}
}