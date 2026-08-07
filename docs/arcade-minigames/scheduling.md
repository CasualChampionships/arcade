# Scheduling

Scheduling is key to implementing a minigame, there are lots of things that you
will want to do in the future, and the scheduling API allows you to do this. If
you have not already taken a look at the [Scheduling](../arcade-scheduler/getting-started.md) 
Documentation, this part of the documentation will look specifically at 
scheduling with minigames.

Let's first take a look at the `MinigameTickedScheduler` which can be accessed 
through the `scheduler` field on a `Minigame` instance. This class, like a 
regular `SimpleTickedScheduler` allows you to schedule events in the future, however 
adds more functionality to give you control over whether tasks are scheduled 
and serialized.

The main additional method that this implementation provides is `schedulePhased`.

The `schedulePhased` method adds a task which will be scheduled for future 
execution, much like `schedule`, however the task will only execute if the 
minigame is still in the *same* phase as it was initially scheduled in. For 
example:
```kotlin
class ExampleMinigame(
    server: MinecraftServer,
    uuid: UUID
): Minigame(server, uuid) {
    // ...
    
    fun foobar() {
        this.setPhase(ExamplePhases.Grace)
        this.scheduler.schedulePhased(1.Ticks) {
            println("Hello from the past")
        }
        this.setPhase(ExamplePhases.Active)
    }
}
```
If we call `foobar` and wait one tick nothing will happen, this is because we 
scheduled a task in the `Grace` phase, scheduled the task (to only run in the 
`Grace` phase), and then changed the phase to `Active` clearing any tasks that 
were going to be run.

## Cancelling Tasks

Every `schedule` method returns a `ScheduledTask` handle, which you can use to
stop the task before it runs:
```kotlin
val task = this.scheduler.schedulePhased(3.Ticks) {
    println("This is a phased task!")
}

// This cancels the task, and it will no longer be run
// after the scheduled time.
task.cancel()

// Whether the task has already run, or has been cancelled.
println(task.isFinished)
```

There are many custom implementations of tasks; however, we will discuss them 
later in the documentation when their purpose becomes clear.

## Coroutines

Minigame schedulers have coroutine support, and we can easily launch
coroutines with the utility extension functions:
```kotlin
val minigame: Minigame = // ...
    
minigame.launch {  }
minigame.async {  }
minigame.launchPhased { }
minigame.asyncPhased {  }
```
Coroutines launched from `launch` and `async` run on the minigame's default
scheduler, and will be cancelled when the minigame is closed. Coroutines 
launched from the phased variations will run on the minigame's phased
scheduler, and will additionally be cancelled when the minigame changes phase.

## Cleaning Up When A Phase Ends

A common thing to want is work which must happen *either* when the time is up 
*or* when the phase changes early, whichever comes first. Removing a UI element 
is a good example.

A task can't express this, since it only runs once and only at its scheduled 
time. A phased coroutine can, because cancelling it unwinds it; put the cleanup 
in a `finally` block and it runs either way:
```kotlin
enum class ExamplePhases(
    override val id: String
): Phase<ExampleMinigame> {
    // ...
    Active("active") {
        override fun start(minigame: ExampleMinigame) {
            val bossbar: VirtualBossbar = // ...
            minigame.visuals.addBossbar(bossbar)

            minigame.launchPhased {
                try {
                    delay(10.Minutes)
                } finally {
                    minigame.visuals.removeBossbar(bossbar)
                }
            }
        }
    }
}
```
The bossbar is removed after 10 minutes, and it is *also* removed if we change 
phase before those 10 minutes are up.

Note that coroutines are transient; nothing you launch this way survives a 
restart. If you need that, use a `Routine`, which supports the same `try`/
`finally` pattern and is serialized with the minigame.
