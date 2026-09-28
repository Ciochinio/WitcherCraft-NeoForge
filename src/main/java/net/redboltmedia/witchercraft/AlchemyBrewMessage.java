package net.redboltmedia.witchercraft;

import java.util.Optional;

import net.redboltmedia.witchercraft.init.WitchercraftModSounds;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * SERVERBOUND: the Brew button in the Alchemy tab. The server checks the
 * player's open {@link AlchemyMenu}:
 *
 * - output slot occupied, no base, or no ingredients: silently ignored;
 * - a wrong recipe: "Nothing happens." below the Brew button
 *   ({@link AlchemyFeedbackMessage}) and the fail sound, nothing consumed;
 * - a match: the brew sound; the result appearing is the feedback.
 *
 * SLICE 1 TEST BUILD: a match only reports what it would brew and consumes
 * nothing; slice 2 turns it into real brewing. That one report is temporary
 * and therefore not localized.
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber
public record AlchemyBrewMessage() implements CustomPacketPayload {
	public static final AlchemyBrewMessage INSTANCE = new AlchemyBrewMessage();
	public static final Type<AlchemyBrewMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "alchemy_brew"));
	public static final StreamCodec<RegistryFriendlyByteBuf, AlchemyBrewMessage> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	private static final float BREW_VOLUME = 1.0F;
	private static final float FAIL_VOLUME = 0.5F;

	@Override
	public Type<AlchemyBrewMessage> type() {
		return TYPE;
	}

	public static void handleData(AlchemyBrewMessage message, IPayloadContext context) {
		if (context.flow() != PacketFlow.SERVERBOUND)
			return;
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof AlchemyMenu menu)
				brew(player, menu);
		});
	}

	private static void brew(ServerPlayer player, AlchemyMenu menu) {
		// silent refusals, like a crafting table: the slots already show why
		AlchemyInput input = menu.recipeInput();
		if (!menu.getSlot(AlchemyMenu.OUTPUT_SLOT).getItem().isEmpty() || input.base().isEmpty() || input.filledIngredients().isEmpty())
			return;

		Optional<RecipeHolder<AlchemyRecipe>> match = AlchemyRecipes.find(player.level().getServer(), input);
		if (match.isEmpty()) {
			playSound(player, WitchercraftModSounds.ALCHEMY_FAIL.get(), FAIL_VOLUME);
			feedback(player, Component.translatable("gui.witchercraft.alchemy.nothing_happens"));
			return;
		}

		// TEMPORARY (slice 1): report only; slice 2 consumes the grid and fills the output
		ItemStack result = match.get().value().result().create();
		playSound(player, WitchercraftModSounds.ALCHEMY_BREW.get(), BREW_VOLUME);
		feedback(player, Component.literal("[Test] Would brew " + result.getCount() + "x ").append(result.getHoverName()));
	}

	/** A world sound at the brewer, so players nearby hear it too. */
	private static void playSound(ServerPlayer player, SoundEvent sound, float volume) {
		player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, volume, 1.0F);
	}

	private static void feedback(ServerPlayer player, Component text) {
		PacketDistributor.sendToPlayer(player, new AlchemyFeedbackMessage(text));
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, AlchemyBrewMessage::handleData);
	}
}
