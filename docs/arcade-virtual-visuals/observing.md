# Observing

Visuals are only displayed to their observers. An `Observer` is anything that can be sent
packets, usually a player, but replay recorders are observers too. More information about
these in the [Observers Section](../arcade-observers/getting-started.md).

## Displaying Visuals

Every visual implements `VirtualVisual`, and we can display one to a player by making
them start observing it:

```kotlin
val bossbar = VirtualBossbar()
bossbar.title.set(Component.literal("Hello!"))

val player: ServerPlayer = // ...
bossbar.startObservingAndSendPackets(player.asObserver())
```

The player is now being tracked by the visual, and has been sent everything needed to
display it. When we no longer want to display it, we stop them observing:

```kotlin
bossbar.stopObservingAndSendPackets(player.asObserver())
```

Both of these are no-ops if the observer is already in that state, so it's safe to call
them more than once.

We can query which observers are being displayed a visual, as well as which visuals an
observer is currently being displayed:

```kotlin
val observing: Boolean = bossbar.observers.isObserving(player.asObserver())

val visuals: Collection<VirtualVisual> = player.asObserver().observingVisuals()
```

## Ticking Visuals

Setting a [value](values.md) does not send it immediately, the changes are sent when the
visual is ticked:

```kotlin
bossbar.title.set(Component.literal("Updated!"))
bossbar.tick()
```

Visuals are not ticked for you, whoever creates a visual is responsible for ticking it
once per tick, for example, in a server tick event:

```kotlin
GlobalEventHandler.Server.register<ServerTickEvent> {
    bossbar.tick()
}
```

A visual that never changes doesn't need to be ticked at all.

> [!NOTE]
> If you're using visuals in a minigame you do not need to do any of this, the minigame
> will tick the visual and manage its observers for you, see the
> [Minigame Visuals Section](../arcade-minigames/visuals.md).

## Displaying One At A Time

Some visuals can only sensibly be displayed one at a time; a player can only be shown one
sidebar and one tab display, but any number of bossbars. Sidebars and tab displays handle
this for us, observing a second one will stop the observer observing the first:

```kotlin
first.startObservingAndSendPackets(observer)
second.startObservingAndSendPackets(observer)
// The observer is now only observing `second`
```

## Players Leaving

When a player disconnects they automatically stop observing every visual they were being
displayed, so we don't need to clean up after them.

Their per-player overrides are kept, see the [Values Section](values.md), so if the
player reconnects and starts observing again, they'll be shown their overrides.

Observers which aren't players don't disconnect, so whoever created one is responsible
for cleaning it up:

```kotlin
observer.stopObservingVisuals()
```

## Replays

If a player is being recorded with the [replay module](../arcade-replay/getting-started.md)
then all the visuals they're observing are recorded too, and this also applies to chunk
recorders. We don't need to do anything for our visuals to appear correctly in a replay.

## Implementing Your Own

If none of the built-in visuals suit your use case, we can implement `VirtualVisual`
ourselves:

```kotlin
class MyVisual(
    override val observers: ObserverTracker = SimpleObserverTracker()
): VirtualVisual {
    override fun tick() { }

    override fun sendSpawnPackets(observer: Observer, sender: PacketSender) { }

    override fun sendDespawnPackets(observer: Observer, sender: PacketSender) { }

    override fun onStartObserving(observer: Observer) { }

    override fun onStopObserving(observer: Observer) { }
}
```

The spawn and despawn methods are given both an `observer` and a `sender`. The observer
is who the packets are for, so it's what we resolve our values against, and the sender is
where the packets are written to. Usually these are the same, but they differ when
something resends the visual on the observer's behalf, such as a replay recorder, so
always read values from the `observer` and write packets to the `sender`.

We can use [values](values.md) to hold what the visual displays, which lets it support
per-player overrides just like the built-in visuals.
