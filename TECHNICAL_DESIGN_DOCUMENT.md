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
  `WitcherGuiScreen(String pageId)` constructor opens straight onto a tab. `isPauseScreen()` always
  returns false. Every page therefore has the same live-world behavior in single-player and multiplayer.
- **`WitcherGuiDamageInterrupt`** - the server listens to `LivingDamageEvent.Post`, after reductions
  and health application. Positive health damage closes the shell only when `DamageSource.getEntity()`
  is an `Enemy` or a player. This includes owned projectiles while excluding fire, lava, falls,
  drowning, poison ticks, and other environmental sources. A clientbound packet calls the shell's
  `onClose()` instead of clearing the screen directly, so an active meditation sends its normal cancel
  request and restores the hidden HUD.
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
- **`MapPage`** - the assembled world-map page. It owns client view and overlay state, renders streamed
  terrain plus the player, waypoint, temporary-target, and POI marker layers, and exposes the bottom-bar
  controls, waypoint manager, marker interactions, hover cards, and POI filters. Wheel zoom preserves the
  world position under the cursor; authoritative waypoint and POI state remains outside the page.
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
- **Placeholder art.** The navbar icons (`textures/screens/nav_<page>.png`) and every page's background
  (`textures/screens/shell_background_<page>.png`, falling back to `textures/screens/shell_background.png`)
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
4. **Icon** - three flat, MCreator-visible 32x32 glyphs under `assets/witchercraft/textures/screens/`:
   `<slug>_notlearned.png` (locked or available), `<slug>_notequipped.png` (learned but not slotted), `<slug>_equipped.png`
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

### 4.5 Stateful animated mobs

A mob with stance, transition, attack, idle, and locomotion animations needs one server-owned state
machine. Animation conditions read that state. They must not infer combat or stance independently on
the client, because separate guesses allow incompatible animations to run together.

#### State ownership and persistence

Store gameplay state in MCreator entity-data fields when procedures and rendering both need it. Add
each field to all four generated entity locations: accessor declaration, `defineSynchedData`, save, and
load. The server changes these fields. Entity data then synchronizes the result to clients.

A two-stance mob normally needs:

- A persistent stance flag, such as `SpikesOut`.
- A transition selector where zero means no transition and other values identify the direction.
- A transition timer measured in ticks.
- An attack selector and timer.
- Any edge-detection state needed to turn a short server event into a complete client animation.

The Alghoul example uses `CombatTicks`, `SpikesOut`, `StanceAnimation`, `StanceTicks`,
`AttackAnimation`, `AttackTicks`, and `WasSwinging`. These fields live in the `Alghoul` MCreator entity
definition rather than a separate capability.

#### Server state machine

Use an ordinary Blockly procedure for discrete gameplay effects. Use a locked procedure only when a
transition requires tick-by-tick state that Blockly cannot express safely. Keep its element definition
as a minimal `no_ext_trigger` stub and set `locked_code: true` in `witchercraft.mcreator`.

The server tick should follow one ordering rule:

1. Apply forced suppression or control effects.
2. Start or cancel stance transitions.
3. Advance transition timers and commit the persistent stance at the transition boundary.
4. Detect attack edges and advance attack timers.

Forced suppression wins over combat. In the current Alghoul example, Night Vision is a temporary Axii
substitute. It resets the combat delay, blocks deployment, disables retaliation immediately, and starts
the retract animation when spikes are active. When the effect expires during combat, the normal combat
delay starts again.

The Alghoul waits 50 ticks after acquiring a living target, plays its 15-tick deployment animation,
then sets `SpikesOut`. Losing the target starts the 15-tick retraction and clears `SpikesOut` at once.
This prevents thorns damage while the spikes are visually retracting. A new melee swing starts a
20-tick slap animation only when no stance transition is active.

#### Animation arbitration

Animation conditions must be mutually exclusive. Use this priority order:

1. Stance transition.
2. Attack.
3. Walk for the active stance.
4. Idle for the active stance.

Transition conditions read only the transition selector. Attack conditions require no transition and
select the variant for the persistent stance. Walk and idle conditions require no transition and no
attack. Movement separates walk from idle. This arrangement lets a generated renderer apply all
registered animation states without blending contradictory poses.

MCreator walking animations use `applyWalk`, so travel distance drives their clock and movement speed
drives their weight. Choose an amplitude that reaches full weight at the entity's normal movement
speed. Non-locomotion animations use `AnimationState` and elapsed ticks.

The Alghoul maps its eight definitions this way:

- `spikes_on` and `spikes_off` handle the two transition directions.
- `slap_spikes` and `slap_no_spikes` handle attacks.
- `idle_spikes` and `idle_no_spikes` handle stationary poses.
- `walk_spikes` and `walk_no_spikes` handle locomotion.

#### Gameplay hooks

Keep damage reactions in Blockly when the blocks can express them. The Alghoul's `AlghoulRetaliate`
procedure runs from the entity hurt trigger. It checks `SpikesOut` and deals 2 thorns damage to a living
attacker. The synchronized stance flag is the gameplay authority. Animation progress is never used to
decide whether retaliation applies.

Effects that stand in for unfinished systems should have one explicit integration point. Night Vision
currently supplies the Alghoul's spike-suppression signal. Replace that one check with the real Axii
effect later. Do not duplicate Axii handling in animation conditions or the retaliation procedure.

#### Blockbench coordinate conversion

Model and animation exporters may disagree about coordinate conversion. Minecraft animation channels
add values to an already converted `ModelPart` pose. Raw animation values can therefore bend a child
chain in a direction that never appears in Blockbench while still compiling without warnings.

For the affected modded-entity export, convert values as follows:

- Rotation: Blockbench `(x, y, z)` becomes Java `degreeVec(-x, -y, z)`.
- Position: Blockbench `(x, y, z)` becomes Java `posVec(-x, y, z)`. `posVec` negates Y internally.
- Scale: copy `(x, y, z)` unchanged.
- Keep timestamps, interpolation, animation length, and loop state unchanged.

Do not apply this rule to another model without checking it. Some exporter versions and hand-edited
files already contain converted values. A second conversion breaks them in the opposite direction.
For example, the Alghoul's six stance, attack, and idle definitions were already correct, while both
walk definitions retained raw X and Y rotation signs.

Use a fixed-frame test when the cause is unclear. Reset every model part, apply one animation at a known
millisecond with weight `1.0`, and compare an asymmetric pose from the same camera side in Blockbench.
If the fixed pose differs, inspect exported transforms and hierarchy. If it matches, inspect animation
gating, clock choice, and amplitude.

#### MCreator source ownership

Permanent model imports belong under `models/`, while generated copies belong under `src/main/java`.
For the Alghoul these are:

- `models/mojmap-1.21.x/ModelAlghoul.java`
- `models/animations/AlghoulAnimation.java`
- `src/main/java/net/redboltmedia/witchercraft/client/model/ModelAlghoul.java`
- `src/main/java/net/redboltmedia/witchercraft/client/model/animations/AlghoulAnimation.java`

Permanent import fragments must not contain package declarations, imports, or `super(root)`. MCreator
adds them during generation. Keep permanent and generated animation definitions synchronized, but fix
the permanent file first. MCreator may delete or replace generated Java during workspace regeneration.

The state-machine procedure follows the locked-code workflow. Animation conditions and damage logic
remain Blockly-generated where possible. The Alghoul's idle checks and combat tick stay locked because
their movement and timed-state tests do not have reliable Blockly equivalents in this workspace.

Natural spawning remains off until collision, attack reach, both stance transitions, suppression, and
retaliation have passed in-game tests.

A simpler derivative can reuse the same pattern without copying the stance machine. The Ghoul derives
its geometry and corrected no-spike animations from the Alghoul, removes every visible spike cube, and
keeps only walk, idle, and slap. Its sole locked tick procedure converts the short melee swing edge into
a 20-tick attack animation. Movement and combat AI remain ordinary MCreator entity settings.

---

## 5. World map system

### 5.1 System ownership

`WorldMapTerrainCapture` and `WorldMapTerrainTile` are locked code elements in the base package.
They own server observation, capture scheduling, terrain serialization, and exploration-mask
serialization. Do not move these collections into generated `PlayerVariables`.

Blockly remains the preferred place for ordinary gameplay triggers and effects. It cannot safely
express chunk-watch events, bounded queues, atomic files, checksums, or background I/O, so the map
infrastructure stays in locked Java. Blockly procedures may call narrow locked procedures when a map
action needs to connect to generated gameplay.

### 5.2 Legitimate exploration contract

The capture entry point is NeoForge `ChunkWatchEvent.Watch`. This event means the server is sending
an already loaded `LevelChunk` to a specific player. It supplies both facts the map needs. The player
may reveal that chunk, and the shared terrain tile may be captured. Do not replace it with a general
chunk-load event, which would record spawn chunks, tickets, and automation that no player explored.

