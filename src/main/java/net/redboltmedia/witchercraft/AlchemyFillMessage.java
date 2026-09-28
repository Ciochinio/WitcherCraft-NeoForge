package net.redboltmedia.witchercraft;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * SERVERBOUND: a click on a recipe in the Alchemy tab's recipe book. The server
 * checks the player's open menu is an {@link AlchemyMenu} and that the player
 * knows the recipe (effective known set, so every recipe in ALL_KNOWN), then
 * {@link AlchemyMenu#fillFrom} returns the base and ingredient slots to the
 * inventory and moves the recipe's items in. The output slot is left alone.
 * Anything else is silently ignored.
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber
public record AlchemyFillMessage(Identifier recipeId) implements CustomPacketPayload {
	public static final Type<AlchemyFillMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "alchemy_fill"));
	public static final StreamCodec<RegistryFriendlyByteBuf, AlchemyFillMessage> STREAM_CODEC = StreamCodec.composite(
			Identifier.STREAM_CODEC, AlchemyFillMessage::recipeId, AlchemyFillMessage::new);

	@Override
	public Type<AlchemyFillMessage> type() {
		return TYPE;
	}

	public static void handleData(AlchemyFillMessage message, IPayloadContext context) {
		if (context.flow() != PacketFlow.SERVERBOUND)
			return;
		context.enqueueWork(() -> {
			if (!(context.player() instanceof ServerPlayer player) || !(player.containerMenu instanceof AlchemyMenu menu))
				return;
			if (!AlchemyKnowledge.isKnown(player, message.recipeId()))
				return;
			AlchemyRecipes.byId(player.level().getServer(), message.recipeId()).ifPresent(holder -> menu.fillFrom(player, holder.value()));
		});
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, AlchemyFillMessage::handleData);
	}
}
