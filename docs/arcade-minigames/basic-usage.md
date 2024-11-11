# Basic Usage

> Return to [table of contents](getting-started.md)

## Creating a Minigame

Firstly we will take a look at the `Minigame` class. This is the heart
of all minigames that you create, it implements the bare-bones logic and
has common utilities that are used in minigames.

You can implement your own minigame by extending the `Minigame` class:

```kotlin
class ExampleMinigame(
    server: MinecraftServer,
    uuid: UUID
): Minigame(server, uuid) {
    override val id: ResourceLocation = TODO("Not yet implemented")

    override fun phases(): Collection<Phase<ExampleMinigame>> {
        TODO("Not yet implemented")
    }
}
```

Let's break down what's going on here. Each minigame has access to the `MinecraftServer`,
and each minigame has a `UUID` which are both passed in the constructor.
Each minigame also needs to define an `id` which identifies what *type* of minigame
it is. It also defines a method called `phases`, which we will have a look at in a moment.

The first thing you should specify is the id of your minigame:

```kotlin
class ExampleMinigame(
    server: MinecraftServer,
    uuid: UUID
): Minigame(server, uuid) {
    override val id: ResourceLocation = ResourceLocation.fromNamespaceAndPath("modid", "example")

    override fun phases(): Collection<Phase<ExampleMinigame>> {
        TODO("Not yet implemented")
    }
}
```

## Implementing Logic

Now, let's have a look at Phases. Phases determine the logic for your minigame.
Typically, your minigame will have multiple phases which will be cycled through
as time passes in your minigame. For example, for a UHC minigame you might have a
`Grace` phase where pvp is disabled, then an `Active` phase where pvp will be
enabled, and where the border starts shrinking, and finally a `DeathMatch` phase
where all players are teleported into an area to fight to the death.

To implement your phases, you need to implement the `Phase` interface.
This can be done in a multitude of ways, but the recommended way is to use an
enum class, for example:

```kotlin
enum class ExamplePhases(
    override val id: String
): Phase<ExampleMinigame> {
    Grace("grace"),
    Active("active"),
    DeathMatch("death_match")
}
```

Using an enum class automatically determines the order in which the phases
should be progressed in.

Alternatively, you could implement your phases like so:

```kotlin
object ExamplePhases {
    object Grace: Phase<ExampleMinigame> {
        override val id = "grace"
        override val ordinal = 0
    }

    object Active: Phase<ExampleMinigame> {
        override val id = "active"
        override val ordinal = 1
    }
    
    object DeathMatch: Phase<ExampleMinigame> {
        override val id = "death_match"
        override val ordinal = 2
    }
}
```

Now we have our phases we can return them in our `phases` method in our minigame class.

> [!NOTE]
> The order in which you return these doesn't matter, they will be sorted by their ordinal position later.

```kotlin
class ExampleMinigame(
    server: MinecraftServer,
    uuid: UUID
): Minigame(server, uuid) {
    override val id: ResourceLocation = ResourceLocation.fromNamespaceAndPath("modid", "example")

    override fun phases(): Collection<Phase<ExampleMinigame>> {
        return listOf(ExamplePhases.Grace, ExamplePhases.Active, ExamplePhases.DeathMatch)
    }
}
```

That's our bare-bones minigame implementation done for now.

We can now return to our phases, as we want them to change the state of the minigame.
We can do this by overriding the methods in the `Phase` interface.

There are three methods we can override; `start`, `initialize`, and `end`:
```kotlin
enum class ExamplePhases(
    override val id: String
): Phase<ExampleMinigame> {
    Grace("grace") {
        override fun start(minigame: ExampleMinigame, previous: Phase<ExampleMinigame>) {
            
        }

        override fun initialize(minigame: ExampleMinigame) {
            
        }
        
        override fun end(minigame: ExampleMinigame, next: Phase<ExampleMinigame>) {
            
        }
    },
    Active("active"),
    DeathMatch("death_match")
}
```

- The `start` method will be called when the minigame is changing to `this` phase.
- The `initialize` method will always be called *after* the `start` method, and
  it will also be called when a minigame is deserialized, this will be discussed
  in further detail later in the [Serialization](./serialization.md) section.
  For now, we will stick to using the `start` method.
