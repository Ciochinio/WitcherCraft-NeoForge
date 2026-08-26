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

- **Static perks (the majority):** unconditional flat buffs. Applied ONCE on menu
  close (and login) as **permanent** modifiers (the `entity_add_modifier`
  `permanent` flag = TRUE, so they persist in NBT across relog/respawn). Recompute
  = remove-all-then-add-equipped. Zero per-tick cost.
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
- Step 1a: add the new vars (Section 3) + `RecomputeEquippedPerks` (menu close +
  login): sets `witchercraftEquippedPerk<X>` from the perk-socket vars, and
  applies/removes STATIC perk modifiers as permanent.
- Step 1b: split `PerkModifiersProcedure` - static branches move into the
  menu-close recompute; the ~5 conditional branches become `PerkModifiersConditional`
  (per-tick/event, gated on equipped).
- Step 1c: mutagen bonus math (static -> menu-close recompute, Section 7).
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
