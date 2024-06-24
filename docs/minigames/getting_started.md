# Getting Started

> Return to [table of contents](../minigames.md)

## Creating a Minigame

Firstly we will take a look at the `Minigame` class. This is the heart
of all minigames that you create, it implements the bare-bones logic and
has common utilities that are used in minigames.

You can implement your own minigame by extending the `Minigame` class:

```kotlin
class ExampleMinigame(server: MinecraftServer): Minigame<ExampleMinigame>(server) {
    override val id: ResourceLocation = TODO("Not yet implemented")

    override fun getPhases(): Collection<Phase<ExampleMinigame>> {
        TODO("Not yet implemented")
    }
}
```

The first thing you should specify is the id of your minigame:

```kotlin
class ExampleMinigame(server: MinecraftServer): Minigame<ExampleMinigame>(server) {
    override val id: ResourceLocation = ResourceLocation.fromNamespaceAndPath("modid", "example")

    override fun getPhases(): Collection<Phase<ExampleMinigame>> {
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

Now we have our phases we can return them in our `getPhases` method in our minigame class.

> [!NOTE]
> The order in which you return these doesn't matter, they will be sorted by their ordinal position later.

```kotlin
class ExampleMinigame(server: MinecraftServer): Minigame<ExampleMinigame>(server) {
    override val id: ResourceLocation = ResourceLocation.fromNamespaceAndPath("modid", "example")

    override fun getPhases(): Collection<Phase<ExampleMinigame>> {
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
in further detail later in the [Serialization](serialization.md) section.
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

class ExampleMinigame(server: MinecraftServer): Minigame<ExampleMinigame>(server) {
    override val id: ResourceLocation = ResourceLocation.fromNamespaceAndPath("modid", "example")

    override fun getPhases(): Collection<Phase<ExampleMinigame>> {
        return listOf(ExamplePhases.Grace, ExamplePhases.Active, ExamplePhases.DeathMatch)
    }
}
```

Now we have everything set up we can register our minigame, so we can run it on the server!
We need to create a minigame factory which can generate instances of our minigame.

We can do this in our `DedicatedServerModInitializer`:
```kotlin
object ExampleMinigameMod: DedicatedServerModInitializer {
    override fun onInitializeServer() {
        // This should be the same id that your minigame uses
        Minigames.registerFactory(ResourceLocation.fromNamespaceAndPath("modid", "example")) { context ->
            ExampleMinigame(context.server)
        }
    }
}
```

Now we have registered our minigame factory we can hop in-game.
Ensure that you have operator permissions.

You can then run the following command:
```
/minigame create <factory-id>
```
This will create an instance of your minigame.

You can then add players by running the following command:
```
/minigame join <minigame-id> <player(s)?>
```

And then start the minigame:
```
/minigame start <minigame-id>
```

For more information about the `/minigame` command, see the [User Command Section](../commands.md)

> See the next section on [Players](players.md)