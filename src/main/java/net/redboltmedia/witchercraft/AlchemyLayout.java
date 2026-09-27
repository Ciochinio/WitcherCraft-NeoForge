package net.redboltmedia.witchercraft;

/**
 * Geometry of the Alchemy tab's SLOT BLOCK: the alchemy circle (base in the
 * centre, five ingredients around it), the output slot, and the player
 * inventory. Shared by {@link AlchemyMenu} (slot positions) and
 * {@link AlchemyScreen} (drawing and placement).
 *
 * Unlike the other shell layouts, these are NORMAL GUI pixels, not the 640x360
 * design canvas: real item slots draw at vanilla scale so vanilla handles every
 * click, drag and tooltip. Slot coords are the 16x16 item position inside the
 * block, like vanilla {@code Slot} x/y; the 18x18 frame is drawn 1px outside.
 *
 * The whole block (with the recipe book panel to its left) is centred in the
 * shell's content region, then shifted by BLOCK_OFFSET_X/Y.
 *
 * Keep it a dumb data holder: slice 2's layout placer tool
 * (tools/alchemy-layout-placer.html) will export this file wholesale.
 */
public final class AlchemyLayout {
	private AlchemyLayout() {
	}

	// --- slot block size and placement (GUI pixels) --------------------------
	public static final int BLOCK_W = 176;
	public static final int BLOCK_H = 180;
	public static final int BLOCK_OFFSET_X = 0;
	public static final int BLOCK_OFFSET_Y = 0;

	// --- recipe book panel, left of the block (reserved space in step 0) -----
	public static final int BOOK_W = 147;
	public static final int BOOK_H = 166;
	public static final int BOOK_GAP = 4;

	// --- alchemy circle --------------------------------------------------------
	public static final int BASE_X = 62;
	public static final int BASE_Y = 36;
	// ingredient slots clockwise from the top
	public static final int[] INGREDIENT_X = {62, 92, 81, 43, 32};
	public static final int[] INGREDIENT_Y = {4, 26, 62, 62, 26};

	// --- output ------------------------------------------------------------------
	public static final int OUTPUT_X = 144;
	public static final int OUTPUT_Y = 36;

	// --- player inventory (3 rows) and hotbar -----------------------------------
	public static final int INVENTORY_X = 8;
	public static final int INVENTORY_Y = 98;
	public static final int HOTBAR_Y = 156;

	// --- colours -----------------------------------------------------------------
	public static final int PANEL_COLOR = 0xD0101015;
	public static final int PANEL_BORDER_COLOR = 0xFF33333D;
	public static final int BOOK_COLOR = 0x80101015;
	public static final int MISFIT_COLOR = 0xFFD04040; // outline when the block does not fit (step 0 check)
}
