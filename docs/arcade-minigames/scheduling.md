# Scheduling

Scheduling is key to implementing a minigame, there are lots of things that you
will want to do in the future, and the scheduling API allows you to do this. If
you have not already taken a look at the [Scheduling](../arcade-scheduler/getting-started.md) 
Documentation, this part of the documentation will look specifically at 
scheduling with minigames.

Minigames schedule everything through *scopes*. A `MinigameScope` owns the
tasks, coroutines, and event listeners that are scheduled or registered through
it. Whenever the scope is closed then everything it owns is cancelled or
unregistered. This means that you rarely have to clean anything up yourself, you
just pick the scope with the right lifetime and let it do that for you.

Every minigame has two scopes which always exist. The root scope lives for the
minigame's entire lifetime, and this is what the `scheduler` field on a
`Minigame` refers to:

```kotlin
val minigame: Minigame = // ...

minigame.scheduler.schedule(10.Seconds) {
    println("Hello from 10 seconds in the future!")
}
```

The current scope always belongs to the phase the minigame is in; everything it
owns is cancelled or unregistered whenever the phase changes, and the scope is
then ready to be used again by the next phase:

```kotlin
val minigame: Minigame = // ...

// This is cancelled if the phase changes in the next 30 seconds
minigame.scopes.current.schedule(30.Seconds) {
    minigame.chat.broadcast(Component.literal("30 seconds have passed!"))
}
```

Nothing scheduled on a minigame runs while the minigame is paused, and the
minigame only starts ticking its scopes once it has started.

## Scope Lifetimes

We create scopes through the minigame's `scopes` manager, providing the
`MinigamePhaseLifetime` which determines when the scope closes:

```kotlin
val minigame: Minigame = // ...
val scope = minigame.scopes.create(MinigamePhaseLifetime.Forward)

// This will be cancelled if the minigame goes back to an earlier phase
scope.schedule(30.Seconds) {
    minigame.chat.broadcast(Component.literal("30 seconds have passed!"))
}
```

The available lifetimes are:
- `Forever` - Survives every transition, only the minigame closing ends it.
- `Current` - Doesn't survive any phase transition, essentially closes after any
  phase change.
- `Forward` - Survives only if the next phase comes *strictly after* the current
  one, so it ends if you backtrack to an earlier phase.
- `Until(bound)` - Survives if the next phase is *strictly before* `bound`.
- `During(phases)` - Survives if the next phase is in the given set of phases.
- `Between(lower, upper)` - Survives if the next phase is *strictly between*
  `lower` and `upper`.

You can also close a scope yourself at any point, which is useful for behaviour
that isn't tied to a phase at all:
```kotlin
val scope: MinigameScope = // ...

scope.close()
```
Closing a scope is idempotent, and once closed it will reject anything else you
try to schedule or register on it.

> [!NOTE]
> The root and current scopes are managed by the minigame, so they cannot be
> closed this way; they only close when the minigame does.

## Cancelling Tasks

Every `schedule` method returns a `ScheduledTask` handle, which you can use to
stop the task before it runs:
```kotlin
val scope: MinigameScope = // ...
val task = scope.schedule(3.Ticks) {
    println("This is a scoped task!")
}

// This cancels the task, and it will no longer be run
// after the scheduled time.
task.cancel()

// Whether the task has already run, or has been cancelled.
println(task.isFinished)
```

## Coroutines

Minigame scopes have coroutine support, and we can easily launch coroutines with
the utility extension functions:
```kotlin
val minigame: Minigame = // ...
    
minigame.launch {  }
minigame.async {  }
```
Coroutines launched from `launch` and `async` run on the minigame's root scope,
and will be cancelled when the minigame is closed. To tie a coroutine to a
shorter lifetime, launch it on the scope you want instead:
```kotlin
val minigame: Minigame = // ...

minigame.scopes.current.launch {
    // Cancelled when the phase changes
}
```

## Cleaning Up When A Phase Ends

A common thing to want is work which must happen *either* when the time is up 
*or* when the phase changes early, whichever comes first. Removing a UI element 
is a good example.

A task can't express this, since it only runs once and only at its scheduled 
time. A coroutine in a scope can, because cancelling it unwinds it; put the
cleanup in a `finally` block, and it runs either way:
```kotlin
val minigame: Minigame = // ...
val bossbar: VirtualBossbar = // ...

minigame.scopes.current.launch {
    minigame.visuals.addBossbar(bossbar)
    try {
        delay(10.Minutes)
    } finally {
        minigame.visuals.removeBossbar(bossbar)
    }
}
```
The bossbar is removed after 10 minutes, and it is *also* removed if we change 
phase before those 10 minutes are up.

The coroutine which runs a phase is itself launched in the current scope, so the
same pattern works directly inside your phase logic:
```kotlin
private suspend fun runGraceLogic() {
    try {
        this.settings.canPvp.set(false)
        delay(10.Minutes)
    } finally {
        this.settings.canPvp.set(true)
    }
}
```

Note that coroutines are transient; nothing you launch this way survives a 
restart. If you need that, use a `Routine`, which supports the same `try`/
`finally` pattern and is serialized with the minigame, see the
[Serialization Section](./serialization.md).
