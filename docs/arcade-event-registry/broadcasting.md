# Broadcasting Events

You may want to broadcast your own events, which Arcade makes simple. 
To get started you need to implement your `Event` class, this is a class that 
contains all the data for a given event. This class must implement the `Event` 
interface:
```kotlin
class MyEvent(
    val foo: String,
    val bar: Int
): Event
```

If your event is player, level, or minigame related you should implement their 
respective event interfaces, `PlayerEvent`, `LevelEvent`, `MinigameEvent`. 
Your event can implement multiple of these.
```kotlin
class MyPlayerEvent(
    override val player: ServerPlayer
): PlayerEvent
```

This is for compatability with the minigames-event system, so it can tell whether a 
given event is relevant to the minigame, more information about this in the 
[Minigame Event's Section](../arcade-minigames/events.md).

Then all you need to do to broadcast your event is pass the event instance into the 
`GlobalEventHandler#broadcast` method, this will automatically invoke any listeners.
```kotlin
class MyEvent(
    val foo: String,
    val bar: Int
): Event

fun broadcastMyEvent() {
    val event = MyEvent("Foo", 10)
    GlobalEventHandler.Server.broadcast(event)
}
```

As previously mentioned in the event listening section, events can have different 
phases, we can specify the phase we want our event to broadcast with:
```kotlin
class MyEvent(
    val foo: String,
    val bar: Int
): Event

fun broadcastDoingSomething() {
    val event = MyEvent("Foo", 10)
    // Broadcast in the "default" and "pre" phases
    GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.PRE_PHASES)
    
    // Do Something ...
    
    // Broadcast in the "post" phase
    GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.POST_PHASES)
}
```

You do not have to use these specific phases, you can use any you wish, however, it 
should be noted that these should be documented and additionally, you likely want to 
always broadcast a "default" phase.

## Cancellable Events

You may want to implement an event that is cancellable. To do this your `Event` 
class must extend `CancellableEvent.Default` or `CancellableEvent.Typed<T>` 
depending on whether you want your cancellable event to have a return type, as 
discussed in the [Cancelling Events Section](listening.md#cancelling-events).

The obvious use-case is for mixing into vanilla and adding events that allow 
listeners to cancel said behaviour.
```kotlin
class MyCancellableEvent: CancellableEvent.Default()
```

```java
@Mixin(Foo.class)
public class FooMixin {
    @Inject(
        method = "foo",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onFoo(CallbackInfo ci) {
        MyCancellableEvent event = new MyCancellableEvent();
        GlobalEventHandler.Server.broadcast(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
```

## Thread Safety

By default, arcade assumes that all event listeners expect events to be broadcast 
from the main thread, with the exception if the event type inherits `AsyncEvent`. 
As a result, if an event is broadcast off the main thread, then it will be pushed to 
the main thread for listeners.
The listener can explicitly specify a threading strategy, which by default pushes all 
non-`AsyncEvent`s to the main thread. 
By default, we can choose `ThreadingTarget.ForceMainThread` which forces *all* events
to be broadcasted on the main thread, or `ThreadingTarget.UseCurrentThread` which
will force non-`AsyncEvent`s to be broadcast on the broadcasting thread.
```kotlin
EventListener.of<ServerTickEvent>(
    strategy = ThreadingTarget.ForceMainThread,
    listener = { _ ->
        
    }
)
```
The reason for not wanting to require the main thread would be to allow you to mutate
or cancel events that were broadcast off-thread, as mutating them after they've been
pushed to the main thread is not very useful.
