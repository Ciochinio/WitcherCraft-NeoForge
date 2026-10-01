package net.redboltmedia.witchercraft;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Operator-only dev command for alchemy knowledge, so the recipe book, starters
 * and knowledge modes can be tested without manuscripts:
 *
 * <pre>
 * /witchercraft alchemy learn  (all | &lt;recipe&gt;) [&lt;players&gt;]
 * /witchercraft alchemy forget (all | &lt;recipe&gt;) [&lt;players&gt;]
 * /witchercraft alchemy reset [&lt;players&gt;]   back to a new player: clear, then grant starters
 * /witchercraft alchemy list [&lt;player&gt;]
 * /witchercraft alchemy manuscript &lt;recipe&gt; [&lt;players&gt;]   give a manuscript teaching the recipe
 * </pre>
 *
 * Without players, the command applies to whoever runs it. {@code forget all}
 * keeps the starters-granted flag, so starters do not come back on the next
 * login; {@code reset} grants them again. {@code manuscript} works in every
 * knowledge mode, so manuscripts can be tested even where loot and trades offer none.
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber
public final class AlchemyCommands {
	private AlchemyCommands() {
	}

	private static final DynamicCommandExceptionType UNKNOWN_RECIPE = new DynamicCommandExceptionType(
			id -> Component.translatableEscape("commands.witchercraft.alchemy.unknown_recipe", id));

	private static final SuggestionProvider<CommandSourceStack> RECIPES = (context, builder) -> SharedSuggestionProvider
			.suggestResource(AlchemyRecipes.ids(context.getSource().getServer()), builder);

	private interface PlayersAction {
		int run(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) throws CommandSyntaxException;
	}

	@SubscribeEvent
	public static void register(RegisterCommandsEvent event) {
		event.getDispatcher().register(Commands.literal("witchercraft").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.literal("alchemy")
						.then(Commands.literal("learn")
								.then(withPlayers(Commands.literal("all"), AlchemyCommands::learnAll))
								.then(withPlayers(Commands.argument("recipe", IdentifierArgument.id()).suggests(RECIPES), AlchemyCommands::learnOne)))
						.then(Commands.literal("forget")
								.then(withPlayers(Commands.literal("all"), AlchemyCommands::forgetAll))
								.then(withPlayers(Commands.argument("recipe", IdentifierArgument.id()).suggests(RECIPES), AlchemyCommands::forgetOne)))
						.then(withPlayers(Commands.literal("reset"), AlchemyCommands::reset))
						.then(Commands.literal("manuscript")
								.then(withPlayers(Commands.argument("recipe", IdentifierArgument.id()).suggests(RECIPES), AlchemyCommands::giveManuscript)))
						.then(Commands.literal("list")
								.executes(context -> list(context, context.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(context -> list(context, EntityArgument.getPlayer(context, "player")))))));
	}

	/** {@code node} runs {@code action} on the command's source, or on {@code [<players>]} if given. */
	private static <T extends ArgumentBuilder<CommandSourceStack, T>> T withPlayers(T node, PlayersAction action) {
		return node.executes(context -> action.run(context, List.of(context.getSource().getPlayerOrException())))
				.then(Commands.argument("players", EntityArgument.players())
						.executes(context -> action.run(context, EntityArgument.getPlayers(context, "players"))));
	}

	private static Identifier recipeArgument(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		Identifier id = IdentifierArgument.getId(context, "recipe");
		if (!AlchemyRecipes.ids(context.getSource().getServer()).contains(id))
			throw UNKNOWN_RECIPE.create(id.toString());
		return id;
	}

	private static int learnOne(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) throws CommandSyntaxException {
		Identifier id = recipeArgument(context);
		for (ServerPlayer player : players)
			AlchemyKnowledge.learn(player, id, true);
		context.getSource().sendSuccess(() -> Component.translatable("commands.witchercraft.alchemy.learn.one", id.toString(), players.size()), true);
		return players.size();
	}

	private static int learnAll(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
		for (ServerPlayer player : players)
			AlchemyKnowledge.learnAll(player);
		context.getSource().sendSuccess(() -> Component.translatable("commands.witchercraft.alchemy.learn.all", players.size()), true);
		return players.size();
	}

	private static int forgetOne(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) throws CommandSyntaxException {
		Identifier id = recipeArgument(context);
		for (ServerPlayer player : players)
			AlchemyKnowledge.forget(player, id);
		context.getSource().sendSuccess(() -> Component.translatable("commands.witchercraft.alchemy.forget.one", id.toString(), players.size()), true);
		return players.size();
	}

	private static int forgetAll(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
		for (ServerPlayer player : players)
			AlchemyKnowledge.forgetAll(player);
		context.getSource().sendSuccess(() -> Component.translatable("commands.witchercraft.alchemy.forget.all", players.size()), true);
		return players.size();
	}

	private static int reset(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
		for (ServerPlayer player : players)
			AlchemyKnowledge.reset(player);
		context.getSource().sendSuccess(() -> Component.translatable("commands.witchercraft.alchemy.reset", players.size()), true);
		return players.size();
	}

	private static int giveManuscript(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) throws CommandSyntaxException {
		Identifier id = recipeArgument(context);
		for (ServerPlayer player : players) {
			ItemStack manuscript = AlchemyManuscripts.create(id);
			if (!player.getInventory().add(manuscript))
				player.drop(manuscript, false);
		}
		context.getSource().sendSuccess(() -> Component.translatable("commands.witchercraft.alchemy.manuscript", id.toString(), players.size()), true);
		return players.size();
	}

	private static int list(CommandContext<CommandSourceStack> context, ServerPlayer player) {
		Set<Identifier> known = AlchemyKnowledge.effectiveKnown(player);
		int total = AlchemyRecipes.ids(context.getSource().getServer()).size();
		String ids = known.stream().map(Identifier::toString).collect(Collectors.joining(", "));
		context.getSource().sendSuccess(() -> Component.translatable("commands.witchercraft.alchemy.list", player.getDisplayName(), known.size(), total,
				AlchemyKnowledge.mode().name(), ids), false);
		return known.size();
	}
}
