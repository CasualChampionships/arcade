# Advanced Usage

## Registry Event Listeners

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
        Registry.register(registry, ResourceLocation.withDefaultNamespace("foo"), DimensionType(/* */))
    }
}
```

## Injected Listener Providers

This allows us to dynamically add `ListenerProvider`s depending on the specific event 
being broadcasted.

This may help performance instead, for example, instead of each minigame registering 
for a specific `PlayerEvent` then checking whether the player from that event belongs 
in a minigame. We can instead add an injected listener provider which gets the player's 
minigame then adds that minigame's listener provider.

Here's how this is used for minigame related events:
```kotlin
GlobalEventHandler.addInjectedProvider { event, consumer ->
    if (event is ExtensionEvent) {
        return@addInjectedProvider
    }
    val minigames = ObjectOpenHashSet<Minigame<*>>(3)
    if (event is PlayerEvent) {
        val minigame = event.player.getMinigame()
        if (minigame != null) {
            minigames.add(minigame)
        }
    }
    if (event is LevelEvent) {
        val minigame = event.level.getMinigame()
        if (minigame != null) {
            minigames.add(minigame)
        }
    }
    if (event is MinigameEvent) {
        minigames.add(event.minigame)
    }
    for (minigame in minigames) {
        consumer.accept(minigame.events.getInjectedProvider())
    }
}
```