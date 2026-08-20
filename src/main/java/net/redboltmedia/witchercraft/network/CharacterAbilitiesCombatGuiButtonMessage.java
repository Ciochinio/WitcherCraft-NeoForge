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
public record CharacterAbilitiesCombatGuiButtonMessage(int buttonID, int x, int y, int z) implements CustomPacketPayload {
	public static final Type<CharacterAbilitiesCombatGuiButtonMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "character_abilities_combat_gui_buttons"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CharacterAbilitiesCombatGuiButtonMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, CharacterAbilitiesCombatGuiButtonMessage message) -> {
		buffer.writeInt(message.buttonID);
		buffer.writeInt(message.x);
		buffer.writeInt(message.y);
		buffer.writeInt(message.z);
	}, (RegistryFriendlyByteBuf buffer) -> new CharacterAbilitiesCombatGuiButtonMessage(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt()));

	@Override
	public Type<CharacterAbilitiesCombatGuiButtonMessage> type() {
		return TYPE;
	}

	public static void handleData(final CharacterAbilitiesCombatGuiButtonMessage message, final IPayloadContext context) {
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

			MuscleMemoryEffectProcedure.execute(entity);
		}
		if (buttonID == 6) {

			PreciseBlowsEffectProcedure.execute(entity);
		}
		if (buttonID == 7) {

			CrippllingStrikesEffectProcedure.execute(entity);
		}
		if (buttonID == 8) {

			StrengthTrainingEffectProcedure.execute(entity);
		}
		if (buttonID == 9) {

			CrushingBlowsEffectProcedure.execute(entity);
		}
		if (buttonID == 10) {

			SunderArmorEffectProcedure.execute(entity);
		}
		if (buttonID == 11) {

			FleetFootedEffectProcedure.execute(entity);
		}
		if (buttonID == 12) {

			DefenceEffectProcedure.execute(entity);
		}
		if (buttonID == 13) {

			DeadlyPresicionEffectProcedure.execute(entity);
		}
		if (buttonID == 14) {

			ColdBloodEffectProcedure.execute(entity);
		}
		if (buttonID == 15) {

			AnatomicalKnowledgeEffectProcedure.execute(entity);
		}
		if (buttonID == 16) {

			CripplingShotEffectProcedure.execute(entity);
		}
		if (buttonID == 17) {

			FlooodOfAngerEffecProcedure.execute(entity);
		}
		if (buttonID == 18) {

			RazorFocusEffectProcedure.execute(entity);
		}
		if (buttonID == 19) {

			UndyingEffectProcedure.execute(entity);
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(CharacterAbilitiesCombatGuiButtonMessage.TYPE, CharacterAbilitiesCombatGuiButtonMessage.STREAM_CODEC, CharacterAbilitiesCombatGuiButtonMessage::handleData);
	}
}