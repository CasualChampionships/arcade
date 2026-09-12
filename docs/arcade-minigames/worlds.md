# Worlds

Managing worlds is likely something you want to do with your minigames.
Typically, you'll have a dedicated world *per* minigame. However, it is 
also possible to have multiple worlds per minigame.

It's also possible for multiple minigames to share a world, and for each
minigame to have its own bounding box. Having multiple minigames sharing a 
world does disable a few features, for example, worlds will no longer tick 
freeze if one of the minigames is paused.

When you add a world to a minigame, it gives the minigame more context and control. 
It allows for world-specific events, allows for setting custom gamerules, 
and managing the tick rates in the world.

Typically, you want to add (or create) your worlds in an initializer event listener.

## Creating Worlds

It's very likely that you'll want to continuously generate new worlds for your 
minigames, especially if you're running minigames in parallel and do not know 
the number of minigames that will be running at the same time.

We will be using Arcade's [dimensions api](../arcade-dimensions/getting-started.md) to create custom levels. 
It's recommended to have read that documentation before continuing.

The easiest way to do this is with `MinigameLevelManager#create`, which builds
the level and adds it to the minigame in one go:

```kotlin
class ExampleMinigame(
    server: MinecraftServer,
    uuid: UUID
): Minigame(server, uuid, ID, ExamplePhase.entries) {
    val level: ServerLevel get() = this.levels.require(LEVEL)

    @Listener
    private fun onInitialize(event: MinigameInitializeEvent) {
        this.levels.create(LEVEL) {
            vanillaDefaults(VanillaDimension.Overworld)
        }
    }

    companion object {
        val ID: Identifier = Identifier("modid", "example")
        val LEVEL: Identifier = Identifier("modid", "example_level")
    }
}
```

Every level is added under an `Identifier` of your choosing, and this is how you
get it back later with `get` or `require`. This id *does not* need to match the
level's dimension id, but it must be unique to the other levels in the minigame.
Using an id, rather than the dimension itself, is what lets the minigame
re-link your levels after a reload.

The minigame handles creating the dimension key and loading the level for you;
the level is added to the server when the minigame initializes.

## Adding Existing Worlds

If we have a vanilla world, a world specified in a datapack, or a `CustomLevel`
that we've built ourselves, we can add it to our minigame instead. This can be
done using the `MinigameLevelManager#add` method:

```kotlin
val minigame: Minigame = // ...
val level: ServerLevel = // ...
val id = Identifier("modid", "example_level")

minigame.levels.add(id, level)
```

## World Ownership

When adding a level to a minigame, we have two options; either we pass 
the responsibility of managing and closing that world over to the minigame, or 
we keep that responsibility. This is what a level's `LevelOwnership` describes:

- `Borrowed` means that the minigame takes no responsibility in removing or
  deleting the level after the minigame is closed. This should typically only be
  used if you are either using a vanilla dimension *or* have some other manual
  management for your levels.
- `Owned` means that the minigame owns responsibility for removing the level
  after the minigame is closed, but it *doesn't* delete the level.
- `Exclusive` means that the minigame owns *full* responsibility for removing
  *and* deleting the level after the minigame is closed.

Ideally minigames should have *exclusive ownership* of their levels which only
exist exclusively during a minigame's lifetime, which allows the minigame to
completely manage the level itself. This is what `create` defaults to, whereas
`add` defaults to `Borrowed`, since the level it is given may be a vanilla one.

```kotlin
val minigame: Minigame = // ...
val level: CustomLevel = // ...

minigame.levels.add(LEVEL, level, LevelOwnership.Owned)
```

Anything other than `Borrowed` requires a `CustomLevel`, and if your minigame is
serializable then the level must not be transient, otherwise it won't be there
when the minigame is reloaded. When you use `create` the correct persistence is
chosen for you based on the ownership.

## Bounds

If multiple minigames share a level, each minigame can declare the region of
that level which belongs to it. Events are then filtered by those bounds, see
the [Events Section](./events.md#filtered-events):

```kotlin
val minigame: Minigame = // ...
val level: ServerLevel = // ...
val bounds: BoundingBox = // ...

minigame.levels.add(LEVEL, level, LevelOwnership.Borrowed, bounds)
```

If you leave the bounds as `null` then the minigame uses the whole level.

## Querying Worlds

```kotlin
val minigame: Minigame = // ...

// Gets the level, null if none was added under that id
val nullableLevel: ServerLevel? = minigame.levels.get(LEVEL)
// Gets the level, throwing if none was added under that id
val level: ServerLevel = minigame.levels.require(LEVEL)

// Whether a level was added under that id
val hasId: Boolean = minigame.levels.has(LEVEL)
// Whether a given level is part of the minigame
val hasLevel: Boolean = minigame.levels.has(level)
// Whether a given level and position are within the minigame
val hasPos: Boolean = minigame.levels.has(level, BlockPos(0, 64, 0))

// All the ids of levels that were added
val ids: Collection<Identifier> = minigame.levels.ids()
// All the levels that are part of the minigame
val levels: Collection<ServerLevel> = minigame.levels.all()
```

We can also set the game rules for every level in the minigame at once:
```kotlin
val minigame: Minigame = // ...

minigame.levels.setGameRules {
    set(GameRules.IMMEDIATE_RESPAWN, true, minigame.server)
}
```

## Spawn Dimension and Position

By default, if a player dies without a respawn point, or with an obstructed respawn point 
they will respawn at the world spawn (usually in `minecraft:overworld`). 
This can be problematic in minigames, so Arcade provides a way to set the default spawn dimension and position.

```kotlin
val minigame: Minigame = // ...

// This will make the default spawn point be in the nether around (1500, 64, 3000)
minigame.levels.spawn = MinigameLevelManager.SpawnLocation.global(
    location = minigame.server.nether().asLocation(Vec3(1500.0, 64.0, 3000.0)),
    overridesPlayerSpawnPoint = false
)
```

If you need, you can also specify the spawn location on a per-player basis.

```kotlin
class MySpawnLocation: MinigameLevelManager.SpawnLocation {
    override val overridesPlayerSpawnPoint: Boolean
        get() = false

    override fun get(player: ServerPlayer): LocationWithLevel<ServerLevel>? {
        TODO("Logic for determining location for player spawn")
    }
}
```

And then you can assign this as your spawn:
```kotlin
val minigame: Minigame = // ...

minigame.levels.spawn = MySpawnLocation()
```

## Transferring Worlds

If one minigame leads into another, for example a lobby into the game itself,
the levels and the ownership of those levels can be handed over:

```kotlin
val minigame: Minigame = // ...
val next: Minigame = // ...

minigame.levels.transferTo(next)
```

The levels keep the ids they were added under, and the minigame that transferred
them no longer takes responsibility for them.
