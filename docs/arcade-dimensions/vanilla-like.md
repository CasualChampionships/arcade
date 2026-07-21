# Vanilla-Like Dimensions

You may want to create dimensions that mirror the vanilla behaviour of the 
overworld, nether, and end. Such that the vanilla portals work correctly
between custom dimensions, as well as more niche things like advancement triggers.

Arcade provides a built-in way of dynamically creating 'vanilla-like' dimensions 
which aim to accurately mirror the behaviour of the default vanilla dimensions.

We can use the `VanillaLikeLevelsBuilder` to build our vanilla dimensions. We can
call `add` to add the dimensions we want without customizing the dimensions further.

```kotlin
val builder = VanillaLikeLevelsBuilder()
    .add(VanillaDimension.Overworld, VanillaDimension.End) // varargs
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
