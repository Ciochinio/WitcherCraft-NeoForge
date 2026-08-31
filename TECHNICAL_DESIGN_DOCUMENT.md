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
2. [Character Stats and Attributes](#2-character-stats-and-attributes)
3. [The GUI shell](#3-the-gui-shell)
4. [Cockatrice and hybrid mob movement](#4-cockatrice-and-hybrid-mob-movement)
5. [World map terrain pipeline](#5-world-map-terrain-pipeline)

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

Alongside them sits one readout that is not a bar:

- **The witcher medallion**, in the **centre**, which shakes when monsters are near. It is an
  instrument, not a resource, so it deliberately takes no part in the cursor protocol below - it
  is drawn at a fixed height in the empty column between the two bar stacks, and it never moves.

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
| Medallion | `VanillaGuiLayers.AIR_LEVEL` | `guiHeight() - 39 - 20`, fixed | nothing - it is not on a cursor |

**The medallion is the exception, and it is allowed to be.** Both bar columns are 81 pixels wide
and anchored at their outer edge: hearts and armor run right from `guiWidth() / 2 - 91`, food and
air run left from `guiWidth() / 2 + 91`. That leaves a **20 pixel channel at dead centre that
nothing ever draws in**, at every row, all the way up the screen. The medallion lives there at a
constant y, which is why it needs no cursor, cannot be pushed around by armor or bonus max health,
and cannot collide with anything stacking on either side.

Two consequences worth knowing. `MEDALLION_W` **cannot exceed 20** without overlapping the heart
and hunger rows; its height is unconstrained. And because y is a constant rather than a cursor read,
the medallion does not strictly *need* the `canHurtPlayer()` gate the bars need - it would land
correctly in creative. It shares that gate anyway, for looks rather than correctness: in creative the
whole bar cluster is absent and a lone medallion hovering over empty ground reads as a bug.

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
| [WitcherHud](../src/main/java/net/redboltmedia/witchercraft/WitcherHud.java) | code (locked) | `~/Gui` | The renderer. Decides where a bar goes and blits it. Nothing else. |
| [QuenHudShow](../elements/QuenHudShow.mod.json) | procedure, logic | `~/Signs/Quen` | Is the Quen bar visible: `witchercraftQuenShield > 0` |
| [QuenHudPool](../elements/QuenHudPool.mod.json) | procedure, number | `~/Signs/Quen` | The raw remaining pool in points. The icon maths lives beside the rendering. |
| [ToxicityHudFill](../elements/ToxicityHudFill.mod.json) | procedure, number | `~/Alchemy/ Toxicity` | Fill fraction 0..1: `witchercraftToxicity / 100` |
| [ToxicityHudOverdose](../elements/ToxicityHudOverdose.mod.json) | procedure, logic | `~/Alchemy/ Toxicity` | Use the overdose colour: toxicity at or past `witchercraftToxicityOverdoseThreshold` |
| [MedallionShow](../elements/MedallionShow.mod.json) | procedure, logic | `~/Gui` | Is the medallion drawn at all. Always true today - see the note below |
| [MedallionSenseRange](../elements/MedallionSenseRange.mod.json) | procedure, number | `~/Gui` | **Tunable.** How far the medallion senses, in blocks. **24** |
| [MedallionStepNear](../elements/MedallionStepNear.mod.json) | procedure, number | `~/Gui` | **Tunable.** Inside this distance the shake is at its strongest. **8** |
| [MedallionStepMid](../elements/MedallionStepMid.mod.json) | procedure, number | `~/Gui` | **Tunable.** Inside this distance the shake is medium; beyond it, faint. **16** |
| [MedallionSenseAllHostiles](../elements/MedallionSenseAllHostiles.mod.json) | procedure, logic | `~/Gui` | **Tunable.** Also sense anything in `MobCategory.MONSTER`, not just the tag. **true** |
| [MedallionVariant](../elements/MedallionVariant.mod.json) | procedure, number | `~/Gui` | Which row of `medallion.png` to draw. Reads `witchercraftMedallion` |
| [MedallionCycle](../elements/MedallionCycle.mod.json) | procedure, void | `~/Gui` | Advances `witchercraftMedallion` to the next row, wrapping. Called by `/medallion` |
| [MedallionCommand](../elements/MedallionCommand.mod.json) | command | `~/Admin/Commands` | `/medallion` - cycles the medallion in game |

`MedallionShow` returns `witchercraftPlayerLevel >= 0`, which is always true. That is deliberate,
not a leftover: it makes the procedure genuinely depend on `entity`, so when the gate becomes real
(a medallion item, or a perk) MCreator regenerates it with the same `execute(Entity)` signature and
the call site in the locked renderer keeps compiling. A procedure that returned a bare `true` would
have no dependency, and adding one later would silently break the build.

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
- `witchercraftToxicityOverdoseThreshold` already exists and is initialised to **70** by
  `WitchercraftPlayerBaseStats`. It is `0` before that runs, which is why `ToxicityHudOverdose`
  refuses to report an overdose while the threshold is still zero.

Textures live in `src/main/resources/assets/witchercraft/textures/screens/`:

| File | Size | Rows, top to bottom |
|---|---|---|
| `quen_icons.png` | 18x9 | *columns*: 0 big bubble (2 points), 1 small bubble (1 point) |
| `toxicity_bar.png` | 81x27 | *rows*: 0 empty track, 1 fill, 2 overdose fill |
| `medallion.png` | 20x40 | *rows*: one square 20x20 cell per medallion. 0 gold, 1 steel |

The medallion also reads two entity type tags in
`src/main/resources/data/witchercraft/tags/entity_type/`:

| Tag | Ships as | Role |
|---|---|---|
| `medallion_senses.json` | `zombie` | The explicit opt-in list. Anything here always sets the medallion off. |
| `medallion_ignores.json` | empty | Subtracted from everything, tag and `MobCategory.MONSTER` alike. Populate it if the medallion should stay quiet around illagers, piglins, or anything else humanoid. |

**These are deliberately not the `witchercraft:<category>` bestiary tags.** Those drive oil
weaknesses, so widening them to make the medallion useful would silently rebalance every oil in
the game. Keeping the medallion's senses on their own tags lets the two diverge.

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

### 1.7 How the medallion senses and shakes

**The scan is client side, and that is the whole design.** The client already tracks every entity
the server sends it, so `player.level().getEntities(...)` inside the renderer is free and needs no
player variable, no procedure running on the server, and no packet. It therefore never goes near
`markSyncDirty()`, which pushes all 86 player variables across the wire on every call.

This matters because the obvious thing to reuse is
[EnemyNearbyDetection](../elements/EnemyNearbyDetection.mod.json), the Cold Blood feeder, and it is
not fit for it. That procedure runs a full AABB scan **every tick for every player** and calls
`markSyncDirty()` once per entity found. It is also logically wrong: the flag is reassigned on each
loop iteration, so the *farthest* entity in range decides the result. Fixing it is a separate job;
do not build on it.

The scan runs every `MEDALLION_SCAN_INTERVAL` ticks (**10**) and caches an intensity step:

| Nearest sensed monster | Intensity | Frame time | Moves per second |
|---|---|---|---|
| none in range | 0 | still | - |
| `MedallionStepMid`..`MedallionSenseRange` | 1 | 90ms | 11 |
| `MedallionStepNear`..`MedallionStepMid` | 2 | 60ms | 17 |
| closer than `MedallionStepNear` | 3 | 35ms | 29 |

Two details in that scan are easy to get wrong. The scan box is a **cube** and the range is a
**sphere**, so the result is re-tested against `range * range` afterwards - without it the medallion
reaches about 1.7x further diagonally than straight ahead. And `player.tickCount` restarts at zero
on respawn and on a dimension change, so the interval check treats any backwards jump as due rather
than waiting out a stale interval.

**Steps, not a continuous value.** A wobble that varies smoothly with distance reads as noise; three
states read as information. `MedallionStepNear` and `MedallionStepMid` are the boundaries and are
meant to be retuned.

**The shake is pure translation, and there is one drawing per medallion.** `MEDALLION_SHAKE` is a
list of x/y pixel offsets from rest and the renderer steps through it, moving the whole sprite one
pixel in each direction. The list is deliberately **not** in ring order - stepping around a circle
reads as an orbit, a scrambled order reads as a vibration. Full cycle time is 8 x the frame time.

An earlier version used three pre-tilted frames swinging `1, 0, 1, 2` like a pendulum. Translation
replaced it for two reasons: a pendulum only moves on one axis, and per-medallion art tripled with
every variant added. One square cell per medallion is the whole sheet now.

**Why not rotate the sprite.** `GuiGraphics.pose()` is a JOML `Matrix3x2fStack` in this generator, so
rotation is genuinely available. It looks bad. GUI textures sample nearest neighbour, so a rotated
20 pixel sprite lands source pixels unevenly on screen pixels and shimmers while it moves - exactly
the case here. Whole-pixel translation keeps every frame pixel-exact.

The frame clock is `Util.getMillis()`, not tick count, so the shake is smooth and independent of the
10 tick sense clock.

**Authoring the art: 1:1 with the drawn size, always.** The `blit` overload used here takes
destination size separately from source size, so a bigger PNG scaled down does work mechanically -
but it will shimmer. GUI textures sample nearest neighbour, and the destination is measured in GUI
units while rasterisation happens in screen pixels, so a 20 unit sprite occupies `20 x guiScale`
screen pixels. Only a source that is 20 pixels wide divides evenly into that at **every** GUI scale.
A 40 pixel source is exact at scale 2 and uneven at scale 3; a 64 pixel source is uneven almost
everywhere. On a sprite that is permanently in motion that unevenness is visible. Author at the
drawn size. Current art is flat placeholder colour.

**Leave the shake a pixel of margin.** The cell is 20 wide because the channel is 20 wide, but the
sprite also moves one pixel each way. Art that fills the full 20 will poke into the health or hunger
row at full lean. Keep the drawn shape inside the middle **18** columns. Vertically there is no such
limit - nothing else draws in this column.

**One trap, recorded because it cost a session.** The scan interval is checked as
`player.tickCount - medallionLastScan`. Seeding `medallionLastScan` with `Integer.MIN_VALUE` as a
"never scanned yet" sentinel overflows that subtraction, wraps it negative, and the check then
passes on every frame - the medallion renders perfectly and silently never senses anything. It is
seeded at `-MEDALLION_SCAN_INTERVAL` instead.

### 1.8 How to change things

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

**Move the medallion.** `MEDALLION_X_OFFSET` and `MEDALLION_Y_ORIGIN` in `WitcherHud`, and nothing
else. x is an offset from screen centre; y counts up from the bottom edge on the same origin the
cursors use, so **39** puts its base level with the top of the health row. Expect to need this:
Tough As Nails draws an icon in the same centre channel.

**Retune what the medallion senses.** Radius and the two step boundaries are the `Medallion*`
procedures, editable in MCreator. What counts as a monster is the two tags plus
`MedallionSenseAllHostiles`.

**Add a medallion.** Three places, and they must agree:

1. Append a 20x20 cell to the bottom of `medallion.png`.
2. Bump `MEDALLION_VARIANTS` in `WitcherHud`. This is the texture height the blit divides by, so a
   value that disagrees with the PNG makes **every** row sample wrong, not just the new one.
3. Bump the wrap number in [MedallionCycle](../elements/MedallionCycle.mod.json), or `/medallion`
   will never reach the new row.

Two variants exist as a worked template: row **0** gold, row **1** steel, same shape. The renderer
clamps the row, so a bad value cannot blit off the end of the texture - it just sticks on the last
one.

**How switching works at runtime.** `witchercraftMedallion` (number, **`player_persistent`**) holds
the row index. `MedallionVariant` reads it, and the renderer calls that every frame, so the sprite
swaps the instant the value changes - no reload, no relog. The value is written server side and
reaches the client through the ordinary player variable sync, the same route `QuenHudShow` uses.

`/medallion` cycles it. The command is [MedallionCommand](../elements/MedallionCommand.mod.json)
calling [MedallionCycle](../elements/MedallionCycle.mod.json), which is just
`(witchercraftMedallion + 1) % 2`.

**When the School picker lands**, point it at this same variable rather than adding a second one, and
keep the rule that **the variable is the row index** - 0 generic, 1-6 per School. No lookup table to
keep in sync.

Two things not to do. Do not read
`witchercraftAbilitiesBearSchool` / `CatSchool` / `GriffinSchool`: they are **skill tree perks**, not
identity, only three of the six exist, and nothing makes them exclusive. And do not use a
`player_lifetime` variable, which resets on death and would wipe the player's School the first time
something killed them.

### 1.9 Known limitations

- These bars are not previewable in MCreator's overlay editor, because they are not overlays. Layout
  is verified in game. In practice there is little to verify: y is computed, and x matches vanilla's
  column.
- Toxicity's maximum is the constant **100** inside `ToxicityHudFill`, matching the `Tox1`..`Tox5`
  thresholds. If maximum Toxicity ever becomes a stat, that divisor moves into a variable.
- The older `ToxicityOverlay` element (five arrow images at fixed thresholds) is superseded by this
  system and is left in place only pending cleanup.
- The medallion's reach is capped by entity tracking range, not by `MedallionSenseRange`. The client
  is only told about entities the server is tracking for it, so a radius much past **24** will
  quietly stop growing.
- The medallion has no sound cue yet. A one-shot hum on entering detection, with a cooldown, is the
  intended shape; a looping hum is not.
- Places of power and other non-entity points of interest are not sensed. The cheap way in is to
  make them block entities and query the client chunk cache, or to make them marker entities and put
  them in `medallion_senses`; a radius block sweep is roughly 32,000 checks and is not an option.
  The renderer computes one intensity value, so a second source can feed the same output later.

---

## 2. Character Stats and Attributes

### 2.1 What the system is for

Every character stat - crit chance, life steal, potion duration, passive regeneration and the rest -
is a **real Minecraft attribute**, not a number the mod recomputes. A source declares what it
contributes, the game aggregates and caches, and everything that needs the value reads it back.

The stat a source contributes to, and by how much, lives **with that source**. Katakan Decoction's
crit chance is a row on the Katakan effect, not a branch inside a crit procedure. Adding a decoction
touches one element.

### 2.2 Why this is not per-tick recomputation

The mod used to keep every stat as a player variable, recomputed each tick by a dedicated procedure
that walked a chain of `if` checks. That model had three problems, and all three are structural
rather than fixable in place:

- **A per-stat procedure had to know about every source.** Ten decoctions meant ten branches in
  every stat they touched, and adding one meant editing several unrelated procedures.
- **Every write cost a full sync.** MCreator emits `markSyncDirty()` on *any* player-variable write,
  changed or not, and that ships the entire `PlayerVariables` blob to the client. A stat procedure
  writing one number per tick sent the whole blob twenty times a second, per player.
- **Two of them ran on `EntityTickEvent`**, so they did that for every entity in the world, not just
  players.

Attributes solve all three: vanilla owns the aggregation, caches the result, recalculates only when a
modifier is added or removed, and `setSyncable(true)` handles the client copy.

### 2.3 The pieces

**14 custom attributes**, registered in `WitchercraftModAttributes` from `attribute`-type elements:
crit chance, crit damage, additional damage, increased damage, life steal, oil damage, dodge chance,
potion duration, sign intensity, instant kill chance, reflect damage, toxicity overdose threshold,
passive health regeneration, passive stamina regeneration.

Health, movement speed and attack speed use **vanilla** `MAX_HEALTH`, `MOVEMENT_SPEED` and
`ATTACK_SPEED`. Nothing is registered for them; only their sources live here.

A stat's **base value is the attribute's `defaultValue`** - crit chance **5**, crit damage **115**,
toxicity overdose threshold **70**, everything else **0**. These are *contract*: there is no longer a
"base stats" procedure, and nothing seeds them at login or respawn.

**Four ways a modifier reaches an attribute**, in order of preference:

1. **Declarative, on the effect element.** A `modifiers` row on a potion effect. Zero code. Use this
   whenever an effect grants a flat bonus unconditionally - Katakan, Ekimmara, Leshen, Blizzard,
   Petri's Philter, Swallow, Troll, Tawny Owl, Thunderbolt, Full Moon, Sign Hold.
2. **`PerkModifiers`** (`~/Character Abilities`). Anything gated on a perk boolean, including
   perk-plus-condition like Anatomical Knowledge while a bow is held.
3. **`ConditionalModifiers`** (`~/Character Stats`). Anything gated on effect-plus-world-state -
   Thunderbolt during a storm, Water Hag at full health, Werewolf on a clear night, and the
   in-combat regeneration penalty. Also vanilla effects we do not own, like `LUCK`.
4. **Event-driven refresh.** Only for amounts that change at runtime: Wyvern's per-hit stack,
   Succubus's per-2s stack, Grave Hag's per-kill regeneration.

**The two watchers run every tick and write nothing.** `entity_add_modifier` self-guards with
`hasModifier`, and `removeModifier` returns early without setting the dirty flag when the modifier is
absent. So a stable perk loadout costs a map lookup per entry and no sync traffic at all.

The in-combat regeneration penalty is an `ADD_MULTIPLIED_TOTAL` modifier of **-0.5**, which is
exactly "half rate", rather than arithmetic in a procedure.

### 2.4 Traps, and why things are shaped the way they are

**Perk modifiers must be transient and re-applied by a watcher, never applied once at purchase.**
`ServerPlayer.restoreFrom` only calls `assignPermanentModifiers` when its `restoreAll` flag is set,
which is the dimension-change path - **not** death respawn. A modifier applied at purchase silently
disappears the first time the player dies. The watcher re-adds it within a tick and needs no
knowledge of which code paths clear attributes.

**A computed local cannot be read inside a `wait` block.** MCreator emits procedure-locals as
`double x = 0;` followed by reassignment, and `wait` compiles to `queueServerWork(n, () -> {...})`.
Java refuses to capture a reassigned local in a lambda. `DamageCalculator` wraps its damage
application in `wait(1)`, so anything it needs must come from an attribute, a player variable, or a
procedure dependency - never a local. This is why the oil bonus is an attribute modifier rather than
a term computed in the damage procedure.

**`RangedAttribute` clamps to min/max.** This is load-bearing for Thunderbolt: "crit chance becomes
100 during a storm" is expressed as **+100 clamped to a max of 100**, not as an assignment.

**Effect modifiers scale with amplifier** - `amount * (amplifier + 1)`. Every decoction here is
applied at level 0, so declared amounts carry through unchanged. Applying one at a higher level
would silently multiply it.

**A dynamic modifier needs an expiry hook.** `entity_add_modifier` only adds when absent; it cannot
update an amount, so changing one means remove-then-add at the point the value changes. Because
nothing removes it when the effect ends, each dynamic source also needs an `onExpired` procedure -
`WyvernDecoctionEnd`, `SuccubusDecoctionEnd`, `GraveHagDecoctionEnd`, `CorrectOilEnd`. Declaring
`onExpired` on the element is not enough on its own: the dispatch in `expireEffects` inside
`WitchercraftModMobEffects` is hand-written and needs the matching branch.

**A stack that only refreshes on hit goes stale.** Wyvern's counter used to reset on the *next* hit,
so leaving combat left the old value showing and applying. Every dynamic source now also has an
`onActiveTick` procedure that clears it once combat ends, guarded so it writes only on the
transition.

### 2.5 How to change things

**Retune a bonus.** Edit the number where the source declares it - the `modifiers` row on the effect,
or the `sync(...)` line in the relevant watcher. Nothing else needs touching.

**Change a base value.** Edit the attribute element's **default value**. That is the whole knob.

**Change a stat's ceiling.** Edit the attribute's min/max. Remember Thunderbolt relies on crit
chance's max being exactly **100**.

**Add a source to an existing stat.** Pick the lowest-numbered option in 2.3 that fits. If it is an
effect with an unconditional flat bonus, add a `modifiers` row and write no code.

**Add a whole new stat.** Create an `attribute` element, add it to `WitchercraftModAttributes` and to
the `addAttributes` event, add an `attribute.witchercraft.<name>` lang string, then add sources and
readers. Read it anywhere with `entity_get_attribute_value`, which already guards for entities that
do not have the attribute and returns 0.

**Read a stat.** `entity_get_attribute_value`. Do not reintroduce a mirror player variable for the
character sheet - the attributes are syncable, so the client already has the value.

### 2.6 Known limitations

- **The oil bonus is still routed through a status effect.** `CorrectOil` is a 10-tick flag that the
  oil-hit procedures set to signal "that hit was oil-matched". It works, but it depends on event
  ordering between separately registered handlers, and the bonus can leak onto a different target
  inside its window. `NOTES-oil-consolidation.txt` in the repo root has the full write-up and the
  intended fix.
- **`OilDamage` is read by `CorrectOilStart` at the moment the effect starts.** If oil damage changes
  while `CorrectOil` is already active, the modifier keeps the old value until the effect is
  re-applied. Harmless in practice, since the only source is the Luck effect.
- **Six element files use unicode escapes and a BOM** (`AltQuenCast`, `QuenActiveShieldAura`,
  `QuenActiveShieldTick`, `QuenBlock`, `SignCastHold`, `SignCastHoldCost`). They store `<` as a
  literal escape sequence rather than the character, so ordinary text search and replace will not
  match them. MCreator reads them fine; only tooling is affected.
- **Armour is no longer tracked.** The old `witchercraftArmor` variable was written and never read,
  and went with the base-stats procedure.

---

## 3. The GUI shell

### 3.1 What the system is for

WitcherCraft's own screens (perk tree/equip, alchemy, bestiary, and the rest) started as separate
MCreator container GUIs, each opened by its own keybind or procedure. That does not scale into the
witcher-game "one panel, many tabs" layout, and it makes every screen carry the full container +
menu + registration + button-message stack even when it only needs to draw.

The shell replaces that with one persistent, client-only **fullscreen** `Screen` that fills the
screen with a background image and draws a top navbar + the active **page** over it - the React
mental model: the shell stays mounted, clicking a tab is `setState(activeTabId)`, and no new screen
opens and no server round-trip happens for navigation. It is opened with **P** (default tab) or with
a per-page keybind (straight onto a tab), and lives in `client/gui/shell/`.

### 3.2 Why a plain Screen, not a container

Navigation is pure client state, so nothing about switching tabs needs the server. Making the shell a
plain `net.minecraft.client.gui.screens.Screen` (opened with `Minecraft.setScreen`, not `openMenu`)
drops the entire `Menu` + `WitchercraftModMenus` registration + open-procedure + button-message
stack that every MCreator GUI otherwise requires. A page that later needs **real item slots** brings
its own container at that point; the shell itself never needs one.

Two consequences to know:

1. **This generator has no `render(GuiGraphics)` to override.** The `GuiGraphics` class is renamed
   `GuiGraphicsExtractor` here, and `Screen`'s render pipeline
   (`extractRenderStateWithTooltipAndSubtitles`) calls `extractBackground(...)` then
   `extractRenderState(...)`, both taking a `GuiGraphicsExtractor`. The shell overrides those two,
   not `render`. Tooltips queued with `setTooltipForNextFrame` during `extractRenderState` are
   flushed by the pipeline's `extractDeferredElements`, so pages can set tooltips inline.
2. **`Screen.onClose()` is patched to `popGuiLayer()`** in this fork. Because the shell is opened with
   `setScreen`, its `onClose` calls `minecraft.setScreen(null)` directly instead of `super.onClose()`.

### 3.3 The pieces

All hand-written. Classes that must open in MCreator's code editor use locked code elements, while
small helpers may remain unowned. MCreator does not regenerate either form:

- **`GuiPage`** - the page interface. `render(g, x, y, w, h, mouseX, mouseY, partial)` plus design-space
  mouse click, release, drag, and scroll callbacks, then `keyPressed` / `pollTooltip` / `onShown` /
  `onClose`. The shell
  hands every page the **content region** - the whole area below the navbar - as a design-coords rect;
  the page fills it however it likes. All coords are design-canvas pixels (see 3.3a).
- **`WitcherGuiScreen`** - the shell. Renders against a **fixed virtual design canvas**
  (`DESIGN_W`x`DESIGN_H`, 16:9) scaled uniformly to fit the real screen (see 3.3a). `extractBackground`
  paints opaque black over the whole (gui-scaled) screen, then - inside the `pose().pushMatrix()` /
  `translate` / `scale` transform - blits the **active tab's own background** (`backgroundFor(activeTabId)`)
  to fill the design canvas; the black shows through as **letterbox bars** on non-16:9 screens.
  `extractRenderState` draws the active page in the
  content region, then the navbar (a **centred group of fixed-width tabs**, icon + label) **on top** so
  page content never covers the tabs, then pops the transform and renders the page's tooltip
  (`pollTooltip`) in screen space at the real cursor. Input is transformed screen->design before
  hit-testing. Drag deltas are divided by the same shell scale. A
  `WitcherGuiScreen(String pageId)` constructor opens straight onto a tab. The screen reports itself
  as pause-capable, which pauses an integrated single-player server but does not pause multiplayer.
- **`WitcherGuiLayout`** - the tool-generated data holder for shell **chrome only** (no page content).
  The **design canvas** (`DESIGN_W/H`), a `BG` fallback texture, navbar sizing (`NAV_Y/H`,
  `NAV_TAB_W`, `NAV_GAP`, `NAV_ICON`), a `CONTENT_MARGIN`, a `NAV[]` of tabs (each `pageId` + label key
  + **icon texture**; positions computed, no rect), and a `BACKGROUNDS[]` of `(pageId, texture)` pairs
  - `backgroundFor(pageId)` returns a page's own background, or `BG` if it has none. Helpers give the
  navbar tab rects and the **content region** (`contentX/Y/W/H()` = the whole area below the navbar).
  All coords are design-canvas pixels.
- **`WitcherGuiPages`** - the route table: `forId(pageId)` returns the handling page; an id with no
  bespoke class falls back to a cached `PlaceholderPage`. Bespoke pages register in the `CUSTOM` map.
- **`PlaceholderPage`** - a not-yet-built tab: centres a "&lt;Name&gt; - coming soon" in the content
  region. Every navbar page with no bespoke `GuiPage` uses it, until that page gets its own class +
  its own placer tool (the Skills pattern).
- **`PerkPage`** - the perk tree + equip grid, fit-scaled into the content region (see 3.5).
- **`MapPage`** - the first world-map milestone. It owns only client view state and renders a clipped
  diagnostic grid, player marker, cursor coordinates, and bottom-bar controls. Terrain, fog, POIs,
  and waypoints are absent. Wheel zoom preserves the world position under the cursor.
- **`MapLayout`** - tool-generated map viewport, bottom-bar, control, text, and color constants. Its
  editor is `tools/map-layout-creator.html`, which can import the checked-in Java values.
- **`WitcherGuiKeybind`** - a hand-written `@EventBusSubscriber(Dist.CLIENT)` that registers the **P**
  mapping plus one **per-page** mapping built from `NAV[]` (`key.witchercraft.open_shell.<pageId>`; the
  six known pages get conflict-free defaults - I / K / J / N / M / G - via a `DEFAULT_KEYS` map, any
  other tab stays unbound; all rebindable in Controls), and opens the shell on a client tick
  (`ClientTickEvent.Post`, game bus) onto the matching tab. No server message, no MCreator keybind
  element; auto-detected by FML like the HUD overlay classes. Adding a nav tab in the tool
  automatically gets it a keybind.

The navbar is driven by `NAV[]` (order + visuals, tool-edited) while behaviour comes from the page
registry (code). They are paired by `pageId`: a `NAV` entry whose id has no bespoke page shows a
`PlaceholderPage`; a page with no `NAV` entry simply never shows. That separation is deliberate -
"how the navbar looks" is data, "what a page does" is code.

### 3.3a Scaling: gui-scale independence + letterbox

The first cut laid the shell out directly in gui-scaled pixels, so its apparent size rode on the
player's GUI-scale setting - tiny at scale 1, overflowing the screen at scale 4. The fix is a **fixed
virtual design canvas**: everything is authored in `DESIGN_W`x`DESIGN_H` (16:9) coordinates, and the
shell computes `scale = min(width/DESIGN_W, height/DESIGN_H)` and centres the scaled canvas, applying
it with `pose().pushMatrix()` / `translate(offX, offY)` / `scale(s, s)`.

Two properties fall out:

- **GUI-scale independence.** `this.width` is already `physicalWidth / guiScale`, so
  `scale * guiScale = (this.width/DESIGN_W) * guiScale = physicalWidth/DESIGN_W` - constant. The UI
  fills the same fraction of the *physical* screen at every gui-scale setting.
- **No distortion + black letterbox.** The scale is uniform (single factor), and because the canvas
  is 16:9, fitting it to a non-16:9 screen leaves margins. `extractBackground` fills the whole screen
  opaque black *before* the transform, so those margins render as black bars; the background image is
  blitted to fill the 16:9 canvas and is therefore never stretched to the screen's aspect. (This is
  what the "add a black background instead of stretching" request asked for - no separate texture
  needed, just the black fill.)

Costs: input and tooltips need care. `mouseClicked` maps the screen cursor back to design space
(`(mouse - offset) / scale`) before hit-testing. Tooltips must render in **screen** space - a page
must not call `setTooltipForNextFrame` itself (it would be scaled and mispositioned); instead it
stashes the tooltip and the shell reads it via `GuiPage.pollTooltip()` after popping the transform and
renders it at the real cursor.

### 3.4 The tools: one per GUI, plus a navbar-only chrome editor

Each GUI is authored by its **own** placer tool - the Skills page has `equip-grid-placer.html`
(-> `PerkEquipLayout.java`) and `tree-node-placer.html` (-> `PerkTree.java`); a future Inventory page
would get its own. `tools/gui-layout-creator.html` is deliberately **not** one of those - it edits the
shell **chrome only**: the design canvas, the navbar (tab order / `pageId` / label / icon, add /
delete / reorder), and each tab's **own background texture** (a `background` field per tab; blank
falls back to the shared `BG`). It shows the content region as a greyed "reserved" box (a reminder
that pages are laid out by their own tools) and **regenerates `WitcherGuiLayout.java` live -> copy ->
paste over the file**. Seeded with the current layout, so `Reset` reproduces the checked-in file. It
also carries a collapsible **"How to add and connect a new GUI"** section - the step-by-step checklist
(add the tab, art, page class, register in `WitcherGuiPages`, optional dedicated placer tool, optional
keybind default, lang keys) for turning a placeholder tab into a real page, including the client-only
constraint from 3.9.

### 3.5 How the perk screen became a page

The old `PerkEquipGuiScreen` was an `AbstractContainerScreen` drawing through MCreator's
`extractLabels`. `PerkPage` is the same rendering + input, with two structural changes and nothing
else:

- **Fit-scaled into the content region.** The shell hands `PerkPage` the below-navbar content rect; it
  maps its fixed 360x230 `PerkEquipLayout` space onto that rect with a uniform, centred `pose()` scale
  (a nested transform inside the shell's design->screen one), so Skills fills the area below the
  navbar. Draw calls stay in perk-local coords (the ported body is nearly identical to the original)
  and still use the tool-generated `PerkEquipLayout` + `PerkTree` verbatim. **Both perk tools keep
  working unchanged** - their 360x230 canvas now simply *is* the content region, scaled up. Input and
  the hovered-node tooltip map the mouse back through the same fit scale.
- **The recompute moved.** The perk-effect recompute used to fire from `PerkEquipGuiMenu.removed()`
  when the container closed. With no container, it now runs server-side after every state-changing
  action inside `PerkEquipGuiButtonMessage.handleButtonAction` (`buttonID != 0` ->
  `RecomputeEquippedPerksProcedure.execute`). This is strictly more robust: the recompute follows the
  authoritative change, not the screen lifecycle.

`PerkEquipGuiButtonMessage` is self-registering (`@EventBusSubscriber` + `registerMessage`), so it
survived the retirement intact even though its old owning element is gone - the page keeps sending
the exact same packets for learn / place / remove / mutagen-cycle.

### 3.6 Retiring the old perk screen

Removed: `PerkEquipGuiScreen`, `PerkEquipGuiMenu`, `PerkEquipGuiOpenProcedure`,
`DebugRecomputePerksKeybindMessage`, their three `elements/*.mod.json`, their `mod_elements` entries
in `witchercraft.mcreator`, the `PERK_EQUIP_GUI` menu holder + screen registration, and the
`DEBUG_RECOMPUTE_PERKS_KEYBIND` (the old **P** "Open Perk Equip Screen" mapping, whose key the shell
now reuses). Kept and still registered: `PerkEquipGuiButtonMessage`, `PerkEquipLayout`, `PerkTree`,
`PerkRegistry`, `PerkEquipVars`, `PerkLearnedVars`, `RecomputeEquippedPerksProcedure`.

`PerkEquipGuiButtonMessage` is now an **orphaned** file - a real, compiled, self-registering class
that no MCreator element owns. That is intentional and matches the existing perk helper classes; it
just will not appear as an element in the MCreator browser.

### 3.6a Retiring the four old tab GUIs and the point-threshold tier gates (2026-08-29)

The four original per-branch perk screens (`CharacterAbilities{Combat,Alchemy,Signs,General}Gui` +
their `Menu`/`ButtonMessage`/`GuiOpenProcedure`/`GuiSkillPointsUsedProcedure`) were left alone during
the Phase 1/2 work (TDD 3.5-3.6 predate this) because the Pause Menu's "Skill Tree" button still
opened `CharacterAbilitiesGeneralGuiOpenProcedure` directly - a live, reachable path into the old
screen even after `PerkPage` fully replaced its function. That's fixed now: `PauseMenuGuiScreen`'s
Skill Tree button opens `WitcherGuiScreen("skills")` directly (client-only `Minecraft.setScreen`, no
network message - opening a screen isn't server-authoritative state, see 3.9), and
`PauseMenuGuiButtonMessage` no longer has a buttonID 5 branch. `PauseMenuGui` was marked
`locked_code: true` in `witchercraft.mcreator` so this hand-edit survives MCreator resaving the
project (its `Screen`/`ButtonMessage`/`Menu` java are all now MCreator-immune, same protection
`PerkEquipGui` had while it existed).

With that link cut, the old-GUI cluster (4 screens + their menus/button-messages/open-procedures/
skill-points-used-display-procedures, 15 elements) had zero remaining references anywhere in the mod
and was moved - not deleted - to **`trash/`** at the repo root (mirrors the original `src/`/`elements/`
paths, `git mv`'d so history is preserved). Same treatment for the **branch-specific tier-gate
procedures** (`CharacterAbilities{Combat,Alchemy,Signs}Tier{2,3}Procedure`, 6 elements) plus the two
already-dead top-level ones (`CharacterAbilitiesTier2/3Procedure`): these turned out to still be
load-bearing right up until this pass - every Tier2/3 perk's buy procedure (23 of the 45 perks) called
its branch's tier-check (`witchercraftPerks<Branch>SkillPointsUsed >= 3`) as a second gate ALONGSIDE
the new tree's prerequisites, contradicting the "tier gates replaced by prereqs" Phase 2 intent
(Section 8 of the rework plan). Removed by unwrapping the tier-check `controls_if` out of each of the
26 affected `elements/<Perk>Effect.mod.json` Blockly XML (not just the generated Java - see the
CLAUDE.md warning about MCreator regenerating from XML) and the matching Java. **This is a real balance
change**, made on explicit request: perks that used to require both a prerequisite AND a branch point
threshold now require only the prerequisite.

19 `mod_elements` entries removed from `witchercraft.mcreator`, 46 files moved to `trash/` total (27
Java + 19 `elements/*.mod.json`). The two files this touches that MCreator regenerates wholesale on
every build (`WitchercraftModScreens`, `WitchercraftModMenus`) had their now-dead registration lines
removed by hand to keep `compileJava` green today; since the registry no longer lists these elements,
MCreator's own next regeneration will produce the same (registration-free) result, so this isn't a
divergence risk the way the `en_us.json` gotcha (3.11) is.

### 3.7 How to change things

- **Rearrange the navbar / chrome** - open `tools/gui-layout-creator.html`, edit, copy
  `WitcherGuiLayout.java` over the file.
- **Add a real page** - implement `GuiPage` (its own class; fill the content region), register its
  singleton in `WitcherGuiPages.CUSTOM` under its `pageId`, add a `NAV[]` entry with that `pageId`,
  and give it its own placer tool if it needs authored geometry (the Skills pattern). Until then the
  `pageId` shows a `PlaceholderPage`.
- **Change an open key** - normal keybinds; rebind in Controls, or change the defaults in
  `WitcherGuiKeybind` (`OPEN_SHELL` / `DEFAULT_KEYS`).

### 3.8 Known limitations

- **Placeholder pages.** Skills, Meditation, and Map have real pages. Inventory, Alchemy, and Glossary
  remain `PlaceholderPage` "coming soon" tabs until each is built (own class + own tool). None wire real
  item slots yet - a page that needs live inventory slots must bring a container.
- **Existing standalone screens are only partly folded in.** The perk screen (Skills) and Meditation
  are now real shell pages; Meditation's old container GUI + in-world opener were deleted outright in
  the meditation-redesign slice 4, and the ESC pause-menu "Meditation" button now opens the shell tab
  client-side (like the Skills button). Alchemy, Glossary, etc. still remain their own container
  screens - they can be re-homed as pages later, or bridged (a tab that opens the old screen).
- **Placeholder art.** The navbar icons (`textures/gui/nav/<page>.png`) and every page's background
  (`textures/gui/shell/backgrounds/<page>.png`, falling back to `textures/gui/shell/background.png`)
  are generated placeholders meant to be replaced. Backgrounds are fit to the 16:9 design canvas
  (black letterbox on other screen aspects, no screen-aspect distortion), but a replacement image that
  is not itself 16:9 will be stretched to the canvas - author each at `DESIGN_W:DESIGN_H`.

### 3.9 Client-side only: what a page can and cannot do directly

The shell (and therefore every page in it) is **entirely client-side** - opened with
`Minecraft.setScreen`, no `AbstractContainerMenu`, no server round-trip for navigation. That has one
consequence that matters when writing a page: **a page must not call a state-mutating MCreator
procedure directly.**

Most generated procedures assume they are running with a `ServerLevel` / `ServerPlayer` in hand (they
check `!world.isClientSide()`, write to server-authoritative NBT/attachment data, or trigger further
server-side effects). Calling one straight from a page's `render`/`mouseClicked` on the client will, at
best, silently no-op behind that `isClientSide()` guard, and at worst read/write client-only state that
immediately desyncs from the server's copy - the exact bug class the old container-screen pattern
existed to avoid by keeping all mutation server-side.

Two categories, and where the line actually is:

- **Safe to call directly, client-side.** Pure **read-only** procedures that just report already-synced
  player state - e.g. `PerkPage` calls `CharacterAbilitiesSkillPointsAvailableProcedure.execute(entity)`
  every frame to print the points-remaining counter. These read attachment/NBT data the server already
  keeps synced to the client, take no branch on `isClientSide()`, and produce no server-side effect.
  Safe because they're idempotent lookups, not the same reasoning as "it happens not to crash."
- **Must go through a network message.** Anything that **changes** server-authoritative state - learning
  a perk, equipping/unequipping, moving an item, spending points, opening a follow-on menu - has to be
  sent to the server and applied there, the same as any other multiplayer-safe mutation. The pattern is
  `PerkEquipGuiButtonMessage`: a page encodes an action id (+ minimal args) into a `CustomPacketPayload`,
  `ClientPacketDistributor.sendToServer(...)` it, and a server-side `handleButtonAction` re-validates and
  applies it (never trust the client's view of "is this legal"). The client-side page does not wait for
  a reply - it either updates client-visible state optimistically (like `heldPerk`, which is genuinely
  client-only selection state) or simply re-reads the synced value next frame once the server's change
  propagates back.

Practically: when building a new page, first ask "does this button change anything the server tracks?"
If yes, it needs its own `CustomPacketPayload` + `@EventBusSubscriber` handler (copy
`PerkEquipGuiButtonMessage`'s shape) - do not reach for `SomeProcedure.execute(entity)` for it, even
though that "compiles and looks like it should work." If no (it's just displaying something), calling
the procedure directly is fine and is exactly what `PerkPage` already does.

### 3.10 Adding a new perk

A perk touches several files that must stay in sync. In order:

1. **Branch + ID** (`PerkRegistry.IDS` / `NAMES`). Pick the branch (Combat=red 100s, Alchemy=green
   200s, Signs=blue 300s, General=neutral 400s) and a free ID in that range. Append to both arrays at
   the same index - `IDS[i]` and `NAMES[i]` must line up. `NAMES[i]` is PascalCase (e.g.
   `"CripplingShot"`) - it is the single source the slug, icon folder, and lang keys all derive from
   (`slug = name.toLowerCase()`), so get it right here once rather than fixing it in four places later.
2. **Learned + equipped vars** - `witchercraftPerks<Name>` and `witchercraftEquippedPerk<X>`
   (player-persistent booleans), same pattern as every existing perk.
3. **Tree node** (`PerkTree.NODES`) - `new Node(id, x, y, prereqId...)`. Position it by hand or with
   `tools/tree-node-placer.html`. **Prereqs are an OR group**: zero = always learnable, one or more =
   learning ANY one of them unlocks this node (several parents are alternative unlock paths, not a
   requirement to learn every one) - enforced identically in `PerkPage.prereqsMet` (client, render
   state) and `PerkEquipGuiButtonMessage.prereqsMet` (server, the actual learn gate). Keep both in sync
   by hand if you ever change this rule; it is intentionally duplicated rather than shared, the same way
   the rest of the equip screen keeps client-render and server-authority logic separate.
4. **Icon** - a folder `assets/witchercraft/textures/screens/perk/<slug>/` with three 32x32 glyphs:
   `notlearned.png` (locked or available), `notequipped.png` (learned but not slotted), `equipped.png`
   (slotted). Bare glyph only - no baked-in frame or background; the coloured cell border and selection
   ring are drawn by `PerkPage` around whatever the icon is. Missing files fall back to a 3-letter text
   abbreviation, so a half-finished icon set degrades visibly rather than crashing.
5. **Buy procedure wiring** - a `<Name>Effect` procedure (sets the learned flag + spends the point,
   mirror an existing perk) and a `<Name>Show` procedure (visibility gate, only used by the 4 retired
   tab GUIs now - kept for parity, not load-bearing for the tree). Add a `case <id>:
   <Name>EffectProcedure.execute(entity); break;` line to the big switch in
   `PerkEquipGuiButtonMessage.tryLearn` - this is the one place that actually dispatches a tree
   right-click to the perk's buy logic.
6. **Apply the buff, gated on EQUIPPED, not learned** - a static flat stat goes in `PerkModifiers`
   (applied on menu close); a triggered/conditional effect goes in its own event procedure or
   `PerkModifiersConditional`, gated on `witchercraftEquippedPerk<Name>` (+ its condition, if any).
7. **Lang keys** - `perk.witchercraft.<slug>.name` and `perk.witchercraft.<slug>.desc` in
   `en_us.json` (see 3.11 - both are required, this is not optional polish). The tooltip is built purely
   from these two keys (`PerkRegistry.nameKey` / `descKey`) plus the branch tint colour - nothing else to
   wire for the hover tooltip to work. Description length is not constrained: `PerkPage.wrapToWidth`
   greedy word-wraps the resolved text to `TOOLTIP_WRAP_WIDTH` (200px) at tooltip render time, since
   Minecraft's `List<Component>` tooltip is one line per list entry with no wrapping of its own.
8. **GDD** - add the perk to Section 12's table and describe its effect/values in the relevant branch
   section, per the project's "keep the GDD current" rule.

### 3.11 Localization

**Every GUI-facing string must go through the lang file, no exceptions for "it's just a placeholder."**
Use `Component.translatable(key, args...)` and add the key to `en_us.json`, not `Component.literal("...")`
with the text baked into Java. This is cheap to do at write time and expensive to retrofit later (see the
perk tooltip rewrite in 3.10, which had to reverse-engineer descriptions out of four old screens' lang
files because they were never centralised against the new tree). That retrofit is now done: every perk
has real `perk.witchercraft.<slug>.name` and `.desc` keys (see below), so the tooltip no longer relies on
the Java fallback for its text.

**`en_us.json` is MCreator-managed the same way `witchercraft.mcreator` is: MCreator rewrites the WHOLE
file from its own element data on save, and it has no idea about lang keys added by hand outside its GUI -
they get silently dropped, not merged.** This bit TWICE in one session: every hand-added key vanished
between turns, reverting the file byte-for-byte to its pre-session git state. Confirmed NOT the cause:
`./gradlew compileJava` (tested directly - a marker key survived a compile untouched) and no lingering
MCreator/watcher process (checked via full process list, not just name-matching "mcreator" - only a normal
Gradle daemon was present, unrelated). That leaves the MCreator desktop app itself being opened directly
as the only remaining explanation. Treat `en_us.json` with the same caution as the pending-registry
workflow - if MCreator has been opened at all since the last check, don't trust a hand-edit to have
survived; re-verify the keys are still there before assuming a past pass is still in effect.

**Because of that risk, every `Component.translatable(key)` built from `PerkRegistry` data uses
`Component.translatableWithFallback(key, fallback)` instead**, where `fallback` is
`PerkRegistry.fallbackName(id)` (a spaced, non-localized display name, e.g. `"RazorFocus"` ->
`"Razor Focus"`) or a `"<Name>: Placeholder description."` string built from it. This is defence in depth,
not a substitute for the real lang keys: the real `perk.witchercraft.<slug>.name` / `.desc` keys now
exist (45 perks x 2), with `.desc` text ported from the retired ability-tab tooltips - a handful with no
honest match carry a `"Description coming soon."` placeholder, on the assumption they aren't wired to
anything yet. Crucially, those 90 keys were written to BOTH `en_us.json` AND the `language_map.en_us`
block inside `witchercraft.mcreator`, because that map - not `en_us.json` - is what MCreator regenerates
the resource file from. If the keys were only in `en_us.json`, MCreator would wipe them; being in the map
too is what makes them durable. The fallback stays as a safety net: if `en_us.json` is ever wiped and the
map ever loses them, the tooltip degrades to readable placeholder text instead of the raw dotted key
string, rather than failing silently or ugly.
Vanilla's plain `Component.translatable` (no fallback) still renders the raw key on a miss, so a missing
key is visible immediately in-game and cheap to catch in testing - the fallback variant is only worth the
extra argument where the string is genuinely GUI-facing every session, like this one.

**One tracked exception:** `PerkPage`'s points-available text (`CharacterAbilitiesSkillPointsAvailableProcedure`)
returns a pre-formatted, hardcoded-English `String`, not a `Component` - historically shared with the four
tab GUIs retired in 3.6a (they drew it the same raw way). Localizing it properly means either duplicating
its `witchercraftPlayerLevel - witchercraftPerksLearned` formula next to a translatable key, or rewriting
the procedure itself; neither was in scope for the tooltip/description pass that added this note, so it
stays a known gap rather than a silent one. If you touch that counter again, migrate it properly instead
of patching around it a second time.

**Dead lang keys get removed, not just left to rot.** When 3.6a retired the four tab GUIs, their
`gui.witchercraft.character_abilities_{combat,alchemy,signs,general}_gui.*` keys (65 entries - buttons,
labels, the pre-3.10 tooltip text) were deleted from `en_us.json` in the same pass, verified unreferenced
by grepping every `.java` file under `src/main` first. A lang key with no reader is just noise for anyone
searching the file by hand - don't leave it "just in case" once its owning code is confirmed gone.

**...but deleting them from `en_us.json` alone does NOT stick.** Those same 65 keys were found back in
`en_us.json` a later session, because the 3.6a pass only removed them from the generated resource file and
never touched `language_map.en_us` in `witchercraft.mcreator` - so the next MCreator save regenerated them
verbatim. They have now been removed from BOTH places. Rule of thumb: any lang-key add or delete must be
done in both `en_us.json` and `witchercraft.mcreator`'s `language_map`, or MCreator will silently undo it.

**Retired GUI display plumbing gets trashed with its GUI.** The old per-perk tab GUIs each had a
`<Perk>ShowProcedure` (returns whether a perk node should render as available) driving their visibility.
The shell's `PerkPage` reads `PerkLearnedVars` / `PerkEquipVars` directly instead, so all 45 per-perk
`*Show` procedures were dead (zero references, verified by grep) and were moved to `trash/` (Java +
`elements/*.mod.json`) with their `mod_elements` entries removed from `witchercraft.mcreator`. The two
non-perk `*Show` procedures - `MedallionShow` and `QuenHudShow` - are still read by `WitcherHud` and stay.
The live `<Perk>Effect` procedures (the actual gameplay) are untouched.

---

## 4. Cockatrice and hybrid mob movement

### 4.1 Ownership

`Cockatrice` is an unlocked MCreator living-entity element. MCreator owns its generated entity,
renderer, registration, spawn egg, attributes, AI task list, synchronized entity data, model choice,
texture choice, and animation assignments. Keep it unlocked. Health, damage, follow range, movement
speed, hitbox, AI goals, spawn settings, and similar tuning belong in the living-entity editor.

MCreator's `flyingMob` option stays disabled. That option replaces ground navigation with permanent
flying navigation and prevents the grounded half of this creature from behaving like a normal melee
mob.

The hand-maintained exception is the locked `CockatriceFlightTick` procedure. MCreator calls it from
the entity's tick-update trigger. It applies direct airborne steering while leaving the generated
ground navigation intact. The procedure can be opened through its locked procedure element in
MCreator and survives regeneration.

### 4.2 Flight contract

The controller uses directly steered flights rather than swapping the generated entity's protected navigation
or movement-control objects. On the ground, MCreator's melee and wander goals have full control. On
takeoff, the controller stops navigation, disables gravity, and steers with delta movement. Without a
combat target, it waits 10 to 30 seconds on the ground, checks for four blocks of overhead clearance,
then flies for a random 15 to 45 seconds. Idle waypoints stay within 16 blocks of the takeoff position,
so repeated direction changes do not carry the creature across the world. Combat takeoffs retain a
shorter six-second flight. A horizontal collision ends either type of flight. The controller then
restores gravity and lets the mob descend. Fall damage is disabled in the entity editor because
landing height varies with terrain.

The controller derives air speed from the normal `MOVEMENT_SPEED` attribute. This keeps the editor's
movement-speed field meaningful for both forms. Flight timing, lift, steering blend, and aerial attack
cooldown are constants in `CockatriceFlightTickProcedure`; changing those values requires editing the
locked procedure.

The server owns flight and attack state. Two synchronized MCreator entity-data entries carry the
small amount of state the renderer needs:

| Entry | Meaning |
|-------|---------|
| `Flying` | The creature is airborne or landing and should use an air animation. |
| `AttackAnimation` | `0` means none, `1` head bash, `2` grounded wing slam, and `3` aerial wing bash. |

Do not replace these with unsynchronized persistent data. Animation conditions run on the client and
would otherwise disagree with the server.

### 4.3 Animation resources

The editable Blockbench source is `models/blockbench/Cockatrice.bbmodel`. MCreator's imported Java
model is `models/mojmap-1.21.x/ModelCockatrice.java`, and all animation definitions remain together in
`models/animations/CockatriceAnimation.java`. A single animation class is intentional. MCreator can
reference each public `AnimationDefinition` field separately, so splitting the file adds maintenance
without changing behavior.

The six locked animation-condition procedures read the synchronized state. Walking uses MCreator's
walking-animation path and its normal walk progress. The attack animations last 15 ticks, matching the
0.75-second Blockbench timelines.

### 4.4 Current test boundary

Natural spawning is off. Test with the Cockatrice spawn egg until ground movement, clearance during
takeoff, landing on uneven terrain, collision around trees, and aerial attack reach have been checked
in game. Enabling biome spawning is an editor task after those checks, not a Java change.

---

## 5. World map terrain pipeline

### 5.1 Milestone 2A ownership

`WorldMapTerrainCapture` and `WorldMapTerrainTile` are locked code elements in the base package.
They own server observation, capture scheduling, terrain serialization, and exploration-mask
serialization. Do not move these collections into generated `PlayerVariables`.

Blockly remains the preferred place for ordinary gameplay triggers and effects. It cannot safely
express chunk-watch events, bounded queues, atomic files, checksums, or background I/O, so the 2A
core stays in locked Java. Later Blockly procedures may call narrow locked procedures when a map
action needs to connect to generated gameplay.

### 5.2 Legitimate exploration contract

The capture entry point is NeoForge `ChunkWatchEvent.Watch`. This event means the server is sending
an already loaded `LevelChunk` to a specific player. It supplies both facts the map needs. The player
may reveal that chunk, and the shared terrain tile may be captured. Do not replace it with a general
chunk-load event, which would record spawn chunks, tickets, and automation that no player explored.

The queue stores only dimension keys and packed chunk positions. Processing calls
`ServerChunkCache.getChunkNow`. A null result increments `skipped_unloaded` and ends that job. Never
replace this with `getChunk`, a future request, or a ticket because those paths can load or generate
terrain.

Only the Overworld enters the pipeline in this milestone. Storage paths and records retain a
dimension key so later dimensions can use separate data without changing the formats.

### 5.3 Terrain tile format

Each `c.<chunkX>.<chunkZ>.wct` file represents one 16 by 16 chunk at one fixed sample per horizontal
block. Files live under the dimension folder at
`data/witchercraft_world_map/terrain/r.<regionX>.<regionZ>/`.

Version 4 is big-endian and contains:

1. Magic `WCTM`, format version, chunk coordinates, sample count, and capture game time.
2. A bounded palette of serialized block states. Entry zero means that no block state is available.
3. Exactly 256 records in local Z-major order. Each record stores ground height, map-color ID, tint
   kind, and resolved tint; foliage height, color, tint kind, and tint; plus water-surface height and
   resolved water tint; plus decoration kind, map color, tint kind, and resolved tint; plus block-state
   palette indices for the ground, foliage, and decoration layers.
4. A CRC32 of every preceding byte.

The signed-short sentinel `Short.MIN_VALUE` means that a foliage or water layer is absent. Capture
starts at `WORLD_SURFACE` and scans downward within the already loaded column. It ignores non-colliding
decorative plants. Blocks in the standard leaves or logs tags enter the foliage layer. Water records
its highest surface and the scan continues to the underwater ground. The first remaining block with a
non-empty collision shape becomes ground. This keeps grass tufts and tree canopies out of ground height
while retaining foliage height as a separate input to visible-surface shading.

Decoration kind `0` means absent and `1` means a visible non-colliding block such as grass, a flower,
fern, mushroom, petals, or a tagged modded plant. Capture stores decorations regardless of the client
display setting. The full-resolution renderer shows configured decorations at all zooms.

The reader accepts versions one through four. It supplies absent arrays and an empty block-state palette
for fields that did not exist in an older format. Exploration files remain version one and are not reset.
The format change does not alter storage paths, authorization, or the one-chunk packet and persistence unit.
After a captured tile reaches durable storage, the server sends that tile to players currently tracking
the chunk. This replaces an old-format fallback that the client may have requested before recapture
finished, without generating or force-loading terrain.

Tint kind `0` means no biome tint, `1` grass, `2` foliage, and `3` water. Capturing the resolved tint
keeps rendering independent of loaded chunks. The tile constructor rejects any array that is not
exactly 256 samples. The reader rejects unknown versions, bad coordinates, wrong sample counts,
oversized files, trailing bytes, and checksum failures.

Writes use a unique temporary sibling and then atomic replacement when the filesystem supports it.
A failed or interrupted write leaves the previous complete tile intact. Missing or unreadable tiles
mean absent terrain and must never affect markers or exploration data.

### 5.4 Exploration-mask format

Each player has a separate `<uuid>.wce` file under
`data/witchercraft_world_map/exploration/` in each dimension folder. Version 1 contains magic
`WCEX`, the player UUID, a bounded count, sorted packed chunk positions, and CRC32. Runtime growth and
decoding share a hard safety ceiling of 2,000,000 chunks per player per dimension. Bad files load as
an empty mask and produce a warning.

Masks become dirty only when a new chunk is added. The server snapshots dirty masks every 200 ticks
and writes them on the I/O executor. A mask stays logically dirty until its write succeeds. Server
shutdown waits for outstanding writes and retries a failed dirty mask once.

### 5.5 Work queue and diagnostics

The prototype processes one terrain tile per server tick. This is a measurement starting point, not
a final configuration default. Watched chunks nearer the observing player sort ahead of distant
ones. Packed positions are deduplicated, and the hard pending ceiling is 32,768 captures.

Every 1,200 ticks the server logs total queued and captured tiles, current pending work, unloaded
skips, queue-full drops, and storage failures. Profile this in the development client before changing
the per-tick budget or adding a refresh cooldown. Tile and exploration writes run on Minecraft's I/O
executor. Column sampling remains on the server thread because chunk state is not safe to read from
the background writer.

### 5.6 Milestone 2B networking

`WorldMapTileRequestMessage` carries at most 64 packed chunk positions. Its decoder rejects larger
batches before allocation. `WorldMapTerrainCapture.requestTiles` accepts requests only from a player
in the Overworld, applies a server-side ceiling of 128 requested tiles per 20 ticks, and checks every
position against that player's exploration mask. Client coordinates never grant visibility.

Authorized tile reads run on Minecraft's I/O executor. The server sends each successful read as one
bounded `WorldMapTileDataMessage` after returning to the server thread. Its size varies with the v4
block-state palette. Missing or corrupt files
produce no tile response. The client retries a missing visible tile after five seconds, which also
covers the short race between watching a new chunk and its first atomic terrain write.

`WorldMapClientTileCache` requests the visible rectangle plus a one-chunk margin. When more than 64
tiles are missing, it selects the nearest 64. This keeps minimum zoom from creating a huge packet.
One view processes at most two batches, matching the server limit of 128 requested tiles per 20
ticks.

Each request has a positive ID. After all authorized disk reads finish and all available tile
messages enter the connection, the server sends `WorldMapTileRequestCompleteMessage` with that ID.
The map page reports itself as non-pausing while a view is dirty or a request is in flight. It sends
the next batch directly from the completion handler, without relying on player ticks that stop while
single-player is paused. After two batches, or when no candidates remain, the page becomes
pause-capable again. Changing the visible chunk bounds through pan, zoom, or center starts another
short request chain. Rejected rate-limited requests still receive completion, so malformed timing
cannot leave single-player permanently unpaused.

### 5.7 Milestone 2C client renderer

`WorldMapClientTileCache` keeps received messages as decoded CPU samples. It does not create a GPU
texture per chunk. The decoded cache holds at most 2,048 chunks in access order. The chunk tile remains
the storage, authorization, and network unit. `WorldMapTileDataMessage` carries the version-four layered
samples and block-state palette for one authorized chunk.

The client groups samples on a 16 by 16-chunk grid using floor division, including at negative
coordinates. Each region covers 256 by 256 blocks and tracks which of its 256 authorized chunk samples
are present. Missing samples produce transparent pixels. `MapPage` fills the viewport with fog before
drawing terrain, so transparency never exposes neighboring or unauthorized data.

The renderer builds each region image lazily at one block per source pixel. Every zoom level uses that
full-resolution image, so zooming cannot replace structures and shorelines with box-averaged pixels.
A region revision invalidates the image
when one of its chunks changes. A tile arriving on a region's east or south dependency edge also dirties
the neighboring region that reads its height for west or north shading.

Version 4 terrain tiles add a palette of serialized block states plus palette indices for ground,
foliage, and decoration samples. The server still stores `MapColor` as a fallback. On the client, each
serialized state resolves through the block registry and active baked-model set. The resolver chooses
the largest upward-facing quad, including uncullable model quads, and falls back to the model's particle
sprite. It calculates an alpha-weighted average of the sprite pixels and caches that color by serialized
state. Tinted texture colors are multiplied by the captured biome tint at the configured strength, which
matches Minecraft's material-tint relationship instead of replacing the texture color with the tint.
A missing state, model, or readable sprite uses the stored `MapColor`. `MapColor.NONE` remains transparent.
The client clears block colors and invalidates region images after a resource reload, so changing a
resource pack updates the map without recapturing server terrain.

Water uses a muted stored biome color at 75 percent opacity over the underwater ground and darkens
gradually with depth. Foliage blends over ground at 75 percent opacity.

Hillshade compares each full-resolution visible height with the northern and northwestern samples. Open
ground uses ground height, rendered foliage uses foliage height, and water uses water-surface height.
Decorations keep ground height. The renderer assigns each signed difference to one of three slope bands.
The northern band carries twice the brightness weight
of the northwestern band, which keeps the light direction readable while preserving diagonal terrain
steps. Slope sensitivity controls which band receives a height difference, while hillshade strength
controls the resulting brightness contrast. The final factor stays bounded between 0.65 and 1.35 before
terrain brightness applies. Foliage uses all three slope bands. A lower non-water, non-foliage pixel also
checks higher foliage immediately to its west, north, and northwest. This adds a one-pixel contact shadow
east, south, and southeast of raised foliage, matching the northwest light direction. The shadow reaches
its configured maximum at a twelve-block height difference and cannot lower the pre-brightness factor
below 0.5. If the current, northern, or northwestern slope sample is foliage, canopy relief multiplies the
ordinary hillshade contrast before its bounds apply. Water reduces the ordinary slope-shading strength and
receives no canopy contact shadow.
Available samples cross chunk and region boundaries. A missing neighbor uses the center height and causes
no request or inferred exploration. Tile changes on a region's east or south edge invalidate the dependent
neighbor, and a southeast-corner change also invalidates the diagonal region that reads it as northwest.

`WorldMapClientConfig` registers a NeoForge client configuration with terrain brightness, biome-color
strength, hillshade strength, hillshade slope sensitivity, canopy relief strength, canopy shadow strength,
and a decoration visibility toggle. A setting change revises cached region images but does not alter
server capture or saved terrain. The defaults are 1.0 brightness, 0.9 biome color strength, 0.75 hillshade
strength, 1.0 slope sensitivity, 1.35 canopy relief strength, 0.35 canopy shadow strength, and decorations
enabled.

MCreator's generated `WitchercraftMod` constructor calls `WorldMapClientConfig.register()` only from its
preserved user-code block. The locked config class resolves the active mod container by mod ID. Do not add
a `ModContainer` parameter to the generated constructor because MCreator removes that signature change
during regeneration while preserving the user-code body.

All region screen edges come from one origin, `viewportCenter - mapCenter * zoom`, and the edge's world
coordinate. Adjacent regions therefore calculate the same shared pixel edge. The 26.1.2 long `blit`
overload receives independent destination, source, and texture dimensions, so each selected region image
uses UV coordinates zero through one exactly once at every supported zoom.

The decoded cache and the 64-entry GPU region cache have separate limits. The visible viewport may
temporarily exceed the region limit because a region referenced by the current GUI extraction cannot be
evicted. Replaced and evicted textures receive versioned identifiers and enter a retirement queue. The
client releases them during a later GUI frame, after Minecraft has executed the deferred draw commands
that can still reference their texture views. A debug diagnostic emitted every
five seconds while rendering reports decoded and GPU cache estimates, source level, region rebuilds,
uploads, draw calls, and average map-render time. Region size and cache limits remain profiling constants,
not persistent-format fields.

Terrain tile version 4 is the write format. The reader also accepts versions 1 through 3, initializes
layers and block-state palettes that did not exist in those formats as absent, and verifies their original CRC. This preserves
previously explored terrain after a renderer upgrade without loading or regenerating old chunks.

Milestone 2C is complete. Milestone 2D visual validation is in progress. The full-resolution renderer
removed the damaging box-averaged LOD levels. Milestone 2D.1 replaces the flat centered gradient with
discrete north and northwest slope shading without reintroducing spatial color blur. Milestone 2D.2 uses
the visible ground, foliage, or water height. Raised foliage also casts a height-scaled directional contact
shadow on adjacent lower ground. A separate canopy-relief multiplier controls contrast between neighboring
foliage heights. Later Milestone 2D sections will evaluate texture-alpha compositing, water, and final color
tuning.
Separate stored lighting remains a possible later refinement. Milestone 2D must pass representative
in-game visual, restart, and performance checks before waypoint work begins.

`MapPage` fills the viewport with its fog color first, then draws cached authorized tiles, the player
marker, and the existing controls. A missing tile therefore reveals nothing. The cache is scoped to
the current client connection so terrain from one server cannot appear while connected to another.
