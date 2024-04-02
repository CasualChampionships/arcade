# Events

Events are a key part of making your minigame work.

If you have not already, you should read the [Events](./events.md)
documentation. This covers all the basics as well as what events are
available to you.

However, minigames handle events slightly differently. Each minigame will
have its own `MinigameEventHandler` which is responsible for handling all
the events that happen during a minigame's lifetime.

The special thing about this event handler is that it will *filter* events
and will only invoke your registered listeners to those events that are
relevant to the minigame.

As we know, minigames keep track of players and which levels our minigame
is in. The `MinigameEventHandler` uses this information to filter out
`PlayerEvent`s and `LevelEvent`s that are only applicable to our minigame's
players and levels.

## Registering Listeners

There are two ways to register listeners to the `MinigameEventHandler`:

The first way is directly through the `MinigameEventHandler`:
```kotlin
val minigame: Minigame = // ...
minigame.events.register<ServerTickEvent> {
    // ...
}
```

Same as regular event handlers you can specify your own `EventListeners`
as well as provide your own priority to define what order you want your
listeners to be called in, read more about this in the [Events](./events.md)
documentation.

Minigames gives you more control over your listeners, however, as previously mentioned,
we already know that it will filter for only events that are relevant to the minigame:
```kotlin
minigame.events.register<PlayerTickEvent> {
    // ...
}
```
This listener will only be invoked for all players that are playing in the current minigame.

However, we can have even more control over when our listeners are invoked, specifically
during which phases. We can for example declare an event listener that will only be invoked
in specified phases:
```kotlin
val minigame: MyMinigame = // ...
val grace = MyMinigamePhase.Grace
val active = MyMinigamePhase.Active
minigame.events.registerInPhases<ServerTickEvent>(grace, active) {
    // ...
}
```
In this case, our listener will only be invoked in the given minigame phases.

If you want it to be between a larger selection of phases, we can do that too, and instead
only invoke our listener if the minigame is between two phases (inclusive):
```kotlin
val minigame: MyMinigame = // ...
val grace = MyMinigamePhase.Grace
val deathMatch = MyMinigamePhase.DeathMatch
minigame.events.registerBetweenPhases<ServerTickEvent>(grace, deathMatch) {
    // ...
}
```

Something to note about these last two is that they are permanent for the lifetime of
the minigame. However, if we want to register a phased specific event that only
lasts until the end of the current phase, we can do the following:
```kotlin
minigame.events.registerPhased<ServerTickEvent> {
    // ...
}
```
This listener will be deleted after the minigames phase changes.

These ways are nice for defining a couple listeners, and it also allows you
to define listeners outside your actual minigame class. However, it can get
messy when you need to define tens of listeners to define your minigame
behaviour. So let's have a look at the alternative way of declaring a listener:

```kotlin
class MyMinigame(
    server: MinecraftServer
): Minigame<MyMinigame>(server) {
    // ...
    
    @MinigameEvent
    private fun onServerTick(event: ServerTickEvent) {
        // ...
    }

    @MinigameEvent
    private fun onMinigameAddPlayer(event: MinigameAddPlayerEvent) {
        // ...
    }
}
```

We can instead use the `@MinigameEvent` annotation which allows us to declare
a method with the parameter defining which event that it will be listening to;
there are no restrictions to what you name your method. By default this will be
permanent for the lifetime of the minigame.

If you are using IntelliJ you can add `net.casual.arcade.minigame.annotation.MinigameEvent`
in `Settings > Editor > Inspections > Java > Declaration redudancy > Unused declaration > Entry points > Annotations...`
as an entry point and IntelliJ will stop giving you warnings that it is unused.

Similarly to the control we have with the `register` methods we can do the same with
the annotation:
```kotlin
@MinigameEvent(
    priority = 2_000,
    phases = ["grace", "death_match"]
)
private fun onMinigameAddPlayer(event: MinigameAddPlayerEvent) {
    // ...
}
```
We can specify the priority of our event, and this works the same as before. We can also specify
all the phases that we want this listened to be called during. This uses the `id`s of the
minigame phases (as we cannot pass enums into annotations), so you may also want to create
constant variables with the ids to make this easier.

And further, we can define bounds so that our listener is only called when our minigame
is between the given phases (inclusive):
```kotlin
@MinigameEvent(
    priority = 2_000,
    start = "grace",
    end = "death_match"
)
private fun onMinigameAddPlayer(event: MinigameAddPlayerEvent) {
    // ...
}
```