# Session handoff / TLDR (read this first)

**Status: Phase 1's Blockly core is DONE and verified working in-game.** Every
perk (static, conditional, triggered) now gates on EQUIPPED, not learned, and
mutagen color synergy applies correctly. What's left in Phase 1 is the equip
screen (custom Java) - a different, bigger kind of work, not yet started.

**What got built, in order:**
1. Renamed `witchercraftAbilities*` -> `witchercraftPerks*` (806 refs, verified
   by `gradlew compileJava`).
2. Added 65 new player vars: `witchercraftPerkSocket1..12`,
   `witchercraftMutagenSocket1..4`, `witchercraftMutagenOwnedRed/Green/Blue`,
   `witchercraftSelectedPerk`, `witchercraftEquippedPerk<Name>` x45.
3. `RecomputeEquippedPerks<Combat/Alchemy/Signs/General>` (4 procedures, split
   by branch for editor readability) - derive each perk's equipped flag from
   whether its ID sits in any of the 12 perk-socket vars.
4. Split old `PerkModifiers` into: `PerkModifiers` (12 STATIC flat-stat perks,
   applied transiently on recompute, no_ext_trigger) + new
   `PerkModifiersConditional` (5 perks whose condition changes live - bow-in-hand,
   enemy-nearby, day/night - stays on player_ticks, condition logic copied
   verbatim from the original).
5. Re-gated the 8 triggered perks that had a real effect-gate (CripplingStrikes,
   Undying, Refreshment x2, and 5 sign-upgrade perks in the SignCast* procedures)
   from learned to equipped. Finding: ~20 other "perks" (intensity perks, school
   perks) have NO effect implemented yet - only buy/show + debug reset exist.
6. `ApplyMutagenBonus` - per mutagen group, counts equipped perks whose ID falls
   in the socketed mutagen's color range and applies
   `base + perNode * matchCount` to the color's attribute (red->IncreasedDamage,
   green->MaxHealth, blue->SignIntensity).
7. `RecomputeEquippedPerks` is now the MASTER: calls the 4 branch derivations,
   then `PerkModifiers`, then `ApplyMutagenBonus` (6 calls total). Currently
   fired only by a throwaway `DebugRecomputePerksKeybind` (key **P**) - this
   needs to be replaced by real menu-close + player-login triggers once the
   equip screen exists (they don't exist yet as an event source).

**The one real bug hit and the lesson from it (Section 14 has the full
writeup):** MCreator has TWO different "equals" blocks and they are NOT
interchangeable - `logic_binary_ops` EQ is for BOOLEAN equality,
`math_binary_ops` EQ is for NUMBER equality. Using the boolean one to compare
numbers doesn't error, it just silently fails to bind the operands (floating
blocks, empty comparison slots) when reopened in MCreator. Second, more subtle
finding: a working procedure using ONLY constant-valued
`entity_add_modifier`/`entity_remove_modifier` (like the original hand-written
`PerkModifiers`) is safer than one that computes the value via a local variable
+ arithmetic (`math_dual_ops`) - the first `ApplyMutagenBonus` used a local
accumulator and came out broken in MCreator even though the XML was byte-valid
and pattern-matched proven code; rebuilding it with N separate constant-valued
stacking modifiers instead of one computed value fixed it. **Lesson for future
generated procedures: prefer stacking constant modifiers over local-var
arithmetic when the value can be decomposed additively.** If you must compute a
value, build a small isolated test procedure first and have the user verify it
in MCreator before generating the full-size version.

**Everything was generated with bash heredoc/sed scripts building Blockly XML
by hand** (not typed in the MCreator GUI), then JSON-validated via PowerShell
`ConvertFrom-Json`, then verified visually + live in-game by the user after
each chunk. This is fast for repetitive perk-shaped logic but fragile - always
verify counts (block/value tag balance, expected block-type counts) before
declaring a generated procedure done, and always get an in-MCreator visual
check before scaling a pattern up (4-perk proof before 45-perk; single-value
test before the mutagen procedure).

