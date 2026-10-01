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
 * CLIENTBOUND: the alchemy recipe index, stored in {@link AlchemyClientIndex}:
 *
 * - the ingredient allowlist (item ids), so the client predicts slot placement
 *   exactly like the server;
 * - every recipe's id, category and result item, but never its base or
 *   ingredients. Enough for locked book entries and manuscript names, and not
 *   secret: a manuscript's name reveals its result anyway;
 * - the knowledge mode, so the book knows whether to show locked entries.
 *
 * Sent by {@link AlchemyRecipes#sendIndex} on login, after /reload, and after a
 * server config reload. Recipe contents travel separately, and only for known
 * recipes ({@link AlchemyKnownSyncMessage}).
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber
public record AlchemyIndexSyncMessage(List<Item> ingredientItems, List<Entry> recipes, AlchemyKnowledge.Mode mode) implements CustomPacketPayload {
	public static final Type<AlchemyIndexSyncMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "alchemy_index_sync"));

	/** One recipe as every player may see it: id, book category, and result item. */
	public record Entry(Identifier id, AlchemyRecipe.Category category, Item result) {
		public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
				Identifier.STREAM_CODEC, Entry::id,
				AlchemyRecipe.Category.STREAM_CODEC, Entry::category,
				ByteBufCodecs.registry(Registries.ITEM), Entry::result,
				Entry::new);
	}

	private static final StreamCodec<RegistryFriendlyByteBuf, AlchemyKnowledge.Mode> MODE_STREAM_CODEC = ByteBufCodecs
			.idMapper(i -> AlchemyKnowledge.Mode.values()[i], AlchemyKnowledge.Mode::ordinal).cast();

	public static final StreamCodec<RegistryFriendlyByteBuf, AlchemyIndexSyncMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.registry(Registries.ITEM).apply(ByteBufCodecs.list()), AlchemyIndexSyncMessage::ingredientItems,
			Entry.STREAM_CODEC.apply(ByteBufCodecs.list()), AlchemyIndexSyncMessage::recipes,
			MODE_STREAM_CODEC, AlchemyIndexSyncMessage::mode,
			AlchemyIndexSyncMessage::new);

	@Override
	public Type<AlchemyIndexSyncMessage> type() {
		return TYPE;
	}

	public static void handleData(AlchemyIndexSyncMessage message, IPayloadContext context) {
		if (context.flow() == PacketFlow.CLIENTBOUND)
			context.enqueueWork(() -> AlchemyClientIndex.setIndex(message.ingredientItems(), message.recipes(), message.mode()));
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, AlchemyIndexSyncMessage::handleData);
	}
}
