package net.redboltmedia.witchercraft.network;

import net.redboltmedia.witchercraft.procedures.SignCastKeyReleaseProcedure;
import net.redboltmedia.witchercraft.procedures.SignCastKeyPressProcedure;
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
public record SignCastKeybindMessage(int eventType, int pressedms) implements CustomPacketPayload {
	public static final Type<SignCastKeybindMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "key_sign_cast_keybind"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SignCastKeybindMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, SignCastKeybindMessage message) -> {
		buffer.writeInt(message.eventType);
		buffer.writeInt(message.pressedms);
	}, (RegistryFriendlyByteBuf buffer) -> new SignCastKeybindMessage(buffer.readInt(), buffer.readInt()));

	@Override
	public Type<SignCastKeybindMessage> type() {
		return TYPE;
	}

	public static void handleData(final SignCastKeybindMessage message, final IPayloadContext context) {
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

			SignCastKeyPressProcedure.execute(world, x, y, z, entity);
		}
		if (type == 1) {

			SignCastKeyReleaseProcedure.execute(world, x, y, z, entity);
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(SignCastKeybindMessage.TYPE, SignCastKeybindMessage.STREAM_CODEC, SignCastKeybindMessage::handleData);
	}
}