**Where to start next session:** the equip screen (Phase 1 Step 3). This is
custom Java, not Blockly - a different mode of work. Needs: the single-GUI
restructure (tabs for Combat/Alchemy/Signs/General/Mutagens + a fixed right
panel), rendering 12 perk slots + 4 mutagen sockets with dynamic icons (inner
glyph over a static colored frame per Section 5), left-click-hold /
left-click-place / right-click-upgrade interaction with valid-slot highighting,
rank pips (not used yet since perks are still binary), hover tooltips, and
wiring `RecomputeEquippedPerks` to fire on menu-close + player-login instead of
the debug keybind. The perk -> ID -> color table (Section 12) and the "add a
perk" checklist (Section 13) are ready references for this and future perk work.
Also still open: the mutagen ITEMS (learn from item, convert back) - deferred
here because equipping them needs the screen anyway.

**Housekeeping:** the `DebugRecomputePerksKeybind` (key P) is scaffolding -
remove it once the screen provides a real trigger. `witchercraftAbilities*`
named saves/worlds will have lost their perk state from the Step 0 rename (NBT
keys changed) - expected, not a bug, in a dev/test context. Nothing has been
committed to git this whole session (per project rule) - nothing was asked to
be committed either.

---

# WitcherCraft - Skill Tree & Mutagen Rework Plan

> Portable design plan. If a Claude session is lost, paste this whole file into a
> new session to restore full context on the rework. This is a DESIGN doc, not an
> approval to build - follow the project rule in CLAUDE.md: propose before
> implementing, wait for an explicit in-the-moment go-ahead per phase.

## 1. Goal

Recreate the Witcher 3 (and the Gamescom "Skill Tree Revamp" remaster) ability
system in the mod:

- A **branch tree** on the left where you learn/upgrade abilities (Combat,
  Alchemy, Signs, General).
- An **equip grid** on the right (12 slots in 4 groups of 3 + 4 mutagen sockets)
  where learned abilities must be slotted to actually take effect.
- **Mutagens** that socket into each group and grant a bonus that scales with the
  number of same-color abilities equipped in that group.

Reference layout: left = compact branch tree; right = W3 geometry (central
mutagen medallion, two groups each side, each group a vertical column of 3 slots
with its mutagen socket). "Skill tree revamp" remaster keeps this right side
identical and only changes the left side into proper prerequisite-based trees.

## 2. Why this is the right shape (key insight)

The mod ALREADY has the hard parts:

- Each ability is a **global player variable** (currently boolean),
  e.g. `global:witchercraftAbilitiesPreciseBlows`, set true when bought. This is
  player NBT, synced - **not an item**. That structurally solves the user's core
  worry: there is no item entity to dupe, drop, or lose. Learning writes state.
- Buffs are applied by one big `player_ticks` procedure,
  `elements/PerkModifiers.mod.json`, which for each ability does
  `if <state> then entity_add_modifier(...) else entity_remove_modifier(...)`,
  against CUSTOM attributes. Some are conditional (Cold Blood only with no enemy
  nearby, Anatomical Knowledge only with a bow, etc.).
- The left panel already exists as 4 GUIs
  (`CharacterAbilities{Combat,Alchemy,Signs,General}Gui`), built from
  `imagebutton` components with `onClick` -> a buy procedure and
  `displayCondition` -> a show procedure.

**The one conceptual gap:** right now `learned == active` (every bought ability is
always on). Witcher's whole mechanic is that learning is NOT enough - you must
slot the ability. So the core change is: `PerkModifiers` must gate on **equipped**,
not **learned**. `learned` keeps its job (gates tree display + whether you may
slot it).

## 3. Data model (all player variables, zero items)

