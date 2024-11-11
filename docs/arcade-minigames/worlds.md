# Worlds

> Return to [table of contents](getting-started.md)
 
Managing worlds is likely something you want to do with your minigames.
Typically, you'll have a dedicated world *per* minigame. However, it is also possible to have multiple worlds per minigame.

It's also possible for multiple minigames to share a world, and for each
minigame to have its own bounding box. Having multiple minigames sharing a world does disable a few features, for example, worlds will no longer tick freeze if one of the minigames is paused.

When you add a world to a minigame, it gives the minigame more context and control. It allows for world-specific events, allows for setting custom gamerules, and managing the tick rates in the world.

Typically, you want to add (or load) your worlds in an initializer event listener.

## Regular Worlds

If we have a vanilla world or a world specified in a datapack we can load it
into out minigame. This can be done using the `MinigameLevelManager#add` method:

```kotlin
val minigame: Minigame = // ...
val level: ServerLevel = // ...
    
minigame.levels.add(level)
```

## Custom Worlds

It's very likely that you'll want to continuously generate new worlds for your 
minigames, especially if you're running minigames in parallel and do not know 
the number of minigames that will be running at the same time.

We will be using Arcade's [dimensions api](../arcade-dimensions/getting-started.md) to create custom levels. 
It's recommended to have read that documentation before continuing. 

When adding a custom level to a minigame, we have two options; either we pass 
the responsibility of managing and closing that world over to the minigame or 
we keep that responsibility. If we create a `CustomLevel` and haven't added it
to the `MinecraftServer` but pass it to our minigame it will take 
responsibility of that level, it will ensure it is loaded when the minigame is
initialized and will unload it when the minigame closes. If the `CustomLevel`
is already loaded then the minigame will not take responsibility, and it left
to you to manage the world after the minigame is finished.

We can create and register a custom level like so:
```kotlin
class ExampleMinigame(
    server: MinecraftServer,
    uuid: UUID
): Minigame(server, uuid) {
    // ...

    @Listener
    private fun onInitialize(event: MinigameInitializeEvent) {
        // Add a level which our minigame will handle
        this.levels.add(CustomLevelBuilder.build(this.server) {
            randomDimensionKey()
            vanillaDefaults(VanillaDimension.Overworld)
        })

        // Add a level which we must handle
        val level = this.server.addCustomLevel {
            randomDimensionKey()
            vanillaDefaults(VanillaDimension.Overworld)
        }
        this.levels.add(level)
    }
}
```

## Spawn Dimension and Position

By default, if a player dies without a respawn point, or with an obstructed respawn point they will respawn at the world spawn (usually in `minecraft:overworld`). This can be problematic in minigames, so Arcade provides a way to set the default spawn dimension and position.

```kotlin
val minigame: Minigame = // ...

// This will make the default spawn point be in the nether around (1500, 64, 3000)
minigame.levels.spawn = SpawnLocation.global(minigame.server.nether(), BlockPos(1500, 64, 3000))
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

And then you can assign this as your spawn:
```kotlin
val minigame: Minigame = // ...

minigame.levels.spawn = MySpawnLocation()
```

> See the next section on [Settings](settings.md)