# Scheduling Tasks

We can schedule tasks using the `GlobalTickedScheduler`. This is sided, in the same way as the `GlobalEventHandler`: use `GlobalTickedScheduler.Server` for tasks that should run on the server thread, and `GlobalTickedScheduler.Client` for tasks that should run on the client thread. Tasks themselves are not sided; only the scheduler that runs them is.

To specify when we want the task to be executed, we use a `MinecraftTimeDuration`:

```kotlin
GlobalTickedScheduler.Server.schedule(MinecraftTimeUnit.Ticks.duration(20)) {
    println("Hello 20 ticks in the future!")
}
```

We also have a shorthand for creating `MinecraftTimeDuration`s:
```kotlin
GlobalTickedScheduler.Server.schedule(20.Ticks) {
    println("Hello 20 ticks in the future!")
}
```

> [!NOTE]
> All tasks scheduled with the `GlobalTickedScheduler` will run at the end of the specified tick.

If you simply just want to run something at the end of the current tick then we can call the `later` method, this is the same as scheduling a task for zero ticks in the future:
```kotlin
GlobalTickedScheduler.Server.later {
    println("Hello later in the same tick!")
}
```

## Cancelling Tasks

Every `schedule` method returns a `Scheduled` handle, which you can use to stop the task before it runs:
```kotlin
val handle = GlobalTickedScheduler.Server.schedule(20.Ticks) {
    println("This never gets printed")
}
handle.cancel()
```
Scheduling the same task twice gives you two independent handles, so cancelling one does not affect the other.

Handles are deliberately minimal; they only let you cancel something and ask whether it has finished. If you want to
await something, or run a loop, use a coroutine instead.

## Scheduling Looping Tasks

A `Task` is one-shot, so a loop is written as a coroutine on the scheduler's scope:
```kotlin
var i = 0
GlobalTickedScheduler.Server.asCoroutineScope().launch {
    delay(3.Ticks)
    while (isActive) {
        println("${3 + (5 * i++)} ticks have past")
        delay(5.Ticks)
    }
}
```

## Coroutines

Every scheduler has a `CoroutineScope` which dispatches onto it, so launching a coroutine which runs
on a scheduler is just:
```kotlin
GlobalTickedScheduler.Server.asCoroutineScope().launch {
    // Do something suspending
}
```
The coroutine resumes on the scheduler, which means cancelling the scheduler cancels the coroutine,
unwinding it through any `finally` blocks it has. This behavior is useful in `MinigameTickedScheduler`
in the minigames module, see the [Minigame Scheduling Section](../arcade-minigames/scheduling.md)
which will go into further depth.

We can also utilize the `delay` function declared in the utilities module to
delay execution by a number of *ticks*. This delay function will only work
on scheduler dispatchers or the server dispatcher.
```kotlin
suspend fun example() {
    delay(35.Ticks) // Delays exactly 35 game ticks
}
```
