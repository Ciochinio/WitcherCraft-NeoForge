package net.redboltmedia.witchercraft.network;

import net.redboltmedia.witchercraft.WitchercraftMod;
import net.redboltmedia.witchercraft.client.gui.PerkEquipLayout;
import net.redboltmedia.witchercraft.procedures.MutagenMedallionClickProcedure;

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

/**
 * Perk equip screen button/action packet.
 *
 * HAND-MAINTAINED, locked_code=true. The buttonID encodes the action so the
 * fixed MCreator packet shape can carry all of them (the screen is client-only
 * for selection; only these state changes are server-authoritative):
 *   0                              reserved / no-op
 *   1000000 + slotIdx*1000 + perkId   place perkId into perk slot slotIdx (0-11)
 *   2000000 + slotIdx                 clear perk slot slotIdx
 *   3000000 + group                   cycle mutagen colour of group (0-3)
 *   4000000                           medallion click (placeholder button)
 * The server re-validates every action; it never trusts the client's view.
 */
@EventBusSubscriber
public record PerkEquipGuiButtonMessage(int buttonID, int x, int y, int z) implements CustomPacketPayload {
	public static final Type<PerkEquipGuiButtonMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "perk_equip_gui_buttons"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PerkEquipGuiButtonMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, PerkEquipGuiButtonMessage message) -> {
		buffer.writeInt(message.buttonID);
		buffer.writeInt(message.x);
		buffer.writeInt(message.y);
		buffer.writeInt(message.z);
	}, (RegistryFriendlyByteBuf buffer) -> new PerkEquipGuiButtonMessage(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt()));

	@Override
	public Type<PerkEquipGuiButtonMessage> type() {
		return TYPE;
	}

	public static void handleData(final PerkEquipGuiButtonMessage message, final IPayloadContext context) {
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
		if (buttonID >= 4000000) { // medallion click (placeholder button)
			if (PerkEquipLayout.MEDALLION_ENABLED)
				MutagenMedallionClickProcedure.execute(entity);
		} else if (buttonID >= 3000000) { // cycle mutagen colour: empty -> red -> green -> blue -> empty
			int group = buttonID - 3000000;
			if (group < 0 || group >= PerkEquipVars.MUTAGEN_GROUPS)
				return;
			int next = (PerkEquipVars.getMutagenSocket(entity, group) + 1) % 4;
			PerkEquipVars.setMutagenSocket(entity, group, next);
		} else if (buttonID >= 2000000) { // clear a perk slot
			int slot = buttonID - 2000000;
			if (slot < 0 || slot >= PerkEquipVars.PERK_SLOTS)
				return;
			PerkEquipVars.setPerkSocket(entity, slot, 0);
		} else if (buttonID >= 1000000) { // place / move a perk into a slot
			int a = buttonID - 1000000;
			int slot = a / 1000;
			int perkId = a % 1000;
			if (slot < 0 || slot >= PerkEquipVars.PERK_SLOTS)
				return;
			if (perkId <= 0)
				return;
			if (PerkEquipVars.getPerkSocket(entity, slot) != 0)
				return; // target must be empty
			// move semantics: if this perk already sits in another slot, vacate
			// it first so re-slotting an equipped perk moves it (never duplicates).
			for (int i = 0; i < PerkEquipVars.PERK_SLOTS; i++) {
				if (i != slot && PerkEquipVars.getPerkSocket(entity, i) == perkId)
					PerkEquipVars.setPerkSocket(entity, i, 0);
			}
			PerkEquipVars.setPerkSocket(entity, slot, perkId);
		}
		// buttonID 0: reserved no-op. Recompute runs from PerkEquipGuiMenu.removed().
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(PerkEquipGuiButtonMessage.TYPE, PerkEquipGuiButtonMessage.STREAM_CODEC, PerkEquipGuiButtonMessage::handleData);
	}
}
