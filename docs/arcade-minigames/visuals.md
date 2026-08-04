# Visuals

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
`Countdown` and the ready broadcasters, these are used in minigames by default when 
unpausing to count down and to check that all players are ready. You
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
```

Readiness is handled by the `playerReadyBroadcaster` and `teamReadyBroadcaster`,
which determine how players are prompted to ready up (by default they use chat).
You can overwrite these, and then check readiness using the suspending
`checkReadyPlayers` and `checkReadyTeams` extensions, or track it yourself with
`trackReadyPlayers` and `trackReadyTeams`:
```kotlin
val minigame: Minigame = // ...

// Optionally customize how players are asked to ready up
minigame.visuals.playerReadyBroadcaster = // ...

minigame.launch {
    // Suspends until all playing players are ready
    minigame.checkReadyPlayers()
    println("Playing players are ready!")
}

// Or track readiness manually
val tracker = minigame.trackReadyPlayers()
if (tracker.isReady()) {
    println("Everyone is ready!")
}
```

## Phased Visual Components

Often we want a gui component to be shown only for a certain phase of our 
minigame, and removed appropriately even if we backtrack to a previous phase.

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

Instead, what we can do is launch a coroutine on the phased scheduler's scope, 
and remove the bossbar from a `finally` block:
```kotlin
enum class ExamplePhases(
    override val id: String
): Phase<ExampleMinigame> {
    Grace("grace") {
        override fun initialize(minigame: ExampleMinigame) {
            val duration = 10.Minutes
            val bossbar = MyCustomTimerBossBar()
            bossbar.setDuration(duration)

            minigame.launchPhased {
                minigame.visuals.addBossbar(bossbar)
                try {
                    delay(duration + 1.Ticks)
                } finally {
                    minigame.visuals.removeBossbar(bossbar)
                }
            }
        }
    }
    // ...
}
```

The bossbar is added when the coroutine starts, and removed when it finishes,
because the cleanup is in a `finally`. If the 
full duration elapses, `delay` returns and the bossbar is removed. If the phase 
changes first, the minigame cancels everything on the phased scheduler, which 
unwinds the coroutine through the same `finally`, and the bossbar is removed 
then instead.
