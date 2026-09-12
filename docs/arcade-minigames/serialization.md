# Serialization

Minigames can be saved to disk and reloaded over a server restart, so a minigame
that was halfway through its `Grace` phase when the server went down is still
halfway through its `Grace` phase when the server comes back up.

This is opt-in; a minigame is only saved if it implements `SerializableMinigame`.
Minigames that don't implement it are simply closed when the server stops.

## Making a Minigame Serializable

To mark your minigame as serializable, implement the `SerializableMinigame`
interface. This provides additional methods to describe how your minigame
should be serialized and deserialized:

```kotlin
class ExampleMinigame(
    server: MinecraftServer,
    uuid: UUID
): Minigame(server, uuid, ID, ExamplePhase.entries), SerializableMinigame {
    var points: Int = 0

    override fun factory(): MinigameFactory {
        return ExampleMinigameFactory
    }

    override fun serialize(output: ValueOutput) {
        output.putInt("points", this.points)
    }

    override fun deserialize(input: ValueInput, version: Int) {
        this.points = input.getIntOr("points", 0)
    }

    companion object {
        val ID: Identifier = Identifier("modid", "example")
    }
}
```

You only need to write out the state that is specific to *your* minigame.
Everything the minigame itself manages is saved for you; the current phase, the
state and uptime of the minigame, the players (including offline ones),
spectators, admins, teams, tags, levels, settings, stats, chat, advancements,
recipes, and any routines scheduled on the minigame's scopes.

