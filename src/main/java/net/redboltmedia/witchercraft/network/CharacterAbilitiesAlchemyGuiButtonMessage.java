package net.redboltmedia.witchercraft.network;

import net.redboltmedia.witchercraft.procedures.*;
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
public record CharacterAbilitiesAlchemyGuiButtonMessage(int buttonID, int x, int y, int z) implements CustomPacketPayload {
	public static final Type<CharacterAbilitiesAlchemyGuiButtonMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(WitchercraftMod.MODID, "character_abilities_alchemy_gui_buttons"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CharacterAbilitiesAlchemyGuiButtonMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, CharacterAbilitiesAlchemyGuiButtonMessage message) -> {
		buffer.writeInt(message.buttonID);
		buffer.writeInt(message.x);
		buffer.writeInt(message.y);
		buffer.writeInt(message.z);
	}, (RegistryFriendlyByteBuf buffer) -> new CharacterAbilitiesAlchemyGuiButtonMessage(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt()));

	@Override
	public Type<CharacterAbilitiesAlchemyGuiButtonMessage> type() {
		return TYPE;
	}

	public static void handleData(final CharacterAbilitiesAlchemyGuiButtonMessage message, final IPayloadContext context) {
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

			CharaterAbilitesGeneralGuiOpenProcedure.execute(world, x, y, z, entity);
		}
		if (buttonID == 2) {

			CharacterAbilitiesCombatGuiOpenProcedure.execute(world, x, y, z, entity);
		}
		if (buttonID == 3) {

			CharacterAbilitiesAlchemyGuiOpenProcedure.execute(world, x, y, z, entity);
		}
		if (buttonID == 4) {

			CharacterAbilitiesSignsGuiOpenProcedure.execute(world, x, y, z, entity);
		}
		if (buttonID == 5) {

			RefreshmentEffectProcedure.execute(entity);
		}
		if (buttonID == 6) {

			DelayedRecoveryEffectProcedure.execute(entity);
		}
		if (buttonID == 7) {

			SideEffectsEffectProcedure.execute(entity);
		}
		if (buttonID == 8) {

			PoisonedBladesEffectProcedure.execute(entity);
		}
		if (buttonID == 9) {

			ProtectiveCoatingEffectProcedure.execute(entity);
		}
		if (buttonID == 10) {

			HunterInstinctEffectProcedure.execute(entity);
		}
		if (buttonID == 11) {

			PyrotechnicsEffectProcedure.execute(entity);
		}
		if (buttonID == 12) {

			EfficiencyEffectProcedure.execute(entity);
		}
		if (buttonID == 13) {

			ClusterBombsEffectProcedure.execute(entity);
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(CharacterAbilitiesAlchemyGuiButtonMessage.TYPE, CharacterAbilitiesAlchemyGuiButtonMessage.STREAM_CODEC, CharacterAbilitiesAlchemyGuiButtonMessage::handleData);
	}
}