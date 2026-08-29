package net.redboltmedia.witchercraft;

import java.util.ArrayList;
import java.util.List;

/**
 * Perk-tree topology: node positions (GUI-local, left half of the equip screen)
 * and per-node prerequisites. A node's branch is its colour = perkId / 100.
 *
 * {@code prereqs} is an OR group, not AND: zero entries = always learnable, one
 * or more = learning ANY single listed perk unlocks this node (multiple parents
 * are alternative unlock paths converging on one node, not a requirement to
 * learn them all). Enforced identically client-side (PerkPage, for render state)
 * and server-side (PerkEquipGuiButtonMessage, for the actual learn action) - see
 * TECHNICAL_DESIGN_DOCUMENT.md 3.10 "Adding a new perk".
 *
 * Editable two ways: by hand (plain Java array literals, `new Node(id, x, y,
 * prereqId...)` - no tool required for a quick tweak), or generated wholesale by
 * tools/tree-node-placer.html (drag-position nodes, click-link to draw/delete
 * prerequisite arrows, live-exports a complete file to paste over this one).
 * Both are equally valid; the tool is just a visual aid for large layout passes.
 */
public final class PerkTree {
	private PerkTree() {
	}

	public static final int NODE_SIZE = 24;

	public static final class Node {
		public final int perkId;
		public final int x, y;
		public final int[] prereqs;

		Node(int perkId, int x, int y, int... prereqs) {
			this.perkId = perkId;
			this.x = x;
			this.y = y;
			this.prereqs = prereqs;
		}

		public int cx() {
			return x + NODE_SIZE / 2;
		}

		public int cy() {
			return y + NODE_SIZE / 2;
		}
	}

	public static final Node[] NODES = {
			new Node(101, 32, 6),
			new Node(102, 36, 132, 114, 105), // requires SunderArmor, CrushingBlows
			new Node(103, 90, 8),
			new Node(104, 4, 26, 101), // requires AnatomicalKnowledge
			new Node(105, 32, 44, 101, 104), // requires AnatomicalKnowledge, CripplingStrikes
			new Node(106, 120, 28, 103), // requires CripplingShot
			new Node(107, 92, 176, 113, 115), // requires StrengthTraining, Undying
			new Node(108, 64, 64, 105, 109), // requires CrushingBlows, FloodOfAnger
			new Node(109, 92, 58, 106, 103), // requires DeadlyPrecision, CripplingShot
			new Node(110, 36, 178, 113, 102), // requires StrengthTraining, ColdBlood
			new Node(111, 64, 90, 108), // requires FleetFooted
			new Node(112, 124, 92, 106), // requires DeadlyPrecision
			new Node(113, 64, 152, 102, 115, 114), // requires ColdBlood, Undying, SunderArmor
			new Node(114, 64, 116, 111), // requires PreciseBlows
			new Node(115, 92, 132, 109, 112, 114), // requires FloodOfAnger, RazorFocus, SunderArmor
			new Node(201, 8, 24),
			new Node(202, 66, 24),
			new Node(203, 124, 24),
			new Node(204, 8, 56),
			new Node(205, 66, 56),
			new Node(206, 124, 56),
			new Node(207, 8, 88),
			new Node(208, 66, 88),
			new Node(209, 124, 88),
			new Node(301, 8, 24),
			new Node(302, 66, 24),
			new Node(303, 124, 24),
			new Node(304, 8, 56),
			new Node(305, 66, 56),
			new Node(306, 124, 56),
			new Node(307, 8, 88),
			new Node(308, 66, 88),
			new Node(309, 124, 88),
			new Node(310, 8, 120),
			new Node(311, 66, 120),
			new Node(312, 124, 120),
			new Node(313, 8, 152),
			new Node(314, 66, 152),
			new Node(315, 124, 152),
			new Node(401, 8, 24),
			new Node(402, 66, 24),
			new Node(403, 124, 24),
			new Node(404, 8, 56),
			new Node(405, 66, 56),
			new Node(406, 124, 56),
	};

	public static Node byId(int perkId) {
		for (Node n : NODES)
			if (n.perkId == perkId)
				return n;
		return null;
	}

	/** nodes belonging to a branch colour (1 red / 2 green / 3 blue / 4 neutral). */
	public static List<Node> forColor(int color) {
		List<Node> out = new ArrayList<>();
		for (Node n : NODES)
			if (n.perkId / 100 == color)
				out.add(n);
		return out;
	}
}