The `factory` method is required, this is what recreates an instance of your
minigame before its data is loaded back into it, so your `MinigameFactory` must
be registered as described in the [Basic Usage Section](./basic-usage.md#registering-a-minigame).

If the shape of your data changes, you can bump the `serializationVersion` of
your minigame. The version the minigame was saved under is passed into
`deserialize`, which lets you fix up any data that has changed over versions:

```kotlin
override val serializationVersion: Int
    get() = 1

override fun deserialize(input: ValueInput, version: Int) {
    if (version < 1) {
        this.points = input.getIntOr("score", 0)
    } else {
        this.points = input.getIntOr("points", 0)
    }
}
```

## Saving and Loading

Serializable minigames are saved whenever the server saves, and again when the
server stops. You can also save a minigame yourself; this runs asynchronously in
the background but can be joined with the returned `Job`:

```kotlin
val minigame: ExampleMinigame = // ...

val job: Job = minigame.save()
// Suspends until save is complete
job.join()
```

Minigames are loaded back automatically when the server starts, you do not need
to do anything for this to happen. If a minigame's data cannot be loaded, for
example because its factory is no longer registered, the data is quarantined
inside the world's `minigames/quarantined` directory instead of being deleted,
so nothing is lost while you fix the problem.

When a minigame closes, its saved data is deleted.

> [!NOTE]
> Levels which are part of a serializable minigame must outlive the server
> restart. See the [World Section](./worlds.md) for how level ownership and
> persistence interact.

## Routines

Coroutines are transient, nothing you launch in one survives a restart, so a
serializable minigame cannot use them for its phase logic. Instead, it uses
`Routine`s, which are the serializable counterpart.

A routine is a resumable unit of scheduled work. Its own fields are its state,
serialized by the `MapCodec` that it provides, and it may suspend itself with
`delay` and `await`. Minigame routines implement `MinigameRoutine<M>`, where `M`
is your minigame type, which gives the routine's body access to the `minigame`
it is running for:

```kotlin
class GraceRoutine: MinigameRoutine<ExampleMinigame> {
    override fun codec(): MapCodec<out Routine<ExampleMinigame>> {
        return codec
    }

    override suspend fun RoutineScope<ExampleMinigame>.run() {
        try {
            // Any methods that should "run once", i.e. not run if
            // the Routine is reloaded should be wrapped in a step
            step { minigame.settings.canPvp.set(false) }
            delay(5.Minutes)
            step { minigame.chat.broadcast(Component.literal("The grace period is over!")) }
        } finally {
            step { minigame.settings.canPvp.set(true) }
        }
    }

    companion object: CodecProvider<GraceRoutine> {
        override val id: Identifier = Identifier("modid", "grace")
        override val codec: MapCodec<out GraceRoutine> = MapCodec.unit(::GraceRoutine)
    }
}
```

When a routine is restored its body is immediately re-executed from the top to
rebuild its state, and it then continues from wherever it was suspended. This is
why side effects belong inside a `step`, steps that have already run are skipped
on the way back. Conversely, anything the routine needs to exist for as long as
it is suspended, such as a bossbar it displays, should be created *outside* a
step so that it is recreated when the routine is restored.

A `delay` resumes with only the remaining duration left, so a routine which was
five minutes into a ten-minute delay has five minutes left to run after the
restart:

```kotlin
delay(duration) { remaining -> timer.setRemainingDuration(remaining) }
```

The block passed to `delay` is invoked every time the routine enters that
suspension point, with the duration remaining before it resumes; the full
duration when the routine first reaches it, and however much is left when the
routine is restored.

Routines can also suspend until an event is broadcast for the minigame, and
these too survive a restart, the routine simply keeps waiting for the event:

```kotlin
await<PlayerDeathEvent> { event -> minigame.players.setSpectating(event.player) }
```

If you need the event's data later in the routine, record it with a codec:

```kotlin
val victim = await<PlayerDeathEvent, _>(UUIDUtil.CODEC) { event -> event.player.uuid }
```

Finally, if a routine should run until something else ends it, suspend with
`awaitCancellation`.

Whenever you change the structure of a routine's body, bump its `version`. A
routine saved under a different version cannot safely be replayed, as its body
may no longer issue the same sequence of suspension points.

### Registering Routines

Routines are registered in `TaskRegistries.ROUTINE`, using the
`CodecProvider` helper on the companion object:

```kotlin
object ExampleMinigameMod: ModInitializer {
    override fun onInitialize() {
        GraceRoutine.register(TaskRegistries.ROUTINE)
        ActiveRoutine.register(TaskRegistries.ROUTINE)
    }
}
```

A routine that isn't registered cannot be scheduled, and you will get an
exception if you try.

### Routines and Phases

Where a non-serializable minigame sets a coroutine for a phase, a serializable
one sets a routine:

```kotlin
class ExampleMinigame(
    server: MinecraftServer,
    uuid: UUID
): Minigame(server, uuid, ID, ExamplePhase.entries), SerializableMinigame {
    @Listener
    private fun onInitialize(event: MinigameInitializeEvent) {
        this.phases.routines[ExamplePhase.Grace] = GraceRoutine()
        this.phases.routines[ExamplePhase.Active] = ActiveRoutine()
    }

    // ...
}
```

These behave the same way as phase coroutines do; the routine starts when the
minigame enters the phase, it is cancelled if the minigame leaves the phase, and
when it finishes the minigame advances to the next phase.

To change phase from inside a routine, use `requestPhase` rather than setting the
phase directly:

```kotlin
override suspend fun RoutineScope<ExampleMinigame>.run() {
    step("round") { minigame.points += 1 }
    delay(1.Minutes)
    if (minigame.points < 3) {
        requestPhase(ExamplePhase.Grace)
    }
}
```
Setting the phase directly from inside a routine would cancel the routine that is
currently running, so the phase change is instead deferred until later in the
tick.

### Scheduling Routines

Routines aren't only for phases, they can be scheduled on any scope, which gives
you serializable work with whatever lifetime you want:

```kotlin
val minigame: ExampleMinigame = // ...
val scope = minigame.scopes.create(MinigamePhaseLifetime.Forward)

scope.schedule(30.Seconds, GraceRoutine())
```

The scope's lifetime is saved along with the routine, so after a restart the
routine is restored into a scope with the same lifetime.

## Serializing Components

Components have their own serialization, and are restored before the rest of the
minigame's data is loaded. See the [Components Section](./components.md#serializing-components).
