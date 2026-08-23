# WitcherCraft - Technical Design Document (Living Draft)

This document is the companion to `GAME_DESIGN_DOCUMENT.md`.

The GDD says **what** a system is for and how it should feel. This document says **how it is
built** and, more importantly, **how to change it** without breaking it. If you are about to
touch one of these systems, read its chapter first - most of what is written here exists because
the obvious approach does not work, and the reason why is recorded so nobody rediscovers it the
hard way.

Anything marked **tunable** is a value you are expected to change. Anything described as a
*contract* is load-bearing: other parts of the build assume it holds.

## Table of Contents

1. [GUI and HUD](#1-gui-and-hud)

---

## 1. GUI and HUD

### 1.1 What the system is for

WitcherCraft adds resources that vanilla Minecraft has no way to display: the Quen shield pool and
Toxicity. Both are decisions the player makes in the middle of a fight, so both need to be readable
at a glance, in the place the eye already goes - the bottom centre of the screen, alongside health
and hunger.

Two readouts exist today:

- **Quen shield**, on the **left**, directly above health and armor, drawn as heart-style icons.
  One big bubble per **2** points of pool and a small bubble for a leftover odd point, ten per row,
  extra rows stacking upward. Present only while a shield is up, in the same way the armor row is
  present only while you are wearing armor.
- **Toxicity**, on the **right**, above hunger and below oxygen. Always present, filling smoothly
  from the screen edge inward toward the centre, and switching to its overdose colour once you cross
  the Overdose Threshold.

The split is deliberate and is the convention future bars should follow: **the left column is
defensive state, the right column is the alchemy and consumables resource**. Keeping the two on
separate columns also means they never contend for the same vertical space (see 1.3).

### 1.2 Why this is not an MCreator overlay

MCreator's Overlay element is the obvious home for this and it cannot do the job. The reason is
worth stating precisely, because it also rules out several tempting workarounds.

A HUD bar has to sit **above whatever is already there**. What is already there varies: armor row
present or absent, absorption active or not, and this mod supports max health well above 20, which
makes vanilla render multiple rows of hearts. Minecraft's own solution is a running cursor, not a
layout table. NeoForge adds two public fields to `net.minecraft.client.gui.Gui`:

```java
public int leftHeight;   // left of the hotbar: health, armor
public int rightHeight;  // right of the hotbar: food, vehicle health, air
```

Both are reset to `39` at the start of every frame in `Gui.extractRenderState`, and each HUD layer
draws at `guiHeight() - <cursor>` and then advances the cursor by the space it used. Health does
`leftHeight += (numHealthRows - 1) * healthRowHeight + 10`; armor does `leftHeight += 10` only if
you are actually wearing armor.

Three consequences:

1. **Enumerating cases is not merely tedious, it is wrong.** Vanilla compresses the heart row pitch
   once you pass two rows: `healthRowHeight = Math.max(10 - (numHealthRows - 2), 3)`. The vertical
   offset is a formula with a clamp in it, not a finite set of layouts, and with arbitrary max
   health the case list is unbounded.
2. **MCreator overlays hook the wrong event.** They generate a `RenderGuiEvent.Pre` handler.
   `GuiLayerManager.render` fires that event *after* the reset to 39 and *before* any layer has run,
   so inside a generated overlay both cursors are always exactly 39.
3. **A custom-code procedure does not rescue it.** The `java_code` / `java_code_get` blocks are real
   and can read `Minecraft.getInstance().gui.leftHeight` perfectly well, but a procedure is a static
   method on its own class called as `XProcedure.execute(entity)` - it has no `event` and no
   `GuiGraphics`, so it cannot draw. And the component's y coordinate is emitted as a literal at
   generation time, so there is no runtime value for it to influence anyway.

Hence the renderer is a **Code element**, which is MCreator's supported way to add a hand-written
class. Its code is locked by design rather than by a flag, and its own template header states that
registering new events in it is an intended use.

### 1.3 How placement works

The renderer subscribes to `RenderGuiLayerEvent.Post` and branches on `event.getName()`. Vanilla's
layer order is `HOTBAR, PLAYER_HEALTH, ARMOR_LEVEL, FOOD_LEVEL, VEHICLE_HEALTH, AIR_LEVEL, ...`.

| Readout | Hooked after | Draws at | Then |
|---|---|---|---|
| Quen | `VanillaGuiLayers.ARMOR_LEVEL` | `guiHeight() - gui.leftHeight` | `gui.leftHeight += (rows - 1) * rowHeight + 10` |
| Toxicity | `VanillaGuiLayers.VEHICLE_HEALTH` | `guiHeight() - gui.rightHeight` | `gui.rightHeight += 10` |

Two choices in that table are load-bearing:

- **Toxicity hooks `VEHICLE_HEALTH`, not `FOOD_LEVEL`.** When you are mounted, `extractFoodLevel`
  bails out entirely and `extractVehicleHealth` draws horse hearts in that slot instead, advancing
  `rightHeight` once per row. Hooking after `FOOD_LEVEL` would bury Toxicity under the horse hearts.
  Hooking after `VEHICLE_HEALTH` is correct mounted and unmounted. `AIR_LEVEL` runs next, reads the
  advanced cursor, and renders above us - which is what produces hunger, then Toxicity, then oxygen.
- **Advancing the cursor is a contract, not politeness.** It is what lets the next bar stack instead
  of overlapping, ours or another mod's. Any bar added later must advance it too.

**The gotcha that will bite you.** These layer events fire *even when the layer does not render*.
The visibility check lives inside the wrapped layer, not around the event, and the `RenderGuiLayerEvent`
javadoc says so explicitly. Vanilla gates health, armor, food, and air on
`gameMode.canHurtPlayer() && !options.hideGui`, so in creative, in spectator, or with the HUD hidden
by F1, nothing advances the cursors, they stay at 39, and an unguarded bar lands on top of the
hotbar. `WitcherHud` replicates that gate at the top of the handler. Do not remove it.

### 1.4 The pieces

| Element | Type | MCreator path | Role |
|---|---|---|---|
| [WitcherHud](../src/main/java/net/redboltmedia/witchercraft/WitcherHud.java) | code (locked) | `~/GUI` | The renderer. Decides where a bar goes and blits it. Nothing else. |
| [QuenHudShow](../elements/QuenHudShow.mod.json) | procedure, logic | `~/Signs/Quen` | Is the Quen bar visible: `witchercraftQuenShield > 0` |
| [QuenHudPool](../elements/QuenHudPool.mod.json) | procedure, number | `~/Signs/Quen` | The raw remaining pool in points. The icon maths lives beside the rendering. |
| [ToxicityHudFill](../elements/ToxicityHudFill.mod.json) | procedure, number | `~/Alchemy/ Toxicity` | Fill fraction 0..1: `witchercraftToxicity / 100` |
| [ToxicityHudOverdose](../elements/ToxicityHudOverdose.mod.json) | procedure, logic | `~/Alchemy/ Toxicity` | Use the overdose colour: toxicity at or past `ToxicityOverdoseThreshold` |

**The separation is the point.** The locked file contains no tuning values that describe game state -
only geometry. Everything a designer would want to change lives in ordinary block-based procedures
that open normally in MCreator. This is the same split as `QuenEffectTick` dispatching to `QuenAura`.

Supporting state:

- `witchercraftQuenShieldMax` (number, player_lifetime) holds the pool size at cast time.
  [QuenCast](../elements/QuenCast.mod.json) sets **Max** to the formula and then sets the pool **from
  Max**, so the base value has exactly one definition. The icon renderer does not read Max at all,
  since it draws only the shield you actually have; Max is kept because it is the single definition
  of the cast formula, and because empty sockets would need it. Nothing clears it, and a stale value
  is harmless.
- `witchercraftQuenShield` **must be zero whenever no shield is up**, because that is the entire
  visibility condition. `QuenBlock` zeroes it when hits drain the pool, but a shield that *times out*
  with damage still on it would otherwise keep a stale value and leave the bar on screen. So
  [QuenBroke](../elements/QuenBroke.mod.json) also clears it. That procedure is the right home
  because `WitchercraftModMobEffects.expireEffects` dispatches it from both `MobEffectEvent.Remove`
  (shattered) and `MobEffectEvent.Expired` (timed out), so it covers every way the shield can end.
  Note that this is a state fix, not a display fix: gameplay was never wrong, since `QuenBlock`
  independently gates on `hasEffect(QUEN_EFFECT)`.
- `ToxicityOverdoseThreshold` already exists and is initialised to **70** by
  `WitchercraftPlayerBaseStats`. It is `0` before that runs, which is why `ToxicityHudOverdose`
  refuses to report an overdose while the threshold is still zero.

Textures live in `src/main/resources/assets/witchercraft/textures/screens/`:

| File | Size | Rows, top to bottom |
|---|---|---|
| `quen_icons.png` | 18x9 | *columns*: 0 big bubble (2 points), 1 small bubble (1 point) |
| `toxicity_bar.png` | 81x27 | *rows*: 0 empty track, 1 fill, 2 overdose fill |

`81` is vanilla's icon-row width (10 icons x 8 + 1) and `9` is its icon height on a `10` pixel pitch,
so both line up with hearts and armor. Quen icons are 9x9 cells with the art inset to 7x7 so that
adjacent bubbles read as separate on the 8 pixel step. Current art is flat placeholder colour.

### 1.5 How Quen icons are drawn

This mirrors vanilla's `Gui.extractHearts` loop, minus the empty-container pass - only the shield you
actually have is drawn, so there are no empty sockets behind it.

```java
int points    = Mth.ceil(pool);                 // a fraction left still earns a small bubble
int iconCount = ceilDiv(points, 2);
int rows      = ceilDiv(iconCount, 10);
int rowHeight = Math.max(10 - (rows - 2), 3);   // vanilla's compression past two rows
for (int i = iconCount - 1; i >= 0; i--) {
    int xo = xLeft + (i % 10) * 8;              // 9px cells on an 8px step
    int yo = yLineBase - (i / 10) * rowHeight;
    cell = (i * 2 + 1 == points) ? SMALL : BIG; // odd leftover point is the small bubble
}
```

**One icon is 2 points of pool**, deliberately the same rate as a heart being 2 HP, so the Quen row
is directly comparable to the health row beneath it. Base Quen (**8**) therefore reads as four big
bubbles. A second row needs a pool above 20, which at the current formula means Sign Intensity around
**150%** - unreachable today, but the multi-row and compression paths are implemented so that
changing the formula later does not require touching the renderer.

The cursor advance is `(rows - 1) * rowHeight + 10`, exactly what vanilla's health layer does, which
is why a multi-row Quen correctly pushes everything above it further up.

Adding empty sockets later, if the reflow of a draining multi-row shield turns out to read badly, is
one more cell in `quen_icons.png` plus one draw call before the filled icon.

### 1.6 How the toxicity fill is drawn

Two blits. The empty track at full width, then the fill row clipped to the filled width:

```java
int filled = (int) Math.round(clamp(fill, 0, 1) * BAR_W);
int offset = anchorRight ? BAR_W - filled : 0;   // source crop and destination offset are equal
```

**Fill direction is a layout contract, not a preference.** Every vanilla readout is anchored at the
**outer** edge of its column and moves toward the centre: hearts and armor run right from `xLeft`,
food and air run left from `xRight`. A right-column bar that fills left to right therefore reads as
starting in mid-screen, which is why toxicity passes `anchorRight = true`. Anything added to the left
column passes `false`.

The anchor carries position, not valence. Health, armor, food, and air all share the outer-edge
anchor and mean completely different things; what distinguishes a resource you want full from one you
want empty is its colour and its form, not the direction it grows. The useful side effect of
mirroring toxicity is that hunger recedes away from the centre as it worsens while toxicity advances
toward the centre as it worsens - same axis, opposite motion, both legible.

The ten-argument `blit` overload takes the source region size and the destination size from the same
`width`/`height` pair, so passing a smaller width crops the texture rather than squashing it. This is
the same technique the vanilla experience bar uses. The result is genuinely smooth - there is no
frame quantisation, because this is real Java and not MCreator's declarative sprite component.

### 1.7 How to change things

**Retune when a bar appears, or what counts as full.** Edit the relevant `*Hud*` procedure in
MCreator. No Java, no rebuild of the renderer. These run every frame, so keep them to plain variable
maths - no entity lookups, no effect queries.

**Change the art.** Replace the PNG, keeping its cell layout and cell size. With a client running,
the debug resource reload (F3+T by default) picks up the new texture without a restart, so art
iteration does not need a rebuild.

**Change what one Quen icon is worth.** `POINTS_PER_ICON` in `WitcherHud`. This is a balance-visible
number: at **2** it matches a heart, and lowering it makes Quen look bigger and reach a second row
sooner.

**Change bar or icon geometry.** `BAR_W`, `BAR_H`, `ICON`, `ICON_STEP`, `ICONS_PER_ROW`, and `ROW` in
`WitcherHud`. Textures must change with them. Keep `ROW` at `10` unless you have a reason: it is
vanilla's row pitch, and other mods stacking on the same cursor assume that grid.

**Add a colour state to a bar.** Add a row to its PNG, bump the `*_TEX_ROWS` constant, add a
`*_ROW_*` index, and pick between them with a new logic procedure. Toxicity's overdose row is the
worked example.

**Add a new readout.** Decide the column (left for defensive state, right for resources), write a
value procedure and a visibility procedure, add a branch in `onRenderGuiLayer` hooked to the layer you
want to sit above, and advance that column's cursor. `drawBar` and `drawQuenIcons` are both reusable,
so switching a readout between bar and icons is a change at the call site. If the bar should hide entirely when
inactive, return **before** advancing the cursor - that is what makes the rows above it close the gap,
and it is exactly how the Quen branch behaves.

**Move a bar to the other column.** Change the hooked layer name and swap `leftHeight` for
`rightHeight` and the x anchor (`guiWidth() / 2 - 91` on the left, `guiWidth() / 2 + 91 - BAR_W` on
the right).

### 1.8 Known limitations

- These bars are not previewable in MCreator's overlay editor, because they are not overlays. Layout
  is verified in game. In practice there is little to verify: y is computed, and x matches vanilla's
  column.
- Toxicity's maximum is the constant **100** inside `ToxicityHudFill`, matching the `Tox1`..`Tox5`
  thresholds. If maximum Toxicity ever becomes a stat, that divisor moves into a variable.
- The older `ToxicityOverlay` element (five arrow images at fixed thresholds) is superseded by this
  system and is left in place only pending cleanup.