Naming: the whole system is "Perks", not "Abilities". The ~40 existing
`witchercraftAbilities<X>` player variables were renamed to
`witchercraftPerks<X>` in Step 0 (see Section 8). New vars follow the same
convention, and a var name should say what it holds ("socket" = what goes in it).

- `witchercraftPerkSocket1..12` (number) - perk ID occupying each perk slot,
  0 = empty. Grouped 1-3, 4-6, 7-9, 10-12 = the 4 mutagen groups.
- `witchercraftMutagenSocket1..4` (number) - mutagen type socketed per group,
  0 = empty.
- `witchercraftMutagenOwned<Type>` (number, e.g. Red/Green/Blue) - how many of
  each mutagen you own as a learned count. No slots, no scrolling, no cap.
  Mutagen item -> "learn" -> increments this and consumes the item; convertible
  back to an item (decrement + give item). Counts are uncapped, so you never run
  out of space.
- Perk "learned" state (`witchercraftPerks<X>`): currently boolean; if/when ranks
  are added, switch to a number (0 = not learned, N = rank). Keep this in mind as
  the ranks hedge - see Phase 3.
- `witchercraftEquippedPerk<X>` (boolean, derived) - NOT recomputed each tick.
  Recomputed on **menu close** (and player login) by `RecomputeEquippedPerks`
  from the perk-socket vars (`= perkSocket1==idX or ... or perkSocket12==idX`).
- `witchercraftSelectedPerk` (number) - the currently "held" perk for the
  click-to-place flow (0 = nothing held).

Mutagen types: define **colors x tiers** in the data model, but only wire up the
3 colors (red/green/blue) for now. Tiers sit dormant, ready if wanted, at no cost.

Note the sync cost: all player vars are synced together. This adds a few dozen
vars on top of the existing set. Acceptable.

## 4. Interaction model (custom screen, Minecraft-native)

- **Hover node** -> tooltip: name, description, current + next rank effect, cost,
  prerequisites. (Custom-drawn tooltip; more control than the editor tooltip node.)
- **Right-click node** -> learn/upgrade one rank (spend a point). 0->1 is
  learning, 1->2+ is ranking up. Consistent single rule. Right-click is native to
  MC inventories (place-one / split-stack), and maps to "increment".
- **Left-click a learned node** -> mark it "held" (highlight-in-place, style B):
  the node gets a selected glow/border but STAYS in the tree (equipping is a
  permanent reference/assignment, not a physical move - the ability never leaves
  the tree). Sets `witchercraftSelectedAbility`. Chosen over cursor-follow because
  cursor-follow would show the same icon in the tree and on the cursor at once.
- While an ability is held, **highlight the valid target slots** (glow/pulse the
  legal empty slots) so the player can see where it can go. Important for early
  discoverability.
- **Left-click a slot** -> drop the held ability in (if legal). Click-to-select
  then click-to-place; no drag, no cursor-follow.
- **Left-click an occupied slot with empty hands** -> pull the ability back out
  (free respec, any time). The tree node's "equipped" marker clears.
- Held state is always visible; clicking empty space or pressing Esc cancels it.
- Rank pips: small dots on the node, drawn in code from the rank number
  (filled = current rank). Zero PNGs, works for any max-rank.
- Fallback if right-click ever feels wrong: shift+left-click to upgrade. Ship
  right-click first.

## 5. The Blockly vs custom-Java boundary

Pure Blockly (no Java):

- All state variables and their reads/writes.
- `RecomputeEquippedPerks` (runs on menu close + login): sets equipped flags and
  applies/removes STATIC perk modifiers as permanent modifiers.
- `PerkModifiersConditional` (per-tick/event, only the ~5 conditional perks,
  gated on equipped) - the "conditional modifiers" procedure.
- Mutagen item <-> count conversion (item use procedure + inventory give/remove).
- Mutagen bonus math (part of the menu-close recompute; static).
- Perk learn/upgrade point-spend logic and prerequisite checks.

Requires custom Java (do it once, properly):

