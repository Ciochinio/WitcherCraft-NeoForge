# WitcherCraft - Game Design Document (Living Draft)

This document describes the design of WitcherCraft: what each system is meant to do,
how it should feel to play, and the rules that govern it. It records the current,
committed design rather than every idea considered during development.

WitcherCraft is not a standalone game and does not replace Minecraft's core loop. It is
an addition layered on top of Minecraft: the player still mines, builds, explores, and
survives, but gains the toolkit of a Witcher - Signs, Alchemy, oils, a custom combat and
progression model, School armor, and a Bestiary of Witcher monsters to hunt. Every system
below is designed to slot into ordinary Minecraft play, not to wall it off.

Dated changes are recorded in `CHANGELOG.txt`.

Balance values likely to change during tuning are written in **bold** throughout this
document, so the numbers to revisit are easy to spot.

## Table of Contents

1. [Summary](#1-summary)
   - [Design Pillars](#design-pillars)
   - [Key Features](#key-features)
   - [What Players Will Talk About](#what-players-will-talk-about)
   - [At a Glance](#at-a-glance)
2. [Signs](#2-signs)
   - [The Fantasy](#the-fantasy)
   - [Selecting and Casting](#selecting-and-casting)
   - [Cost, Cooldown, and Stamina](#cost-cooldown-and-stamina)
   - [Sign Intensity](#sign-intensity)
   - [The Five Signs](#the-five-signs)
   - [Alternate Signs](#alternate-signs)
3. [Alchemy](#3-alchemy)
4. [Toxicity](#4-toxicity)
5. [Combat and the Damage Model](#5-combat-and-the-damage-model)
6. [Character Progression](#6-character-progression)
7. [Witcher School Armor Sets](#7-witcher-school-armor-sets)
8. [Bestiary and Monsters](#8-bestiary-and-monsters)
9. [World and Utility Systems](#9-world-and-utility-systems)
10. [Content Appendix](#10-content-appendix)

---

## 1. Summary

### Design Pillars

WitcherCraft turns a Minecraft character into a Witcher. Four ideas hold the whole thing
together:

- **The Witcher toolkit, faithfully.** Signs, potions, decoctions, oils, and bombs work the
  way they do in The Witcher, fitted to Minecraft's controls and pace. Aard shoves, Igni
  burns, oils are matched to the thing you're fighting. They should read as Witcher tools, not
  as reskinned enchantments.

- **Preparation over spam.** A Witcher wins by showing up ready. Toxicity limits how much
  alchemy you can carry into a fight, oils only pay off when you know what you're hunting, and
  Signs run on stamina. The strong play is picking the right tools beforehand, not hammering
  every button during.

- **A build you commit to.** The skill tree is capped, the School sets pull in different
  directions, and combat runs on stats you invest in. You can't have all of it. A Sign mage,
  an alchemy bruiser, and a crit swordsman are all real builds, and they don't play alike.

- **Content that grows one entry at a time.** Every Sign, potion, oil, monster, perk, and
  armor set is a self-contained piece of content. New ones drop in without touching the
  systems underneath, so the mod grows a beast or a brew at a time.

### Key Features

#### Witcher Signs

Five Signs, each its own active ability with its own particles, cast, and cooldown. You open
the Sign menu, pick one, and cast it with a single key. Signs run on stamina. Later perks swap
in upgraded "alternate" versions that change how a Sign plays, not just its numbers.

#### Alchemy Anywhere

Potions, decoctions, oils, and bombs, all brewed from in-game menus with no crafting table.
Potions are timed buffs. Decoctions are stronger and last far longer. Oils coat your weapon to
hit one monster category harder. Bombs are thrown as projectiles and go off in an area.

#### Toxicity

Every potion and decoction adds Toxicity. Go over your maximum and your health starts draining
until it comes back down or you clear it. It's the ceiling that keeps alchemy from being free
power, and you can raise that ceiling through the Alchemy tree.

#### A Custom Combat Model

WitcherCraft runs its own damage math instead of vanilla's: flat bonus damage, percent
increased damage, crit chance and crit multiplier, life steal, and status effects like Bleed.
Oils feed extra damage into that math when they match the target's category.

#### Persistent Character Building

You level up, earn skill points, and spend them across four tabs: Combat, Signs, Alchemy, and
General. The cap and the one-point-per-level economy mean you'll never fill the whole tree, so
where you spend it matters.

#### Witcher School Armor

Six full armor sets, one per Witcher School. Wear a complete set and its School Effect kicks
in, each leaning toward a different playstyle: crit damage, Sign intensity, survivability, oil
strength, and so on.

#### A Living Bestiary

The Witcher monsters, sorted by category: Necrophages, Vampires, Specters, Relicts, Draconids,
and more. Each has a Bestiary entry with its lore, stats, and the oil it's weak to.

### What Players Will Talk About

- Spotting a monster's category, coating the right oil, and watching something that felt
  spongy fold in seconds.
- A Sign-focused Griffin witcher who opens with Igni or Aard and reshapes the whole fight.
- Riding the Toxicity line: one more decoction than feels safe, because it's the one that wins.
- Committing hard to a School and a tree, then rerolling into a build that plays nothing
  like it.

### At a Glance

| | |
|--|--|
| Platform | Minecraft 1.21, NeoForge |
| Type | Content and systems addition (not a total conversion) |
| Core additions | Signs, Alchemy, Toxicity, custom combat, skill tree, School armor, Bestiary |
| Progression cap | Level **30**, **1** skill point per level |
| Setting | The Witcher universe, inside the Minecraft world |

---

## 2. Signs

### The Fantasy

Signs are the witcher's battle magic: short, sharp bursts that buy you space, pin a crowd, or
pull a fight back from the edge. You don't stand off and sling them - they happen in the middle
of melee. A shove to get room. A shield right before a hit lands. A gout of fire into a pack.
Every Sign reads instantly from its particles, and a good cast changes how the fight is going
instead of just adding a little damage.

### Selecting and Casting

There's one cast key for all five Signs, so casting splits into choose, then cast:

1. **Open the Sign menu** with `Tab`.
2. **Pick a Sign.** It becomes your active Sign and the others clear; the menu closes. That
   Sign stays active until you swap, so you only come back here to change Signs.
3. **Cast** with `R`. This fires whichever Sign is currently active.

Only one Sign is active at a time. Casting stays a single keypress, and choosing which Sign to
carry in is its own small decision before the fight.

### Cost, Cooldown, and Stamina

Two things gate a cast, and a third governs the ones you hold:

- **Stamina.** Signs run off the stamina bar, which is Minecraft's hunger bar reskinned as a
  white stamina meter. A cast needs a minimum amount available; below that, it's refused ("not
  enough stamina") and nothing is spent. A successful cast drains some stamina, and a
  cost-reduction effect can soften that.
- **Cooldown.** After a cast, a short shared cooldown (around **two seconds**) runs on the
  HUD. Cast during it and you get "can't cast yet." It covers every Sign, so switching doesn't
  dodge it.
- **Upkeep.** Some alternates aren't cast and forgotten, they're *held*: keep the Sign key down
  and the Sign keeps running. Those bill a per-second upkeep out of stamina for as long as they
  last. Holding for the first second is the wind-up that tells a hold apart from a tap, the
  second after that is free, and every second beyond it costs. Each held Sign sets its own rate
  - **Fire Stream 4/second**, **Active Shield 2/second** - and falling to the stamina minimum
  ends the hold on its own with "not enough stamina hold." A held Sign is bounded by your bar
  rather than by a timer, so keeping one up is a live decision about what you won't be able to
  cast afterwards.

If a Sign can't find anything to act on, it refunds its cost where that makes sense - Axii, for
instance, gives it back when there's no target.

### Sign Intensity

Sign Intensity is how strong your Signs are, boiled down to one number. It isn't stored
anywhere; it's recomputed constantly from whatever's boosting it right now. Signs-tree perks,
School armor, and a few alchemy effects all add into it, which means anything that raises
Intensity takes hold the moment it's active and drops off the moment it ends.

A few things that feed it today:

- **Petri's Philter** - a potion that exists mainly to spike Intensity for a while.
- **Foglet Decoction** - adds Intensity while it's raining.
- **Griffin armor** and Signs-tree perks - steady, always-on bonuses.

It shows on the character screen, so a Sign build can see what it's actually getting.

### The Five Signs

Each Sign owns one job.

| Sign | Role | Behavior |
|------|------|----------|
| **Aard** | Displacement | A telekinetic cone that throws enemies away from the caster and staggers a group in front of them. |
| **Igni** | Area damage | A wide, piercing stream of flame thrown forward that burns everything it passes through. |
| **Quen** | Defense | A timed shield that absorbs a pool of damage, scaling with Sign Intensity, shown as shield bubbles above your health and marked by a particle spinning around the caster. |
| **Yrden** | Control | A magic trap on the ground that slows and holds enemies caught inside it. |
| **Axii** | Crowd control | A targeted charm that freezes a single enemy in place; refunds its cost when there's no target. |

**Aard** is the panic button. A burst of force in the direction you're facing that throws
nearby enemies back and buys room when you're swarmed.

**Igni** is your main damage Sign. A dense stream of fire straight ahead that pierces through
enemies instead of stopping at the first, setting them alight. Best when they're lined up.

**Quen** is the defensive Sign. Casting it grants a pool of absorption (**base 8**, scaling with
Sign Intensity) that eats incoming damage until it's spent or the **10-second** timer runs out -
whichever comes first. It absorbs virtually everything (melee, projectiles, fire and burning,
explosions, magic) but deliberately lets fall, freezing, drowning, starvation, and the void
through untouched, since a magic ward shouldn't cushion a fall. A hit bigger than what's left
partially drains the shield and lets the rest through as real damage, same as an absorption
effect - so the shield doesn't just "not work" on a big hit, it softens it. While it's up, a
row of yellow shield bubbles appears above your health, one bubble per **2** points absorbed and a
small bubble for an odd leftover, emptying as the pool is eaten, and a single particle
orbits the caster with a slight vertical bob; both cut out the moment the shield breaks or expires,
with a clear "Quen broke!" message so there's no ambiguity about whether it's still up. A gold
action-bar readout of the exact remaining pool runs alongside the bar as a debugging aid. It's what you throw
up before a hit you can't dodge.

**Yrden** is the crowd-control Sign. It lays a ring on the ground; enemies inside it are slowed
and held while you reposition or focus someone down. Best against fast enemies and in
chokepoints.

**Axii** is the single-target lock. It fires at whatever you're aiming at, and if there's an
enemy there it charms them, freezing them in place so you can hit them freely or peel one off.
Aim at nothing and it tells you so and hands your stamina back.

### Alternate Signs

Every Sign has an upgraded alternate form unlocked in the Signs tree. Own the perk and casting
the Sign fires the alternate instead - same key, same slot, different behavior. They change how
a Sign plays rather than just pushing its numbers, and they're still being finalized.

| Base Sign | Alternate | Change in behavior |
|-----------|-----------|--------------------|
| Aard | **Far-Reaching Aard** | Widens Aard from a frontal cone into a full 360-degree radial blast, shoving every nearby enemy away from the caster at once to clear space on all sides. |
| Igni | **Fire Stream** | An enhanced, sustained flame projection. |
| Quen | **Active Shield** | A held, channeled shield that starts small and charges the longer you keep it raised, paid for in stamina, instead of the fire-and-forget timed pool. |
| Yrden | **Magic Trap** | An upgraded trap variant of the Yrden zone. |
| Axii | **Alternate Axii** | An upgraded charm; like base Axii, it refunds its cost when it finds no target. |

Because the alternates sit behind perks, they double as build payoffs: go deep into the Signs
tree and you don't just get stronger Signs, you get ones that do something new.

**Active Shield** is the one built out in full so far, and it inverts how Quen is played. Hold
the Sign key and the shield goes up small - **2** points of absorption - then adds **2** more
every second you keep it raised, scaling with Sign Intensity the way the base Sign does. It
draws on the same shield pool as normal Quen and reads on the same bubble HUD, so raising it
while a normal Quen is still up tops that shield up instead of throwing it away. Blocking is
identical to the base Sign: hits eat into the pool, which then charges back up, and a hit
bigger than what's left shatters it and lets the remainder through. What bounds it is stamina:
**2 per second** for as long as you hold. That's the trade - your entire stamina bar for a wall
that keeps getting stronger as long as you can pay for it, where base Quen is a fixed buffer
you throw up and forget. Let go, run dry, or eat a hit bigger than the pool and it drops with an "Active Shield
down" message. A shattered shield ends the hold outright rather than popping straight back up,
so recovering from a break costs you a fresh press of the key.

---

## 3. Alchemy

Alchemy is the witcher's prep kit: the stuff you make and carry so you're ready before the
fight starts. It comes in four families - potions, decoctions, oils, and bombs - and all of it
is brewed and used without a crafting table. The full item lists, with each entry's exact
effect and numbers, live in the [Content Appendix](#10-content-appendix); this section is about
how the system works.

Potions and decoctions share one cost, [Toxicity](#4-toxicity), which is what keeps you from
drinking everything at once. Oils and bombs cost no Toxicity.

### Brewing

Brewing happens in the alchemy menu, opened from the pause/system menu. There's no crafting
table, furnace, or brewing stand - the menu is the whole workbench.

The grid has a base slot plus ingredient slots. What you put in the **base slot** decides what
family you're brewing:

| Base reagent | Makes |
|--------------|-------|
| **White Gull** | Potions and decoctions |
| **Saltpeter** | Bombs |
| **Tallow** | Oils |

The remaining slots take the Witcher alchemical substances - Vitriol, Rebis, Aether, and the
rest. Fill a recipe exactly and hit brew: the ingredients are consumed and a stack of the
result appears in the output. Recipes yield in batches rather than one at a time - for
example, Swallow brews from White Gull + Rebis x**2** + Vitriol + Aether and produces **3**
doses.

### Potions

Potions are timed buffs you drink before or during a fight. Each lasts **3600 ticks (3
minutes)** at base, scaled up by your Potion Duration stat. Toxicity cost is set per potion -
most sit in the **10** to **25** range - rather than being a flat rate. With the Refreshment
perk, drinking a potion also heals **10%** of your max health.

They cover the situational toolkit a witcher expects: emergency healing, Sign power, night
sight, elemental protection, and so on. The full roster and per-potion effects are in the
appendix.

**Cat is deliberately not night vision.** Vanilla night vision lies about light level: it
flattens the world to maximum brightness, kills the atmosphere, and there is already a potion
for it. Cat instead re-grades what your eyes already receive - the whole view collapses to
greyscale and exposure is pushed hard, so dark terrain lifts into readable mid-greys while
anything already bright clips to white. It never changes light levels, mob spawning, or fog, so
it stays weaker than night vision in true darkness and reads as a different tool rather than a
reskin. It costs you colour for the duration, which is the point: you see shapes and movement,
not detail. Because it is a camera grade, it is drawn on your client only and cannot be seen by
other players.

Its drawback is enforced by the same grade rather than by a separate rule. The exposure lift is
a flat multiplier, so in daylight everything above roughly a third brightness clips out and the
screen washes to near-white: you are genuinely blinded outdoors during the day, exactly as Cat
blinds Geralt. This is intended and is the reason Cat needs no Toxicity surcharge or timer
penalty on top of its **10**. It is a night-and-underground potion, and drinking it at the wrong
time punishes you immediately and obviously.

### Decoctions

Decoctions are the heavy, long-haul version of a potion. They last **7200 ticks (6 minutes)**
at base (also scaled by Potion Duration) and cost **50** Toxicity each - double a potion - so
they're something you commit to for a long stretch rather than pop reactively. Refreshment
heals **20%** of max health when you drink one.

Because each one eats half again as much of your Toxicity budget, how many decoctions you can
run at once is a direct function of how far you've pushed your Overdose Threshold. A dedicated
alchemist stacks several; everyone else picks one. Full list in the appendix.

### Oils

Oils coat your blade to hit one monster category harder. To apply one, hold the oil in your
**off-hand** and a sword in your **main hand**; the oil enchants the sword. Only one oil can be
on a weapon at a time - applying a new one strips the old. Each application consumes **1** oil
and puts oils on a **200-tick (10-second)** cooldown, and the coating stays on the blade until
you replace it.

Every oil is tied to a monster category (Beast, Insectoid, Necrophage, Vampire, Specter,
Ogroid, Draconid, Relict, Cursed, and Hanged Man's Venom). Hit a monster whose category
matches the oil on your blade and the oil's bonus damage lands; hit anything else and it does
nothing. The bonus itself is resolved in the [combat model](#5-combat-and-the-damage-model),
which reads a short-lived "correct oil" flag set the instant you land a matching hit. Knowing
what you're fighting - from the [Bestiary](#8-bestiary-and-monsters) - is what makes oils pay
off.

### Bombs

Bombs are thrown, not drunk. Each is a projectile entity that flies where you aim and detonates
on impact into an area effect - a burst of particles plus its payload, with no terrain damage.
Payloads differ by bomb: some deal direct damage, others apply a status across the blast.
Dimeritium Bomb, for instance, hits everything within **5** blocks for **12** damage on its own
"Bomb" damage type. The full set and each bomb's payload are in the appendix.

---

## 4. Toxicity

Toxicity is the meter that stops alchemy from being free power. Every potion and decoction you
drink pushes it up; sit too high and it starts killing you.

- **Gaining it.** Every decoction adds **50**. Potions vary by recipe, from **10** (Cat) up to
  **25** (Swallow, Thunderbolt, Full Moon, White Raffard's). It's tracked as a single global
  value and shown on a dedicated HUD bar, sitting above the hunger bar, that fills as you climb
  and turns red once you cross the Overdose Threshold.
- **Losing it.** Toxicity decays on its own, ticking down by **1** every **3 seconds**, so
  it's a temporary debt rather than a permanent one. Stop drinking and it drains back to zero.
- **The Overdose Threshold.** This is your ceiling. Stay under it and there's no penalty. Reach
  or pass it and you overdose: every tick you take magic damage that scales with how far over
  you are, `1 + round(toxicity / threshold)`. Sitting just over the line chips about **2** per
  tick; push to double the threshold and it's **3**, and so on. The damage keeps coming until
  Toxicity decays back under the line or you clear it.
- **White Honey.** The reset button. Drinking it wipes every active potion and decoction effect
  and sets Toxicity straight to **0** (and triggers the Refreshment heal if you have it). The
  catch is that it strips your buffs along with the poison, so it's an emergency vent, not a way
  to detox and keep your decoctions.
- **Raising the ceiling.** The Overdose Threshold grows through the Alchemy skill tree. Investing
  there is what lets a committed alchemist run several decoctions at once where an unspecced
  character can only manage one.

The result is a push-your-luck resource: the strongest alchemy setups live just under the
threshold, and the interesting decision is always whether one more brew is worth walking that
close to the edge.

---

## 5. Combat and the Damage Model

WitcherCraft doesn't leave combat to vanilla's math. When you land a melee hit, the mod takes
your weapon's base damage and runs it through its own model, so every witcher stat - crits,
life steal, oils, perks - actually changes the number that lands.

### The core formula

On a hit, the weapon's base damage is combined with your stats:

```
hit         = (base + AdditionalDamage) x (1 + IncreasedDamage%)
on crit     = hit x CritDamage%            (crit if roll of 1-100 <= CritChance)
life steal  = damage dealt x LifeSteal%    (healed back to the attacker)
```

- **Additional Damage** - flat damage added before the percentage multiplier.
- **Increased Damage** - a percentage multiplier applied to the total.
- **Crit Chance** - rolled **1-100** on every hit; a roll at or under your chance is a crit.
- **Crit Damage** - the multiplier a crit applies, written as a percent (e.g. **150** means x1.5).
- **Life Steal** - the percentage of damage dealt that returns to you as health.

The result lands as a normal player attack. (One guard: the Ender Dragon is skipped, because it
doesn't take this damage type correctly.)

### Stats are always live

Every combat stat is a base value plus whatever is boosting it right now - perks, School armor,
and active potions or decoctions - the same live-aggregation model as
[Sign Intensity](#sign-intensity). Each stat is a real attribute the game itself tracks, so a
bonus applies the moment its source does and disappears the moment the source does, with no
recalculation step in between and nothing to fall out of sync. A few current examples:

- **Precise Blows** adds **12** crit chance, **Crushing Blows** adds **8**, and **Anatomical
  Knowledge** adds **10** while you're holding a bow or crossbow.
- **Katakan Decoction** adds **10** crit chance. **Thunderbolt** during a thunderstorm sets crit
  chance to **100** - every hit crits.
- **Muscle Memory** adds **3** flat damage. **Cold Blood** adds **5** while no enemy is near;
  **Flood of Anger** adds **5** while one is.

The character screen surfaces the full stat line:

| Stat | What it does |
|------|--------------|
| Additional Damage | Flat damage added before multipliers |
| Increased Damage | Percentage multiplier on total damage |
| Crit Chance | Percent chance a hit crits |
| Crit Damage | The crit multiplier, as a percent |
| Life Steal | Percent of damage dealt healed back |
| Oil Damage | Scales the bonus from a correctly matched oil |
| Attack Speed | How fast the weapon swings |
| Dodge Chance | Percent chance to fully negate an incoming hit |
| Reflect Damage | Returns a share of damage taken to the attacker |

### Passive regeneration

Health and stamina both trickle back on their own, and both are live-aggregated the same way the
combat stats are: a base value you can tune directly, plus whatever is currently boosting it.

- **It's a rate, not a lump.** Both are expressed as **per second** and paid out once a second,
  so the bars creep rather than jump. Each contributing source is worth **0.3333/second** -
  **Swallow**, **Troll Decoction**, and **Sun and Stars** in daylight for health; **Gourmet**,
  **Tawny Owl**, **Sun and Stars** after dark, and **Werewolf Decoction** on a clear night for
  stamina. **Grave Hag Decoction** is the odd one out: it scales with kills, adding
  `round(kills / 2) x 0.3333` per second, and only while you're in combat.
- **Combat halves it.** Being in combat multiplies the finished rate by **0.5**, for health and
  stamina alike. Regeneration never stops entirely, it just slows to where it can't out-heal a
  fight, so disengaging is still the way to recover.
- **Base rates are a tuning knob.** Base health and stamina regeneration both sit at **0**, so
  today every point of regen is something you earned from a perk, potion, or decoction. They
  exist so flat regen can be dialled in globally later without touching any of the sources.
- **Stamina banks its fractions.** The stamina bar is Minecraft's hunger bar, which only holds
  whole points, so fractional regeneration accumulates in a buffer and is spent a point at a
  time, at most one per second. The buffer empties whenever the bar is full, so stamina can
  never be banked past the cap and then dumped in all at once.

The character screen reads these straight off the live values, so the **hp/s** and stamina
figures it shows are the actual per-second rates, combat multiplier included.

### Oils in combat

This is where the oils from [Alchemy](#oils) pay off. When you hit a monster whose category
matches the oil on your blade, a short-lived "correct oil" flag is set. While it's up, your
Additional Damage gains **+4 x (1 + Oil Damage%)** - so the oil bonus rides on top of the normal
formula, and the Oil Damage stat (from perks and gear like the Viper School) scales how big it
gets. Hit the wrong category and the flag never sets, so the bonus is zero.

### Dodge

When something hits you, the mod rolls **1-100** against your Dodge Chance. Beat the roll and the
hit is cancelled outright - no damage taken - and dodge goes on a **60-tick (3-second)** cooldown
so it can't fire on every incoming blow.

### Bleed and status effects

Bleed is a damage-over-time status that ticks on its own, independent of your swings. It's applied
on hit by certain sources - for example the **Crippling Strikes** perk bleeds the target for
**100 ticks (5 seconds)** - and deals `1 + level` magic damage once per second for as long as it
lasts. The damage is driven by the status's own remaining duration, so re-applying Bleed mid-fight
refreshes the wound and restarts its clock rather than stacking a second source of damage. Because
it rides on the status effect itself, Bleed works identically on monsters and on players. Other
combat statuses come from their own sources: burning from Igni, slows from Yrden and Axii, and the
payloads of individual bombs and decoctions.

### Reflect

Reflect Damage returns a portion of the damage you take to whoever dealt it, giving tank-leaning
builds a way to punish being hit.

---

## 6. Character Progression

You get stronger by playing. The witcher side of your character levels up alongside the ordinary
Minecraft one, and spending what you earn is where builds are made.

### Experience and levels

WitcherCraft rides on Minecraft's own experience. Every point of vanilla XP you gain also feeds a
separate WitcherCraft experience pool; fill the pool and you level up, and it resets for the next
level. The bar you have to fill grows in brackets, so early levels come quickly and later ones
take real time:

| Levels | Added to the next level's requirement |
|--------|---------------------------------------|
| 1-10 | **+25** |
| 11-20 | **+50** |
| 21+ | **+100** |

The cap is level **30**.

### Skill points

Each level grants **1** skill point. The points you have to spend are simply your level minus the
perks you've already learned, so there's no hidden bank - what you've earned is what you see. A
full run to 30 yields **30** points against more than **30** perks, so you cannot buy everything.
That's the point: your build is defined as much by what you skip as by what you take.

### The skill tree

Perks are split across four tabs, each pointed at one side of the witcher kit:

- **Combat** - damage, crits, bleed, and dodge. Precise Blows, Crushing Blows, Muscle Memory,
  Cold Blood, Flood of Anger, Crippling Strikes, Anatomical Knowledge, and so on.
- **Signs** - Sign intensity and cost, plus the alternate Sign forms (Far-Reaching Aard, Fire
  Stream, Magic Trap, Exploding Shield).
- **Alchemy** - the Toxicity ceiling, potion duration and strength, and Refreshment (the heal on
  drinking a potion or decoction).
- **General** - cross-cutting utility perks.

Each tab has **3 tiers**. A tab's Tier 2 opens once you've spent **3** points in that tab, and
Tier 3 after **6**. Depth in one tree therefore costs you breadth in the others - committing to
Signs all the way to its Tier 3 is a large fraction of a whole run's points.

### How perks work

Buying a perk flips its switch, and the combat, Sign, and alchemy systems check those switches
live, every tick. That's why a freshly bought perk changes your stats immediately, and why
situational perks like Cold Blood (bonus damage only when no enemy is near) can turn themselves on
and off as the fight moves. All of your character's power flows through this - levels into perks,
perks into the live stats above - rather than through a separate pool of attribute points.

---

## 7. Witcher School Armor Sets

The Schools are the witcher orders, and each has its own armor. WitcherCraft has six full sets -
Wolven, Feline, Griffin, Ursine, Viper, and Manticore - and each is a complete four-piece set
(helmet, chestplate, leggings, boots) with its own custom 3D model.

The payoff is the **School Effect**. Wear all four pieces of a set and its effect switches on and
stays on, shown on your HUD; take any piece off and it drops immediately. You can only run one
School at a time, so the set you wear is a commitment that should match how you're building.

| School | Set effect |
|--------|-----------|
| **Wolven** | Balanced - modest combat and Sign bonuses, the generalist set |
| **Feline** | +**50%** critical hit damage - a glass-cannon crit set |
| **Griffin** | +**20%** Sign Intensity and reduced Sign cooldown - the Sign set |
| **Ursine** | +**20%** maximum health - the survivability set |
| **Viper** | Stronger oils - raises the Oil Damage that scales matched-oil hits |
| **Manticore** | Boosted potion duration and alchemy power - the alchemist's set |

Mechanically, a complete set applies a permanent "School of the ..." effect to the wearer; the
combat, Sign, and alchemy systems read that effect the same way they read perks and potions. The
exact bonus values above are the design targets and are still being tuned.

Alongside the sets, the skill tree carries **School Techniques** perks (Cat, Bear, and Griffin)
that reward matching your build to your armor's weight class - light, heavy, and medium
respectively - so committing to a School pays off twice: once from the set effect, once from the
perks built around it.

---

## 8. Bestiary and Monsters

Hunting monsters is the witcher's whole reason to exist, and WitcherCraft frames them the way The
Witcher does: every creature belongs to a category, and every category has a weakness you're meant
to learn and exploit.

### Categories and weaknesses

Monsters are grouped into the classic bestiary categories - Necrophages, Vampires, Specters,
Relicts, Draconids, Ogroids, Insectoids, Beasts, and Cursed Ones. Each monster carries a
`witchercraft:<category>` tag, and that tag is the hook the whole system hangs on: it's what the
[oils](#oils) match against. Coating the right oil for a creature's category is the core piece of
preparation, and getting it wrong means leaving most of your damage on the table.

### The Bestiary

The Bestiary is an in-game encyclopedia, opened from the system menu. Each creature has its own
entry - lore, combat stats such as health and attacks, and the oil it's weak to - so a player who
does their reading walks into a fight already knowing how to win it. Reading the Bestiary is the
intended first step of any hunt, the same loop as consulting a contract board and then a book in
the source games.

### Monsters and alchemy

The bestiary also feeds alchemy. Many [decoctions](#decoctions) are brewed from a specific beast -
Ekimmara, Katakan, Leshen, Wyvern, and so on - so the monsters you hunt become the mutagens that
power your build. Killing well is how you unlock the strongest, longest brews.

### Roster status

The category framework, the tag-driven oil weaknesses, the Bestiary UI, and the monster-derived
decoctions are all in place. The creature roster itself - models, AI, spawning, and attack
patterns - is under active development, being filled in one monster at a time. The current live
list of implemented creatures is maintained on the project wiki rather than frozen here.

---

## 9. World and Utility Systems

Around the big systems sits a layer of menus and conveniences that make the witcher toolkit usable
without ever touching a crafting table.

### The system menu and keybinds

Almost everything is reached through two keys plus the system menu:

| Key | Opens |
|-----|-------|
| **Tab** | The Sign menu (choose your active Sign) |
| **R** | Cast the active Sign |
| **B** | The system menu |

The system menu (**B**) is the hub: from it you reach Alchemy (brewing), the Skill Tree, the
Character screen, the Bestiary, the Glossary, and Meditation.

### Custom HUD

WitcherCraft draws its own HUD on top of the vanilla one. The intent is that everything you need to
make an in-the-moment decision - can I cast, how toxic am I, is my shield still up, is my build
actually giving me the numbers I expect - is readable at a glance, without opening a screen.

Two resource bars sit with the vanilla ones at the bottom of the screen, deliberately mirroring how
health and hunger read:

- **Quen shield**, on the **left** above health and armor, read the same way as your hearts: one
  bubble per **2** points of shield, a small bubble for an odd leftover point, ten to a row and extra
  rows stacking upward if a build ever pushes the pool that high. It only exists while a shield is
  up, the same way the armor row only exists while you're wearing armor.
- **Toxicity**, on the **right**, stacked between hunger and oxygen. Always visible, filling
  smoothly inward from the screen edge and switching to a warning colour once you're overdosing.
  Hunger below it shrinks back toward the edge as it empties; toxicity creeps the other way, in
  toward you, so the two never read as the same thing despite sharing a side.

The left column is defensive state and the right column is the alchemy resource. That split is the
convention any future bar should follow. Both bars place themselves relative to whatever vanilla has
already drawn, so they stay correct with any amount of armor, absorption, or bonus max health.

Alongside them, a player-stats overlay shows your live combat and Sign stats, and the Sign cooldown
is shown while it's running.

### Meditation

Meditation is a clock. Open it and pick an hour on a 24-hour dial, and the world jumps to that
time. It replaces sleeping as the way to move time, and it exists for the hunt: waiting for night
to bring out the monsters that only appear then, skipping to daylight, or simply passing the time
while Toxicity drains back to safe.

### Glossary

The Glossary is a reference for the Witcher universe's terminology - the in-world vocabulary a
player new to the setting can lean on. It sits beside the Bestiary as the mod's "look things up"
half.

### Admin and debug tools

A small set of commands exists for development and testing - adjusting stats, fixing a character's
level, and a general dev tool. These are workshop tools rather than intended play, and aren't part
of the player-facing design.

---

## 10. Content Appendix

Full rosters, for reference. Balance values (durations, magnitudes) are still being tuned; where a
specific magnitude has been settled it's shown in **bold**, and where an effect's exact numbers are
still being finalized only the effect type is given. The authoritative live effects are what the
in-game Alchemy and Character screens show.

### Potions

Each potion lasts **3600 ticks (3 minutes)** at base (scaled by Potion Duration), except the
instant ones noted. Toxicity cost is per-potion, not a flat rate. With the Refreshment perk,
drinking any potion also heals **10%** of max health.

| Potion | Toxicity | Effect |
|--------|----------|--------|
| **Swallow** | +**25** | Passive health regeneration |
| **Petri's Philter** | +**20** | +**20** Sign Intensity |
| **Thunderbolt** | +**25** | +**20%** increased damage; sets crit chance to **100** (guaranteed crits) during a thunderstorm |
| **Full Moon** | +**25** | +**4** maximum health (2 hearts) |
| **Blizzard** | +**15** | +**55%** attack speed and +**5** dodge chance |
| **Black Blood** | +**20** | Reflects damage to attackers as thorns; +**15%** extra reflected against Necrophages and Vampires |
| **Cat** | +**10** | Collapses your view to high-contrast monochrome so dark terrain becomes readable, at the cost of being blinded by daylight; outlines every monster within **50** blocks with a glow, even through walls |
| **Killer Whale** | +**20** | Underwater sight and breath (effect still being finalized) |
| **Golden Oriole** | +**15** | Poison and toxin resistance (effect still being finalized) |
| **Tawny Owl** | +**0** | Passive stamina regeneration (currently applies no Toxicity - likely an oversight) |
| **White Raffard's Decoction** | +**25** | Instant heal for **50%** of max health |
| **White Honey** | +**0** | Clears all potion and decoction effects and resets Toxicity to **0** |

### Decoctions

Each decoction lasts **7200 ticks (6 minutes)** at base (scaled by Potion Duration) and adds
**50** Toxicity. With Refreshment, drinking one heals **20%** of max health.

Every decoction costs **50** Toxicity.

| Decoction | Effect |
|-----------|--------|
| **Ekimmara** | +**10%** life steal |
| **Katakan** | +**10** crit chance |
| **Foglet** | +**25** Sign Intensity while raining |
| **Leshen** | +**20** reflect damage (returns **20%** of damage taken as thorns) |
| **Wyvern** | Increased damage builds +**1%** per hit landed in combat, up to +**10%**; resets when you leave combat |
| **Succubus** | Increased damage builds +**1%** every **2 seconds** in combat, up to +**10%**; resets when you leave combat |
| **Water Hag** | +**40%** increased damage while at full health |
| **Nekker Warrior** | +**50%** increased damage while mounted |
| **Troll** | Passive health regeneration |
| **Grave Hag** | Health regeneration that grows with each kill in combat; resets when you leave combat |
| **Werewolf** | Passive stamina regeneration, at night in clear weather |
| **Wraith** | Intended to trigger a protective burst when you take a hit of **25%**+ max health - not yet implemented |

### Oils

Every oil is applied off-hand onto a sword, sits one at a time, and adds **+4 x (1 + Oil Damage%)**
Additional Damage on a hit against its category (see [Combat](#oils-in-combat)). Example monsters
are drawn from the creatures currently implemented; blank rows are categories with no monster in
the mod yet.

| Oil | Example monsters |
|-----|------------------|
| **Necrophage Oil** | Drowner, Foglet, Rotfiend |
| **Vampire Oil** | Bruxa |
| **Beast Oil** | |
| **Insectoid Oil** | |
| **Specter Oil** | |
| **Ogroid Oil** | |
| **Draconid Oil** | |
| **Relict Oil** | |
| **Cursed Oil** | |
| **Hanged Man's Venom** | General-purpose coating, not tied to a category |

### Bombs

Thrown projectiles that detonate into an area effect with no terrain damage.

| Bomb | Payload |
|------|---------|
| **Dimeritium Bomb** | **12** damage to everything within **5** blocks |
| **Grapeshot** | Fragmentation - area physical damage |
| **Dancing Star** | Ignites the blast area in fire |
| **Devil's Puffball** | Lingering poison cloud |
| **Northern Wind** | Freezes and slows caught enemies |
| **Samum** | Blinds and staggers caught enemies |

Payload magnitudes other than Dimeritium's are still being tuned.

### Perks

A work-in-progress snapshot of the skill tree; perks and their values are still being finalized.
Perks are switches the combat, Sign, and alchemy systems read live (see
[Character Progression](#how-perks-work)).

**Combat**

| Perk | Effect |
|------|--------|
| **Muscle Memory** | +**3** Additional Damage |
| **Precise Blows** | +**12** crit chance |
| **Crushing Blows** | +**8** crit chance |
| **Anatomical Knowledge** | +**10** crit chance while holding a bow or crossbow |
| **Cold Blood** | +**5** Additional Damage while no enemy is near |
| **Flood of Anger** | +**5** Additional Damage while an enemy is near |
| **Crippling Strikes** | Applies Bleed on hit (**100** ticks / **5** seconds) |
| **Sunder Armor** | Armor-shredding on-hit effect |

**Signs**

| Perk | Effect |
|------|--------|
| **Far-Reaching Aard** | Swaps Aard for its radial knockback alternate (pushes all nearby enemies away) |
| **Fire Stream** | Swaps Igni for its sustained-stream alternate |
| **Magic Trap** | Swaps Yrden for its upgraded trap alternate |
| **Active Shield** | Swaps Quen for its held, charging shield alternate |

**Alchemy**

| Perk | Effect |
|------|--------|
| **Refreshment** | Drinking a potion heals **10%** / a decoction **20%** of max health |
| **Cluster Bombs** | Bomb enhancement |
| Toxicity capacity | Raises the Overdose Threshold |
| Potion mastery | Raises potion duration and strength |

**School Techniques** (armor-weight synergy): **Cat School** (light), **Bear School** (heavy),
**Griffin School** (medium).
