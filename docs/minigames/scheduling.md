# Scheduling

> Return to [table of contents](../minigames.md)

Scheduling is key to implementing a minigame, there are lots of things that you will want to do in the future, and the scheduling API allows you to do this. If you have not already taken a look at the [Scheduling](../scheduling.md) Documentation, this part of the documentation will look specifically at scheduling with minigames.

Let's first take a look at the `MinigameScheduler` which can be accessed through the `scheduler` field on a `Minigame` instance. This class, like a regular `TickedScheduler` allows you to schedule events in the future, however adds more functionality to give you control over whether tasks are scheduled and serialized.

There are three main additional methods that this implementation provides: `schedulePhased`,`schedulePhasedCancellable`, and `schedulePhasedInLoop`.

The `schedulePhased` method adds a task which will be scheduled for future execution, much like `schedule`, however the task will only execute if the minigame is still in the *same* phase as it was initially scheduled in. For example:
```kotlin
class MyMinigame(server: MinecraftServer): Minigame<MyMinigame>(server) {
    // ...
    
    fun foobar() {
        this.setPhase(MyMinigamePhase.Grace)
        this.scheduler.schedulePhased(1.Ticks) {
            println("Hello from the past")
        }
        this.setPhase(MyMinigamePhase.Active)
    }
}
```
If we call `foobar` and wait one tick nothing will happen, this is because we scheduled a task in the `Grace` phase, scheduled the task (to only run in the `Grace` phase), and then changed the phase to `Active` clearing any tasks that were going to be run.

This behaviour is the same for `schedulePhasedInLoop`, and it works how you expect.

## Cancellable Tasks

Now let's have a look at `schedulePhasedCancellable`, this further gives us control of our task as it lets you run a task if the original task you scheduled was cancelled (by a phase change). Calling this method with a task will return a `CancellableTask`, which essentially just
wraps our original task.

We can do some things to control what happens with our `CancellableTask`:
```kotlin
val cancellable = this.scheduler.schedulePhasedCancellable(3.Ticks) {
    println("This is a cancellable task!")
}

// This method will tell the method to simply run the task
// it was originally scheduled to run if it is cancelled.
cancellable.runIfCancelled()

// This adds a task to run if the task is cancelled.
cancellable.ifCancelled {
    println("This was cancelled")
}

// This cancels the task, and it will no longer be run after the 
// scheduled time, it also invokes all the `cancelled` tasks.
cancellable.cancel()
```
If we ran this then it would output `"This is a cancellable task!"` and then `"This was cancelled"`, and if we were to wait three ticks, nothing further would happen.

Cancellable tasks are useful as they will automatically be cancelled when the phase of the minigame changes. If a task that we needed to run during the phase in the future didn't have the time to run, we can still run it.

This is, for example, useful for UI elements that only appear in specific phases:
```kotlin
enum class MyMinigamePhases(
    override val id: String
): MinigamePhase<MyMinigame> {
    // ...
    Active("active") {
        override fun start(minigame: MyMinigame) {
            val bossbar: CustomBossBar = // ...
            // Bossbar task removes the bossbar when it is executed
            val task = BossbarTask(minigame, bossbar)
            minigame.scheduler.schedulePhasedCancellable(10.Minutes, task).runIfCancelled()
        }
    }
}
```
In the above example we schedule the `BossbarTask` and tell it to `runIfCancelled()`, so in the case that 10 minutes have not passed, but we change phases then the bossbar will still be removed.

There are many custom implementations of tasks; however, we will discuss them later in the documentation when their purpose becomes clear.

> See the next section on [Commands](commands.md)