- The equip/tree **screen rendering**: an `imagebutton` has one fixed image, so
  drawing the correct ability icon inside an arbitrary slot (12 slots + 4 sockets
  = 16 dynamic icons) needs custom rendering. Also the rank pips, the held-state
  highlight, the valid-slot highlight, the custom tooltip, and left/right-click
  routing.

Art pipeline (W3-style, cuts art work): a perk icon = only the **inner glyph on a
transparent background** (e.g. the sword). The colored **frame/background is
static art per color** that never moves. Rendering draws the inner glyph over the
static colored frame. So equipping a skill = drawing its glyph into the slot's
existing frame; no per-slot full-icon art, and the frame stays put while only the
content is assigned.

Recommended structure: build the screen as a normal MCreator `gui` element (frame,
background art, static click-targets, tabs) so layout stays in the GUI editor,
then set that element's `locked_code: true` and hand-maintain only the dynamic
icon-draw + click-routing + tooltip in its Java. Everything else stays Blockly.

Single-GUI restructure (decided): the current design is one separate Minecraft GUI
per tab. For a tree with movable/slotted nodes this must become **one GUI** with
tab widgets (Combat/Alchemy/Signs/General/Mutagens) switching the left content
while the right equip grid stays fixed. The 4 existing tab GUIs get retired as
layout (their buy/show procedures are reused).

## 6. Node-placer tool (authoring aid)

Because the single-GUI + custom-render approach puts node coordinates in a data
file (not a drag-canvas), build a **standalone HTML node-placer**: one file opened
in a browser that loads a tree background image, shows each node as a draggable
box, draws prerequisite connector lines between nodes, and live-outputs the
coordinate + prerequisite table to paste into the mod's data file. Better than the
MCreator editor for this: one focused window per tree, visualizes the graph, can
validate reachability, and never touches `witchercraft.mcreator` (so no
in-memory-overwrite risk).

## 7. Mutagen bonus formula (PLACEHOLDER - tune later)

Flat base for socketing + increment per connected (same-color) ability in the
group. "Connected" = a matching-color ability equipped in that mutagen's group of
3 (so max 3).

- Green: +2 max HP flat, +1 max HP per connected node (max +5)
- Red: +10% increased damage flat, +5% per node (max +25%)
- Blue: +10% sign intensity flat, +5% per node (max +25%)

These are rough sketches, not final balance. Applied in the `PerkModifiers`
recompute as attribute modifiers named per group/mutagen.

## 8. Phase plan

Build in order; each phase is independently useful. Do NOT start a phase without
an explicit go-ahead.

### Architecture: static vs conditional (the recompute split)

Perks fall in two buckets:

- **Static perks (the majority):** unconditional flat buffs. Applied on menu
  close (and login) as **transient** modifiers (`permanent`=FALSE). Transient
  adds are idempotent (replace by id, no stacking), survive gameplay after close,
  and the login re-apply covers relog - simpler and safer than permanent (no
  remove-then-add dance). Lives in the `PerkModifiers` procedure, now
  `no_ext_trigger` and called by the master after the 4 branch derivations. Zero
  per-tick cost.
- **Conditional perks (~5):** buffs whose condition changes during play, so they
  can't be static. Minecraft attribute modifiers can't self-condition (a modifier
  is either present or absent), so these need a watcher. They live in
  `PerkModifiersConditional`, which runs per-tick/event but only iterates the
  handful of conditional perks, each gated on `witchercraftEquippedPerk<X>`.
  Perks with a value that scales off a live stat also go here.

Conditional perks in the current `PerkModifiersProcedure` (to move into
`PerkModifiersConditional`): Anatomical Knowledge + Crippling Shot (bow/crossbow
in hand), Cold Blood (no enemy nearby), Flood of Anger (enemy nearby), Sun and
Stars (day/night). Everything else there is static.

### Phase 1 - Equip + mutagen system (do first)

