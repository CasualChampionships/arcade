# Worlds

> Return to [table of contents](../minigames.md)
 
Managing worlds is likely something you want to do with your minigames.
Typically, you'll have a dedicated world *per* minigame. However, it is also possible to have multiple worlds per minigame.

While it is technically possible to share a world between multiple minigames, it is strongly discouraged.

When you add a world to a minigame, it gives the minigame more context and control. It allows for world-specific events, allows for setting custom gamerules, and managing the tick rates in the world.

Typically, you want to add (or load) your worlds in your minigame's `initialize` method.

## Persistent Worlds

Persistent worlds are those which will remain over the entire lifetime of the server, which you do not want to be deleted by the minigame after it has closed.

These can be added using the `MinigameLevelManager#add` method:

```kotlin
val minigame: Minigame<*> = // ...
val level: ServerLevel = // ...
    
minigame.levels.add(level)
```

## Temporary Worlds (Using Fantasy)

### Custom Dimensions

It's very likely that you'll want to continuously generate new temporary worlds for your minigames, especially if you're running minigames concurrently and do not know the number of minigames that will be running at the same time.

When registering temporary worlds, it then becomes Arcade's responsibility and you do not need to worry about closing the worlds after the minigame has ended; this is all handled for you.

For temporary worlds we will be using [Fantasy](https://github.com/NucleoidMC/fantasy).

We can create and register a temporary world like so:
```kotlin
class ExampleMinigame(server: MinecraftServer): Minigame<ExampleMinigame>(server) {
    // ...

    override fun initialize() {
        super.initialize()

        val fantasy = Fantasy.get(this.server)
        val level = fantasy.openTemporaryWorld(RuntimeWorldConfig())
        this.levels.add(level)
    }
}
```

See [Fantasy's Documentation](https://github.com/NucleoidMC/fantasy/blob/1.20.4/README.md#creating-runtime-dimensions) for more information about creating custom temporary dimensions.

### Vanilla-like Dimensions

You may want your minigame to be set in a vanilla-like environment with the three dimensions; `minecraft:overworld`, `minecraft:the_nether`, and `minecraft:the_end`. However, you don't want to use the vanilla dimensions, as this limits to you only being able to run one minigame at a time.

Arcade provides a way to dynamically generate vanilla-like dimensions:
```kotlin
val seed = 1283905120395782
val (overworld, nether, end) = FantasyUtils.createTemporaryVanillaLikeLevels(
    overworldConfig = FantasyUtils.createOverworldConfig(seed),
    netherConfig = FantasyUtils.createNetherConfig(seed),
    endConfig = FantasyUtils.createEndConfig(seed)
)
```

These dimensions will behave like the vanilla ones, their portals will link properly to each other, and by using the dimension configs the generations and rules will be the same, however, you can customize these to your liking.

Advancement triggers will be invoked as if the player was in the vanilla dimensions, e.g. the player will be granted advancements such as `Subspace Bubble` and `The End` as normal. 

## Spawn Dimension and Position

By default, if a player dies without a respawn point, or with an obstructed respawn point they will respawn at the world spawn (usually in `minecraft:overworld`). This can be problematic in minigames, so Arcade provides a way to set the default spawn dimension and position.

```kotlin
val minigame: Minigame<*> = // ...

// This will make the default spawn point be in the nether around (1500, 64, 3000)
minigame.levels.spawn = SpawnLocation.global(LevelUtils.nether(), BlockPos(1500, 64, 3000))
```

If you need, you can also specify the spawn location on a per-player basis.

```kotlin
class MySpawnLocation: SpawnLocation {
    override fun level(player: ServerPlayer): ServerLevel? {
        TODO("Logic for determining dimension for player spawn")
    }

    override fun position(player: ServerPlayer): BlockPos? {
        TODO("Logic for determining position for player spawn")
    }
}
```

And then you can just assign this as your spawn:
```kotlin
val minigame: Minigame<*> = // ...

minigame.levels.spawn = MySpawnLocation()
```

> See the next section on [Settings](settings.md)