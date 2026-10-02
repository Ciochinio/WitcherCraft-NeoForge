package net.redboltmedia.witchercraft;

import net.redboltmedia.witchercraft.procedures.AllPerksProcedure;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.HashMap;

/**
 * What the perk tree knows about perks, collected from Blockly instead of
 * hardcoded here. Every perk is one Blockly procedure listed in AllPerks; its
 * first block is PerkDefine(perkId, perkName, learned), which lands in
 * {@link #define}. Running AllPerks for a player is a "scan": it returns every
 * defined perk id with that player's learned flag, and records each id's name
 * (icons: textures/screens/&lt;name lowercased&gt;_&lt;state&gt;.png, lang:
 * perk.witchercraft.&lt;name lowercased&gt;.name/.desc).
 *
 * A scan also runs each perk's equip line, which is the same derived value the
 * server computes, so it is safe on the client. Perk procedures must therefore
 * not do anything else (no attribute changes, messages, sounds); real perk
 * effects belong in their own procedures that read the equipped var.
 *
 * Learning: {@link #learn} runs AllPerks with one id flagged, and that perk's
 * "if PerkLearnRequested" block checks and spends the skill point and sets its
 * learned var. Scan state is per thread, so the client and the integrated
 * server never see each other's scans. See TECHNICAL_DESIGN_DOCUMENT.md 3.10.
 */
public final class PerkCatalog {
	private PerkCatalog() {
	}

	// perk id -> name; names are the same for every player, so kept once
	private static final Map<Integer, String> NAMES = new ConcurrentHashMap<>();

	private static final class Scan {
		final Map<Integer, Boolean> learned = new HashMap<>();
		final Entity entity;
		final int learnId;

		Scan(Entity entity, int learnId) {
			this.entity = entity;
			this.learnId = learnId;
		}
	}

	private static final ThreadLocal<Scan> CURRENT = new ThreadLocal<>();

	// client: the last scan of the local player, refreshed once per frame by PerkPage
	private static Map<Integer, Boolean> clientLearned = Map.of();

	/** Called by PerkDefineProcedure. Outside a scan (a plain recompute) only the name is kept. */
	public static void define(int perkId, String perkName, boolean learned) {
		if (perkId <= 0)
			return;
		if (perkName != null && !perkName.isEmpty())
			NAMES.put(perkId, perkName);
		Scan scan = CURRENT.get();
		if (scan != null)
			scan.learned.put(perkId, learned);
	}

	/** Called by PerkLearnRequestedProcedure: true only for the perk {@link #learn} is learning. */
	public static boolean learnRequested(Entity entity, int perkId) {
		Scan scan = CURRENT.get();
		return scan != null && scan.learnId > 0 && scan.learnId == perkId && scan.entity == entity;
	}

	/** Every perk defined in AllPerks, with this player's learned flag. */
	public static Map<Integer, Boolean> scan(Player player) {
		return run(player, 0);
	}

	/** Server: run the perk's learn block. The caller has checked the tree (visible, prereqs). */
	public static void learn(Player player, int perkId) {
		run(player, perkId);
	}

	private static Map<Integer, Boolean> run(Player player, int learnId) {
		Scan outer = CURRENT.get();
		Scan scan = new Scan(player, learnId);
		CURRENT.set(scan);
		try {
			AllPerksProcedure.execute(player);
		} finally {
			CURRENT.set(outer);
		}
		return scan.learned;
	}

	/** Client: rescan the local player. PerkPage calls this at the start of each frame. */
	public static void refreshClient(Player player) {
		clientLearned = scan(player);
	}

	/** Client: learned flag from the last {@link #refreshClient}. */
	public static boolean isLearnedClient(int perkId) {
		return clientLearned.getOrDefault(perkId, false);
	}

	/** The perk's name from its PerkDefine block, or "" if no perk claims this id yet. */
	public static String name(int perkId) {
		return NAMES.getOrDefault(perkId, "");
	}
}
