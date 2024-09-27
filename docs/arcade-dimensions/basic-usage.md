# Basic Usage

## Creating a Custom Dimension

Creating a custom level is super easy, we can do this using the `CustomLevelBuilder`:

```kotlin
val builder = CustomLevelBuilder()
    .randomDimensionKey()
    .randomSeed()
    .vanillaDefaults(VanillaDimension.Overworld)
```

The above example shows a very minimal example of customizing our level, we set
the dimension key to be random, the seed to be random and for it to use the vanilla
overworld dimension presets.

Firstly, let's go through what is necessary for use to create a custom level.

We **must** specify a dimension key, and we **must** specify a level stem. 
```kotlin
val key = ResourceKey.create(
    Registries.DIMENSION,
    ResourceLocation.withDefaultNamespace("foo")
)

val builder = CustomLevelBuilder()
    .dimensionKey(key)              // ResourceKey<Level>      
    .levelStem(LevelStem.OVERWORLD) // ResourceKey<LevelStem>
```
A level stem contains the dimension type and chunk generator for our dimension. 
You can also specify these separately if you do not have a reference to a level stem.

### Custom Chunk Generators

Custom chunk generators are super simple, we simply pass a `ChunkGenerator` instance
into our builder:
```kotlin
val server: MinecraftServer = // ...

val builder = CustomLevelBuilder()
    .chunkGenerator(VoidChunkGenerator(server))
```

By default, there are only the vanilla chunk generators and a `VoidChunkGenerator`
implemented by arcade.

### Custom Dimension Types

For your dimension type, you *should* register it before your server has started, either
by doing it the [data-driven way](https://minecraft.wiki/w/Dimension_definition) or
by registering it in your mod initializer:
```kotlin
override fun onInitialize() {
    val dimensionTypeKey = ResourceKey.create(
        Registries.DIMENSION_TYPE,
        ResourceLocation.withDefaultNamespace("foo")
    )
    RegistryEventHandler.register(Registries.DIMENSION_TYPE) { (registry) ->
        Registry.register(registry, dimensionTypeKey, DimensionTypeBuilder.build {
            bedWorks = false
            piglinSafe = true
            height = 512
            // ...
        })
    }
}
```
This allows you to use your dimension type resource key to specify the dimension type:
```kotlin
val builder = CustomLevelBuilder()
    .dimensionType(dimensionTypeKey) // ResourceKey<DimensionType>
```

Alternatively, we can dynamically create `DimensionType`s however they cannot be
synchronized to clients, as these are synced during the configuration phase. Because 
of this we need to be careful with what we change in our dynamic `DimensionType`s. 
For example, we cannot change the height of the world, but we can for example
change whether beds work. Anything that is purely server-side can be changed without issue.
```kotlin
val builder = CustomLevelBuilder()
    .dimensionType {
        bedWorks = false
        piglinSafe = true
        // This will not work, we must stick to the overworld height limit
        height = 512 
        // ...
    }
```

### Level Property Options

There are many level property options that we can configure. What we define to be
level properties is anything that can change over the level's lifetime, for example,
the weather, the time of day, or difficulty level. 

Changing these essentially just set the *initial* state of these properties.

By default, if we do not set the initial state of these properties then your level
will **inherit** these properties from the primary level (usually `minecraft:overworld`).

Here's an example of everything we can change:
```kotlin
val builder = CustomLevelBuilder()
    .defaultLevelProperties() // Sets the default properties (stops the inheriting)
    .timeOfDay(1200) // Sets the time of day (ticks)
    .weather { // Set the weather
        clearWeatherTime = 0
        raining = false
        rainTime = 0
        thundering = true
        thunderTime = 100
    }
    .difficulty { // Set the difficulty
        value = Difficulty.HARD
        locked = false
    }
    .gameRules { // Modify the game rules
        set(GameRules.RULE_DO_IMMEDIATE_RESPAWN, true)
    }

```

### Level Generation Options

The level generation options customize how our level generates, once we set these
they are permanent for the lifetime of level. We've already covered the level stem,
or the dimension type and chunk generator, but there are some other things we can modify.

```kotlin
val builder = CustomLevelBuilder()
    .randomSeed() // Use a randomly generated seed
    .seed(1234567890) // Use a pre-determined seed
    .flat(true) // Whether the world is considered 'flat'
    .tickTime(true) // Whether the world should tick its daylight cycle
    .generateStructures(false) // Whether structures generate
    .debug(true) // Whether to use the 'debug' level generation
    .customSpawners(WanderingTraderSpawnerFactory) // Custom mob spawning rules
```

Most of the above is self-explanatory, lets have a look at the less obvious ones.

`flat()` doesn't make the world generate as a flat work, instead it just
marks it as such; flat worlds have their sky rendered lower, so the dark sky below 
sea level doesn't render, and also changes the color of the fog.

`debug()` generates the debugging level, see the [wiki](https://minecraft.wiki/w/Debug_mode) 
for more info.

`customSpawners()` allows you to add custom mob spawning rules for your level.
This is used in vanilla for non-biome-specific mobs that only spawn in the 
overworld, such as phantoms, patrols, cats, and wandering traders.

### Persistence

We can specify how we want our level to persist after we unload it or after the
server stops. We have three different options:
- `Temporary`: The world will be deleted when it's unloaded or when the server stops.
- `Permanent`: The world will be saved to disk when unloaded or when the server stops,
but it will not automatically be loaded when the server starts.
- `Persistent`: The world will be saved to disk when unloaded or when the server stops,
and will be automatically reloaded when the server starts.

```kotlin
val builder = CustomLevelBuilder()
    .persistence(LevelPersistence.Permanent)
```

Most of the time you will either want to use `Temporary` or `Permanent`.

Arcade puts a big focus on keeping levels identical to when they were serialized,
all level properties and generation options will be saved; we can even ensure that
the `CustomLevel` implementation is correct. This will be documented in the advanced
section as for most cases the default `CustomLevel` implementation will suffice.

### Building

Once we have configured our builder, we are ready to actually build our level:
```kotlin
val builder = CustomLevelBuilder()
    .randomDimensionKey()
    .randomSeed()
    .vanillaDefaults(VanillaDimension.Overworld)

val server: MinecraftServer = // ...
val level: CustomLevel = builder.build(server)
```

It is important to note that this **does not** add the level to the server, just
creates it, the next section discusses how we add, remove, and delete levels from
the server.

## Using Custom Dimensions

### Adding Custom Dimensions

Once we have an instance of a `CustomLevel` we can add it to our `MinecraftServer`
to be able to access and use the dimension in-game:
```kotlin
val server: MinecraftServer = // ...
val level: CustomLevel = // ...

server.addCustomLevel(level)
```

All the methods for modifying levels on the server and implemented as Kotlin 
extension functions, however you can also find the methods in `ArcadeDimensions`
as static methods for Java:
```java
MinecraftServer server = // ...
CustomLevel level = // ...

ArcadeDimensions.add(server, level);
```

Adding a custom level to the server may throw an exception if a custom level of a
difference instance under the same dimension key is already registered.

There are some other methods for adding custom levels:
```kotlin
val server: MinecraftServer = // ...
val builder: CustomLevelBuilder = // ...
    
server.addCustomLevel(builder)
    
server.addCustomLevel { // CustomLevelBuilder
    randomDimensionKey()
    randomSeed()
    vanillaDefaults(VanillaDimension.Overworld)
    // ...
}
```

### Loading Custom Dimensions

## Vanilla-Like Dimensions