# 64箭扩散 - 均匀填充版 (半径0.5~3.0分层，内部也有箭，速度随半径增大)
# 半径0.5 (4支)
summon arrow ~0.3536 ~5 ~0.3536 {Motion:[0.0707d,-0.8d,0.0707d]}
summon arrow ~-0.3536 ~5 ~0.3536 {Motion:[-0.0707d,-0.8d,0.0707d]}
summon arrow ~-0.3536 ~5 ~-0.3536 {Motion:[-0.0707d,-0.8d,-0.0707d]}
summon arrow ~0.3536 ~5 ~-0.3536 {Motion:[0.0707d,-0.8d,-0.0707d]}

# 半径1.0 (6支)
summon arrow ~1.0000 ~5 ~0.0000 {Motion:[0.2000d,-0.8d,0.0000d]}
summon arrow ~0.5000 ~5 ~0.8660 {Motion:[0.1000d,-0.8d,0.1732d]}
summon arrow ~-0.5000 ~5 ~0.8660 {Motion:[-0.1000d,-0.8d,0.1732d]}
summon arrow ~-1.0000 ~5 ~0.0000 {Motion:[-0.2000d,-0.8d,0.0000d]}
summon arrow ~-0.5000 ~5 ~-0.8660 {Motion:[-0.1000d,-0.8d,-0.1732d]}
summon arrow ~0.5000 ~5 ~-0.8660 {Motion:[0.1000d,-0.8d,-0.1732d]}

# 半径1.5 (10支)
summon arrow ~1.5000 ~5 ~0.0000 {Motion:[0.3000d,-0.8d,0.0000d]}
summon arrow ~1.2141 ~5 ~0.8817 {Motion:[0.2428d,-0.8d,0.1763d]}
summon arrow ~0.7500 ~5 ~1.2990 {Motion:[0.1500d,-0.8d,0.2598d]}
summon arrow ~0.0000 ~5 ~1.5000 {Motion:[0.0000d,-0.8d,0.3000d]}
summon arrow ~-0.7500 ~5 ~1.2990 {Motion:[-0.1500d,-0.8d,0.2598d]}
summon arrow ~-1.2141 ~5 ~0.8817 {Motion:[-0.2428d,-0.8d,0.1763d]}
summon arrow ~-1.5000 ~5 ~0.0000 {Motion:[-0.3000d,-0.8d,0.0000d]}
summon arrow ~-1.2141 ~5 ~-0.8817 {Motion:[-0.2428d,-0.8d,-0.1763d]}
summon arrow ~-0.7500 ~5 ~-1.2990 {Motion:[-0.1500d,-0.8d,-0.2598d]}
summon arrow ~0.0000 ~5 ~-1.5000 {Motion:[0.0000d,-0.8d,-0.3000d]}
summon arrow ~0.7500 ~5 ~-1.2990 {Motion:[0.1500d,-0.8d,-0.2598d]}
summon arrow ~1.2141 ~5 ~-0.8817 {Motion:[0.2428d,-0.8d,-0.1763d]}

# 半径2.0 (12支)
summon arrow ~2.0000 ~5 ~0.0000 {Motion:[0.4000d,-0.8d,0.0000d]}
summon arrow ~1.7321 ~5 ~1.0000 {Motion:[0.3464d,-0.8d,0.2000d]}
summon arrow ~1.0000 ~5 ~1.7321 {Motion:[0.2000d,-0.8d,0.3464d]}
summon arrow ~0.0000 ~5 ~2.0000 {Motion:[0.0000d,-0.8d,0.4000d]}
summon arrow ~-1.0000 ~5 ~1.7321 {Motion:[-0.2000d,-0.8d,0.3464d]}
summon arrow ~-1.7321 ~5 ~1.0000 {Motion:[-0.3464d,-0.8d,0.2000d]}
summon arrow ~-2.0000 ~5 ~0.0000 {Motion:[-0.4000d,-0.8d,0.0000d]}
summon arrow ~-1.7321 ~5 ~-1.0000 {Motion:[-0.3464d,-0.8d,-0.2000d]}
summon arrow ~-1.0000 ~5 ~-1.7321 {Motion:[-0.2000d,-0.8d,-0.3464d]}
summon arrow ~0.0000 ~5 ~-2.0000 {Motion:[0.0000d,-0.8d,-0.4000d]}
summon arrow ~1.0000 ~5 ~-1.7321 {Motion:[0.2000d,-0.8d,-0.3464d]}
summon arrow ~1.7321 ~5 ~-1.0000 {Motion:[0.3464d,-0.8d,-0.2000d]}