The queue stores dimension keys, packed chunk positions, priority, and sequence. Processing calls
`ServerChunkCache.getChunkNow`. A null result increments `skipped_unloaded` and ends that job. Never
replace this with `getChunk`, a future request, or a ticket because those paths can load or generate
terrain.

Only the Overworld currently enters the terrain-capture pipeline. Storage paths and records retain a
dimension key, allowing another dimension to use isolated data without changing the formats.

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
the chunk. This also replaces any compatible older tile the client displayed while recapture was pending,
without generating or force-loading terrain.

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

Each server tick processes at least one and at most eight terrain tiles. After the first tile, processing
stops when the tick has spent 3 milliseconds in capture. Watched chunks nearer the observing player sort
ahead of distant ones, and newer chunks win equal-distance ties so movement does not leave the latest edge
behind. Request-triggered repairs sort ahead of ordinary captures. Packed positions are deduplicated, and
the hard pending ceiling is 32,768 captures.

Every 1,200 ticks the server logs total queued and captured tiles, current pending work, unloaded
skips, repair requests and completions, time-budget stops, queue-full drops, and storage failures. Profile this in the development client before changing
the per-tick budget or adding a refresh cooldown. Tile and exploration writes run on Minecraft's I/O
executor. Column sampling remains on the server thread because chunk state is not safe to read from
the background writer.

### 5.6 Terrain authorization and networking

`WorldMapTileRequestMessage` carries at most 64 packed chunk positions. Its decoder rejects larger
batches before allocation. `WorldMapTerrainCapture.requestTiles` accepts requests only from a player
in the Overworld, applies a server-side ceiling of 128 requested tiles per 20 ticks, and checks every
position against that player's exploration mask. Client coordinates never grant visibility.

Authorized tile reads run on Minecraft's I/O executor. The server sends each successful read as one
bounded `WorldMapTileDataMessage` after returning to the server thread. Its size varies with the v4
block-state palette. A missing or corrupt authorized tile queues a high-priority repair only when
`getChunkNow` confirms that the chunk is already loaded. A successful repair writes the ordinary shared
tile, updates tracking players, and sends the repaired tile directly to players waiting on that request.
It never creates a chunk ticket or loads terrain. Unauthorized files produce no tile response. An accepted
completion causes the client to suppress those requested positions for the rest of the connection. A
later live tile update removes that suppression, which covers the race between watching a new chunk and
its first atomic write.

`WorldMapClientTileCache` requests the visible rectangle plus a one-chunk margin. When more than 64
tiles are missing, it selects the nearest 64. It paces requests at one batch per 500 milliseconds,
matching the server limit of 128 requested tiles per 20 ticks without increasing packet size. The
chain continues until every candidate in the current view has been returned or checked as absent.

Each request has a positive ID. After all authorized disk reads finish and all available tile
messages enter the connection, the server sends `WorldMapTileRequestCompleteMessage` with that ID and
an accepted flag. The flag is false when rate limiting rejects the request. The client retries those
positions after its pacing interval instead of recording them as absent. The whole Witcher shell is
non-pausing, so the integrated server keeps running throughout the visible request chain. Changing the
visible chunk bounds through pan, zoom, or center reprioritizes the remaining candidates around the new center.

Disk-cache discovery retains a 250-millisecond throttle only while the same page bounds remain visible.
Crossing a leaf or overview-page boundary bypasses that throttle, so a fast pan cannot spend the rest of
the old scan interval showing the map background. The I/O submission order is visible pages nearest the
center, then the prefetched ring nearest the center. Existing in-flight reads are not cancelled.

### 5.7 Client terrain renderer

`WorldMapClientTileCache` keeps received messages as decoded CPU samples. It does not create a GPU
texture per chunk. The decoded cache targets 4,096 chunks in access order, but it retains all chunks
inside the current view and its 256-block prefetch band until that view finishes. The chunk tile remains
the storage, authorization, and network unit. `WorldMapTileDataMessage` carries the version-four layered
samples and block-state palette for one authorized chunk.

The client groups samples into independently replaceable 4 by 4-chunk leaf images using floor division,
including at negative coordinates. Each leaf covers 64 by 64 blocks and tracks which of its 16 authorized
chunk samples are present. Missing samples produce transparent pixels over the black map background. `MapPage` fills the
viewport before drawing leaves, so a missing or not-yet-built leaf never exposes neighboring or
unauthorized data.

The client thread resolves each received tile's 256 display samples because baked models and
resource-pack sprites are not safe to access from a worker. A single daemon worker builds leaf pixels and
hillshade from immutable tile snapshots. GUI rendering never runs the 4,096-pixel leaf loop. At most eight
builds may be pending, and visible leaves nearer the view center enter the queue first. A leaf revision
combines multiple arriving chunks into one pending rebuild. If newer terrain arrives during a build, the
client publishes the completed authorized snapshot instead of leaving the leaf black, then schedules one
combined follow-up. An older GPU texture remains visible until its replacement is ready, and the render
thread registers at most four completed build textures per frame.

At 0.65x zoom and below, the renderer selects a persistent overview tier. Each overview covers 256 by
256 world blocks in a 256 by 256 texture, retaining one texture pixel per world block. It returns to
64-block leaves above 0.85x. The gap between thresholds supplies hysteresis
during animated zoom. At overview scale the viewport needs tens of draw calls instead of hundreds.
An existing overview renders behind detailed leaves during zoom-in, and existing leaves fill a missing
overview until its replacement is ready. Overview builds use the same worker, revision, atomic PNG,
coverage-sidecar, resource-pack key, and publish-then-follow-up rules as leaf builds.
A tile arriving on a leaf's east or south dependency edge also dirties the
neighboring leaf that reads its height for west or north shading.

Version 4 terrain tiles add a palette of serialized block states plus palette indices for ground,
foliage, and decoration samples. The server still stores `MapColor` as a fallback. On the client, each
serialized state resolves through the block registry and active baked-model set. The resolver chooses
the largest upward-facing quad, including uncullable model quads, and falls back to the model's particle
sprite. If a model has no upward-facing quad, the resolver uses its largest uncullable quad before the
particle fallback. This covers crossed flower and grass models. It calculates an alpha-weighted average of the sprite pixels and caches that color by serialized
state. The cached texture sample also stores alpha coverage as the sum of accepted pixel alpha divided by
the sprite's fully opaque alpha, plus the selected quad's model tint flag. The renderer multiplies a
texture color by the captured biome tint only when `BakedQuad.MaterialInfo.isTinted()` is true. This keeps
untinted model faces at their resource-pack color. A tint flag alone is insufficient for flowers because
some crossed flower models expose a tinted material even though their petals must retain authored colors.
The resolver therefore suppresses biome tint for every block in `BlockTags.FLOWERS`. This covers vanilla
flowers and modded flowers that use the standard tag while allowing tintable grass, foliage, and other
plant models to use biome color. Flower color extraction rejects pixels whose green channel exceeds both
other channels by more than 15 percent, then ranks the remaining pixels by brightness and saturation and
alpha-averages the highest-scoring quarter. This selects petal colors instead of stems without hardcoding
flower IDs or colors. Tall flowers use the captured upper-half state, so sunflower petals participate in
the same rule. Alpha coverage still uses the whole sprite rather than only the selected accent pixels. A
particle-only fallback retains no model tint flag.
A missing state, model, or readable sprite uses the stored `MapColor`. `MapColor.NONE` remains transparent.
The client clears block colors and invalidates region images after a resource reload, so changing a
resource pack updates the map without recapturing server terrain.

Water alpha-averages the active resource pack's still-water texture, multiplies it by the captured biome
water tint, then blends 60 percent of that tint back into the result. Its opacity follows
`0.50 + 0.30 * (1 - exp(-depth / 8))`, starting near 54 percent at one block and approaching 80 percent
in deep water. The same depth factor darkens the finished composite
by up to 18 percent. This keeps shallow seabeds visible while separating deep water without generated
noise. Water uses 35 percent of ordinary terrain hillshade strength. Resource reload clears the cached
water sample together with block texture colors and invalidates region images. If the water sprite cannot
resolve, the renderer retains the previous muted-biome-color fallback. Foliage and decorations blend over ground using texture-derived alpha coverage
multiplied by their separate client opacity scales and clamped to full opacity. Decoration coverage has a
65 percent minimum before its scale applies. This keeps sparse flower and grass sprites visible when one
block column becomes one map pixel. A zero decoration scale still hides the layer. Ground stays opaque
because the tile does not store a general deeper layer beneath collidable transparent blocks. Legacy tiles
and states whose texture cannot resolve use 75 percent opacity for foliage or decoration compositing.

