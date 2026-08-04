# Custom Schedulers

## Ticked Scheduler

If you wish to have your own instance of a scheduler you can create an instance of `SimpleTickedScheduler`, this gives you more control over the scheduled tasks.

A scheduler needs to know which logical side it is ticked on, as this determines which thread its tasks run on. You can pass a `LogicalSide` directly, or use the `server` and `client` factory methods:

```kotlin
val scheduler = SimpleTickedScheduler.server()
```

You control when the scheduler is ticked, you essentially control when tasks will be executed, this may be useful, for example, if you want a scheduler that runs tasks at the start of a tick instead of the end.

```kotlin
val scheduler = SimpleTickedScheduler.server()

GlobalEventHandler.Server.register<ServerTickEvent>(phase = PRE) {
    scheduler.tick()
}
```

The client equivalent ticks from `ClientTickEvent` instead. Make sure the side you construct the scheduler with matches the event you tick it from, otherwise coroutines dispatched onto the scheduler will resume on the wrong thread:

```kotlin
val scheduler = SimpleTickedScheduler.client()

GlobalEventHandler.Client.register<ClientTickEvent>(phase = PRE) {
    scheduler.tick()
}
```

Further having an instance of a `SimpleTickedScheduler` allows you to cancel the scheduled events. We can cancel all the events that are scheduled for a certain tick using the `cancel` method, or we can cancel all scheduled tasks with `cancelAll`.

## Custom Implementation

If you really want you can implement your own scheduler by implementing the `TickedScheduler` interface, overriding `target` if your scheduler is not ticked on the server. However, it's likely that for almost all cases you are better off using an instance of `SimpleTickedScheduler`.

## Temporary Schedulers

You can create a temporary ticked scheduler with a pre-determined lifetime by calling `GlobalTickedScheduler.Server.temporaryScheduler()`. This scheduler will be automatically ticked for you (and will have the same behaviour as the `GlobalTickedScheduler`), and will be deleted after the specified lifetime.

```kotlin
val minigame: Minigame = // ...
val duration = 10.Seconds
val scheduler = GlobalTickedScheduler.Server.temporaryScheduler(duration)
scheduler.asCoroutineScope().launch {
    minigame.visuals.countdown.transition(duration = duration, players = minigame.players::all)
    minigame.unpause()
}
```

This may be useful for creating schedulers with brief life-spans where registering your own event handler is too much effort.
