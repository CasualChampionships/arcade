# Minigames

Arcade provides an API for easily creating minigames. In this
section of the documentation, we will be looking at how you can create your
minigames and all the features that will be made available to you.

# Getting Started

Firstly we will take a look at the `Minigame` class. This is the heart
of all minigames that you create, it implements the bare-bones logic and
has common utilities that are used in minigames.

You can implement your own minigame by extending the `Minigame` class:

```kotlin
class MyMinigame(
    server: MinecraftServer
): Minigame<MyMinigame>(server) {
    override val id = TODO()

    override fun start() {
        TODO()
    }

    override fun getPhases(): Collection<MinigamePhase<MyMinigame>> {
        TODO()
    }

    override fun getLevels(): Collection<ServerLevel> {
        TODO()
    }
}
```

The first things you can implement in your minigame is it's `id`, and
which levels your minigame is going to include. Most likely, your minigame
will only include one level, however, it does support multiple levels if
you need multiple dimensions.

```kotlin
class MyMinigame(
    server: MinecraftServer
): Minigame<MyMinigame>(server) {
    override val id = ResourceLocation("modid", "my_minigame")
    
    // ...

    override fun getLevels(): Collection<ServerLevel> {
        return listOf(LevelUtils.overworld())
    }
}
```

One of the biggest responsibilities minigames hold is implementing the
logic for the game. This logic is mostly handled through the use of phases,
and that is what we are going to implement next.

Usually your minigame will have multiple phases, for example, for a UHC
minigame you might have a `Grace` phase where pvp is disabled, then an
`Active` phase, where pvp is enabled and a border starts shrinking, then
a `DeathMatch` phase where all players are teleported into an arena.

You implement your phases by implementing the `MinigamePhase` interface.
It is recommended that you use an `Enum`:

```kotlin
enum class MyMinigamePhases(
    override val id: String
): MinigamePhase<MyMinigame> {
    Grace("grace"),
    Active("active"),
    DeathMatch("death_match")
}
```

Now we have our phases we can finish implementing our minigame methods:

```kotlin
class MyMinigame(
    server: MinecraftServer
): Minigame<MyMinigame>(server) {
    // ...

    override fun start() {
        // Sets to the first phase of our minigame
        this.setPhase(MyMinigamePhases.Grace)
    }

    override fun getPhases(): Collection<MyMinigamePhases> {
        return MyMinigamePhases.values().toList()
    }

    // ...
}
```

That is it for setting up the bare-bones logic of the minigame.]

Here is the entire example:
```kotlin
enum class MyMinigamePhases(
    override val id: String
): MinigamePhase<MyMinigame> {
    Grace("grace"),
    Active("active"),
    DeathMatch("death_match")
}

class MyMinigame(
    server: MinecraftServer
): Minigame<MyMinigame>(server) {
    override val id = ResourceLocation("modid", "my_minigame")

    override fun start() {
        // Sets to the first phase of our minigame
        this.setPhase(MyMinigamePhases.Grace)
    }

    override fun getPhases(): Collection<MyMinigamePhases> {
        return MyMinigamePhases.values().toList()
    }

    override fun getLevels(): Collection<ServerLevel> {
        return listOf(LevelUtils.overworld())
    }
}
```

# Players

Now that we have our basic minigame implementation, let's look at how we
can handle players. The minigame will keep track of all players that are
playing the minigame automatically.

## Adding Players

To add a player to your minigame we can do the following
```kotlin
val minigame: Minigame = // ...
val player: ServerPlayer = // ...
    
val success: Boolean = minigame.addPlayer(player)
```

The method returns whether the player was rejected from joining; The player
will only be accepted to join the minigame if the minigame has been *initialized*
and the minigame is not yet tracking the player.

Furthermore, this method will broadcast an event which may cause the player
to be rejected, but we will go into more depth about this later.

If a player logged out (or the server was restarted), the player will
automatically rejoin the minigame.

## Removing Players

To remove a player from your minigame we can do the following
```kotlin
val minigame: Minigame = // ...
val player: ServerPlayer = // ...
    
minigame.removePlayer(player)
```

This simply tries to remove the player from the minigame.

## Accessing Players

You are able to check whether the minigame is currently tracking a given
player using the following method:
```kotlin
val minigame: Minigame = // ...
val player: ServerPlayer = // ...
    
val isPlaying: Boolean = minigame.hasPlayer(player)
```

The method returns whether the player is being tracked.

You can get all the online playing players using the following method:
```kotlin
val players: List<ServerPlayer> = minigame.getPlayers()
```

This method returns a list of all the playing players.

You can get all the offline (logged-out) players using the following method:
```kotlin
val offline: List<GameProfile> = minigame.getOfflinePlayerProfiles()
```

This method returns a list of `GameProfile`s of the offline players.

And finally you can get all the player profiles, whether online or not with
the following method:
```kotlin
val all: List<GameProfile> = minigame.getAllPlayerProfiles()
```

This method returns a list of `GameProfile`s of all the players.

# Implementing our Minigame

So now that we have our basic minigame setup, we can start adding the logic
that makes our minigame function. The main way that we'll be doing this is
by implementing phases, hooking into events, and scheduling tasks.