The net-new mechanic. Works against the CURRENT tier-gated left trees as-is, so it
doesn't depend on the tree revamp.

- Step 0 (DONE): renamed `witchercraftAbilities<X>` -> `witchercraftPerks<X>`
  across elements, registry, and Java (806 refs). Gradle `compileJava` passed.
  Note: NBT keys changed, so existing test saves lose learned-perk state.
- Step 1a (DONE - derivation): new vars added. `RecomputeEquippedPerks` is a
  MASTER procedure that calls 4 per-branch procedures
  (`RecomputeEquippedPerksCombat/Alchemy/Signs/General`), split for readability.
  Each branch proc sets its perks' `witchercraftEquippedPerk<X>` = OR over
  `PerkSocket1..12 == id`. Wired to a temporary `DebugRecomputePerksKeybind`
  (key P) for testing; menu-close/login trigger + static-modifier application
  still TODO.
- Step 1b (DONE): split `PerkModifiers` - it now holds only the 12 STATIC
  flat-stat perks, equipped-gated, `no_ext_trigger`, called by the master. The 5
  CONDITIONAL perks moved to new `PerkModifiersConditional` (`player_ticks`,
  isremote-guarded, equipped-gated, condition logic preserved verbatim).
- Step 1b-tail (DONE): re-gated the triggered/active perks from learned ->
  equipped. Finding: only 8 of the ~28 actually gate an effect on the learned
  flag today - CripplingStrikes (`BleedHit`), Undying (`UndyingEffectActive`),
  Refreshment (`DecoctionUsed`, `PotionUsed`), and the sign upgrades Delusion /
  ExplodingShield / FarReachingAard / Firestream / MagicTrap (`SignCastKeyPress`,
  `SignCastKeyRelease`, `SignCastHold`, `SignCastHoldCost`). Those reads were
  swapped to `witchercraftEquippedPerk<X>`. The remaining ~20 (intensity perks
  AardIntensity/IgniIntensity/QuenIntensity/... and school perks) have NO effect
  gate wired yet - they appear only in `DevClear` (debug reset, correctly left on
  learned) and their buy/show procs. When their effects get implemented, gate
  them on equipped per Section 13.
- Step 1c (DONE - bonus math): new `ApplyMutagenBonus` procedure, called by the
  master after `PerkModifiers`. Mutagen socket encodes color (1 red / 2 green /
  3 blue / 0 empty). Per group: removes its `mutagen_group<G>` modifier from all
  3 attributes, then if a mutagen is socketed, counts group perks whose ID falls
  in that color's hundreds range (matches, 0-3; neutrals never match) and adds
  base + perNode*matches to the color's attribute (red->CUSTOM:IncreasedDamage,
  green->MAX_HEALTH, blue->CUSTOM:SignIntensity). Values per Section 7.
  Implementation note: uses only constant-valued add/remove modifiers (no local
  var, no arithmetic) - base and each matching slot are SEPARATE stacking
  modifiers (`mutagen_g<G>_base`, `_s1/_s2/_s3`); per group all 4 names are
  removed from all 3 attributes each recompute, then the active color re-adds.
  The mutagen ITEMS (learn/convert-back) are deferred to the equip-screen step.
- Also: the perk -> ID -> color table (first artifact), used by the recompute,
  the screen, and the node-placer.
- Step 3 of Phase 1: the equip screen - right panel (12 perk slots + 4 mutagen
  sockets, W3 geometry), custom render + click routing (Sections 4, 5). Built last,
  on a proven mechanic. Left side can temporarily reuse existing tab GUIs.

Migration caveat: the moment gating flips to equipped, existing saves with
everything-learned go dark until slotted. Intended scarcity, not a bug - flag it.

### Phase 2 - Left-tree revamp

1. Restructure to a single GUI with tab widgets + fixed right panel (Section 5).
2. Replace tier gates (`CharacterAbilitiesTier2/3`, points-in-branch) with
   per-node prerequisite checks (`canBuy = haveSkillPoints && parentLearned`).
