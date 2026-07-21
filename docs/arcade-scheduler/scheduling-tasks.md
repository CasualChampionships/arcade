# Scheduling Tasks

We can schedule tasks using the `GlobalTickedScheduler`. To specify when we want the task to be executed, we use a `MinecraftTimeDuration`:

```kotlin
GlobalTickedScheduler.schedule(MinecraftTimeUnit.Ticks.duration(20)) {
    println("Hello 20 ticks in the future!")
}
```

We also have a shorthand for creating `MinecraftTimeDuration`s:
```kotlin
GlobalTickedScheduler.schedule(20.Ticks) {
    println("Hello 20 ticks in the future!")
}
```

> [!NOTE]
> All tasks scheduled with the `GlobalTickedScheduler` will run at the end of the specified tick.

If you simply just want to run something at the end of the current tick then we can call the `later` method, this is the same as scheduling a task for zero ticks in the future:
```kotlin
GlobalTickedScheduler.later {
    println("Hello later in the same tick!")
}
```

## Scheduling Looping Tasks

We can schedule looping tasks using the `scheduleInLoop` method. This allows us to specify an initial delay, then an interval between task executions then the total duration it should loop for.

```kotlin
var i = 0
GlobalTickedScheduler.scheduleInLoop(3.Ticks, 5.Ticks, 25.Ticks) {
    println("${3 + (5 * i++)} ticks have past")
}
```

## Co-routines

We can convert a scheduler into a coroutine dispatcher using the extension function
`MinecraftTaskScheduler.asCoroutineDispatcher()`:
```kotlin
val server: MinecraftServer = // ...
val dispatcher = GlobalTickedScheduler.get().asCoroutineDispatcher()
server.launch {
    withContext(dispatcher) {
        // Do something suspending
    }
}
```
The coroutine will resume running on the scheduler, this allows us to cancel
the coroutine by cancelling the scheduled task. This behavior is useful
in `MinigameTaskScheduler` in the minigames module, see the 
[Minigame Scheduling Section](../arcade-minigames/scheduling.md)
which will go into further depth.

We can also utilize the `delay` function declared in the utilities module to
delay execution by a number of *ticks*. This delay function will only work
on scheduler dispatchers or the server dispatcher.
```kotlin
suspend fun example() {
    delay(35.Ticks) // Delays exactly 35 game ticks
}
```
