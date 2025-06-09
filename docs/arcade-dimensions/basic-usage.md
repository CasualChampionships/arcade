# Basic Usage

> Return to [table of contents](getting-started.md)

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

We can also provide a 'spoofed' dimension key, this key will be sent to the client
when they join the dimension, so the client believes they are in that dimension.
This may be useful if you want to hide the real dimension key or if you rely on specific
behaviour when the client believes it's in a specific dimension.
```kotlin
val key = ResourceKey.create(
    Registries.DIMENSION,
    ResourceLocation.withDefaultNamespace("spoofed")
)

val builder = CustomLevelBuilder()
    .randomDimensionKey()
    .spoofedDimensionKey(key)
```

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
For example, we cannot change the height of the world, but we can, for example,
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

If our world is permanent, then we can instead load our world from disk as long
as we know the dimension key for the world.

```kotlin
val server: MinecraftServer = // ...
val key = ResourceKey.create(
    Registries.DIMENSION,
    ResourceLocation.withDefaultNamespace("foo")
)
    
var level: ServerLevel? = server.loadCustomLevel(key)
// We can also just provide the location:
level = server.loadCustomLevel(key.location())
```

This will firstly check if a level with the given key is already loaded, if it is,
then it will return that level, otherwise it will call `CustomLevel.read` which
attempts to deserialize the level from disk, the level will then be added to the
server automatically. If the level cannot be read, or doesn't exist then it will
return `null`.

Finally, we can load or add a custom level. This will try to load the level first
if unsuccessful it will then add a specified `CustomLevel`. A `CustomLevelBuilder`
function is provided which is lazy and will only be invoked if the level fails to load.

```kotlin
val server: MinecraftServer = // ...
val key = ResourceKey.create(
    Registries.DIMENSION,
    ResourceLocation.withDefaultNamespace("foo")
)

server.loadOrAddCustomLevel(key) { // CustomLevelBuilder
    // We don't have to specify the dimension key
    // it'll automatically be set to the passed in key
    randomSeed()
    vanillaDefaults(VanillaDimension.Overworld)
}
```

### Removing Custom Dimensions

Removing custom levels is super simple:
```kotlin
val server: MinecraftServer = // ...
val level: CustomLevel = // ...
    
val success: Boolean = server.removeCustomLevel(level)
```

There are two possible methods or removal depending on the persistence of the level.
If the level is temporary then calling `removeCustomLevel` is equivalent to calling
`deleteCustomLevel`, which will be discussed in the next section. Otherwise, if the
level is persistent or permanent, then the level will simply be unloaded.

Typically, you should remove all the players from a level before you attempt to
unload it, if players aren't removed then they'll be teleported to the overworld,
failing this they will be kicked from the server.

If the level is already unloaded, then calling this method will not do anything and
it will return false.

### Deleting Custom Dimensions

Much like removing custom levels, but instead we call `deleteCustomLevel`:
```kotlin
val server: MinecraftServer = // ...
val level: CustomLevel = // ...
    
val success: Boolean = server.deleteCustomLevel(level)
```

This will delete the level regardless of its persistence. The level does not have
to be loaded in order for it to be deleted, but if it is loaded then it will
first be unloaded before deletion.

## Vanilla-Like Dimensions

You may want to create dimensions that mirror the vanilla behaviour of the 
overworld, nether, and end. Such that the vanilla portals work correctly
between custom dimensions, as well as more niche things like advancement triggers.

Arcade provides a built-in way of dynamically creating 'vanilla-like' dimensions 
which aim to accurately mirror the behaviour of the default vanilla dimensions.

We can use the `VanillaLikeLevelsBuilder` to build our vanilla dimensions. We can
call `add` to add the dimensions we want without customizing the dimensions further.

```kotlin
val builder = VanillaLikeLevelsBuilder()
    .add(VanillDimension.Overworld, VanillaDimension.End) // varargs
```

You do not have to add all three vanilla dimensions; any unspecified dimensions
will just not exist, in the example above where we did not specify the nether,
nether portals will simply just not work.

If we want more customization over the vanilla dimensions, we can use the `set`
method instead:
```kotlin
val builder = VanillaLikeLevelsBuilder()
    .set(VanillaDimension.Overworld) {
        randomDimensionKey()
        vanillaDefaults(VanillaDimension.Nether)
    }
    .set(VanillaDimension.End) {
        randomDimensionKey()
        vanillaDefaults(VanillaDimension.Overworld)
    }
```

In this example, we now have an overworld dimension, which will generate like the 
nether, and an end dimension which will generate like the overworld. To travel between
these two dimensions, you will need to use an end portal as these are still technically
the 'overworld' and 'end' dimensions even if they don't generate like it.

One thing to note about the end dimension, it will generate the dragon fight like in vanilla,
but *only* if the dimension type is set to the vanilla end dimension type.

Once you have configured your builder, you can call the `build` method:
```kotlin
val builder: VanillaLikeLevelsBuilder = // ...
val server: MinecraftServer = // ...

val levels: VanillaLikeLevels = builder.build(server)
```

This creates an instance of `VanillaLikeLevels`, similar to the `CustomLevelBuilder`,
this doesn't add the level to the server, you must do this separately. 

You can get specific dimensions, or you can get all the levels:
```kotlin
val levels: VanillaLikeLevels = // ...
    
val nullableLevel: CustomLevel? = levels.get(VanillaDimension.Overworld)
val level: CustomLevel = levels.getOrThrow(VanillaDimension.Overworld)
val all: Collection<CustomLevel> = levels.all()

val server: MinecraftServer = // ...
all.forEach { server.addCustomLevel(it) }
```

> See the next section on [Advanced Usage](advanced-usage.md)