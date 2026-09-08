# Events

Events are fundamental for implementing minigame logic. The goal of the event 
system is to provide hooks that are commonly used in minigames, to reduce the 
number of mixins you need to add yourself.

The list of events is not exhaustive, and it is likely that you will need to 
supplement the events with your own.

This documentation will focus on the application of Arcade's event system for 
Minigames, for an overview of Arcade's overall event system see the [Event Documentation](../arcade-events-server/getting-started.md).

## Motivation

Minigames have a finite lifetime; we do not want the events registered in a 
minigame to be invoked for the entirety of the server's lifetime, for this 
reason minigame's have their own event handler which register at the start of a
minigame and unregister when the minigame closes.

Additionally, when listening to events, we typically only want to listen to 
events that are relevant to our minigame: We don't care if a player outside our
minigame takes damage. The `MinigameEventHandler`, which is the implementation 
of the minigame's event handler, filters events based on relevance, this will 
be covered in more detail below.

## Registering Listeners

There are two main ways to register events, one using the 
`MinigameEventHandler` directly and Arcade also provides annotations for a 
nicer way of registering events. We will first cover using the 
`MinigameEventHandler`.

This is the same as registering to the `GlobalEventHandler`, however, instead 
reference the `MinigameEventHandler`, similarly to the other register method 
you can specify your own priority and event phase.
```kotlin
val minigame: Minigame = // ...
minigame.events.register<ServerTickEvent> {
    // ...
}
```

Typically, you register any events your initializer method in your minigame 
implementation. These event listeners will be registered for the entirety of 
the minigame's lifetime.

### Filtered Events

As previously mentioned in the [Motivation Section](#motivation) minigames will automatically filter events by relevance. By default, this will filter any events that implement the following:
- `PlayerEvent` - The minigame will ensure the player related to the event is part of the minigame.
- `LevelEvent` - The minigame will ensure the level related to the event is part of the minigame.
  - `LocatedLevelEvent` - The minigame will ensure the level and position related to the event are within the minigame.
- `MinigameEvent` - The minigame will ensure the minigame related to the event is the same as the current minigame.

This means that you require fewer checks in your event listeners to get the behaviour you desire. However, these filters are configurable to allow for more flexibility.

Each of these is a `ListenerFilter`, and you can pass your own set of filters
when registering your event:
```kotlin
val minigame: Minigame = // ...

// The default filters, which is what you get if you don't specify any
minigame.events.register<PlayerTickEvent>(filters = ListenerFilter.default()) {
    // ...
}

// Registering with no filters at all
// This will fire for **ALL** players on the server
minigame.events.register<PlayerTickEvent>(filters = ListenerFilter.unfiltered()) {
    // ...
}
```

There are also additional filters that we can use:
```kotlin
val minigame: Minigame = // ...

// Only accepting events from players who are playing in this minigame
minigame.events.register<PlayerTickEvent>(filters = ListenerFilter.of(ListenerFilter.IsPlaying)) {
    // ...
}

// Now we will only accept events from players who are spectating
minigame.events.register<PlayerTickEvent>(filters = ListenerFilter.of(ListenerFilter.IsSpectator)) {
    // ...
}
```

Filters only apply to the events they are relevant to; registering with
`IsPlaying` for an event which isn't a `PlayerEvent` simply does nothing.

### During Minigame Phases

We can have even more control over when our listeners are invoked, specifically
what minigame phases we want our listeners to be invoked in. It's likely that 
there are some behaviours that you only wish to have during certain minigame 
phases, we can do this by registering our listener against a scope with a given
`MinigamePhaseLifetime`:
```kotlin
val minigame: ExampleMinigame = // ...
val scope = minigame.scopes.create(
    MinigamePhaseLifetime.During(ExamplePhase.Grace, ExamplePhase.Active)
)
scope.register<ServerTickEvent> {
    // ...
}
```
In this case, our listener will only be invoked during the `Grace` and `Active`
phases of our minigame.

If you want it to be between a large section of your minigame, you can use the
`Between` lifetime instead, which survives while the minigame is *strictly
between* the two given phases:
```kotlin
val minigame: ExampleMinigame = // ...
val scope = minigame.scopes.create(
    MinigamePhaseLifetime.Between(ExamplePhase.Grace, ExamplePhase.DeathMatch)
)
scope.register<ServerTickEvent> {
    // ...
}
```

When a scope closes, everything registered through it is unregistered for you,
so you never have to remove these listeners yourself. Scopes own more than just
listeners, and the other lifetimes are covered in the
[Scheduling Section](./scheduling.md).

Any listener you register can also be unregistered manually; every `register`
method returns a handle:
```kotlin
val minigame: Minigame = // ...
val handle = minigame.events.register<ServerTickEvent> {
    // ...
}

handle.remove()
```

### Listener Annotation

These ways are nice for defining a couple listeners, and it also allows you
to define listeners outside your actual minigame class. However, it can get
messy when you need to define tens of listeners to define your minigame
behaviour. So let's have a look at the alternative way of declaring a listener:

```kotlin
class ExampleMinigame(
    server: MinecraftServer,
    uuid: UUID
): Minigame(server, uuid, ID, ExamplePhase.entries) {
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
there are no restrictions to what you name your method, however, the method 
**must** be declared private. By default, this will be permanent for the 
lifetime of the minigame.

If you are using IntelliJ you can add `net.casual.arcade.minigame.annotation.Listener`
in `Settings > Editor > Inspections > Java > Declaration redudancy > Unused declaration > Entry points > Annotations...`
as an entry point and IntelliJ will stop giving you warnings that it is unused.

Similarly to the control we have with the `register` methods we can do the same
with the annotation:
```kotlin
@Listener(
    priority = 2_000,
    filters = [ListenerFilter.IsPlaying]
)
private fun onMinigameAddPlayer(event: MinigameAddPlayerEvent) {
    // ...
}
```
We can specify the priority of our event, and this works the same as before, as
well as the filters that will be applied to the listener.

### Listeners Outside Your Minigame

The `@Listener` annotation isn't limited to your minigame class. Any class can
declare annotated listeners by implementing `MinigameEventListener` and being
added to a scope:

```kotlin
class ExampleListeners: MinigameEventListener {
    @Listener
    private fun onServerTick(event: ServerTickEvent) {
        // ...
    }
}

val minigame: Minigame = // ...
minigame.scopes.root.addEventListener(ExampleListeners())
```

All the listeners declared in that class are registered against the scope, and
are unregistered when the scope closes. This is how [Components](./components.md) 
declare their listeners.
