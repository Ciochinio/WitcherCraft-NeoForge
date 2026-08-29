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
public record CharacterAbilitiesSignsGuiButtonMessage(int buttonID, int x, int y, int z) implements CustomPacketPayload {
	public static final Type<CharacterAbilitiesSignsGuiButtonMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "character_abilities_signs_gui_buttons"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CharacterAbilitiesSignsGuiButtonMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, CharacterAbilitiesSignsGuiButtonMessage message) -> {
		buffer.writeInt(message.buttonID);
		buffer.writeInt(message.x);
		buffer.writeInt(message.y);
		buffer.writeInt(message.z);
	}, (RegistryFriendlyByteBuf buffer) -> new CharacterAbilitiesSignsGuiButtonMessage(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt()));

	@Override
	public Type<CharacterAbilitiesSignsGuiButtonMessage> type() {
		return TYPE;
	}

	public static void handleData(final CharacterAbilitiesSignsGuiButtonMessage message, final IPayloadContext context) {
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

			CharacterAbilitiesCombatGuiOpenProcedure.execute(world, x, y, z, entity);
		}
		if (buttonID == 3) {

			CharacterAbilitiesAlchemyGuiOpenProcedure.execute(world, x, y, z, entity);
		}
		if (buttonID == 4) {

			CharacterAbilitiesSignsGuiOpenProcedure.execute(world, x, y, z, entity);
		}
		if (buttonID == 5) {

			FarReachingAardEffectProcedure.execute(entity);
		}
		if (buttonID == 6) {

			AardIntensityEffectProcedure.execute(entity);
		}
		if (buttonID == 7) {

			ShockWaveEffectProcedure.execute(entity);
		}
		if (buttonID == 8) {

			FirestreamEffectProcedure.execute(entity);
		}
		if (buttonID == 9) {

			IgniIntensityEffectProcedure.execute(entity);
		}
		if (buttonID == 10) {

			PyromaniacEffectProcedure.execute(entity);
		}
		if (buttonID == 11) {

			SustainedGlyphsEffectProcedure.execute(entity);
		}
		if (buttonID == 12) {

			YrdenIntensityEffectProcedure.execute(entity);
		}
		if (buttonID == 13) {

			MagicTrapEffectProcedure.execute(entity);
		}
		if (buttonID == 14) {

			ExplodingShieldEffectProcedure.execute(entity);
		}
		if (buttonID == 15) {

			QuenIntensityEffectProcedure.execute(entity);
		}
		if (buttonID == 16) {

			QuenDischargeEffectProcedure.execute(entity);
		}
		if (buttonID == 17) {

			DelusionEffectProcedure.execute(entity);
		}
		if (buttonID == 18) {

			AxiiIntensityEffectProcedure.execute(entity);
		}
		if (buttonID == 19) {

			DominationEffectProcedure.execute(entity);
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(CharacterAbilitiesSignsGuiButtonMessage.TYPE, CharacterAbilitiesSignsGuiButtonMessage.STREAM_CODEC, CharacterAbilitiesSignsGuiButtonMessage::handleData);
	}
}