3. New tree layout + connector art per branch; place nodes via the node-placer
   tool (Section 6).
4. Topology (which node requires which) is design content the USER defines - do
   not invent it. Remaster node data isn't available yet, so this waits on the
   user's tree design.

### Phase 3 - Ranks (optional)

Only if wanted. Cheap because "learned" is already stored as a number:

1. Raise the per-ability rank cap.
2. Make `PerkModifiers` scale the buff value by rank (`value = base * rank`).
3. Render rank pips (already code-drawn) and show current/max in the tooltip.

## 9. Existing elements (as-is, for reference)

- `elements/PerkModifiers.mod.json` - the big buff recompute (gate swap target).
- `CharacterAbilities{Combat,Alchemy,Signs,General}Gui` (+ their `...GuiOpen`) -
  the current per-tab GUIs (retired as layout in Phase 2; buy/show procedures
  reused).
- Skill-point procedures: `CharacterAbilitiesSkillPointsAvailable`,
  `CharacterAbilitiesSkillPointCheck`, `CharacterAbilitiesSkillPointUsed`,
  and per-branch `...SkillPointsUsed`.
- Tier gates: `CharacterAbilitiesTier2/3`, `CharacterAbilitiesCombatTier2/3`,
  `...AlchemyTier2/3`, `...SignsTier2/3` (replaced in Phase 2).
- ~40 ability vars `witchercraftAbilities<Name>` + `witchercraftAbilitiesLearned`.
- Per-ability buy/show procedures, e.g. `MuscleMemoryEffect` / `MuscleMemoryShow`.

## 10. Open decisions / to confirm

- Right-click-to-upgrade: LOCKED (shift+left-click remains the fallback if it
  feels wrong in play).
- Equip interaction: LOCKED to style B (highlight-in-place) with valid-slot
  highlighting while held.
- Final mutagen bonus values (Section 7 are placeholders).
- Whether mutagen "connected" counts only matching-color abilities (assumed yes)
  or any ability in the group (assumed no).
- Ranks: yes/no eventually (data model already hedged either way).
- Tree topology + art: pending the user's design (Phase 2).

## 12. Perk registry (ID / color) - canonical

45 perks total, branch = color. IDs are range-encoded so color is derivable:
`color = floor(id / 100)` -> 1 = red, 2 = green, 3 = blue, 4 = neutral; 0 = empty
slot. Ranges have room to grow without renumbering. The perk-socket vars store
these IDs; `RecomputeEquippedPerks`, the screen, and the node-placer all use this
table. Var name = `witchercraftPerks<Name>`.

DECISION PENDING: General perks are proposed **colorless/neutral** (color 4) -
they can be equipped but never count toward a mutagen's color synergy (W3
behavior). Confirm, or assign them a color.

Combat / RED (101-115): 101 AnatomicalKnowledge, 102 ColdBlood, 103 CripplingShot,
104 CripplingStrikes, 105 CrushingBlows, 106 DeadlyPrecision, 107 Defence,
108 FleetFooted, 109 FloodOfAnger, 110 MuscleMemory, 111 PreciseBlows,
112 RazorFocus, 113 StrengthTraining, 114 SunderArmor, 115 Undying.

Alchemy / GREEN (201-209): 201 ClusterBombs, 202 DelayedRecovery, 203 Efficiency,
204 HunterInstinct, 205 PoisonedBlades, 206 ProtectiveCoating, 207 Pyrotechnics,
208 Refreshment, 209 SideEffects.

Signs / BLUE (301-315): 301 AardIntensity, 302 AxiiIntensity, 303 Delusion,
304 Domination, 305 ExplodingShield, 306 FarReachingAard, 307 Firestream,
308 IgniIntensity, 309 MagicTrap, 310 Pyromaniac, 311 QuenDischarge,
312 QuenIntensity, 313 ShockWave, 314 SustainedGlyphs, 315 YrdenIntensity.