Hillshade compares each full-resolution visible height with the northern and northwestern samples. Open
ground uses ground height, rendered foliage uses foliage height, and water uses water-surface height.
Decorations keep ground height. The renderer assigns each signed difference to one of three slope bands.
The northern band carries twice the brightness weight
of the northwestern band, which keeps the light direction readable while preserving diagonal terrain
steps. Slope sensitivity controls which band receives a height difference, while hillshade strength
controls the resulting brightness contrast. Water uses 35 percent of that strength. The final factor stays bounded between 0.65 and 1.35 before
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
foliage opacity scale, decoration opacity scale, marker scale, zoom sensitivity, view restoration, discovery
action-bar visibility, initial filter visibility, and a decoration visibility toggle. Discovery sound is fixed
and has no configurable selector or toggle. A terrain setting change revises cached region images, while marker
and interaction settings apply directly on their next use. Neither path alters server capture or saved terrain. The defaults are 1.0
brightness, 0.9 biome color strength, 0.75 hillshade strength, 1.0 slope sensitivity, 1.35 canopy relief
strength, 0.35 canopy shadow strength, 1.0 foliage opacity scale, 1.0 decoration opacity scale, 1.0 marker
scale, and decorations enabled.

MCreator's generated `WitchercraftMod` constructor calls `WorldMapClientConfig.register()` and
`WorldMapServerConfig.register()` only from its
preserved user-code block. The locked config class resolves the active mod container by mod ID. Do not add
a `ModContainer` parameter to the generated constructor because MCreator removes that signature change
during regeneration while preserving the user-code body.

The 26.1.2 long `blit` overload receives matching destination, source, and texture dimensions. Terrain rendering applies one
floating-point pose transform to every visible page: translate to viewport center, scale by the exact zoom,
then translate by the exact map-center offset from a nearby 256-block anchor. Pages use integer coordinates
relative to that anchor, which avoids large-coordinate float precision loss. The shared transform keeps pan
and zoom subpixel-continuous and prevents adjacent pages from acquiring independently rounded widths.

The decoded cache and the 512-entry GPU leaf cache have separate limits. The visible viewport may
temporarily exceed the leaf limit because a leaf referenced by the current GUI extraction cannot be
evicted. Replaced and evicted textures receive versioned identifiers and enter a retirement queue. The
client releases them during a later GUI frame, after Minecraft has executed the deferred draw commands
that can still reference their texture views. A debug diagnostic emitted every
five seconds while rendering reports decoded and GPU cache estimates, source level, leaf rebuilds,
uploads, draw calls, and average map-render time. Leaf size and cache limits remain profiling constants,
not persistent-format fields.

Terrain tile version 4 is the write format. The reader also accepts versions 1 through 3, initializes
layers and block-state palettes that did not exist in those formats as absent, and verifies their original CRC. This preserves
previously explored terrain after a renderer upgrade without loading or regenerating old chunks.

### 5.8 Persistent authorized terrain cache

The server persists a random world-map UUID in the world root. It sends that UUID to each player during
login through the existing request-completion payload using request ID zero. Ordinary completions also
carry the UUID. Client data lives below `witchercraft/world-map/<world UUID>/<player UUID>/dimensions/<namespace>/<path>`.
File headers own storage-format versions, so the directory name does not change for a format revision.
This prevents two players, recreated worlds, or reset servers at the same address from sharing terrain.

The client indexes compressed terrain-container entries on two daemon I/O workers before issuing map requests.
Each terrain container covers the same 4 by 4 chunks as one rendered leaf and stores up to 16 independently
deflated version-four tile payloads behind a fixed entry table. These files use the `.wcr` extension, short for
WitcherCraft Region, and the filename `r.<region X>.<region Z>.wcr`. A `.wcr` file contains raw terrain samples,
not a rendered image. The client uses those samples to rebuild leaf and overview PNGs when required.
The container header records its region coordinates
and format version, and a container CRC rejects truncated or corrupted files. Tile payloads retain their own CRC.
The client loads cached leaf PNG files nearest the current view first and then loads nearby raw containers
selected from at most 256 candidate tiles. One container read supplies every present tile in that leaf. The render
thread registers loaded PNGs, while raw decompression, tile decoding, and file reads stay on the I/O
workers. GPU and decoded-memory eviction do not delete these files. Returning to an evicted area can
therefore restore its finished image without another color and hillshade build.
The load area extends by four leaves, or 256 blocks, beyond every viewport edge. Visible leaves enter
the build queue first, followed by prefetched leaves ordered by distance from the view center. Loading
raw cached tiles still revises their leaf. This repairs an image saved while that leaf was only partly
populated. A valid GPU texture remains visible until the combined replacement build finishes.
At overview zoom the disk scheduler loads one complete overview beyond each viewport edge. Disk discovery
is paced to four passes per second instead of walking the visible tile rectangle every render frame.

#### `.wcr` authoring lifecycle

Players and pack authors never create `.wcr` files. They are private client cache files authored by
`WorldMapClientTileCache` through `WorldMapTerrainTile`. Creation starts only after the client accepts an
authorized `WorldMapTileDataMessage` from the server. The client makes the tile available to the renderer,
then submits its disk work to the single cache-writer thread. Map rendering never waits for this write.

The writer creates or updates a container as follows:

1. It calculates the container coordinates with floor division of the chunk coordinates by four. This also
   assigns negative chunk coordinates correctly. The local floor-modulo coordinates select one of 16 entries.
2. It reads and validates the existing `r.<region X>.<region Z>.wcr`, if present. The writer retains the other
   entries in their compressed form. If the existing file is unreadable, it starts a fresh container because
   the server can resend authorized tiles lost with the damaged cache file.
3. It serializes the new chunk as a version-four `WorldMapTerrainTile`, including the tile CRC, then deflates
   that byte sequence. It replaces only the selected entry in the in-memory container table.
4. It writes the container header, the 16 compressed-entry lengths, every present compressed entry, and the
   container CRC to a uniquely named temporary file beside the destination.
5. It atomically replaces the destination with the temporary file. If the filesystem does not support an
   atomic move, it uses a replacing move after the temporary file has finished writing.

The `.wcr` file stores no finished pixels. Leaf and overview builders read its terrain tiles when a cached PNG
is absent or stale. They produce the PNG and coverage sidecar through their separate background path.
Each finished leaf image is written through a temporary PNG and atomic replacement. Leaf
filenames include the terrain visual-settings key and ordered active resource-pack IDs. A settings or
resource-pack change selects a different cached image and rebuilds from raw tiles. Every PNG has a
versioned coverage sidecar containing one bit for each of the leaf's 16 raw chunks. The
loader compares the mask with the indexed raw tile files. It publishes an exact match immediately and
may publish a stale PNG as a temporary fallback while the leaf builder replaces it. The fallback avoids
black leaves while raw tiles are still entering memory. A missing or invalid sidecar also makes the PNG
stale. Per-path locking prevents the I/O loader from holding a PNG open while the builder atomically
replaces it on Windows. A corrupt tile acts as
absent. A corrupt PNG is deleted and rebuilt. Cache scanning logs tile count, file count, and total size.
The cache currently has no automatic disk-pruning policy.

Live tile arrivals mark their owning leaf dirty even when the map is closed. A two-second debounce
combines nearby arrivals. A single maintenance worker then snapshots one due leaf at a time, builds it
with the same CPU renderer, and writes its PNG and coverage sidecar without creating or uploading a GPU
texture. Maintenance pauses while the map is open because visible and prefetched leaves already use the
priority build queue. Block-model and resource-pack sprite resolution remains on the Minecraft client
thread before either builder receives its immutable tile snapshot.

`WorldMapTileRequestMessage` pairs every position with the capture time held by the client, or negative
one when it has no tile. Cached images display before validation finishes. The server still checks the
player's exploration mask, reads authorized tiles on its I/O executor, and sends data only when the server
capture time is newer. An accepted completion marks every position in that batch as validated for the
connection. Live capture updates replace cached data and dirty the affected leaf normally.

`MapPage` fills the viewport with black before drawing cached overview pages or detailed leaves. Missing
terrain therefore remains opaque to the player, and empty regions do not allocate GPU textures. Terrain is
followed by POIs, saved waypoints, the temporary navigation target, and the player marker. The player uses
`textures/screens/map_player_arrow.png`, drawn at a 16 by 16-pixel base size and rotated from client yaw.
Player and waypoint marker sizes multiply by the square root of map zoom, clamped from 0.5 to 2.5, and by
the client `markerScale` setting, whose supported range is 0.5 to 2.0. No fog, cloud, edge-fade, or separate
lighting state enters terrain capture, persistence, or networking.

All decoded and rendered state is connection-scoped. Persistent client paths include both the server-issued
world UUID and player UUID, preventing terrain from another player, recreated world, or reset server at the
same address from appearing.

### 5.8.1 HUD minimap

