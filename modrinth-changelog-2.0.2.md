# AAM Firework Flight 2.0.2

Air-combat mod for **Fabric 1.21.11** — guns, homing missiles, RWR radar, flares and stealth missiles.

## Installation

1. Install **Fabric Loader 0.18.4+**
2. Put **Fabric API 0.141.0+** and this jar into `.minecraft/mods/`
3. Install on **both** client and server (the HUD is client-side, all combat logic is server-side)

## Requirements

| Mod | Version |
|---|---|
| Minecraft | 1.21.11 |
| Fabric Loader | >= 0.18.4 |
| Fabric API | >= 0.141.0 |

## Changes in 2.0.2

- Project links now point to the public source repository
- Internal version bumped to 2.0.2 (no gameplay changes from 2.0.1)

## Fixed in 2.0.1

- Game rule identifiers are now lowercase (`aamflight:destroy_blocks`, `aamflight:mob_armor`), fixing a client crash during startup
- Fabric Loader requirement relaxed to `>=0.18.4`

## Highlights

- **24 guns** with unique 3D models and recipes (bullet / laser / shotgun / sci-fi / mechanic variants)
- **5 bullet types** with their own tracer interval, ignite chance and armor penetration
- **Missiles**: re-tracking, three-stage acceleration (80 → 120 → 180 b/s), 90% detonation chance, 12 HP (shootable and interceptable)
- **RWR radar**: three distance rings, target arcs, enemy/friendly player dots, ENEMY LOCK / ENEMY MISSILE alerts, lock box stable during barrel rolls
- **Countermeasures**: flares (75 blocks) and stealth missiles
- **Optional bundled SSUDC datapack**, selectable in the world creation screen
- Full bilingual localization (zh_cn / en_us)

## Commands

```mcfunction
/aam help                                   # full in-game manual (bilingual)
/aam <option> [value|reset]                 # global values
/aam <option> base|att ...                  # per-item overrides
/aam debug                                  # inspect the held gun
/gamerule aamflight:destroy_blocks <true|false>
/gamerule aamflight:mob_armor <true|false>
```

## Optional datapack

A bundled SSUDC datapack can be toggled in the world creation "Data Packs" screen.

## Credits

Made by Yuike.
