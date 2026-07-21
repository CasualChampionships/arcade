# Listening To Events

## Static Event Listeners

If you wish to listen to a given event for the entire lifetime of your application, 
then you can simply register an event listener to the `GlobalEventHandler`. 
You can do this by calling the `register` method providing the type of the event 
you wish to listen to and the callback for when it is fired:
```kotlin
GlobalEventHandler.Server.register<PlayerTickEvent> { event ->
    val player = event.player
}
```

Listeners are split by side: `ServerSideEvent`s are 
registered on `GlobalEventHandler.Server` (as above) and `ClientSideEvent`s on 
`GlobalEventHandler.Client`. All the examples here are server-side, but the exact 
same ideas apply to the client.

There are some additional parameters for the register method that lets us fine-tune 
our event listener. We can also specify a priority as well as a phase.

The priority of an event determines when an event listener is invoked in relation 
to other event listeners. By default, the priority is `1000`, event listeners with 
a lower priority value will be invoked first.

```kotlin
// This will be invoked *before* any listeners with the default priority (1000)
GlobalEventHandler.Server.register<PlayerTickEvent>(priority = 990) { event ->
    val player = event.player
}
```

The phase lets you specify at what point exactly during the event you want your 
event listener to be invoked. The event defines these, for example, 
the `ServerTickEvent` defines a `"pre"` and a `"post"` phase, for before the tick 
has occurred and after the server has ticked respectively.

```kotlin
GlobalEventHandler.Server.register<ServerTickEvent>(phase = BuiltInEventPhases.POST) { event ->
    val server = event.server
}
```

Ensure to read the documentation of the event you are listening to as it will specify
the phases (if any) that it has. If the phase is not of importance, you can use the 
default phase (which again is determined by the event implementation).

## Dynamic Event Listeners

Dynamic listeners are for the use case when you want to create an event listener for 
a set period of time. We can do this by essentially creating our own event handler 
and by registering and unregistering it to and from the `GlobalEventHandler`.

Arcade provides a class `SingleListenerProvider` that allows you to create an event 
provider that just listens to a single event.
```kotlin
val provider = SingleListenerProvider.of<ServerTickEvent>(priority = 1000, phase = BuiltInEventPhases.POST) { event ->
    // ...
}

// Our event listener provider will now be processed during event broadcasts
GlobalEventHandler.Server.addProvider(provider)

// Our event listener provider will no longer be processed during event broadcasts
GlobalEventHandler.Server.removeProvider(provider)
```

You can also create an instance of `SimpleListenerRegistry` which is essentially a 
stand-alone instance of a `GlobalEventHandler` which allows you to register many 
different events.

```kotlin
val registry = SimpleListenerRegistry()
registry.register<ServerTickEvent>(phase = BuiltInEventPhases.POST) { event ->
    val server = event.server
}

GlobalEventHandler.Server.addProvider(registry)

GlobalEventHandler.Server.removeProvider(registry)
```

And you can implement your own provider if these are not enough by implementing the 
`ListenerProvider` interface.

## Cancelling Events

Some events can be cancelled, this means any logic that would usually run after the 
event will no longer be run.

Let's take the `BrewingStandBrewEvent` as an example, say we do not want players 
to be able to brew level 2 potions, because this event implements the 
`CancellableEvent` it means we can cancel the brewing within our listener:
```kotlin
GlobalEventHandler.Server.register<BrewingStandBrewEvent> { event ->
    val ingredient = event.entity.getItem(3)
    if (ingredient.isOf(Items.GLOWSTONE_DUST)) {
        event.cancel()
    }
}
```

Now when a player tries to put glowstone into a brewing stand, it will not be able 
to brew.

> [!NOTE]
> Since there may be multiple listeners registered for a specific event, any of them can cancel the event. You can check if an event has been cancelled by a listener invoked prior to yours using the `CancellableEvent.isCancelled()` method.

Cancellable events may also be typed, meaning that there must be a specific return 
value which you must specify to cancel the event. One example being 
`PlayerRequestLoginEvent` which is fired whenever a player tries to log in to the 
server, if we want to prevent them from logging in, we need to display them a 
disconnect message, so to cancel this event, we must pass in a `Component`:
```kotlin
GlobalEventHandler.Server.register<PlayerRequestLoginEvent> { event ->
    if (/* ... */) {
        event.cancel(Component.literal("You cannot login now!"))
    }
}
```

As with regular cancellable events, you can get the previous cancellable result using 
the `CancellableEvent.result()`, this will throw an exception if no result has been set.

## Mutable Events

Some events are designed to be mutable, for example, the `PlayerDamageEvent` where we
can change the damage amount:
```kotlin
GlobalEventHandler.Server.register<PlayerDamageEvent> { event ->
    event.damage *= 0.5 // Half all damage dealt to players
}
```

## Recursion

It is possible that in your event listener you may want to do something which will 
cause the same event to be broadcast again. For example, in a `PlayerClientboundPacketEvent` 
event listener you may want to send another packet, which in turn will broadcast the 
event again. This is allowed to some degree. You may have a specific event type recurse 
at most 10 times, at which point the `GlobalEventHandler` will stop broadcasting events 
of that type, this is a safety measure to prevent unwanted crashes.

If for some reason you *really* need to use recursive events, then you can bypass 
the recursion checker by using `GlobalEventHandler.recursive` in your event listener, however this is *highly* discouraged.
```kotlin
GlobalEventHandler.Server.register<PlayerClientboundPacketEvent> { (player, packet) ->
    GlobalEventHandler.Server.recursive {
        if (packet is ClientboundSetScorePacket && packet.score < 100) {
            player.connection.send(ClientboundSetScorePacket(
                packet.owner,
                packet.objectiveName,
                packet.score + 1,
                packet.display,
                packet.numberFormat
            ))
        }
    }
}
```
