# Components

Not all minigame logic belongs in your minigame class. It may be better for
certain logic to be self-contained or minigame-independent especially if you
want to use them in more than one minigame. Or, it may even be the case that
you *can't* modify the minigame class as you're building on someone else's
work.

Components provide a solution. A `MinigameComponent` is a modular
component which can be dynamically added or removed from any `Minigame`
instance, and each one is queried by its own `MinigameComponentType`.

## Implementing a Component

A component implements `MinigameComponent`, which has an `initialize` method
where its logic is set up, a `close` method for when it is removed, and a `type`
identifying it:

```kotlin
class ExampleComponent: MinigameComponent {
    override fun initialize(scope: MinigameScope) {
        val minigame = scope.minigame

        scope.register<PlayerDeathEvent> { (player) ->
            minigame.chat.broadcast(Component.literal("${player.username} died!"))
        }
    }

    override fun close() {
        // ...
    }

    override fun type(): MinigameComponentType<*> {
        return TYPE
    }

    companion object {
        val TYPE = MinigameComponentType<ExampleComponent>(Identifier("modid", "example_component"))
    }
}
```

Everything the component does should be registered against the `MinigameScope`
it is given, so that if the component is removed later everything is properly
cleaned up. The scope lives for as long as the component is attached to the
minigame, see the [Scheduling Section](./scheduling.md) for what else a scope can
own.

## Adding and Removing Components

Components are managed by the `MinigameComponents` manager, accessible through
the `components` field on a minigame:

```kotlin
val minigame: Minigame = // ...

minigame.components.add(ExampleComponent())
```

A minigame can only have one component of any given type. The component is
initialized straight away if the minigame has already been initialized,
otherwise this is deferred until the minigame initializes.

We can remove a component either by instance or by type, which closes its scope
and the component itself:
```kotlin
val minigame: Minigame = // ...

minigame.components.remove(ExampleComponent.TYPE)
```

And we can query the components that a minigame has:
```kotlin
val minigame: Minigame = // ...

// Gets the component, null if the minigame doesn't have it
val component: ExampleComponent? = minigame.components.get(ExampleComponent.TYPE)

// Gets the component, throwing if the minigame doesn't have it
val required: ExampleComponent = minigame.components.require(ExampleComponent.TYPE)

// Whether the minigame has the component
val has: Boolean = minigame.components.has(ExampleComponent.TYPE)

// All the components the minigame has
val all: Collection<MinigameComponent> = minigame.components.all()
```

## Built-In Components

Arcade provides `DefaultStatsTrackingComponent`, which tracks common statistics
for you; play time, relogs, deaths, kills, damage taken, damage dealt, and
damage healed:

```kotlin
val minigame: Minigame = // ...

minigame.components.add(DefaultStatsTrackingComponent())
```

## Serializing Components

Components of a [serializable minigame](./serialization.md) can be saved with it,
but they must opt in to this by implementing `SerializableMinigameComponent`:

```kotlin
class ExampleComponent: SerializableMinigameComponent {
    var kills: Int = 0

    override fun serialize(output: ValueOutput) {
        output.putInt("kills", this.kills)
    }

    override fun deserialize(input: ValueInput, version: Int) {
        this.kills = input.getIntOr("kills", 0)
    }

    override fun type(): MinigameComponentType<*> {
        return TYPE
    }

    companion object {
        val TYPE = MinigameComponentType<ExampleComponent>(Identifier("modid", "example_component"))

        // Call this from your ModInitializer
        fun register() {
            MinigameComponentFactory.register(TYPE) { minigame ->
                ExampleComponent()
            }
        }
    }
}
```

As well as the `serialize` and `deserialize` methods, a serializable component
needs a `MinigameComponentFactory` registered under the same id as its
`MinigameComponentType`. This is what recreates the component when the minigame
is reloaded, so a component that was added to a running minigame is still there
after a restart, without you having to add it again.

Like a serializable minigame, a component has a `serializationVersion` which is
passed back into `deserialize`, letting you fix up data that has changed over
versions.

## Components and Data

Components are for behaviour, they are dynamic and can modify what a minigame
does. If what you have is *static* data instead, such as the map a minigame is
played on, use [Minigame Data](./data.md).
