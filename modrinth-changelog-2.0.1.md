## AAM Firework Flight 2.0.1

### Fixed

- Game rules used camelCase identifiers (`destroyBlocks` / `mobArmor`), which crashes the client during
  startup with `Non [a-z0-9/._-] character in path of location`. They are now `destroy_blocks` / `mob_armor`.
- Game rule command syntax changed accordingly:
  - `/gamerule aamflight:destroy_blocks <true|false>`
  - `/gamerule aamflight:mob_armor <true|false>`
- Fabric Loader requirement relaxed to `>=0.18.4` so the mod loads on older loader versions.

### Notes

- Everything from 2.0.0 is included: 24 guns with 3D models, homing/interceptable missiles,
  RWR radar with three rings and lock-on HUD, flares and stealth missiles, bilingual localization,
  and the optional bundled SSUDC datapack.
