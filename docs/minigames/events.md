# Events

> Return to [table of contents](../minigames.md)

Events are fundamental for implementing minigame logic. The goal of the event system is to provide hooks that are commonly used in minigames, to reduce the number of mixins you need to add yourself.

The list of events is not exhaustive, and it is likely that you will need to supplement the events with your own.

This documentation will focus on the application of Arcade's event system for Minigames, for an overview of Arcade's overall event system see the [Event Documentation](../events.md).

## Motivation

Minigames have a finite lifetime; we do not want the events registered in a minigame to be invoked for the entirety of the server's lifetime, for this reason minigame's have their own event handler which register at the start of a minigame and unregister when the minigame closes.

Additionally, when listening to events we typically only want to listen to events that are relevant to our minigame: We don't care if a player outside our minigame takes damage. The `MinigameEventHandler`, which is the implementation of the minigame's event handler, filters events based on relevance, this will be covered in more detail below.

## Registering Listeners

There are two main ways to register events, one using the `MinigameEventHandler` directly and Arcade also provides annotations for a nicer way of registering events. We will first cover using the `MinigameEventHandler`.

This is the same as registering to the `GlobalEventHandler`, however instead reference the `MinigameEventHandler`, similarly to the other register method you can specify your own priority and event phase.
```kotlin
val minigame: Minigame = // ...
minigame.events.register<ServerTickEvent> {
    // ...
}
```

Typically, you register any events your `initialize` method in your minigame implementation. These event listeners will be registered for the entirety of the minigame's lifetime.

### Filtered Events

As previously mentioned in the [Motivation Section](#motivation) minigames will automatically filter events by relevance. By default this will filter any events that implement the following:
- `PlayerEvent` - The minigame will ensure the player related to the event is part of the minigame.
- `LevelEvent` - The minigame will ensure the level related to the event is part of the minigame.
- `MinigameEvent` - The minigame will ensure the minigame related to the event is the same as the current minigame.

This means that you require fewer checks in your event listeners to get the behaviour you desire. However, these filters are configurable to allow for more flexibility.

Some flags determine the filters when registering your event:
```kotlin
val minigame: Minigame = // ...
    
// Setting flags to NONE will result in no filter at all
minigame.events.register<PlayerTickEvent>(flags = ListenerFlags.NONE) {
    // ...
}
```

There are also additional flags that we can use:
```kotlin
val minigame: Minigame = // ...
    
// Setting flags to IS_PLAYING will result in only accepting events
// from players who are playing in this minigame
minigame.events.register<PlayerTickEvent>(flags = ListenerFlags.IS_PLAYING) {
    // ...
}

// Now we will only accept events from players who are spectating
minigame.events.register<PlayerTickEvent>(flags = ListenerFlags.IS_SPECTATING) {
    // ...
}
```

### During Minigame Phases

We can have even more control over when our listeners are invoked, specifically what minigame phases we want our listeners to be invoked in. It's likely that there are some behaviours that you only wish to have during certain minigame phases, we can do this by using the `registerInPhases` method, this takes a variable about of phases in as a parameter:
```kotlin
val minigame: MyMinigame = // ...
val grace = MyMinigamePhase.Grace
val active = MyMinigamePhase.Active
minigame.events.registerInPhases<ServerTickEvent>(grace, active) {
    // ...
}
```
In this case, our listener will only be invoked during the `Grace` and `Active` phases of our minigame.

If you want it to be between a large section of your minigame, you can use the `registerBetweenPhases` method to register an after phase (inclusive) and before phase (exclusive):
```kotlin
val minigame: MyMinigame = // ...
val grace = MyMinigamePhase.Grace
val deathMatch = MyMinigamePhase.DeathMatch
minigame.events.registerBetweenPhases<ServerTickEvent>(grace, deathMatch) {
    // ...
}
```
In this case, our listener will be invoked in all phases after (and including) the `Grace` phase and before the `DeathMatch` phase.

Something to note about these last two is that they are permanent for the lifetime of
the minigame. However, if we want to register a phased specific event that only
lasts until the end of the current phase, we can do the following:
```kotlin
minigame.events.registerPhased<ServerTickEvent> {
    // ...
}
```
This listener will be deleted after the minigames phase changes.

### Listener Annotation

These ways are nice for defining a couple listeners, and it also allows you
to define listeners outside your actual minigame class. However, it can get
messy when you need to define tens of listeners to define your minigame
behaviour. So let's have a look at the alternative way of declaring a listener:

```kotlin
class MyMinigame(
    server: MinecraftServer
): Minigame<MyMinigame>(server) {
    // ...
    
    @Listener
    private fun onServerTick(event: ServerTickEvent) {
        // ...
    }

    @Listener
    private fun onMinigameAddPlayer(event: MinigameAddPlayerEvent) {
        // ...
    }
}
```

We can instead use the `@Listener` annotation which allows us to declare
a method with the parameter defining which event that it will be listening to;
there are no restrictions to what you name your method. By default this will be
permanent for the lifetime of the minigame.

If you are using IntelliJ you can add `net.casual.arcade.minigame.annotation.Listener`
in `Settings > Editor > Inspections > Java > Declaration redudancy > Unused declaration > Entry points > Annotations...`
as an entry point and IntelliJ will stop giving you warnings that it is unused.

Similarly to the control we have with the `register` methods we can do the same with
the annotation:
```kotlin
@Listener(
    priority = 2_000, 
    during = During(phases = ["grace", "death_match"])
)
private fun onMinigameAddPlayer(event: MinigameAddPlayerEvent) {
    // ...
}
```
We can specify the priority of our event, and this works the same as before. We can also specify
all the phases that we want this listened to be called during. This uses the `id`s of the
minigame phases (as we cannot pass enums into annotations), so you may also want to create
constant variables with the ids to make this easier.

And further, we can define bounds so that our listener is only called when our minigame is between the given phases, after (inclusive) and before (exclusive):
```kotlin
@Listener(
    priority = 2_000, 
    during = During(
        after = "grace", 
        before = "death_match"
    )
)
private fun onMinigameAddPlayer(event: MinigameAddPlayerEvent) {
    // ...
}
```

> See the next section on [Scheduling](scheduling.md)