We will then take a look at making our minigame savable, allowing us to reload
our minigame, even if the server restarts mid-game.

We can then move on to adding commands to help configure and test your minigames,
as well as adding GUIs to create an immersive experience.

Finally, we will be looking at adding custom advancements, recipes, and stats
to our minigame to top it all off.

# Phases

The main way of defining the progression logic of your minigame is by implementing
phases, this way you can have different behaviours at each different phase in your
minigame. We already briefly had a look at phases, but they can be much more powerful
so lets take a deeper dive.

First of all lets have a look at what a `MinigamePhase` can override:
```kotlin
class MyMinigamePhase: MinigamePhase<MyMinigame> {
    override val id: String = // ...
    override val ordinal: Int = // ...
        
    override fun start(minigame: MyMinigame) {
        // ...
    }

    override fun initialize(minigame: MyMinigame) {
        // ...
    }

    override fun end(minigame: MyMinigame) {
        // ...
    }
}
```
So first of all, our phase must define its own id and ordinal which determines in which 
order the phases should be as well as defining a unique identifier. Usually you would define
your phases as a enum:
```kotlin
enum class MyMinigamePhase(override val id: String): MinigamePhase<MyMinigame> {
    MyPhase("my_phase")
}
```
Implementing your phase as an enum automatically takes care of the ordinal, and you only
need to define the unique id to each enumeration.

Having a look at the methods we can override, we have a `start`, `initialize`, and `end`
method. These are called when a phase is set, when a phase is initializing and when a
phase is ending respectively.

