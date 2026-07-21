# Injected Listener Providers

This allows us to dynamically add `ListenerProvider`s depending on the specific event 
being broadcasted.

This may help performance instead, for example, instead of each minigame registering 
for a specific `PlayerEvent` then checking whether the player from that event belongs 
in a minigame. We can instead add an injected listener provider which gets the player's 
minigame then adds that minigame's listener provider.

Here's how this is used for minigame related events:
```kotlin
GlobalEventHandler.Server.addInjectedProvider { event, consumer ->
    if (event is ExtensionEvent) {
        return@addInjectedProvider
    }
    val minigames = ObjectOpenHashSet<Minigame>(3)
    if (event is PlayerEvent) {
        val minigame = event.player.getMinigame()
        if (minigame != null) {
            minigames.add(minigame)
        }
    }
    if (event is LocatedLevelEvent) {
        minigames.addAll(event.level.getMinigames(event.pos))
    } else if (event is LevelEvent) {
        minigames.addAll(event.level.getMinigames())
    }
    if (event is MinigameEvent) {
        minigames.add(event.minigame)
    }
    for (minigame in minigames) {
        consumer.accept(minigame.events.getInjectedProvider())
    }
}
```
