## AAM Firework Flight 2.0.0

### Added

- 24 guns with unique 3D item models and recipes (8 sci-fi + 8 mechanic guns)
- Bullet mechanics: freeze, poison, chain lightning, micro-explosion, homing, hitscan lasers
- 5 bullet types with their own tracer interval, ignite chance and armor penetration
- Missiles: re-tracking, three-stage acceleration (80 → 120 → 180 b/s), 90% detonation chance, 12 HP (shootable)
- RWR radar: three distance rings, target arcs, enemy/friendly player dots, ENEMY LOCK / ENEMY MISSILE alerts
- Lock-on missiles: durability bar, maneuver time, impact countdown, 0.7s "TARGET DESTROYED" ghost
- Countermeasure flares (75 blocks) and stealth missiles (`mstealth`)
- Optional bundled SSUDC datapack (selectable in the world creation screen)
- Two game rules: `aamflightDestroyBlocks`, `aamflightMobArmor`
- Full bilingual localization (zh_cn / en_us)

### Changed

- Config split into registry / commands / storage
- New deep config options: `radar_interval`, `hud_interval`, `score_interval`, `ammo_rate`, `rule_interval`
- Radar packet rate is now per-player
- `att` ids now accept non-ASCII names

### Fixed

- Missile explosion recursion (StackOverflowError)
- Lock box drifting during barrel rolls
- `lmg_gun` (prototype gun) missing model
- Datapack packed under the wrong directory (`resourcepacks/` now)