`WorldMapMinimapHud` is a locked client code element registered under `World Map/GUI`. It renders from
`RenderGuiEvent.Post` only with a live Overworld player, visible HUD, no active screen, an enabled map system,
server minimap permission, and the client minimap toggle. Suppressing it while a screen is open prevents its
player-centered request bounds from competing in the same frame with `MapPage` pan and zoom state.

The configured minimap size is the outer frame rectangle. `minimap_frame_square.png` is a transparent 64 by
64 texture drawn over all map contents. `viewportInset` is expressed in texels of that source texture and
scaled with the outer frame; the resulting inner rectangle owns the background fill, terrain scissor, terrain
request extent, and marker clipping. This separates artwork geometry from map geometry, allowing a thicker or
decorated frame without rendering terrain behind it. Runtime clamping always leaves at least a 32 by 32 inner
viewport.

The minimap calls the existing `WorldMapClientTileCache` rather than storing, decoding, or uploading terrain
again. Its render overload accepts a clockwise rotation around the viewport center. The corresponding request
bounds use the rotated rectangle's axis-aligned world extents, including the ordinary one-chunk margin, so
rotated corners neither go blank nor authorize additional terrain. Player X/Z and yaw interpolate with the
render partial tick. North-up uses zero terrain rotation and rotates the player arrow; player-up rotates terrain
by `180 - yaw`, fixes the downward-authored player-arrow texture upward, and moves the N label around the inner
border. Client zoom remains pixels per world block.

After terrain, the minimap draws authorized POIs, personal waypoints, and the temporary navigation target.
`WorldMapPoiClientCache.updateView` receives a diagonal-sized request rectangle in rotating mode, then POIs
apply the same discovery state, minimum-zoom rule, definition default, and client filter as `MapPage`.
`WorldMapWaypointClientCache.requestSnapshotIfNeeded` retries at most once per two seconds until the first
authoritative snapshot, so HUD-only play does not require opening the full map. Waypoints retain their
persistent visibility and personal-waypoint filter. POI and waypoint centers use floating-point pose
translation rather than rounded destination coordinates, keeping their motion aligned with smoothly centered
terrain.

An in-range temporary target uses atlas index zero from `map_waypoint_icons.png`, matching its full-map gold
pin. An out-of-range target is clamped inside the inner viewport and replaced there only by
`map_tracking_marker.png`, a transparent editable 16 by 16 upward-authored arrow rotated toward the target.
The frame then renders above map contents, followed by the compass label and centered player indicator.
Target state remains the existing connection-scoped, client-only temporary pin; the minimap adds no persistent
tracking field or network message. Before drawing either target form, squared horizontal distance is compared
with the client `targetArrivalRadius` setting (default 5 blocks, range 0 to 64); a positive reached radius hides
minimap guidance and removes the connection-scoped temporary target as a completed destination. It therefore does
not reappear after the player leaves the radius. Zero disables automatic target completion.

### 5.9 Waypoint architecture and ownership

#### Authoritative state

`WorldMapWaypoints` is the server authority for personal waypoints. It is a locked base-package code element
and may only be accessed on the server thread. One codec-backed `SavedData` object in the server's shared world
storage groups records by owner UUID. Each record contains a server-generated waypoint UUID, dimension
identifier, X/Z coordinates, normalized name, icon, and persistent visibility flag. Waypoints therefore survive
logout, death, respawn, and server restart without using generated player variables or client files.

Every public read or mutation accepts a `ServerPlayer` and derives the owner UUID from that player. Neither the
storage API nor the network protocol accepts a caller-supplied owner UUID, so a client cannot inspect or mutate
another player's list. The supported mutations are create, edit name/icon, set visibility, and delete. Editing
does not change a waypoint's dimension or coordinates, and waypoint sharing is not part of this system.

Creation is limited to 200 waypoints per player. The requested dimension must be loaded, coordinates must be
finite and within both the 30,000,000-block hard ceiling and that dimension's current world border, and the
trimmed name must contain 1 to 64 Unicode code points with no control characters. Icons are restricted to the
`WaypointIcon` enum: home, camp, chest, danger, herb, monster, and quest. The same name, coordinate, icon, and
identifier checks are applied while loading stored records.

#### Persistence and compatibility

Save format 2 uses permissive stored strings for UUIDs, dimension IDs, and icon IDs, then validates each decoded
logical record. An invalid record, duplicate waypoint UUID, or record above the per-player limit is discarded
without invalidating unrelated waypoints. An invalid player UUID discards only that player's collection. A
structurally unreadable `SavedData` file still follows Minecraft's normal saved-data recovery behavior.

Color and tracking are not runtime waypoint properties. Their legacy fields remain in the codec with fixed
defaults so format-1 worlds continue to load and can be written without a destructive migration. Saved icons
remain player-selectable, while all icons are tinted on the client with `MapLayout.WAYPOINT_COLOR`.

#### Network protocol and client cache

`WorldMapWaypointMutationMessage` is the single bounded serverbound intent for snapshot, create, edit,
visibility, and delete operations. Its payload has a positive request ID and bounded strings but no owner UUID.
The network context supplies the authenticated `ServerPlayer`; handling is enqueued onto the server thread and
delegated to `WorldMapWaypoints`. The server replies with a `WorldMapWaypointResultMessage` containing the
request ID, operation, and status, followed by a complete `WorldMapWaypointSnapshotMessage`. Full replacement is
bounded by the 200-record server limit and avoids client-side merge or conflict rules.

`WorldMapWaypointClientCache` stores only the latest authoritative snapshot and the 32 most recent mutation
results. Every access, request, and response checks Minecraft's current connection object. Changing or losing
the connection clears waypoints, results, temporary targets, synchronization state, and the request counter, so
one server's state cannot appear on another. Opening `MapPage` requests a fresh snapshot, while the HUD minimap
requests one on demand with a two-second retry interval until synchronized. All map and manager
mutations go through cache request methods; UI code never edits the snapshot directly.

#### Temporary navigation targets and creation

The client cache also owns one temporary navigation target per dimension. Placing another target in the same
dimension replaces it. Targets survive closing the map but clear on connection change. They are not sent to the
server, saved to disk, or counted against the personal-waypoint limit.

`WitcherGuiScreen` forwards Minecraft's double-click classification and Unicode character input through the
`GuiPage` contract. `MapPage` reserves left drag for panning. A right-click on empty map space starts a
300-millisecond pending action; if no second click arrives, it places a temporary target at the transformed
world X/Z. A double right-click cancels that action and opens the modal waypoint-creation panel at the same
coordinates. The panel captures mouse, wheel, key, and character input, submits creation through the client
cache, and waits for its matching result. A rejection leaves the panel open and displays the server status.

#### Rendering and marker interaction

`MapPage` renders saved waypoints from the latest connection-scoped snapshot. It selects only the current
dimension, applies both the waypoint's persistent visibility flag and the world's client-side personal-waypoint
filter, uses the terrain world-to-screen transform, and clips markers to the viewport. Marker centers remain
floating point until pose translation to avoid integer-step panning. Player, waypoint, and temporary-target
sizes use the square root of map zoom clamped from 0.5 to 2.5, multiplied by the client marker-scale setting.
Waypoint opacity rises linearly from 35 percent at 0.25 screen pixels per block to full opacity at 1.0.

Hover hit testing chooses the nearest marker inside the scaled hit radius; exact overlap is resolved in the
order temporary target, saved waypoint, then POI. The shared two-line information card shows a waypoint or
target name followed by floored X/Z coordinates. Right-click interaction uses the same visual layers: a
temporary-target hit removes it, a saved-waypoint hit opens its Target/Delete menu, and a POI hit places the
temporary target at the POI anchor without opening a menu. Only empty space participates in the delayed
single/double-right-click behavior.

The saved-waypoint context menu follows its marker and clamps to the viewport. Target copies the waypoint's
coordinates into the dimension's temporary-target slot. Delete sends the normal server mutation and disables
both actions until the matching result arrives. The menu closes after success, on Escape, or on an outside
click, and blocks map zoom while open. It owns no persistent state and adds no separate network payload.

The temporary target and seven saved icons use `textures/screens/map_waypoint_icons.png`, a transparent 4 by 2
atlas ordered pin, home, camp, chest, danger, herb, monster, and quest. The checked-in atlas is 1774 by 887
pixels. Replacements must preserve its dimensions, cell order, transparent padding, and grid. Neutral icon
pixels are multiplied by `MapLayout.WAYPOINT_COLOR` at render time.

#### Waypoint manager and layout ownership

`WorldMapWaypointManagerOverlay` owns its modal state, case-insensitive search, scroll offset, edit form, delete
confirmation, and one pending mutation. `MapPage` owns the overlay instance and provides only the callback used
to center the map on a selected current-dimension waypoint. The manager reads the client cache and sends changes
through its request methods; it never changes cached or authoritative records directly.

