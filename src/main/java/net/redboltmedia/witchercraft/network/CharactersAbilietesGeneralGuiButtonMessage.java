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
public record CharactersAbilietesGeneralGuiButtonMessage(int buttonID, int x, int y, int z) implements CustomPacketPayload {
	public static final Type<CharactersAbilietesGeneralGuiButtonMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "characters_abilietes_general_gui_buttons"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CharactersAbilietesGeneralGuiButtonMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, CharactersAbilietesGeneralGuiButtonMessage message) -> {
		buffer.writeInt(message.buttonID);
		buffer.writeInt(message.x);
		buffer.writeInt(message.y);
		buffer.writeInt(message.z);
	}, (RegistryFriendlyByteBuf buffer) -> new CharactersAbilietesGeneralGuiButtonMessage(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt()));

	@Override
	public Type<CharactersAbilietesGeneralGuiButtonMessage> type() {
		return TYPE;
	}

	public static void handleData(final CharactersAbilietesGeneralGuiButtonMessage message, final IPayloadContext context) {
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

			PauseMenuGuiOpenProcedure.execute(world, x, y, z, entity);
		}
		if (buttonID == 1) {

			CharacterAbilitiesGeneralGuiOpenProcedure.execute(world, x, y, z, entity);
		}
		if (buttonID == 2) {

			CharacterAbilitiesAlchemyGuiOpenProcedure.execute(world, x, y, z, entity);
		}
		if (buttonID == 3) {

			CharacterAbilitiesCombatGuiOpenProcedure.execute(world, x, y, z, entity);
		}
		if (buttonID == 4) {

			CharacterAbilitiesSignsGuiOpenProcedure.execute(world, x, y, z, entity);
		}
		if (buttonID == 5) {

			SunAndStarsEffectProcedure.execute(entity);
		}
		if (buttonID == 6) {

			SurvivalInstinctEffectProcedure.execute(entity);
		}
		if (buttonID == 7) {

			GourmetEffectProcedure.execute(entity);
		}
		if (buttonID == 8) {

			CatSchoolEffectProcedure.execute(entity);
		}
		if (buttonID == 9) {

			GriffinSchoolEffectProcedure.execute(entity);
		}
		if (buttonID == 10) {

			BearSchoolEffectProcedure.execute(entity);
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(CharactersAbilietesGeneralGuiButtonMessage.TYPE, CharactersAbilietesGeneralGuiButtonMessage.STREAM_CODEC, CharactersAbilietesGeneralGuiButtonMessage::handleData);
	}
}