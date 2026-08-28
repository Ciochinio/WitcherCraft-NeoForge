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

All hand-written, none owned by an MCreator element (the same "orphaned helper class" pattern as
`PerkEquipLayout` / `PerkTree` / `PerkRegistry`), so MCreator never regenerates them:

- **`GuiPage`** - the page interface. `render(g, x, y, w, h, mouseX, mouseY, partial)` +
  `mouseClicked(x, y, w, h, ...)` / `keyPressed` / `pollTooltip` / `onShown` / `onClose`. The shell
  hands every page the **content region** - the whole area below the navbar - as a design-coords rect;
  the page fills it however it likes. All coords are design-canvas pixels (see 3.3a).
- **`WitcherGuiScreen`** - the shell. Renders against a **fixed virtual design canvas**
  (`DESIGN_W`x`DESIGN_H`, 16:9) scaled uniformly to fit the real screen (see 3.3a). `extractBackground`
  paints opaque black over the whole (gui-scaled) screen, then - inside the `pose().pushMatrix()` /
  `translate` / `scale` transform - blits the background to fill the design canvas; the black shows
  through as **letterbox bars** on non-16:9 screens. `extractRenderState` draws the active page in the
  content region, then the navbar (a **centred group of fixed-width tabs**, icon + label) **on top** so
  page content never covers the tabs, then pops the transform and renders the page's tooltip
  (`pollTooltip`) in screen space at the real cursor. Input is transformed screen->design before
  hit-testing. A `WitcherGuiScreen(String pageId)` constructor opens straight onto a tab.
- **`WitcherGuiLayout`** - the tool-generated data holder for shell **chrome only** (no page content).
  The **design canvas** (`DESIGN_W/H`), a `BG` texture, navbar sizing (`NAV_Y/H`, `NAV_TAB_W`,
  `NAV_GAP`, `NAV_ICON`), a `CONTENT_MARGIN`, and a `NAV[]` of tabs (each `pageId` + label key + **icon
  texture**; positions computed, no rect). Helpers give the navbar tab rects and the **content region**
  (`contentX/Y/W/H()` = the whole area below the navbar). All coords are design-canvas pixels.
- **`WitcherGuiPages`** - the route table: `forId(pageId)` returns the handling page; an id with no
  bespoke class falls back to a cached `PlaceholderPage`. Bespoke pages register in the `CUSTOM` map.
- **`PlaceholderPage`** - a not-yet-built tab: centres a "&lt;Name&gt; - coming soon" in the content
  region. Every navbar page with no bespoke `GuiPage` uses it, until that page gets its own class +
  its own placer tool (the Skills pattern).
- **`PerkPage`** - the perk tree + equip grid, fit-scaled into the content region (see 3.5).
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
shell **chrome only**: the design canvas, the background, and the navbar (tab order / `pageId` / label
/ icon, add / delete / reorder). It shows the content region as a greyed "reserved" box (a reminder
that pages are laid out by their own tools) and **regenerates `WitcherGuiLayout.java` live -> copy ->
paste over the file**. Seeded with the current layout, so `Reset` reproduces the checked-in file.

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

- **Placeholder pages.** Only Skills is a real page; Inventory / Alchemy / Bombs / Map / Glossary are
  `PlaceholderPage` "coming soon" tabs until each is built (own class + own tool). None wire real
  item slots yet - a page that needs live inventory slots must bring a container.
- **Existing standalone screens are not yet folded in.** Alchemy, Meditation, Glossary, etc. remain
  their own container screens; only the perk screen has moved into the shell. They can be re-homed as
  pages later, or bridged (a tab that opens the old screen) in the interim.
- **Placeholder art.** The navbar icons (`textures/gui/nav/<page>.png`) and the background
  (`textures/gui/shell/background.png`) are generated placeholders meant to be replaced. The
  background is fit to the 16:9 design canvas (black letterbox on other screen aspects, no
  screen-aspect distortion), but a replacement image that is not itself 16:9 will be stretched to the
  canvas - author the background at `DESIGN_W:DESIGN_H`.
