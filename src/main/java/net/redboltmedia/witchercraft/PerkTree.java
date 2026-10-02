package net.redboltmedia.witchercraft;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

/**
 * Perk-tree topology: the slots of the tree. Each node is a slot id with a
 * position (GUI-local, left half of the equip screen), its prerequisites and a
 * hidden flag. A node's branch is its colour = perkId / 100. Which perk fills a
 * slot is not decided here: a perk's Blockly procedure claims the id with
 * PerkDefine (see PerkCatalog). A slot no perk claims shows its id and cannot
 * be learned.
 *
 * Hidden nodes ({@code .hidden()}) act as if they were not in the tree: not
 * drawn, not learnable, never counted as socketed, and ignored as prerequisites
 * (a node whose prerequisites are all hidden is a root).
 *
 * {@code prereqs} is an OR group, not AND: zero entries = always learnable, one
 * or more = learning ANY single listed perk unlocks this node (multiple parents
 * are alternative unlock paths converging on one node, not a requirement to
 * learn them all). Enforced identically client-side (PerkPage, for render state)
 * and server-side (PerkEquipGuiButtonMessage, for the actual learn action) - see
 * TECHNICAL_DESIGN_DOCUMENT.md 3.10.
 *
 * Editable two ways: by hand (plain Java array literals, `new Node(id, x, y,
 * prereqId...)`, with `.hidden()` appended to hide a slot - no tool required for
 * a quick tweak), or generated wholesale by tools/tree-node-placer.html (add,
 * hide and position slots, click-link to draw/delete prerequisite arrows,
 * live-exports a complete file to paste over this one). Both are equally valid.
 */
public final class PerkTree {
	private PerkTree() {
	}

	public static final int NODE_SIZE = 18; // fixed: a vanilla slot (18x18 frame, 16x16 icon)

	// Sub-tab row: Combat, Alchemy, Signs, General, Mutagens (one shared y).
	public static final int[] TAB_X = {4, 38, 72, 106, 140};
	public static final int TAB_Y = 0;

	// Mutagens tab: top-left of the inventory block's first slot frame. Three
	// rows of 9 at an 18px pitch, then the hotbar 4px lower (162x76 in all).
	public static final int INVENTORY_X = 0;
	public static final int INVENTORY_Y = 24;

	// Bottom status texts: skill points and the instructions (x, y = top-left,
	// scaled about that corner). Wording is in the lang file.
	public static final int POINTS_X = 8;
	public static final int POINTS_Y = 186;
	public static final float POINTS_SCALE = 1f;
	public static final int HINT_X = 100;
	public static final int HINT_Y = 186;
	public static final float HINT_SCALE = 1f;

	public static final class Node {
		public final int perkId;
		public final int x, y;
		public final int[] prereqs;
		public boolean hidden;

		Node(int perkId, int x, int y, int... prereqs) {
			this.perkId = perkId;
			this.x = x;
			this.y = y;
			this.prereqs = prereqs;
		}

		Node hidden() {
			this.hidden = true;
			return this;
		}

		public int cx() {
			return x + NODE_SIZE / 2;
		}

		public int cy() {
			return y + NODE_SIZE / 2;
		}
	}

	public static final Node[] NODES = {
			new Node(101, 8, 24),
			new Node(102, 66, 24),
			new Node(103, 124, 24),
			new Node(104, 8, 56),
			new Node(105, 66, 56),
			new Node(106, 124, 56),
			new Node(107, 8, 88),
			new Node(108, 66, 88),
			new Node(109, 124, 88),
			new Node(110, 8, 120),
			new Node(111, 66, 120),
			new Node(112, 124, 120),
			new Node(113, 8, 152),
			new Node(114, 66, 152),
			new Node(115, 124, 152),
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

	/** The visible node with this id, or null (no such slot, or hidden). */
	public static Node byId(int perkId) {
		for (Node n : NODES)
			if (n.perkId == perkId && !n.hidden)
				return n;
		return null;
	}

	/** Visible nodes belonging to a branch colour (1 red / 2 green / 3 blue / 4 neutral). */
	public static List<Node> forColor(int color) {
		List<Node> out = new ArrayList<>();
		for (Node n : NODES)
			if (n.perkId / 100 == color && !n.hidden)
				out.add(n);
		return out;
	}

	/**
	 * Prerequisites are an OR group: met if any visible prerequisite is learned,
	 * or if the node has no visible prerequisites. Shared by PerkPage (render
	 * state) and PerkEquipGuiButtonMessage (the server's learn check).
	 */
	public static boolean prereqsMet(Node n, IntPredicate learned) {
		boolean any = false;
		for (int pre : n.prereqs) {
			if (byId(pre) == null)
				continue;
			any = true;
			if (learned.test(pre))
				return true;
		}
		return !any;
	}
}
