<div align="center">

<img src="https://raw.githubusercontent.com/Ciochinio/WitcherCraft-1.21/main/src/main/resources/assets/witchercraft/textures/logo.png" alt="WitcherCraft Logo" width="120">

# WitcherCraft

### A faithful recreation of The Witcher universe within Minecraft

[![CurseForge](https://img.shields.io/badge/CurseForge-25k%20downloads-orange?style=flat-square&logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/witchercraft)
[![Modrinth](https://img.shields.io/badge/Modrinth-Available-brightgreen?style=flat-square&logo=modrinth)](https://modrinth.com/mod/witchercraft)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21-green?style=flat-square)](https://minecraft.net)
[![NeoForge](https://img.shields.io/badge/NeoForge-1.21%20compatible-blue?style=flat-square)](https://neoforged.net)
[![License](https://img.shields.io/badge/License-Non--commercial%20fan%20project-red?style=flat-square)](https://www.cdprojektred.com/en/fan-content)

**[Wiki](https://witchercraft.com) · [Documentation](https://witchercraft.com/documentation.html) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/witchercraft) · [Modrinth](https://modrinth.com/mod/witchercraft) · [Discord](https://discord.com/invite/xReJfktHS2)**

---

*If you ever wanted to feel like Geralt while playing Minecraft — this mod is for you.*

</div>

---

## What is WitcherCraft?

WitcherCraft is a NeoForge mod for Minecraft 1.21 that brings the world of **The Witcher 3: Wild Hunt** into the Minecraft engine. It recreates the core RPG systems of the Witcher universe from the ground up — Signs, Alchemy, a multi-tab Skill Tree, custom combat mechanics, Witcher School armor sets, and a living Bestiary of monsters.

> WitcherCraft is currently undergoing a full rework. This repository reflects active development. All content is subject to change.

---

## Features

### Witcher Signs
Five Signs from The Witcher 3, each implemented as an active ability with custom particle systems and a dedicated cooldown:

| Sign | Effect |
|------|--------|
| **Aard** | Telekinetic cone blast — knocks back enemies |
| **Igni** | Fire stream — applies burning on hit |
| **Quen** | Absorbing shield — hold state with separate particle system |
| **Yrden** | Magical trap — slows enemies in radius |
| **Axii** | Charm Sign — manipulates enemies |

Open the Sign Menu with `Tab`, select your Sign, cast with `R`.

---

### Alchemy System
A full alchemy system accessible anywhere in the world via the in-game menus. No crafting table required.

- **Potions** — 12 potions including Swallow, Thunderbolt, Cat, White Honey and more
- **Decoctins** — 12 powerful monster-derived brews including Ekimmara, Katakan, Troll, Wyvern and more
- **Oils** — 10 weapon enchantments targeting specific enemy archetypes
- **Bombs** — 6 throwable projectile entities: Grapeshot, Dancing Star, Devil's Puffball, Northern Wind, Samum, Dimeritium Bomb

All alchemy use is governed by the **Toxicity system** — overuse drains health.

---

### Toxicity System
Every potion and decoction raises your Toxicity level. Exceed your Maximum Toxicity and your health will drain. White Honey resets everything. Maximum Toxicity can be increased through the Alchemy Skill Tree.

This system exists to prevent mindless potion stacking and reward genuine preparation.

---

### Witcher School Armor Sets
Six complete armor sets (Helmet, Chestplate, Leggings, Boots), each with a custom 3D renderer and a unique **School Effect** active when the full set is worn:

| Set | School Effect |
|-----|--------------|
| **Wolven** | Balanced combat and Sign bonuses |
| **Feline** | +50% critical hit damage |
| **Griffin** | +20% Sign Intensity, reduced Sign cooldown |
| **Ursine** | +20% maximum health |
| **Viper** | Enhanced oil effectiveness |
| **Manticore** | Boosted potion duration and Alchemy power |

---

### Skill Tree & Progression
A multi-tab Skill Tree with **Combat**, **Signs**, **Alchemy**, and **General** tabs.

> **Build decisions matter.** Maximum level is 60, and every level after the first gives 1 skill point, enough to eventually learn every perk. Only 12 can be equipped at once, and the equip slots open as you level, so what you slot defines your build.

Perks are arranged in 3 tiers per tab. Tier 2 unlocks after 3 spent points, Tier 3 after 6.

---

### Custom Damage Model
A fully custom `DamageCalculatorProcedure` replacing Minecraft's default combat:

```
Normal:  finalDamage = (baseDamage + additionalDamage) x (1 + increasedDamage x 0.01)
Crit:    finalDamage = above x (critDamage x 0.01)   [if random(1,100) <= critChance]
Heal:    healthRestored = finalDamage x lifeSteal x 0.01
```

Stats: flat bonus damage, % increased damage, crit chance, crit damage multiplier, life steal.

---

### Bestiary & More
- **Bestiary** — in-game monster encyclopedia with lore, stats, and weakness data
- **Glossary** — Witcher universe terminology reference
- **Meditation** — fast-travel to Sunrise, Noon, Sunset, or Midnight
- **Custom HUD** — live display of all player stats

---

## Monsters

WitcherCraft adds a variety of monsters drawn from The Witcher universe, each belonging to a specific category — Necrophages, Vampires, Specters, Relicts, and Draconids. Currently implemented monsters have full Bestiary entries with lore, health, attack data, and oil weaknesses. Several additional monsters have completed 3D models and are currently being integrated with full AI behavior, spawning logic, and attack patterns.

Full monster details and the complete Bestiary are available on the [wiki](https://witchercraft.com/monsters.html).

---

## Installation

1. Download and install **[NeoForge](https://neoforged.net)** for Minecraft 1.21
2. Place the WitcherCraft `.jar` file into your `/mods/` folder
3. Launch Minecraft with the NeoForge profile
4. In-game keybinds:
   - `Tab` — Open Sign Menu
   - `R` — Cast selected Sign
   - `B` — Open the Pause Menu (access Alchemy, Skill Tree, Character, Bestiary, Glossary, Meditation)

---

## Documentation

Full system design documentation including damage formula, skill tree perk list, alchemy effects, and technical architecture is available at:

**[witchercraft.com/documentation.html](https://witchercraft.com/documentation.html)**

---

## Links

| | |
|--|--|
| Wiki & Docs | [witchercraft.com](https://witchercraft.com) |
| CurseForge | [https://www.curseforge.com/minecraft/mc-mods/witchercraft] |
| Modrinth | [https://modrinth.com/mod/witchercraft] |
| Discord | [https://discord.com/invite/xReJfktHS2] |
| DMC Weapons Mod | [https://www.curseforge.com/minecraft/mc-mods/devil-may-cry-weapons-reborn] |

---

## Legal

WitcherCraft is an independent fan modification for Minecraft, created with permission from **CD Projekt Red** under their fan content guidelines.

- This project is **fully non-commercial**
- CD Projekt Red is **not** an official partner or co-creator of this project
- The Witcher® is a registered trademark of CD Projekt Red S.A.
- All visual assets currently in the mod are **temporary placeholders** — final assets will be original works created in a Minecraft pixel art style

---

<div align="center">

© 2021–2026 Red Bolt Media

</div>
