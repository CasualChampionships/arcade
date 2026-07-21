# Boundary Behaviour

Boundaries are intended to behave very similar to world borders, but
they do differ slightly. Boundaries also support the y-axis and so boundaries
have an x-size, y-size, and z-size instead of just one size which is applied
to the x and z axis. Boundaries also allow lerping the center of the boundary
over a period of time.

```kotlin
val boundary: LevelBoundary = // ...
// If duration is not specified it defaults to 0 (instant)
boundary.resize(size = Vec3(100.0, 256.0, 80.0), duration = 5.Minutes)

// Default to 0 duration
boundary.recenter(Vec3(0.0, 64.0, 0.0), duration = 3.Minutes)
```

Boundaries also support damaging players outside of them exactly like the 
vanilla world border:
```kotlin
val boundary: LevelBoundary = // ...
boundary.damagePerBlock = 0.4
boundary.damageSafeZone = 10.0
boundary.warningBlocks = 10
```