- The `end` method will be called when the minigame is changing from `this` phase
  into another phase.

For most of your logic you will mostly be using the `start` method, there are some
cases where you need the other methods.

Here's the example from above implementing some basic logic:

```kotlin
enum class ExamplePhases(
    override val id: String
): Phase<ExampleMinigame> {
    Grace("grace") {
        override fun start(minigame: ExampleMinigame, previous: Phase<ExampleMinigame>) {
            minigame.settings.canPvp.set(false)

            // In 10 minutes we will move to the next phase
            minigame.scheduler.schedulePhased(10.Minutes, PhaseChangeTask(minigame, Active))
        }
    },
    Active("active") {
        override fun start(minigame: ExampleMinigame, previous: Phase<ExampleMinigame>) {
            minigame.settings.canPvp.set(true)

            // In 30 minutes we will move to the next phase
            minigame.scheduler.schedulePhased(30.Minutes, PhaseChangeTask(minigame, DeathMatch))
        }
    },
    DeathMatch("death_match") {
        override fun start(minigame: ExampleMinigame, previous: Phase<ExampleMinigame>) {
            // Change to location of the arena
            val location = Location.of()
            for (player in minigame.players.playing) {
                player.teleportTo(location)
            }
        }
    }
}
```

One last thing that we might want to do is have some logic for when the
minigame initializes. Some logic we may not want to do in the constructor
of our minigame, but instead later just before we start using our minigame.
We'll have a deeper look into events later, but for know we can just use this
example:

```kotlin
class ExampleMinigame(
    server: MinecraftServer,
    uuid: UUID
): Minigame(server, uuid) {
    override val id: ResourceLocation = ResourceLocation.fromNamespaceAndPath("modid", "example")

    override fun phases(): Collection<Phase<ExampleMinigame>> {
        return listOf(ExamplePhases.Grace, ExamplePhases.Active, ExamplePhases.DeathMatch)
    }
    
    @Listener
    private fun onInitialize(event: MinigameInitializeEvent) {
        // Initializing code here!
        println("Example minigame initialized")
    }
}
```

The rest of this documentation will cover the capabilities that minigames provide
to you which you can use in these phases to program your minigame's logic.

Here is the entire example so far:
```kotlin
enum class ExamplePhases(
    override val id: String
): Phase<ExampleMinigame> {
    Grace("grace") {
        override fun start(minigame: ExampleMinigame, previous: Phase<ExampleMinigame>) {
            minigame.settings.canPvp.set(false)

            // In 10 minutes we will move to the next phase
            minigame.scheduler.schedulePhased(10.Minutes, PhaseChangeTask(minigame, Active))
        }
    },
    Active("active") {
        override fun start(minigame: ExampleMinigame, previous: Phase<ExampleMinigame>) {
            minigame.settings.canPvp.set(true)

            // In 30 minutes we will move to the next phase
            minigame.scheduler.schedulePhased(30.Minutes, PhaseChangeTask(minigame, DeathMatch))
        }
    },
    DeathMatch("death_match") {
        override fun start(minigame: ExampleMinigame, previous: Phase<ExampleMinigame>) {
            // Change to location of the arena
            val location = Location.of()
            for (player in minigame.players.playing) {
                player.teleportTo(location)
            }
        }
    }
}

class ExampleMinigame(
    server: MinecraftServer,
    uuid: UUID
): Minigame(server, uuid) {
    override val id: ResourceLocation = ResourceLocation.fromNamespaceAndPath("modid", "example")

    override fun phases(): Collection<Phase<ExampleMinigame>> {
        return listOf(ExamplePhases.Grace, ExamplePhases.Active, ExamplePhases.DeathMatch)
    }

    @Listener
    private fun onInitialize(event: MinigameInitializeEvent) {
        // Initializing code here!
        println("Example minigame initialized")
    }
}
```

Now we have everything set up we can register our minigame, so we can run it on the server!
We need to create a minigame factory which can generate instances of our minigame.

As our minigame is basic, we can just create an `object` singleton factory as
we don't have any constructor arguments.

```kotlin
object ExampleMinigameFactory: MinigameFactory {
    private val codec = MapCodec.unit(this)
    
    override fun create(context: MinigameCreationContext): Minigame {
        return ExampleMinigame(context.server, context.uuid)
    }

    override fun codec(): MapCodec<out MinigameFactory> {
        return this.codec
    }
}
```