# 半径2.5 (16支)
summon arrow ~2.5000 ~5 ~0.0000 {Motion:[0.5000d,-0.8d,0.0000d]}
summon arrow ~2.3087 ~5 ~0.9589 {Motion:[0.4617d,-0.8d,0.1918d]}
summon arrow ~1.7678 ~5 ~1.7678 {Motion:[0.3536d,-0.8d,0.3536d]}
summon arrow ~0.9589 ~5 ~2.3087 {Motion:[0.1918d,-0.8d,0.4617d]}
summon arrow ~0.0000 ~5 ~2.5000 {Motion:[0.0000d,-0.8d,0.5000d]}
summon arrow ~-0.9589 ~5 ~2.3087 {Motion:[-0.1918d,-0.8d,0.4617d]}
summon arrow ~-1.7678 ~5 ~1.7678 {Motion:[-0.3536d,-0.8d,0.3536d]}
summon arrow ~-2.3087 ~5 ~0.9589 {Motion:[-0.4617d,-0.8d,0.1918d]}
summon arrow ~-2.5000 ~5 ~0.0000 {Motion:[-0.5000d,-0.8d,0.0000d]}
summon arrow ~-2.3087 ~5 ~-0.9589 {Motion:[-0.4617d,-0.8d,-0.1918d]}
summon arrow ~-1.7678 ~5 ~-1.7678 {Motion:[-0.3536d,-0.8d,-0.3536d]}
summon arrow ~-0.9589 ~5 ~-2.3087 {Motion:[-0.1918d,-0.8d,-0.4617d]}
summon arrow ~0.0000 ~5 ~-2.5000 {Motion:[0.0000d,-0.8d,-0.5000d]}
summon arrow ~0.9589 ~5 ~-2.3087 {Motion:[0.1918d,-0.8d,-0.4617d]}
summon arrow ~1.7678 ~5 ~-1.7678 {Motion:[0.3536d,-0.8d,-0.3536d]}
summon arrow ~2.3087 ~5 ~-0.9589 {Motion:[0.4617d,-0.8d,-0.1918d]}

# 半径3.0 (16支)
summon arrow ~3.0000 ~5 ~0.0000 {Motion:[0.6000d,-0.8d,0.0000d]}
summon arrow ~2.7716 ~5 ~1.1481 {Motion:[0.5543d,-0.8d,0.2296d]}
summon arrow ~2.1213 ~5 ~2.1213 {Motion:[0.4243d,-0.8d,0.4243d]}
summon arrow ~1.1481 ~5 ~2.7716 {Motion:[0.2296d,-0.8d,0.5543d]}
summon arrow ~0.0000 ~5 ~3.0000 {Motion:[0.0000d,-0.8d,0.6000d]}
summon arrow ~-1.1481 ~5 ~2.7716 {Motion:[-0.2296d,-0.8d,0.5543d]}
summon arrow ~-2.1213 ~5 ~2.1213 {Motion:[-0.4243d,-0.8d,0.4243d]}
summon arrow ~-2.7716 ~5 ~1.1481 {Motion:[-0.5543d,-0.8d,0.2296d]}
summon arrow ~-3.0000 ~5 ~0.0000 {Motion:[-0.6000d,-0.8d,0.0000d]}
summon arrow ~-2.7716 ~5 ~-1.1481 {Motion:[-0.5543d,-0.8d,-0.2296d]}
summon arrow ~-2.1213 ~5 ~-2.1213 {Motion:[-0.4243d,-0.8d,-0.4243d]}
summon arrow ~-1.1481 ~5 ~-2.7716 {Motion:[-0.2296d,-0.8d,-0.5543d]}
summon arrow ~0.0000 ~5 ~-3.0000 {Motion:[0.0000d,-0.8d,-0.6000d]}
summon arrow ~1.1481 ~5 ~-2.7716 {Motion:[0.2296d,-0.8d,-0.5543d]}
summon arrow ~2.1213 ~5 ~-2.1213 {Motion:[0.4243d,-0.8d,-0.4243d]}
summon arrow ~2.7716 ~5 ~-1.1481 {Motion:[0.5543d,-0.8d,-0.2296d]}
particle end_rod ~ ~ ~ 0.5 0.5 0.5 0.5 64 force