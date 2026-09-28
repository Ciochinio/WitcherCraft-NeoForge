package net.redboltmedia.witchercraft;

import java.util.List;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

/**
 * CLIENTBOUND: the alchemy ingredient allowlist (item ids only, no recipe
 * contents), stored in {@link AlchemyClientIndex}. Sent by
 * {@link AlchemyRecipes} to a player on login and to everyone on /reload, so
 * the client predicts slot placement exactly like the server. Slice 3 adds the
 * recipe id to result map here.
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber
public record AlchemyIndexSyncMessage(List<Item> ingredientItems) implements CustomPacketPayload {
	public static final Type<AlchemyIndexSyncMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "alchemy_index_sync"));
	public static final StreamCodec<RegistryFriendlyByteBuf, AlchemyIndexSyncMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.registry(Registries.ITEM).apply(ByteBufCodecs.list()), AlchemyIndexSyncMessage::ingredientItems, AlchemyIndexSyncMessage::new);

	@Override
	public Type<AlchemyIndexSyncMessage> type() {
		return TYPE;
	}

	public static void handleData(AlchemyIndexSyncMessage message, IPayloadContext context) {
		if (context.flow() == PacketFlow.CLIENTBOUND)
			context.enqueueWork(() -> AlchemyClientIndex.setIngredientItems(message.ingredientItems()));
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, AlchemyIndexSyncMessage::handleData);
	}
}
