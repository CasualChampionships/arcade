# Registry Event Listeners

Your mod may include data driven features, usually you would put these in your 
`resources/data` directory for your mod and Minecraft loads everything into it's
dynamic registries just before the server boots.

These are registries that aren't listed in `BuiltInRegistries`, you can find all 
the registries that are like this in the `RegistryDataLoader` class.

If you prefer to programmatically add entries into these registries, you can do so
by using the `RegistryEventHandler`, we provide the registry key for the registry
we want to modify, and an event listener which will be invoked when that registry
is loaded. This fires before the registry is frozen so you can register your entries.

It is important to note that you should register your event in your mod initializer. 
If you try to register your event too late, an exception will be thrown.
```kotlin
override fun onInitialize() {
    RegistryEventHandler.register(Registries.DIMENSION_TYPE) { (registry) ->
        Registry.register(registry, Identifier.withDefaultNamespace("foo"), DimensionType(/* */))
    }
}
```
