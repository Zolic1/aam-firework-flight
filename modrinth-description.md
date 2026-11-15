# AAM Firework Flight

An air-combat mod for **Fabric 1.21.11**: real-bullet guns, homing missiles with lock-on, a War Thunder style RWR radar, countermeasure flares and stealth missiles.

## Features

### Guns (24 of them)

- Real raycast bullets (not entities) with pierce, spread, pellets and tracer types
- Laser weapons (instant hitscan) — `lmg_bspeed >= 900` becomes a beam weapon
- Charge bars: fire only when a bar is full; multi-bar guns (shotgun / grenade launcher) fire bursts
- 5 bullet types: tracking, tracer-incendiary (smoke / end rod), standard, armor-piercing
  (each has its own tracer interval, ignite chance and armor penetration)
- Effects: freeze, poison, chain lightning, micro-explosions, homing bullets
- Every gun has its own 3D item model and crafting recipe

### Missiles

- Homing with re-track count: every overshoot halves the turn rate for 8s, then recovers over 5s
- Three-stage acceleration: 80 → 120 → 180 blocks/s
- 90% detonation chance — otherwise the missile passes through and counts as a miss
- 12 HP: missiles can be shot down by bullets, explosions or other missiles
- All parameters (speed, turn, fuse, blast power, countermeasure resistance, stealth)
  are adjustable **per item**

### RWR radar

- Three distance rings, rotating target arcs, red/blue player dots (enemy/friendly)
- "ENEMY LOCK" / "ENEMY MISSILE" warnings with different alert cadences
- Lock box projected through the camera basis (stable during barrel rolls)
- Locking a missile shows durability bar, maneuver time and impact countdown
- Destroyed targets stay on screen for 0.7s with a "TARGET DESTROYED" tag

### Countermeasures

- **Flares** (right click): 75-block radius, missiles lose lock based on their resistance
- **Stealth missiles**: invisible to RWR unless manually locked from the front

## Commands (permission level 4)

```mcfunction
/aam help                       # full in-game manual (bilingual)
/aam <option> [value|reset]     # global values
/aam <option> base|att ...      # per-item overrides / attributes
/aam debug                      # inspect the held gun
/gamerule aamflight:destroy_blocks <true|false>
/gamerule aamflight:mob_armor <true|false>
```

## Optional datapack

A bundled SSUDC datapack can be toggled in the world creation "Data Packs" screen.

## Requirements

- Fabric Loader >= 0.20.0
- Fabric API >= 0.141.0
- Minecraft 1.21.11

## Credits

Made by Yuike.