More precisely `start` is called whenever the minigame **changes** to the given phase,
`initialize` is called either after `start` is called **or** when the minigame reloads
from a save (when a server restarts), we will discuss this further in [Serialization](#Serialization).
`end` is called when a phase is ending, or in other words before the minigame has
started a new phase.

There is an important difference between `start` and `initialize` as `initialize` may
be called multiple times in the same phase. The purpose of `initialize` is to provide
a way to re-set anything in the given phase that couldn't be serialized. This means you
should avoid overriding it if you are not creating a `SavableMinigame`, and you should 
be weary of what you put inside your `initialize` method. For example, if we had a deathmatch
phase, and we wanted to teleport all players to an arena:
```kotlin
enum class MyMinigamePhases(
    override val id: String
): MinigamePhase<MyMinigame> {
    // ...
    DeathMatch("death_match") {
        override fun start(minigame: MyMinigame) {
            for (player in minigame.getPlayers()) {
                player.teleport(/* ... */)
            }
        }
        
        override fun initialize(minigame: MyMinigame) {
            for (player in minigame.getPlayers()) {
                player.teleport(/* ... */)
            }
        }
    }
}
```
In this example the implementation in `initialize` may lead to bugs as this method will
be called when the minigame reloads and therefore will teleport all players again which
is not what we wanted. Instead, we wanted to implement it in the `start` as it will just
teleport players when the phase starts.

Now with this information, you can define specific behaviours when different phases are
set and unset. However, this doesn't give us full control over our minigame as we are
likely to want to hook into events, and in the next section we will talk about how we
can do this, even for specific events.


# Events

Events are a key part of making your minigame work.

If you have not already, you should read the [Events](./events.md) 
documentation. This covers all the basics as well as what events are 
available to you.

However, minigames handle events slightly differently. Each minigame will
have its own `MinigameEventHandler` which is responsible for handling all
the events that happen during a minigame's lifetime.

The special thing about this event handler is that it will *filter* events
and will only invoke your registered listeners to those events that are
relevant to the minigame.

As we know, minigames keep track of players and which levels our minigame
is in. The `MinigameEventHandler` uses this information to filter out
`PlayerEvent`s and `LevelEvent`s that are only applicable to our minigame's
players and levels.

## Registering Listeners

There are two ways to register listeners to the `MinigameEventHandler`:

The first way is directly through the `MinigameEventHandler`:
```kotlin
val minigame: Minigame = // ...
minigame.events.register<ServerTickEvent> {
    // ...
}
```

Same as regular event handlers you can specify your own `EventListeners`
as well as provide your own priority to define what order you want your
listeners to be called in, read more about this in the [Events](./events.md) 
documentation.

Minigames gives you more control over your listeners, however, as previously mentioned, 
we already know that it will filter for only events that are relevant to the minigame:
```kotlin
minigame.events.register<PlayerTickEvent> {
    // ...
}
```
This listener will only be invoked for all players that are playing in the current minigame.

However, we can have even more control over when our listeners are invoked, specifically
during which phases. We can for example declare an event listener that will only be invoked
in specified phases:
```kotlin
val minigame: MyMinigame = // ...
val grace = MyMinigamePhase.Grace
val active = MyMinigamePhase.Active
minigame.events.registerInPhases<ServerTickEvent>(grace, active) {
    // ...
}
```
In this case, our listener will only be invoked in the given minigame phases.

If you want it to be between a larger selection of phases, we can do that too, and instead
only invoke our listener if the minigame is between two phases (inclusive):
```kotlin
val minigame: MyMinigame = // ...
val grace = MyMinigamePhase.Grace
val deathMatch = MyMinigamePhase.DeathMatch
minigame.events.registerBetweenPhases<ServerTickEvent>(grace, deathMatch) {
    // ...
}
```

Something to note about these last two is that they are permanent for the lifetime of
the minigame. However, if we want to register a phased specific event that only
lasts until the end of the current phase, we can do the following:
```kotlin
minigame.events.registerPhased<ServerTickEvent> {
    // ...
}
```
This listener will be deleted after the minigames phase changes.

These ways are nice for defining a couple listeners, and it also allows you
to define listeners outside your actual minigame class. However, it can get
messy when you need to define tens of listeners to define your minigame
behaviour. So let's have a look at the alternative way of declaring a listener:

```kotlin
class MyMinigame(
    server: MinecraftServer
): Minigame<MyMinigame>(server) {
    // ...
    
    @MinigameEvent
    private fun onServerTick(event: ServerTickEvent) {
        // ...
    }

    @MinigameEvent
    private fun onMinigameAddPlayer(event: MinigameAddPlayerEvent) {
        // ...
    }
}
```

We can instead use the `@MinigameEvent` annotation which allows us to declare
a method with the parameter defining which event that it will be listening to;
there are no restrictions to what you name your method. By default this will be
permanent for the lifetime of the minigame.

If you are using IntelliJ you can add `net.casual.arcade.minigame.annotation.MinigameEvent`
in `Settings > Editor > Inspections > Java > Declaration redudancy > Unused declaration > Entry points > Annotations...`
as an entry point and IntelliJ will stop giving you warnings that it is unused.

Similarly to the control we have with the `register` methods we can do the same with
the annotation:
```kotlin
@MinigameEvent(
    priority = 2_000,
    phases = ["grace", "death_match"]
)
private fun onMinigameAddPlayer(event: MinigameAddPlayerEvent) {
    // ...
}
```
We can specify the priority of our event, and this works the same as before. We can also specify
all the phases that we want this listened to be called during. This uses the `id`s of the
minigame phases (as we cannot pass enums into annotations), so you may also want to create
constant variables with the ids to make this easier.

And further, we can define bounds so that our listener is only called when our minigame
is between the given phases (inclusive):
```kotlin
@MinigameEvent(
    priority = 2_000,
    start = "grace",
    end = "death_match"
)
private fun onMinigameAddPlayer(event: MinigameAddPlayerEvent) {
    // ...
}
```

# Scheduling

Scheduling is key to implementing a minigame, there are lots of things that you will 
want to do in the future, and the scheduling API allows you to do this. If you have not
already taken a look at the [Scheduling](./scheduling.md) Documentation, this part of
the documentation will look specifically at scheduling with minigames.

Let's first take a look at the `MinigameScheduler` which can be accessed through the
`scheduler` field on a `Minigame` instance. This class, like a regular `TickedScheduler`
allows you to schedule events in the future, however adds more functionality to give
you control over whether tasks are scheduled and serialized.

There are 3 main additional methods that this implementation provides: `schedulePhased`,
`schedulePhasedCancellable`, and `schedulePhasedInLoop`.

The `schedulePhased` method adds a task which will be scheduled for future execution,
much like `schedule`, however the task will only execute if the minigame is still in
the *same* phase as it was initially scheduled in. For example:
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
If we call `foobar` and wait 1 tick nothing will happen, this is because we scheduled a
task in the `Grace` phase, scheduled the task (to only run in the `Grace` phase), and then
changed the phase to `Active` clearing any tasks that were going to be run.

This behaviour is the same for `schedulePhasedInLoop`, and it works how you expect.

Now let's have a look at `schedulePhasedCancellable`, this further gives us control of our
task as it lets you run a task if the original task you scheduled was cancelled (by a phase change).
Calling this method with a task will return a `CancellableTask`, which essentially just
wraps our original task.

We can do some things to control what happens with our `CancellableTask`:
```kotlin
val cancellable = this.scheduler.schedulePhasedCancellable(3.Ticks) {
    println("This is a cancellable task!")
}

// This method will tell the method to simply run the task
// it was originally scheduled to run if it is cancelled.
cancellable.runOnCancel()

// This adds a task to run if the task is cancelled.
cancellable.cancelled {
    println("This was cancelled")
}

// This cancels the task, and it will no longer be run after the 
// scheduled time, it also invokes all the `cancelled` tasks.
cancellable.cancel()
```
If we ran this then it would output `"This is a cancellable task!"` and then `"This was cancelled"`,
and if we were to wait three ticks, nothing further would happen.

Cancellable tasks are useful as they will automatically be cancelled when the phase of the
minigame changes. If a task that we needed to run during the phase in the future didn't
have the time to run, we can still run it.

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
            minigame.scheduler.schedulePhasedCancellable(10.Minutes, task).runOnCancel()
        }
    }
}
```
In the above example we schedule the `BossbarTask` and tell it to `runOnCancel()`, so in 
the case that 10 minutes have not passed, but we change phases then the bossbar will still
be removed.

# Serialization

# Commands

# GUIs

# Advancements

# Recipes

# Stats