The manager sorts current-dimension records first, followed by case-insensitive name and UUID as a stable
tie-breaker. Current-dimension rows show planar distance and can center the map. Other rows show their dimension
identifier and disable centering. Editing can change only name and icon. Deletion requires confirmation, and
Escape closes the confirmation, editor, and manager in that order.

`MapLayout` owns creation, manager, context-menu, hover-card, bottom-bar hint, and marker layout constants.
`tools/map-layout-creator.html` previews the editable map layout and exports the corresponding Java constants.

### 5.10 Generated POI test fixture

`PoiTestStructure` is an ordinary MCreator structure element under `~/World Map`. Its source of truth is
`elements/PoiTestStructure.mod.json`. MCreator generates the structure, structure-set, and template-pool
JSON resources. The referenced `data/witchercraft/structure/poi_test_structure.nbt` template is a hollow
4 by 4 by 4 stone-brick cube with a two-block doorway and a chiseled stone-brick marker in its roof.

This is a real `minecraft:jigsaw` structure rather than a procedure-built cube or placed feature. Its
single rigid pool element projects to `WORLD_SURFACE_WG`, generates during `surface_structures`, and is
limited to the `#minecraft:is_overworld` biome tag. Development placement uses random-spread spacing 24
chunks and separation 8 chunks. MCreator derives the stable salt from the registry name. These placement
values make the test structure easy to find and are not production balance.

The element intentionally contains no POI registration logic. The structure provider inspects its real
structure start in already loaded chunk data and maps its stable start chunk to a POI instance. Keeping
the fixture separate from POI code proves that additional structures join through definitions and provider
matching instead of copied Java classes.

### 5.11 POI definitions and providers

POI kinds are datapack resources under `data/<namespace>/witchercraft_pois/<path>.json`; the resource path
becomes the definition ID. A definition selects either one exact `structure` or one `structure_tag`, and every
generated start of a selected structure becomes an instance automatically. The two selector fields are mutually
exclusive. Adding another structure POI does not require Java registration: add one definition JSON referring
to a vanilla, modded, or MCreator structure or structure tag. Presentation fields may be omitted to use the
shared defaults (256-block reveal, 32-block discovery, zero uncertainty, visible by default, 0.25 minimum zoom,
the `witchercraft:general` category, and the shared default icon). Unique art and localization remain ordinary
optional resource additions.

Definitions may provide a `description_translation_key`; otherwise it derives from the name key with a
`.description` suffix. The older `uncertainty_radius` field remains accepted for datapack compatibility but no
longer moves an unknown marker away from its world anchor.

`WorldMapPoiDefinitions` owns the server reload listener and publishes immutable, fully prepared snapshots.
Every resource is decoded and validated independently, so a bad definition is logged with its ID without
discarding valid neighbors. Validation bounds definition and capability counts, identifiers, radii, zoom,
provider types, structure and structure-tag references, empty tags, and overlapping claims on one provider source. Reload reconciliation marks
retained instances inactive when their definition is absent or no longer matches; the shared instance store
preserves those suppressed records.

`WorldMapPoiProvider` is the reusable loaded-world-object contract, and `WorldMapPoiProviders` is its internal
type registry. The `witchercraft:structure` provider expands exact IDs and tags into a structure-object lookup,
while observation reads `StructureStart` values directly from the `LevelChunk` provided by
`ChunkWatchEvent.Watch`. The `witchercraft:poi_type` provider resolves an exact point-of-interest registry ID and
reads matching records from the watched chunk through the server's `PoiManager`; one stable map instance is
created per matching block position. Neither provider locates, generates, tickets, or requests a chunk. Sources
that are not present in the watched loaded chunk are simply ignored.

The structure provider anchors a marker at the structure bounding-box center. Its canonical identity is provider
type, structure registry ID, dimension ID, and structure start chunk. `WorldMapPoiInstance` derives a deterministic
UUID from that identity, so repeat observations by multiple players refresh one entry. `WorldMapPoiManager` owns
the observation lifecycle, logs a newly observed instance immediately, and emits bounded aggregate diagnostics
every 1,200 server ticks.

### 5.12 POI persistence, identity, and discovery

`WorldMapPoiInstances` is the authoritative shared, codec-backed `SavedData` store. Each record
persists the stable marker UUID, definition and provider identifiers, canonical provider identity, dimension,
exact three-dimensional anchor, and active flag. Version two adds an optional `custom_name` (empty by default,
at most 64 code points of plain text) used by lifecycle-managed POIs; version three adds an optional `name_key`
(empty, or a lowercase translation key of at most 128 characters) for generated place names. Older files load
unchanged. Loading
recomputes the UUID from the provider identity and rejects invalid coordinates, identifiers, names, duplicate IDs,
collisions, overlong identities, and records beyond the hard instance limit. Provider observations insert or
refresh this store on the server thread. Removing a definition marks matching records inactive without deleting
them; only a later provider observation can reactivate one after its definition returns. Lifecycle-managed
records (Section 5.17) are the exception: definition reconciliation reactivates them, and only they are ever
deleted from the store.

`WorldMapPoiKnowledge` is a separate codec-backed `SavedData` store grouped by player UUID. Its version-two
knowledge record contains the authoritative marker UUID, a random per-player presentation UUID, monotonic
`revealed` or `discovered` state, and legacy X/Z uncertainty-offset fields. New reveals write zero offsets and
presentation ignores historical non-zero values, so both new and existing unknown markers sit on the exact POI
anchor. The retained fields keep older saves codec-compatible and may be removed in a later format migration.
Knowledge is independent of generated player variables and therefore survives logout, death, respawn, and
client-cache deletion. Version-one entries receive and persist a presentation UUID when first loaded. This opaque
wire identifier prevents clients from brute-forcing the deterministic provider identity from a marker UUID.
Logical records and collection sizes are validated independently during loading. Version three adds shared
discovery (Section 5.17): a world-level `shared_discoveries` list of marker UUIDs, a per-entry `shared` flag, and
an `unknown` state for entries that exist only because of sharing. Every consumer reads knowledge through
`WorldMapPoiKnowledge.effective(entry, sharing)` rather than the raw state.

`WorldMapPoiSpatialIndex` is derived runtime state and is never serialized. It groups active markers into
256-by-256-block cells under their dimension identifier. Startup rebuilds it from the shared store; observations
update it incrementally; definition reload reconciliation rebuilds it after suppressing invalidated records.
Queries return marker IDs only and perform no level, chunk, generator, locate, or ticket operation.

`WorldMapPoiManager` distributes checks across stable player-UUID tick slots. Reveal checks run every 200 ticks
and discovery checks every 20 ticks. Both query the spatial index and use squared horizontal X/Z distance; Y is
retained in storage but ignored for proximity. Normal POIs progress from absent to revealed to discovered. A
player already inside both radii during a reveal pass is promoted immediately. A POI with zero reveal radius
skips the revealed state and may be discovered directly inside its discovery radius. Discovery sends a translated
action-bar message and a direct vanilla level-up sound packet only to that player. POI name, category,
description, and discovery-message keys are written to both `en_us.json` and `witchercraft.mcreator`'s `language_map.en_us`, as
required by Section 3.11. The server notification also supplies readable fallbacks for both nested translation
components, preventing raw dotted keys if a resource pack or generated language file is incomplete.
Definitions with `discovery_required: false` stop at the revealed knowledge state but use their identified marker
presentation immediately; they never show the unknown icon or produce a discovery message or sound.

### 5.13 POI authorization, networking, and client cache

The POI view protocol uses the same 256-by-256-block cells as the runtime spatial index. While `MapPage` is
rendering, `WorldMapPoiClientCache` derives visible cells from the shared world-to-screen view and requests at
most 64 unvalidated cells. One request remains in flight at a time. The server requires a positive request ID,
the player's current dimension, the current definition generation, unique in-range cells, and a per-player
allowance of at most 128 requested cells per 20-tick window. The request contains no player UUID and cannot
mutate knowledge. Handling reads only `SavedData` and runtime maps; it never reads or loads chunks.

`WorldMapPoiDataMessage` carries at most 64 markers per batch. `WorldMapPoiRequestCompleteMessage` echoes the
authoritatively answered cells and carries the canonical `WorldMapWorldIdentity` UUID. A successful completion
atomically replaces those cells, including empty results; rejected or stale requests validate nothing and retry
later. Server responses are deterministically ordered and reject a request rather than partially authorizing it
if its bounded 4,096-marker response ceiling would be exceeded.

`WorldMapPoiMarker.Unknown` contains only the per-player presentation UUID, exact anchor X/Z, minimum zoom,
and default visibility. It cannot hold a definition ID, provider fields, translation key, description, category,
or icon. `WorldMapPoiMarker.Discovered` contains exact X/Z plus name and description translation keys, category,
icon, minimum zoom, default visibility, and the server-approved custom name, which replaces the translated name
when it is not empty. The authoritative deterministic marker UUID never crosses the network; the version-two
knowledge store's random presentation UUID is stable for that player across sessions and is not derivable from
the structure identity.

