package net.redboltmedia.witchercraft;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * HAND-MAINTAINED (locked code element, ~/World Map/Fast Travel). Client state of the map's travel mode.
 *
 * Travel mode exists only after the server answered a signpost right-click with {@link FastTravelOpenMessage};
 * opening the map any other way never enters it. Leaving the map tab or closing the menu ends it and tells the
 * server. The server stays authoritative: this class only remembers what to show and forwards requests.
 * Client thread only.
 */
public final class FastTravelClient {
	public static final Identifier CATEGORY = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "fast_travel");

	private static boolean active;
	private static boolean pending;
	private static int originX;
	private static int originZ;
	private static @Nullable UUID originPresentation;
	private static @Nullable Outcome outcome;
	/** The bare place name of the destination being travelled to, for the arrival message. */
	private static String destinationName = "";

	private FastTravelClient() {
	}

	/** Enters travel mode and opens the Witcher menu on its map tab. */
	public static void open(int x, int z, @Nullable UUID presentation) {
		active = true;
		pending = false;
		outcome = null;
		originX = x;
		originZ = z;
		originPresentation = presentation;
		Minecraft.getInstance().setScreen(new WitcherGuiScreen("map"));
	}

	public static boolean active() {
		return active;
	}

	/** True while a confirmed journey waits for the server. */
	public static boolean pending() {
		return pending;
	}

	public static int originX() {
		return originX;
	}

	public static int originZ() {
		return originZ;
	}

	/** Leaves travel mode because the map was left or closed, and tells the server. */
	public static void leave() {
		if (!active)
			return;
		active = false;
		pending = false;
		ClientPacketDistributor.sendToServer(FastTravelLeaveMessage.INSTANCE);
	}

	/** A discovered signpost marker, which is what travel mode can target. */
	public static boolean isSignpost(WorldMapPoiMarker marker) {
		return marker instanceof WorldMapPoiMarker.Discovered discovered && CATEGORY.equals(discovered.category());
	}

	/** The marker of the signpost travel started from. */
	public static boolean isOrigin(WorldMapPoiMarker marker) {
		if (originPresentation != null)
			return originPresentation.equals(marker.markerId());
		return (int) Math.floor(marker.x()) == originX && (int) Math.floor(marker.z()) == originZ;
	}

	/** A signpost other than the origin, while travel mode is active. */
	public static boolean isDestination(WorldMapPoiMarker marker) {
		return active && isSignpost(marker) && !isOrigin(marker);
	}

	/** The price of travelling to a marker, computed exactly as the server does. */
	public static int price(WorldMapPoiMarker marker) {
		var player = Minecraft.getInstance().player;
		return FastTravelCosts.price(new net.minecraft.core.BlockPos(originX, 0, originZ),
			new net.minecraft.core.BlockPos((int) Math.floor(marker.x()), 0, (int) Math.floor(marker.z())), player != null && player.isCreative());
	}

	/** Horizontal distance in whole blocks from the origin sign to a marker. */
	public static int distance(WorldMapPoiMarker marker) {
		double dx = Math.floor(marker.x()) - originX;
		double dz = Math.floor(marker.z()) - originZ;
		return (int) Math.round(Math.sqrt(dx * dx + dz * dz));
	}

	/** Raw XP the local player could spend. */
	public static long spendable() {
		var player = Minecraft.getInstance().player;
		return player == null ? 0 : FastTravelCosts.spendablePoints(player);
	}

	/** Sends a confirmed journey to a destination marker. Ignored while one is already pending. */
	public static void request(WorldMapPoiMarker destination, int quotedPrice) {
		if (!active || pending)
			return;
		pending = true;
		outcome = null;
		destinationName = destination instanceof WorldMapPoiMarker.Discovered discovered
			? WorldMapPlaceNames.resolve(discovered.nameKey(), discovered.customName()) : "";
		ClientPacketDistributor.sendToServer(new FastTravelRequestMessage(destination.markerId(), quotedPrice));
	}

	/** The latest server answer for the map to show, taken once. */
	public static @Nullable Outcome takeOutcome() {
		Outcome taken = outcome;
		outcome = null;
		return taken;
	}

	/** Handles a {@link FastTravelResultMessage}. */
	public static void onResult(FastTravel.Result result, int price) {
		Minecraft minecraft = Minecraft.getInstance();
		boolean mapOpen = minecraft.screen instanceof WitcherGuiScreen;
		switch (result) {
			case TRAVELLING -> {
				return;
			}
			case SUCCESS -> {
				active = false;
				pending = false;
				if (mapOpen)
					minecraft.setScreen(null);
			}
			case SESSION_ENDED, NO_SESSION, TOO_FAR, DISABLED -> {
				boolean wasActive = active;
				active = false;
				pending = false;
				if (!wasActive && result == FastTravel.Result.SESSION_ENDED)
					return;
			}
			default -> pending = false;
		}
		if (mapOpen && result != FastTravel.Result.SUCCESS)
			outcome = new Outcome(result, price);
		else if (minecraft.player != null && result == FastTravel.Result.SUCCESS && !destinationName.isEmpty())
			minecraft.gui.setOverlayMessage(Component.translatableWithFallback("message.witchercraft.fast_travel.arrived", "You arrive at %s.",
				destinationName), false);
		else if (minecraft.player != null)
			minecraft.gui.setOverlayMessage(FastTravelResultMessage.text(result, price), false);
	}

	/** One server answer: the result and, where it matters, the current price. */
	public record Outcome(FastTravel.Result result, int price) {
	}
}
