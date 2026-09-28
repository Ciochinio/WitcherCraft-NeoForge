package net.redboltmedia.witchercraft;

import java.util.ArrayList;
import java.util.List;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * CLIENTBOUND: the full contents of alchemy recipes the player knows, stored in
 * {@link AlchemyClientIndex}. Unknown recipes are never sent.
 *
 * {@code replace} swaps the whole known list (login, /reload, config reload,
 * dev command); otherwise the entries are added (a recipe just learned).
 * {@code announce} shows the "New recipe learned" toast for the added entries.
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber
public record AlchemyKnownSyncMessage(boolean replace, boolean announce, List<Entry> recipes) implements CustomPacketPayload {
	public static final Type<AlchemyKnownSyncMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "alchemy_known_sync"));

	/** One known recipe: its id and full contents. */
	public record Entry(Identifier id, AlchemyRecipe recipe) {
		public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
				Identifier.STREAM_CODEC, Entry::id, AlchemyRecipe.STREAM_CODEC, Entry::recipe, Entry::new);
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, AlchemyKnownSyncMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, AlchemyKnownSyncMessage::replace,
			ByteBufCodecs.BOOL, AlchemyKnownSyncMessage::announce,
			Entry.STREAM_CODEC.apply(ByteBufCodecs.list()), AlchemyKnownSyncMessage::recipes,
			AlchemyKnownSyncMessage::new);

	@Override
	public Type<AlchemyKnownSyncMessage> type() {
		return TYPE;
	}

	public static void handleData(AlchemyKnownSyncMessage message, IPayloadContext context) {
		if (context.flow() != PacketFlow.CLIENTBOUND)
			return;
		context.enqueueWork(() -> {
			if (message.replace())
				AlchemyClientIndex.setKnown(message.recipes());
			else
				AlchemyClientIndex.addKnown(message.recipes());
			if (message.announce() && !message.recipes().isEmpty()) {
				List<ItemStack> results = new ArrayList<>();
				for (Entry entry : message.recipes())
					results.add(entry.recipe().result().create());
				AlchemyToast.show(results);
			}
		});
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, AlchemyKnownSyncMessage::handleData);
	}
}