Reveal and discovery transitions persist first and then push a request-ID-zero marker update to that player.
Discovery replaces the unknown record by presentation UUID in the same presentation cell.
Definition snapshots carry a monotonically increasing generation. Login and every successful definition reload
send `WorldMapPoiCacheResetMessage`; stale batches are ignored and visible cells are requested again.

`WorldMapPoiClientCache` is memory-only and scoped by both the active connection object and the existing
server-issued world UUID. It groups markers by dimension and presentation cell, bounds validated cells per
dimension, buffers response pages until completion, and clears on connection, world, or definition-generation
change. `markers(dimension)` is the read-only boundary consumed by map rendering.

### 5.14 POI rendering, interaction, and filters

`MapPage` consumes only `WorldMapPoiClientCache.markers(currentDimension)`. POIs share the terrain and waypoint
floating-point world-to-screen transform and render inside the terrain scissor after terrain but before personal
waypoints, the temporary target, and the player. Unknown markers use `map_poi_unknown.png`; discovered markers
use the server-approved individual texture identifier. Both marker sizes use the existing client marker-scale
setting and square-root zoom scaling. Unknown and identified markers both honor their definition's minimum-zoom
rule. Definitions that use the minimum supported value remain visible and
continue shrinking at every zoom level. Personal waypoints and temporary pins retain full opacity while zooming.

Drawing, hover, and right-click hit testing share the same visibility predicate. Marker snapshots are sorted by
the random presentation UUID, nearest-marker selection uses squared screen distance, and exact ties use the
explicit priority temporary target, personal waypoint, then POI. Right-click uses the same layer priority.
Hover cards are always two lines: a personal waypoint or temporary target shows name and coordinates; an unknown
POI shows `Undiscovered location` and a generic exploration hint; a discovered POI shows its translated name and
data-driven translated description. Right-clicking either POI state immediately places or replaces the existing
temporary navigation target at that marker. POIs have no details popup or mutation menu.

`WorldMapPoiFilterOverlay` is a modal owned by `MapPage`. It controls personal waypoints, unknown POIs,
discovered POIs, and the categories present among identified markers in the current client cache; the temporary
target and player marker are intentionally not filterable. Unknown markers do not expose their category. Escape and outside
left click close the overlay, and all map input is consumed while it is open. POI definition `defaultVisible`
applies while a group has no explicit user override. The first click selects hidden, subsequent clicks toggle
hidden/shown, and an explicit shown value can reveal definitions that default to hidden.

`WorldMapPoiFilterPreferences` persists these client-only settings in
`config/witchercraft-world-map-filters.json`. Its versioned, size-bounded JSON records are keyed by the
server-issued world UUID exposed read-only by `WorldMapPoiClientCache`; a zero UUID is never written. Per-category
overrides are stored by category identifier alongside the three general filters. Categories without a world-specific
override consult the client config's `defaultHiddenPoiCategories` identifier list; Services is hidden there by
default. It retains
at most 128 worlds, skips malformed sibling records independently, and replaces the file atomically where the
filesystem supports it. Filter data never enters packets, server `SavedData`, generated player variables, or
the global NeoForge client config.

### 5.15 Operational diagnostics and verification contracts

The POI observation boundary is the already loaded `LevelChunk` supplied by `ChunkWatchEvent.Watch`.
`WorldMapStructurePoiProvider` inspects structure starts on that object only. Map requests traverse player
knowledge, shared POI instances, and the runtime spatial index; panning never calls a level chunk lookup,
structure locate, generator, forced-chunk, or ticket API. This no-load boundary applies to every provider:
an observation that cannot be answered from the event's loaded object must be skipped.

Structure identity consists of provider type, structure registry ID, dimension ID, and structure-start chunk.
It deterministically produces one marker UUID, so repeated watches and observations by different players reach
the same saved record. An unchanged repeat increments `duplicate_observations`; a changed record with the same
identity is refreshed; a UUID/identity mismatch is rejected and counted in `observation_collisions`.

Definition reload prepares and validates each resource independently before publishing one immutable snapshot.
A malformed resource is rejected without discarding valid siblings. Reconciliation suppresses retained
instances whose definition disappeared or no longer accepts them, rebuilds the spatial index from active
records, resets every connected client's POI cache, and does not delete knowledge. Restoring a definition makes
future observations eligible to reactivate the retained stable record.

Every 1,200 server ticks, the POI diagnostic line reports cumulative observation, instance, transition,
request, and rejection counts. It also reports duplicate/collision counts, reveal/discovery candidate totals,
and the average/maximum manager tick time in microseconds for that reporting window. Timing and candidate
counters reset after each report; observation, transition, and request counters remain cumulative. These
bounded counters make duplicate and multi-player performance checks observable without per-tick logging.

Verification must preserve the following invariants:

1. Panning over unexplored terrain may issue bounded cache requests, but must not change chunk tickets, load
   chunks, generate terrain, or create terrain files for unauthorized positions.
2. Structure observation may inspect only the `LevelChunk` supplied by `ChunkWatchEvent.Watch`. Searches for
   chunk lookup, locate, generator, forced-chunk, and ticket APIs in the POI path must remain empty except for
   that event-owned `getChunk()` handoff.
3. Repeated watches of one structure, including watches from different players or covered chunks, must leave
   `retained_instances` unchanged, may increase `duplicate_observations`, and must keep
   `observation_collisions` at zero.
4. A malformed POI definition must be rejected without removing valid sibling definitions. Removing a valid
   definition and reloading must suppress its markers; restoring it and observing the loaded structure again
   must reactivate the same stable record without duplicating player knowledge.
5. Performance tests with many retained structures record at least three diagnostic intervals of
   `reveal_candidates`, `discovery_candidates`, `tick_avg_us`, `tick_max_us`, and whole-server MSPT. Sustained
   spikes correlated with POI candidate counts require investigation before increasing radii or scan cadence.
6. Repository validation parses all JSON resources, resolves every MCreator element and declared metadata file,
   compiles with Java 25, and starts a dedicated server far enough to load POI definitions successfully.

### 5.16 Configuration and hardening

`WorldMapServerConfig` is a NeoForge `SERVER` configuration. It is stored per world, applies equally to the
integrated server and a dedicated server, and is synchronized read-only to remote clients. `WitchercraftConfigScreen`
registers NeoForge's built-in configuration screen, producing `Client Settings > World Map` and
`World Settings > World Map` sections under Mods > WitcherCraft > Config. Translated section and option labels
belong to both `en_us.json` and `witchercraft.mcreator`'s `language_map.en_us`, preventing MCreator regeneration
from restoring raw translation keys in the generated screen. Future systems such as meditation add sibling
sections rather than separate settings UIs.

The sibling `Meditation` server-config section owns the authoritative stamina economy. Its defaults
are free meditation off, setup cost 2, four hours per step, 2 stamina per step, and a maximum elapsed
time cost of 10. The server checks the selected session's full price before committing, charges setup
immediately, and charges elapsed time on completion or cancellation. Cancellation derives elapsed
clock ticks from the same session anchors used by the accelerated clock, so it never bills the
unreached part of the selected duration. `MeditationCosts` is the shared calculation used by the
server and the synchronized client-side price indicator.
The indicator is selection-only and disappears once the time-lapse begins. Its anchor, icon size,
and icon texture belong to `MeditationLayout` and are editable in `tools/meditation-dial-creator.html`.
`MeditationPlaceCampfireProcedure.hasNearbyCampfire` is the shared client/server scan for normal and
soul campfires within the placement procedure's four-block horizontal and one-block vertical range.
The client uses it to omit setup from the displayed price. The server repeats it before affordability
validation, campfire placement, and setup charging, so the server remains authoritative.

The sibling `Fast Travel` section holds the fast-travel enable switch, the sign-drop switch, the player-placed
sign limit, and shared signpost discovery (Section 5.17). Fast travel additionally requires the map and POIs to be
enabled. The `World Map` section's `mapNameLanguage` (empty, or a language code such as `pl_pl`) is read by
clients through the synchronized config; see "Generated place names" in Section 5.17.

World settings control the map and POI enable switches, a POI-definition allowlist, omitted JSON radius defaults,
same-session tile refresh cooldown, capture count and time budgets, and the personal-waypoint limit. An empty POI
allowlist enables all valid definitions. POI definition selection and default radii require a world restart and are
re-applied at `ServerAboutToStart`, after the per-world config has loaded. Lowering the waypoint limit never deletes
stored records. The configurable limit is bounded by a separate immutable 1,000-record wire/storage ceiling.

