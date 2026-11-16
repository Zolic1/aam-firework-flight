# AAM Air Combat 2.0.3

Air-combat mod for **Fabric 1.21.11** — guns, homing missiles, RWR radar, flares and stealth missiles.

> Renamed from *AAM Firework Flight* to **AAM Air Combat** (mod id stays `aam-firework-flight`, so existing worlds and items are unaffected).

## Installation

1. Install **Fabric Loader 0.18.4+**
2. Put **Fabric API 0.141.0+** and `aam-air-combat-2.0.3.jar` into `.minecraft/mods/`
3. Install on **both** client and server (HUD is client-side, combat logic is server-side)

| Requirement | Version |
|---|---|
| Minecraft | 1.21.11 |
| Fabric Loader | >= 0.18.4 |
| Fabric API | >= 0.141.0 |

## Changes in 2.0.3

### Changed
- Mod display name is now **AAM Air Combat**; jar artifact renamed to `aam-air-combat`
- Full English localization for HUD and lock-on readouts:
  - ammo / score bar (`AMMO 30/30  SCORE 1200`)
  - reloading bar, empty magazine messages
  - `LOCKING xx%`
  - `ENEMY LOCK` / `ENEMY MISSILE` warnings
  - lock box: `Missile`, `Maneuver 18.4s`, `Impact in 3.2s`, `TARGET DESTROYED`, `[Friendly] name`
- Equipment names are translatable: RWR Helmet, Thruster, AAM Launcher

### Fixed
- HUD and lock-on text no longer stay in Chinese when the game language is English

## Highlights

- **24 guns** with unique 3D models and recipes
- **5 bullet types** (tracer interval / ignite chance / armor penetration)
- **Missiles**: re-tracking, three-stage acceleration (80 → 120 → 180 b/s), 90% detonation chance, 12 HP (shootable and interceptable), proximity fuse
- **RWR radar**: three distance rings, target arcs, enemy/friendly player dots, lock box stable during barrel rolls
- **Countermeasures**: flares (75 blocks) and stealth missiles
- **Optional bundled SSUDC datapack**, selectable in the world creation screen

## Commands

```mcfunction
/aam help                                   # in-game manual (bilingual)
/aam <option> [value|reset]                 # global values
/aam <option> base|att ...                  # per-item overrides
/aam debug                                  # inspect the held gun
/gamerule aamflight:destroy_blocks <true|false>
/gamerule aamflight:mob_armor <true|false>
```

## Credits

Made by Yuike.