We can then register our factory in our `ModInitializier`:
```kotlin
object ExampleMinigameMod: ModInitializer {
    override fun onInitialize() {
        Registry.register(
            MinigameRegistries.MINIGAME_FACTORY,
            ResourceLocation.fromNamespaceAndPath("modid", "example"),
            ExampleMinigameFactory.codec()
        )
    }
}
```

Now we have registered our minigame factory we can hop in-game.

## `/minigame` command

The minigame command lets you control all aspects of minigames in Arcade.

As we cover in detail more features of minigames, more of these commands will
become useful, but it is placed here for ease of reference.

The first thing to note is that you must be an operator with a permission level of 4 to run this command.

> [!NOTE]
> `<minigame-id>` can be specified by the `uuid` of the minigame, or by the `id` of the minigame (given that there is only one instance), or by `-` which refers to the minigame of the player executing the command.

- `/minigame list` This lists all minigame instances.
- `/minigame create <factory-id>` This allows you to create a minigame instance using a registered minigame factory.
- `/minigame join <minigame-id> <player(s)?>` This allows you to add players to a minigame instance.
- `/minigame leave <player(s)?>` This allows you to remove players from the minigame they are in.


- `/minigame start <minigame-id>` This starts a minigame.
- `/minigame close <minigame-id>` This closes the minigame.
- `/minigame info <minigame-id> <path?>` This gets information about the state of the minigame. The path argument is optional, if not specified, all the info properties will be displayed. You can specify a path if you are only interested in that specific property, e.g. `/minigame info - eliminated_teams`.
- `/minigame team <minigame-id> spectators set <team>` This sets the spectator team which all minigame spectators will join.
- `/minigame team <minigame-id> admins set <team>` This sets the admin team which minigame admins will join.
- `/minigame team <minigame-id> eliminated add <team>` This marks a team as being eliminated.
- `/minigame team <minigame-id> eliminated remove <team>` This un-marks a team as being eliminated.
- `/minigame chat <minigame-id> spies add <player(s)?>` This adds the specified player(s), or the player executing the command if not specified, to be a chat spy. This will make it so this player will see all chat messages (admin, spectator, and team chats).
- `/minigame chat <minigame-id> spies remove <player(s)?>` This removes the specified player(s) from being a chat spy.
- `/minigame spectating <minigame-id> add <player(s)?>` This marks the player(s) as being a spectator.
- `/minigame spectating <minigame-id> remove <player(s)?>` This un-marks the player(s) as being spectators.
- `/minigame admin <minigame-id> add <player(s)?>` This makes the specified player(s) an admin.
- `/minigame admin <minigame-id> remove <player(s)?>` This removes the specified player(s) from being an admin.
- `/minigame settings <minigame-id>` This opens up the minigame's setting GUI.
- `/minigame settings <minigame-id> <setting>` This gets the value of the specified setting.
- `/minigame settings <minigame-id> <setting> set from option <option>` This sets the value of a setting from one of the pre-defined setting options.
- `/minigame settings <minigame-id> <setting> set from value <value>` This sets the value of a setting to any value specified (this is JSON).
- `/minigame tags <minigame-id> <player> add <tag>` This adds a minigame tag to the specified player.
- `/minigame tags <minigame-id> <player> remove <tag>` This removes a minigame tag from the specified player.
- `/minigame tags <minigame-id> <player> list` This lists all the minigame tags the specified player has.
- `/minigame phase <minigame-id>` This gets the current phase the minigame is in.
- `/minigame phase <minigame-id> set <phase>` This sets the current phase the minigame is in.
- `/minigame pause <minigame-id>` This pauses the minigame.
- `/minigame unpause <minigame-id>` This unpauses the minigame.
- `/minigame unpause <minigame-id> countdown <time?> <unit?>` This starts a countdown that will unpause the minigame. You can optionally specify a time with a unit, if not specified it will default to 10 seconds.
- `/minigame unpause <minigame-id> ready <players|teams>` This broadcasts a ready check for either all players or teams. Once all are ready, admins will be prompted to run the unpause countdown command.


> See the next section on [Players](players.md)