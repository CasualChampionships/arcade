# Data

Minigames often need *static* data; the map they are played on, where players
spawn, what loot is available. Arcade provides a data-driven way to do this.

The `MinigameData` allows you to provide this static data. Each piece of data 
has a `MinigameDataType` which identifies it, and data is grouped into a 
`MinigameDataSet`, an immutable collection which can be indexed by type.

> [!NOTE]
> Data is static and describes what a minigame is played *with*. If what you
> want is dynamic behaviour instead, use [Components](./components.md).

## Loading a Data Set

Typically, data sets are loaded in from disk. A data set lives in an archive,
either a directory or a zip, which must have a `minigame_data.json` at its root
listing the types of data it provides, for example:

```json
[
  "arcade:world"
]
```

Arcade uses this to find the `MinigameDataProvider` for each type and to create
the `MinigameData` instances. We can then create our set from the archive:

```kotlin
val server: MinecraftServer = // ...
val archive = ReadableArchive.from(Path.of("maps", "example_map.zip"))

val data: MinigameDataSet = MinigameDataSet.from(archive, server)
```

Data sets can also be created dynamically, which is useful for testing or for
data you generate yourself, and an existing set can be extended with additional
data:

```kotlin
val world: MinigameWorldData = // ...

val data: MinigameDataSet = MinigameDataSet.from("example", world)

// Overriding the world data of an existing set
val overridden: MinigameDataSet = data.with(world)

// A set with no data at all
val empty: MinigameDataSet = MinigameDataSet.empty()
```

A data set holds onto the archive it was read from, so you should close it 
once your minigame is done with it.

## Using a Data Set

We index a set by `MinigameDataType`, either getting the data or requiring it:

```kotlin
val data: MinigameDataSet = // ...

// Gets the data, null if the set doesn't provide it
val world: MinigameWorldData? = data.get(MinigameWorldData.TYPE)

// Gets the data, throwing if the set doesn't provide it
val required: MinigameWorldData = data.require(MinigameWorldData.TYPE)
```

Because a set is only as good as the data in it, you should check that it
provides everything your minigame needs as soon as you have created it. Once
`has` succeeds, subsequent calls to `require` with those types are guaranteed to
be safe:

```kotlin
val data: MinigameDataSet = // ...

if (!data.has(MinigameWorldData.TYPE)) {
    throw IllegalArgumentException("Data set ${data.id} is missing a world")
}
```

## World Data

`MinigameWorldData` is the built-in data type for the world files of a map. To
provide it, your archive needs a `world/` directory containing the world files,
and `arcade:world` in its `minigame_data.json`.

Once the minigame has created its dimension, we extract the world data into it:

```kotlin
val minigame: Minigame = // ...
val data: MinigameDataSet = // ...

data.require(MinigameWorldData.TYPE).extract(minigame.server, DIMENSION)
```

> [!NOTE]
> The dimension you extract into must either be unloaded or not exist yet,
> otherwise the game will simply overwrite the extracted files on the next save.

## Custom Data

To add your own data type, implement `MinigameData` along with a
`MinigameDataProvider` which creates it from an archive:

```kotlin
class ExampleSpawnData(val spawns: List<Vec3>): MinigameData {
    override fun type(): MinigameDataType<ExampleSpawnData> {
        return type
    }

    companion object: MinigameDataProvider<ExampleSpawnData> {
        override val type = MinigameDataType<ExampleSpawnData>(Identifier("modid", "spawns"))

        override fun get(archive: ReadableArchive, server: MinecraftServer): ExampleSpawnData {
            val spawns = archive.parseJson("spawns.json", Vec3.CODEC.listOf(), server).getOrThrow()
            return ExampleSpawnData(spawns)
        }
    }
}
```

Providers must be registered in the `MinigameRegistries.MINIGAME_DATA_PROVIDER`
registry so that Arcade can resolve them from a `minigame_data.json`:

```kotlin
object ExampleMinigameMod: ModInitializer {
    override fun onInitialize() {
        ExampleSpawnData.register(MinigameRegistries.MINIGAME_DATA_PROVIDER)
    }
}
```

An archive which lists `modid:spawns` and has a `spawns.json` will then provide
`ExampleSpawnData` in its data set.
