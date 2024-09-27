# Events

Arcade implements a simple events system for common events that are mostly aimed towards minigame development.

This document will explain the overarching events api, for minigame-specific details see [Minigame Event's Section](old-minigames/events.md).

## Listening To Events

### Static Event Listeners

If you wish to listen to a given event for the entire lifetime of your application, then you can simply register an event listener to the `GlobalEventHandler`. You can do this by calling the `register` method providing the type of the event you wish to listen to and the callback for when it is fired:
```kotlin
GlobalEventHandler.register<PlayerTickEvent> { event ->
    val player = event.player
}
```

There are some additional parameters for the register method that lets us fine-tune our event listener. We can also specify a priority as well as a phase.

The priority of an event determines when an event listener is invoked in relation to other event listeners. By default, the priority is `1000`, event listeners with a lower priority value will be invoked first.

```kotlin
// This will be invoked *before* any listeners with the default priority (1000)
GlobalEventHandler.register<PlayerTickEvent>(priority = 990) { event ->
    val player = event.player
}
```

The phase lets you specify at what point exactly during the event you want your event listener to be invoked. The event defines these, for example, the `ServerTickEvent` defines a `"pre"` and a `"post"` phase, for before the tick has occurred and after the server has ticked respectively. 

```kotlin
GlobalEventHandler.register<ServerTickEvent>(phase = BuiltInEventPhases.POST) { event ->
    val server = event.server
}
```

Ensure to read the documentation of the event you are listening to as it will specify the phases (if any) that it has. If the phase is not of importance, you can use the default phase (which again is determined by the event implementation).

### Dynamic Event Listeners

Dynamic listeners are for the use case when you want to create an event listener for a set period of time. We can do this by essentially creating our own event handler and by registering and unregistering it to and from the `GlobalEventHandler`.

Arcade provides a class `SingleEventHandler` that allows you to create an event handler that just listens to a single event.
```kotlin
val eventHandler = SingleEventHandler.of<ServerTickEvent>(priority = 1000, phase = BuiltInEventPhases.POST) { event ->
    // ...
}

// Our event handler will now be processing event broadcasts
GlobalEventHandler.addHandler(eventHandler)

// Our event will no longer be processing event broadcasts
GlobalEventHandler.removeHandler(eventHandler)
```

You can also create an instance of `EventHandler` which is essentially a stand-alone instance of a `GlobalEventHandler` which allows you to register many different events.

```kotlin
val eventHandler = EventHandler() 
eventHandler.register<ServerTickEvent>(phase = BuiltInEventPhases.POST) { event ->
    val server = event.server
}

GlobalEventHandler.addHandler(eventHandler)

GlobalEventHandler.removeHandler(eventHandler)
```

And you can implement your own handler if these are not sufficient by implementing the `ListenerHandler` interface. 

### Cancelling Events

Some events can be cancelled, this means any logic that would usually run after the event will no longer be run.

Let's take the `BrewingStandBrewEvent` as an example, say we do not want players to be able to brew level 2 potions, because this event implements the `CancellableEvent` it means we can cancel the brewing within our listener:
```kotlin
GlobalEventHandler.register<BrewingStandBrewEvent> { event ->
    val ingredient = event.entity.getItem(3)
    if (ingredient.isOf(Items.GLOWSTONE_DUST)) {
        event.cancel()
    }
}
```

Now when a player tries to put glowstone into a brewing stand, it will not be able to brew.

> [!NOTE]
> Since there may be multiple listeners registered for a specific event, any of them can cancel the event. You can check if an event has been cancelled by a listener invoked prior to yours using the `CancellableEvent.isCancelled()` method.

Cancellable events may also be typed, meaning that there must be a specific return value which you must specify to cancel the event. One example being `PlayerRequestLoginEvent` which is fired whenever a player tries to log in to the server, if we want to prevent them from logging in, we need to display them a disconnect message, so to cancel this event, we must pass in a `Component`:
```kotlin
GlobalEventHandler.register<PlayerRequestLoginEvent> { event ->
    if (/* ... */) {
        event.cancel(Component.literal("You cannot login now!"))
    }
}
```

As with regular cancellable events, you can get the previous cancellable result using the `CancellableEvent.result()`, this will throw an exception if no result has been set.

### Mutable Events

Some events are designed to be mutable, such events are usually for registering objects, for example, registering advancements with the `ServerAdvancementReloadEvent`:
```kotlin
GlobalEventHandler.register<ServerAdvancementReloadEvent> { event ->
    val advancement: AdvancementHolder = // ...
    event.add(advancement)
    
    val advancements: Collection<AdvancementHolder> = // ...
    event.addAll(advancements)
}
```

This will then automatically handle the registration of any objects you passed into the event.

### Recursion

It is possible that in your event listener you may want to do something which will cause the same event to be broadcast again. For example, in a `PlayerClientboundPacketEvent` event listener you may want to send another packet, which in turn will broadcast the event again. This is allowed to some degree. You may have a specific event type recurse at most 10 times, at which point the `GlobalEventHandler` will stop broadcasting events of that type, this is a safety measure to prevent unwanted crashes.

If for some reason you *really* need to use recursive events, then you can bypass the recursion checker by using `GlobalEventHandler.recursive` in your event listener, however this is *highly* discouraged.
```kotlin
GlobalEventHandler.register<PlayerClientboundPacketEvent> { (player, packet) ->
    GlobalEventHandler.recursive {
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

## Broadcasting Events

You may want to broadcast your own events, which Arcade makes simple. To get started you need to implement your `Event` class, this is a class that contains all the data for a given event. This class must implement the `Event` interface:
```kotlin
class MyEvent(
    val foo: String,
    val bar: Int
): Event
```

If your event is player, level, or minigame related you should implement their respective event interfaces, `PlayerEvent`, `LevelEvent`, `MinigameEvent`. Your event can implement multiple of these.
```kotlin
class MyPlayerEvent(
    override val player: ServerPlayer
): PlayerEvent
```

This is for compatability with the minigames-event system, so it can tell whether a given event is relevant to the minigame, more information about this in the [Minigame Event's Section](old-minigames/events.md).

Then all you need to do to broadcast your event is pass the event instance into the `GlobalEventHandler#broadcast` method, this will automatically invoke any listeners.
```kotlin
class MyEvent(
    val foo: String,
    val bar: Int
): Event

fun broadcastMyEvent() {
    val event = MyEvent("Foo", 10)
    GlobalEventHandler.broadcast(event)
}
```

As previously mentioned in the event listening section, events can have different phases, we can specify the phase we want our event to broadcast with:
```kotlin
class MyEvent(
    val foo: String,
    val bar: Int
): Event

fun broadcastDoingSomething() {
    val event = MyEvent("Foo", 10)
    // Broadcast in the "default" and "pre" phases
    GlobalEventHandler.broadcast(event, BuiltInEventPhases.PRE_PHASES)
    
    // Do Something ...
    
    // Broadcast in the "post" phase
    GlobalEventHandler.broadcast(event, BuiltInEventPhases.POST_PHASES)
}
```

You do not have to use these specific phases, you can use any you wish, however, it should be noted that these should be documented and additionally, you likely want to always broadcast a "default" phase.

### Cancellable Events

You may want to implement an event that is cancellable. To do this your `Event` class must extend `CancellableEvent.Default` or `CancellableEvent.Typed<T>` depending on whether you want your cancellable event to have a return type, as discussed in the [Cancelling Events Section](#cancelling-events).

The obvious use-case is for mixing into vanilla and adding events that allow listeners to cancel said behaviour.
```kotlin
class MyCancellableEvent: CancellableEvent.Default()
```

```java
@Mixin(Foo.class)
public class FooMixin {
    @Inject(
        method = "foo",
        at = @At("Head"),
        cancellable = true
    )
    private void onFoo(CallbackInfo ci) {
        MyCancellableEvent event = new MyCancellableEvent();
        GlobalEventHandler.broadcast(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
```

> [!NOTE]
> Events that are broadcast off the main thread **cannot** be cancelled.

### Thread Safety

Typically, all events should be broadcast from the main server thread. However, if your event is intended to be broadcast off the main thread then you should also implement the `ServerOffThreadEvent`, this is more-so to show the intention that the event is intended to be broadcast off the main thread. All events that are broadcast off the main thread **will** be pushed back onto the main thread, without implementing the `ServerOffThreadEvent` the event handler will produce warnings as it's likely unintentional.