Configuration never weakens hard validation. Terrain and POI requests remain batch- and rate-limited, waypoint
mutations allow at most 32 requests per player per 20 ticks, strings and palettes remain bounded, and disabled
systems reject or complete requests without exposing cached state. The client never draws retained terrain when the
world disables the map. POI discovery action-bar presentation moved to a bounded clientbound payload so each client
may hide that message; the authoritative fixed discovery sound remains server-issued.

Client `.wcr` reads and writes now share the same per-path lock. Atomic replacement retries bounded Windows sharing
violations and removes failed temporary files. Missing or corrupt terrain remains disposable and recoverable from
the authoritative server without affecting waypoints or POI knowledge. Capture diagnostics additionally report
average and maximum sampling time; existing renderer, cache-size, POI candidate, tick-time, queue, failure, rebuild,
upload, draw-call, and disk-footprint diagnostics remain the profiling basis for later default changes.

### 5.17 Fast-travel signposts

Stages 1 and 2 of the fast-travel milestone add the signpost block, its lifecycle as a shared POI, village
signposts, generated place names, shared discovery, and the recipe. Travel sessions, XP pricing, and the map travel
mode are later stages and must build on the contracts below. Stage 3 travel authorization must read discovery
through `WorldMapPoiKnowledge.effective`, so shared discoveries count.

#### Ownership boundary

The block is an ordinary MCreator element, `FastTravelSign` (browser folder `~/World Map/Fast Travel`). Its two
halves are one block with a custom logic property `upper`; the upper state has its own model, like a vanilla
door. Both halves share one plain 10 by 16 by 10 bounding box (collision and selection). The models are `models/fast_travel_sign_lower.json` and `models/fast_travel_sign_upper.json`
(MCreator workspace models with `.textures` mapping files), split at y=16 from
`models/blockbench/FastTravelSign.bbmodel`. A replacement model must also split cleanly at y=16. The block has no
drops of its own (drop amount zero, empty loot table), no block entity, piston reaction `BLOCK`, and zero
flammability, so every normal removal path goes through a procedure.

These Blockly procedures own placement and destruction and are the source of truth for that behavior:

| Procedure | Trigger | Behavior |
| --- | --- | --- |
| `FastTravelSignCanSurvive` | Placing condition (`canSurvive`) | Upper half survives only on a lower half. Lower half requires the Overworld and air or its own upper half above. MCreator's generated `updateShape` turns a half that fails this into air, which is how the upper half disappears with its base. |
| `FastTravelSignPlaced` | Block placed by entity | For the lower half, calls `FastTravelSignRegister`; on success places the upper half, otherwise removes the lower half and refunds the item outside creative mode. |
| `FastTravelSignDestroyedByPlayer` | Destroyed by player | Removes a lower half directly below (the player broke the upper half), then drops one item if `FastTravelSignRemove` and `FastTravelSignDropsItem` both return true and the player is not in creative mode. |
| `FastTravelSignExploded` | Destroyed by explosion | Same as above without the creative check. The explosion hook receives no block state, so the procedure never relies on knowing which half exploded. |
| `FastTravelSignNeighbourChanged` | Neighbour changed | A lower half without its upper half (removed by a command) removes itself and its record, without a drop. |

The locked procedures `FastTravelSignRegister`, `FastTravelSignRemove`, and `FastTravelSignDropsItem` are thin
entry points into the locked code element `FastTravelSigns`.

#### Identity and authoritative state

The shared POI store is the authoritative sign state. `FastTravelSigns.register` creates a record under the
`witchercraft:fast_travel_sign` definition and provider with identity
`witchercraft:fast_travel_sign|<dimension>|<random UUID>`, so every placement, including a replacement at the same
position, gets a fresh marker UUID and fresh per-player knowledge. The anchor is the lower half. Any older record
at the same anchor is deleted first. The record's `custom_name` holds the bare sign name, defaulting to the
anchor's `x, z` coordinates. Presentation adds the kind: `WorldMapPoiMarker.displayName` formats any
custom-named POI as `<translated kind>: <name>` through the `gui.witchercraft.map.poi.named` key, used by the
map hover card and the discovery message. A non-empty `name_key` takes precedence over `custom_name` (see
"Generated place names"). The `sourceId` distinguishes `witchercraft:player_placed` from
`witchercraft:village`; only player-placed records count toward the per-world limit.

`FastTravelSigns.removeIfGone` checks the records anchored at the broken block and the block below it, and deletes
any whose complete two-block sign no longer stands. It returns true only for an actual deletion, so a destroyed
sign produces at most one item no matter how many of its halves trigger a procedure. Client-side calls of
Register return true (the client predicts the upper half) and client-side calls of Remove return false; the
server is authoritative.

#### Lifecycle-managed POIs

`WorldMapPoiProvider` has two default methods for providers whose instances come from world events rather than
chunk observation. `lifecycleManaged()` makes definition reconciliation reactivate retained records when their
definition returns. `retainsLoadedInstance` is asked about every retained lifecycle record anchored in a chunk
delivered by `ChunkWatchEvent.Watch`; returning false deletes the record. `FastTravelSignPoiProvider` observes
nothing and answers the retention check from the watched `LevelChunk` only, which catches signs removed by
`/setblock`, `/fill`, and other paths that run no procedure. This keeps the no-load contract of Section 5.15.

`WorldMapPoiManager` keeps two derived, unsaved indexes of lifecycle records: exact anchor to marker and chunk to
markers. Its lifecycle API is `putLifecycleInstance`, `makeDiscoverable`, `discoverFor`, `lifecycleInstanceAt`,
`removeInstance`, `renameInstance`, and `countInstances`, all server-thread only. A record whose definition is
unavailable (fast travel or POIs disabled) is stored inactive and stays out of the spatial index, so it is neither
revealed nor sent. A record stored with `discoverable` false is also kept out of the spatial index until
`makeDiscoverable`; this runtime-only hold ends at restart and survives definition reconciliation.

Deleting a record also deletes every player's knowledge entry for it (`WorldMapPoiKnowledge.forget`) and sends
`WorldMapPoiRemovedMessage` with that player's presentation UUID to each affected connected player, whose
client cache drops the marker. Offline players receive a full cache reset at login as before. Renaming pushes
the updated marker to connected players who know it. `WorldMapPoiDiscoveredMessage` and `WorldMapPoiDataMessage`
carry the custom name and name key alongside the translation key. The diagnostic line adds
`lifecycle_removals` and `stale_removals`.

#### Configuration

The `fast_travel_sign` definition ignores the POI allowlist and is published only while
`WorldMapServerConfig.fastTravelEnabled()` is true, which also requires the map and POIs. The definition sets a
128-block reveal radius, a 16-block discovery radius, the `witchercraft:fast_travel` category, and the
`map_poi_fast_travel.png` icon. Sign drops and the player-placed limit (default 256, zero for no limit, hard
ceiling 100,000) apply immediately; the enable switch requires a world restart like other definition selection.

#### Placement naming

A player placement registers the sign undiscoverable, stores a naming session for that player (marker UUID and
a five-minute expiry), and sends `FastTravelSignNameMessage` with the default name. The client opens
`FastTravelSignNameScreen`, a plain `Screen` styled like the waypoint creation panel. However the screen
closes, `removed()` sends the same message type back exactly once with only the name, empty for cancel. The
server consumes the sender's session, so a request can only name the sender's latest sign, once, and the packet
never carries a sign identity. A non-empty name that passes `WorldMapPoiInstance.validCustomName` replaces the
default; the sign is then made discoverable in every case, and `WorldMapPoiManager.discoverFor` discovers it for
the placer at once (normal message and sound), bypassing the reveal pass and discovery radius. Sessions also end, releasing the sign, on logout, on
expiry (checked once per second), and when the same player places another sign. Server stop clears them; the
hold itself is runtime-only.

#### Village signposts

The locked code element `FastTravelVillageSigns` (`~/World Map/Fast Travel`) places one sign per newly generated
village. Minecraft has no structure-placed event and the project uses no mixins, so it follows NeoForge's
`ChunkEvent.Load`, whose `isNewChunk()` is true on a chunk's first promotion to a full chunk. That event must not
touch the level (deadlock risk), so the handler only copies the chunk's own `#minecraft:village` structure
references into a queue; up to 32 queued chunks are processed per server tick. Only the Overworld is watched, and
nothing is queued while fast travel is disabled.

A village is keyed `<structure id>|<start chunk x>,<start chunk z>`, the same identity as the village POI. Its
town center is the first piece of its `StructureStart`, fetched with `StructureManager.fillStartsForStructure`,
which only reads structure starts of chunks that already exist. A village becomes eligible (`pending`) when a
newly generated chunk intersects its town-center bounding box; this is the no-retrofit rule. A pending village is
decided once every chunk under the town center is loaded, checked with `getChunkNow` so nothing loads. Any later
load of a chunk that references a pending village re-checks it, because town-center chunks can become full at
very different times.