General / NEUTRAL (401-406): 401 BearSchool, 402 CatSchool, 403 Gourmet,
404 GriffinSchool, 405 SunAndStars, 406 SurvivalInstinct.

Conditional perks (go in `PerkModifiersConditional`, not the static recompute):
101 AnatomicalKnowledge, 103 CripplingShot (bow/crossbow in hand); 102 ColdBlood
(no enemy nearby); 109 FloodOfAnger (enemy nearby); 405 SunAndStars (day/night).
All others are static.

## 13. How to add a new perk (checklist)

The equip rework makes adding a perk more involved than before. Steps:

1. **Branch + ID.** Decide the branch (Combat=red, Alchemy=green, Signs=blue,
   General=neutral) and pick a free ID in that branch's hundreds range
   (Section 12: red 100s, green 200s, blue 300s, neutral 400s).
2. **Learned var** `witchercraftPerks<Name>` (logic, player_persistent, false) -
   the "owned" flag. Set true by the buy logic.
3. **Equipped var** `witchercraftEquippedPerk<Name>` (logic, player_persistent,
   false) - the "active" flag, derived by the recompute.
4. **Tree GUI button** - imagebutton with `onClick` = a `<Name>Effect` buy
   procedure and `displayCondition` = a `<Name>Show` procedure (mirror an
   existing perk in the same branch GUI).
5. **Derivation line** - in the matching `RecomputeEquippedPerks<Branch>`
   procedure, add: set `EquippedPerk<Name>` = OR over `PerkSocket1..12 == <id>`.
   (Use the generator pattern; see block gotcha below.)
6. **Apply its buff, gated on the EQUIPPED flag (not learned):**
   - Static flat stat -> the menu-close static modifier application.
   - Triggered/active mechanic -> its own event procedure, gated on
     `witchercraftEquippedPerk<Name>`.
   - Conditional -> `PerkModifiersConditional`, gated on equipped AND its
     condition.
7. **Registry table** - add name/ID/color to Section 12.
8. **Icon** - a transparent inner-glyph PNG for the equip screen (frame is
   drawn by color, Section 5).
9. **GDD** - document the perk and its values.

## 14. Block-type gotchas (learned the hard way)

- **Comparing NUMBERS uses `math_binary_ops`** (OP=EQ/NEQ/GT/GTE/LT/LTE), NOT
  `logic_binary_ops`. `logic_binary_ops` EQ is for BOOLEAN equality and will not
  bind number inputs - MCreator silently spills the operands / orphans blocks.
  The number comparison outputs a boolean, which you then feed into
  `logic_binary_ops` OR/AND. (This is why the first recompute build failed.)
- `logic_binary_ops` AND/OR conventionally carry `inline="false"` in this project
  (vertical layout); omitting it renders inline/horizontal. Layout only - does not
  affect connection validity.
- Player-var reads/writes need `<mutation ... is_player_var="true"
  has_entity="true">` plus a `<value name="entity"><block
  type="entity_from_deps"></block></value>` child.

## 11. Project conventions to respect (from CLAUDE.md)

- Edited via MCreator GUI, not hand-written Java. Three files stay in sync:
  `elements/<Name>.mod.json`, the `witchercraft.mcreator` registry, generated Java.
- If MCreator is running, stage registry entries in
  `.claude/pending-mcreator-registry/<Name>.json` instead of editing the live
  `witchercraft.mcreator`.
- `_fv: 89`. Reuse existing Blockly block types from proven elements / installed
  MCreator plugin files - don't invent block types.
- No em-dashes, no section-sign (U+00A7).
- Name every procedure/element touched, with its MCreator browser path, per change.
- Keep `GAME_DESIGN_DOCUMENT.md` updated in the same round as gameplay changes.
- Never commit unless explicitly asked in the moment.
