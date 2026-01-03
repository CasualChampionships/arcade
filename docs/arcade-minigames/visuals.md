# Visuals

> Return to [table of contents](getting-started.md)

Arcade provides a wide array of visual components, read the [Visuals Section](../arcade-visuals/getting-started.md) 
to see more information about those. 
This section is dedicated to how those gui components can be integrated within 
minigames.

All the gui elements are handled by a `MinigameVisualsManager`, you can add all of 
your visual components to this manager, and it will ensure that players will be 
displayed the components, and they will be updated for those players. If a 
player joins the minigame, it will correctly update all the player's visuals, and if
a player leaves, all the visuals will be removed.

## Adding & Removing Visual Components

We simply construct our visual component then add it to the manager using one of the respective methods, once registered, the minigame will handle any ticking or updating that is needed for that component:
```kotlin
val minigame: Minigame = // ...

val bossbar: CustomBossbar = // ...
minigame.visuals.addBossbar(bossbar)

val nametag: PlayerNametag = // ...
minigame.visuals.addNametag(nametag)

val sidebar: Sidebar = // ...
minigame.visuals.setSidebar(sidebar)

val display: PlayerListDisplay = // ...
minigame.visuals.setPlayerListDisplay(display)
```

> [!NOTE]
> You can add as many bossbars and nametags as you wish, however, there can 
> only every be one sidebar and one player list display.

We can also remove any elements with their respective methods:
```kotlin
val minigame: Minigame = // ...

val bossbar: CustomBossbar = // ...
minigame.visuals.removeBossbar(bossbar)
minigame.visuals.removeAllBossbars()

val nametag: PlayerNametag = // ...
minigame.visuals.removeNametag(nametag)
minigame.visuals.removeAllNametags()

minigame.visuals.removeSidebar()

minigame.visuals.removePlayerListDisplay()
```

There are also two additional things that the visual manager controls, that is the 
`Countdown` and `ReadyChecker`, these are used in minigames by default when 
unpausing to check that all players are ready, and then a countdown begins. You
may also want to use these for other applications. These will have default 
implementations, but you can overwrite them.

```kotlin
val minigame: Minigame = // ...
    
minigame.visuals.countdown = TitledCountdown.titled(Component.literal("My Titled Countdown!"))

// Launch a co-routine on the minigame scheduler
minigame.launch {
    // This transition is suspending
    minigame.visuals.countdown.transition(10.Seconds, 1.Seconds, minigame.players::all)
    // Executes *after* our transition is complete!
    println("Minigame Countdown Finished!")
}

minigame.visuals.readier = // ...
    
// We can then use this to check if players are ready
minigame.visuals.readier.arePlayersReady(minigame.players.playing).then {
    println("Playing players are ready!")
}
```

## Phased Visual Components

Previously in the [Scheduling Section](scheduling.md) we briefly talked about 
the `BossbarTask`, the purpose of this task is to be able to display a bossbar 
for a certain phase of our minigame and even if we backtrack to a previous 
phase the bossbar will be removed appropriately.

Each gui component has a respective task that does this:
- `BossbarTask`
- `NametagTask`
- `PlayerListTask`
- `SidebarTask`

Let's take a look at a more concrete example. Let's say we've got a timer 
bossbar that lasts 10 minutes denoting a minigame phase change. There are a 
couple issues with this; what if we want to step into the next phase before the
10 minutes is up, the bossbar would not automatically disappear. Okay well we 
could solve this by simply storing a bossbar as a field as always removing it 
in the `end` method of our phase:

```kotlin
class ExampleMinigame(
    server: MinecraftServer,
    uuid: UUID
): Minigame(server, uuid) {
    // ...

    var bossbar: MyCustomTimerBossBar = MyCustomTimerBossBar()

    // ...
}

enum class ExamplePhases(
    override val id: String
): Phase<ExampleMinigame> {
    Grace("grace") {
        override fun initialize(minigame: ExampleMinigame) {
            minigame.visuals.addBossbar(minigame.bossbar)
        }
        
        override fun end(minigame: ExampleMinigame, next: Phase<ExampleMinigame>) {
            minigame.visuals.removeBossbar(minigame.bossbar)
        }
    }
    // ...
}
```

But this feels very clunky, especially if you have multiple different gui 
components that you want to manage. Not to mention, this will also make 
serialization more difficult if that's something you're also aiming for.

Instead, what we can do is create an instance of `BossbarTask` and schedule it 
as a cancellable task which will run if cancelled:
```kotlin
enum class ExamplePhases(
    override val id: String
): Phase<ExampleMinigame> {
    Grace("grace") {
        override fun initialize(minigame: ExampleMinigame) {
            val duration = 10.Minutes
            val bossbar = MyCustomTimerBossBar()
            bossbar.setDuration(duration)

            val task = BossbarTask(minigame, bossbar)
            minigame.scheduler.schedulePhasedCancellable(duration + 1.Ticks, task).runIfCancelled()
        }
    }
    // ...
}
```

If we take a peek at how the `BossbarTask` is implemented we will see why this works:
```kotlin
open class BossbarTask<T: CustomBossbar>(
    private val minigame: Minigame,
    val bar: T
): Task {
    init {
        this.minigame.visuals.addBossbar(this.bar)
    }

    final override fun run() {
        this.minigame.visuals.removeBossbar(this.bar)
    }
}
```
It simply adds the bossbar when the task is created, then when the task is run 
it will remove the bossbar. So when scheduling this as a task, we will 
guarantee that it will be removed after the specified time. And because we 
scheduled it as phased cancellable that means we can wrap this task in a 
cancellable task which in the case the phase abruptly changes will notify the 
task, calling `runIfCancelled` tells the task to run if it's been notified of 
the cancellation and so the bossbar will be removed.

> See the next section on [Resource Packs](resource_packs.md)