Deciding scans the town-center box for bells (the spike found one or two in every vanilla town center except the
zombie taiga meeting point 2; plains houses may contain other bells, which are ignored) and anchors at the bell
nearest the box center, or at the surface of the box center when there is none. `findSpot` searches columns
within 4 blocks of the anchor from 4 below to 1 above it, nearest first in a fixed order, then the whole town-center
box once. A spot needs a sturdy, non-fluid, non-leaf floor that is not a bell, two free blocks (`canBeReplaced`, no
fluid), and loaded chunks on every side, so placement never loads terrain. `FastTravelSigns.placeGenerated` sets
both halves with normal block updates and registers the record through the same `register` path as player
placement, discoverable at once and without naming. If no spot exists or registration fails, the village is marked
failed and a warning is logged. `findSpot` is the single place to change that rule.

Decisions persist in the `SavedData` `witchercraft:fast_travel/village_signs` (`pending`, `placed`,
`failed`, `used_names`), so a placed or failed village is never reconsidered, even after its sign is broken.

#### Generated place names

Village sign names are translation keys `signpost_name.witchercraft.<kind>.<id>`, where kind is the path after
`minecraft:village_` (`plains`, `desert`, `savanna`, `snowy`, `taiga`) or `other` for any other village
structure. The server builds the lists once from the mod's own `en_us.json` (read from the mod file, so it works
on a dedicated server), so adding a name means adding a localization entry in MCreator; there is no separate data
file. A kind without names uses `other`, and no names at all leaves the key empty (coordinates only). Names in
`used_names` are skipped while unused ones remain. Keys must never be renamed or removed once shipped, because
saved records keep them.

The record stores the key in `name_key` and the coordinates in `custom_name` as the fallback. Clients resolve
the key in `WorldMapPlaceNames` (`~/World Map/POI`), called from `WorldMapPoiMarker.displayName`. With an empty
`mapNameLanguage` it uses the client's current language. Otherwise it loads `assets/witchercraft/lang/<code>.json`
from the client's resource manager (all packs merged, higher packs winning), falls back to `en_us`, then to
`custom_name`. Loaded tables are cached and cleared on every POI cache reset. Kind labels and all interface text
always use the client's own language.

#### Shared discovery

`WorldMapPoiProvider.sharesDiscovery()` (true only for `FastTravelSignPoiProvider`) opts a provider in. Every
discovery of such a POI, including the placer's `discoverFor`, adds it to `shared_discoveries` whatever the
setting. While `sharedDiscovery` is on, the first entry into that set grants it to every other online player
(`grantShared` sets the entry's `shared` flag, creating an `unknown` entry with a fresh presentation UUID if
needed) and sends them the discovery message without the sound. Login grants every shared discovery silently
before the cache reset. The manager polls the setting once per second; on a change it re-grants when switched on and
sends every player a cache reset either way, so clients re-request markers under the new rule. With sharing off,
`effective` hides `unknown` entries and ignores the flag. A player who walks into the discovery radius of a POI
known only through sharing silently gains their own discovered state, and a reveal of an `unknown` entry keeps its
presentation UUID, so switching sharing off later never produces duplicate markers or loses a visit.

#### Recipe

`FastTravelSignRecipe` (`~/World Map/Fast Travel`) is an ordinary MCreator crafting recipe named
`witchercraft:fast_travel_sign`: oak sign, compass, oak sign over two `#minecraft:logs` in the middle column,
producing one signpost, unlocked by an advancement when the player holds a compass. It is a placeholder.

#### Travel service

Stage 3 adds the authoritative server side of travel. Nothing starts a session yet; stage 4 wires the sign's
right-click and the map travel mode onto this API.

| Class (code element) | Role |
| --- | --- |
| `FastTravel` (`~/World Map/Fast Travel`) | Sessions, requests, destination loading, commit. Server thread only. |
| `FastTravelCosts` (`~/World Map/Fast Travel`) | Price and exact XP arithmetic, shared by server and client. |
| `FastTravelRequestMessage` (`~/World Map/Fast Travel`) | Serverbound: destination presentation UUID and the price the client displayed. |
| `FastTravelResultMessage` (`~/World Map/Fast Travel`) | Clientbound: a `FastTravel.Result` (ordinal on the wire) and the current price. Until stage 4 the client shows it as an action-bar message. |
| `NearbyMonsters` (`~/Admin/EnemyNearby`) | The vanilla bed rule, shared with meditation. |

**Sessions.** `FastTravel.startSession(player, anchor)` records the origin marker and anchor with a five-minute
expiry. It is refused unless travel is enabled, the player is in the Overworld with no journey being prepared,
and the anchor holds a complete, registered signpost. `endSession` (the player left travel mode) also cancels a
journey still being prepared. Sessions end with a `SESSION_ENDED` notice on death and dimension change, silently on
logout, and in a once-per-second sweep when the session expired, the player is more than 8 blocks from the
origin anchor, or the origin record or its blocks are gone. The sweep checks distance before touching the origin
blocks, so it never loads a chunk. Combat, monsters, sleeping, and riding only refuse a request; they never end a
session.

**Requests.** `FastTravel.request` resolves the destination with
`WorldMapPoiManager.discoveredByPresentation`, which walks only the sender's knowledge through
`WorldMapPoiKnowledge.effective`. It therefore accepts only an active, accepted POI the player sees as discovered,
shared discoveries included, and gives the same `UNKNOWN_DESTINATION` answer for undiscovered and nonexistent
signs. The destination must be a signpost in the Overworld other than the origin. The server recomputes the price.
A different quoted price answers `PRICE_CHANGED` with the new price and charges nothing. Insufficient XP answers
`NOT_ENOUGH_XP`. One journey can be in preparation per player; further requests answer `BUSY`.

**Loading.** An accepted request adds a `witchercraft:fast_travel` ticket (registered through `RegisterEvent`;
`FLAG_LOADING` only, 40-tick timeout, radius 1) with `addTicketAndLoadWithRadius`, refreshes it every 20 ticks,
and answers `TRAVELLING`. The server tick polls the 3 by 3 chunk area with `getChunkNow`. Success, failure,
timeout (10 seconds), logout, and `endSession` all remove the ticket, and its timeout releases it even if a path
is missed. Map browsing and discovery never load chunks; only a confirmed request does.

**Commit.** With the area loaded, one server-tick step re-runs every player check, re-resolves the destination
through the same presentation UUID and requires the same marker and a complete sign. It then recomputes the price
and XP, and searches for an arrival spot with `DismountHelper.findSafeDismountLocation(PLAYER, level, pos, true)`.
That check covers dangerous blocks, collision, invalid spawn blocks, and the world border. The search visits the
north, east, south, and west sides of the lower half, then the corners, then the ring at distance 2, each at the
anchor height, one above, and one below. Only then does it debit, teleport with `teleportTo` facing the sign, reset
fall distance and motion, and end the session. Any failing check returns a reason and charges nothing. Monsters at
the destination are never checked.

**Player checks** (`checkPlayer`, in this order): travel enabled; a live, unexpired session in the Overworld;
within 8 blocks of the origin; origin record and blocks present; alive, not sleeping, not a passenger, not a
spectator; no `IN_COMBAT` effect when Block Travel in Combat is on; `NearbyMonsters.preventRest` false at the
player's position when Block Travel Near Monsters is on.

**Price.** `FastTravelCosts.price(from, to, creative)` is zero with Free Travel or in creative mode. Otherwise
it is the base cost plus the number of started stretches of "blocks per XP" of horizontal anchor distance,
computed exactly in integers (smallest k with (k times stretch) squared at least the squared distance), then capped
when a cap above zero is set. Settings live in the `fastTravel` config section: `freeTravel`, `baseXpCost`,
`blocksPerXp` (minimum 1), `maximumXpCost`, `blockInCombat`, and `blockNearMonsters`. All apply immediately.

**XP.** `spendablePoints` is the vanilla closed-form total for the current level plus the floor of progress times
that level's point requirement. `debit` subtracts in whole points, finds the new level by binary search over the
closed forms, and sets level and points with `setExperienceLevels` and `setExperiencePoints`. It lowers
`totalExperience` by the same amount. It never calls `giveExperiencePoints`, so the XP-change event that WitcherCraft
levelling listens to never fires, and float progress cannot drift. The closed forms were checked against summed
per-level requirements up to level 2000. Independently, `CharacterExperienceCalculator` now only counts positive
XP changes, so no XP loss lowers WitcherCraft progress.

**Monster rule.** `NearbyMonsters.preventRest(level, player, center)` mirrors the bed check: any `Monster` within
8 blocks horizontally and 5 vertically whose `isPreventingPlayerRest(level, player)` is true. Vanilla returns true
except for zombified piglins, which require anger at the player. `MeditationCanStart` uses the same helper at the
player's feet, replacing its former 12-block any-monster box. `anyMonster` is the fallback for a non-player
entity.
