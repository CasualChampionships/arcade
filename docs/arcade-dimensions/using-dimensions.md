# Using Custom Dimensions

## Adding Custom Dimensions

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

## Loading Custom Dimensions

If our world is permanent, then we can instead load our world from disk as long
as we know the dimension key for the world.

```kotlin
val server: MinecraftServer = // ...
val key = ResourceKey.create(
    Registries.DIMENSION,
    Identifier.withDefaultNamespace("foo")
)
    
var level: ServerLevel? = server.loadCustomLevel(key)
// We can also just provide the location:
level = server.loadCustomLevel(key.identifier())
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
    Identifier.withDefaultNamespace("foo")
)

server.loadOrAddCustomLevel(key) { // CustomLevelBuilder
    // We don't have to specify the dimension key
    // it'll automatically be set to the passed in key
    randomSeed()
    vanillaDefaults(VanillaDimension.Overworld)
}
```

## Removing Custom Dimensions

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

## Deleting Custom Dimensions

Much like removing custom levels, but instead we call `deleteCustomLevel`:
```kotlin
val server: MinecraftServer = // ...
val level: CustomLevel = // ...
    
val success: Boolean = server.deleteCustomLevel(level)
```

This will delete the level regardless of its persistence. The level does not have
to be loaded in order for it to be deleted, but if it is loaded then it will
first be unloaded before deletion.
