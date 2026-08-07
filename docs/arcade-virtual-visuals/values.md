# Values

Everything that a visual displays is held in a `PlayerSpecificValue`. A bossbar's title
is one, so is its progress, its color, and each row of a sidebar.

Every value has a base, which is what all players are shown by default, as well as
optional per-player overrides. This is how any visual in this module is able to display
something different to each player.

## Setting Values

To set the base value we simply call `set` with the value we want to display:

```kotlin
val bossbar = VirtualBossbar()
bossbar.title.set(Component.literal("Hello!"))
bossbar.color.set(BossBarColor.RED)
```

If we want to display something different to a specific player, we can set an override
by additionally passing in that player:

```kotlin
val player: ServerPlayer = // ...
bossbar.title.set(player, Component.literal("Hello, ${player.username}!"))
```

All the other players will continue to be shown `Hello!`. Once a player has an override
they no longer follow the base value, so changing the base later will not affect them:

```kotlin
bossbar.title.set(Component.literal("Goodbye!"))
// Every player is shown "Goodbye!", except our overridden player
```

If we want that player to follow the base value again, we can set them back to it, and
we can check whether a player currently has an override with `isOverridden`:

```kotlin
if (bossbar.title.isOverridden(player)) {
    bossbar.title.setToBase(player)
}
```

## Reading Values

We can read the base value with `get`, and read what a specific player is being shown by
passing in that player, which will return their override if they have one, otherwise the
base value:

```kotlin
val base: Component = bossbar.title.get()
val displayed: Component = bossbar.title.get(player)
```

## Overrides And Players

Overrides are keyed by the player's `UUID`, values never hold onto a `ServerPlayer`
instance. This means that a long-lived visual will not keep a player instance alive after
they've disconnected, and it also means that a player's overrides persist across a relog;
if they log back in and are shown the visual again, they'll see their overrides. If this
isn't what you want, you can remove them with `setToBase`.

Not every observer of a visual is a player, replay recorders are observers too, for
example. Non-player observers can't have overrides and are always shown the base value.

## Updating Values

Setting a value does not immediately send it to any players, the changes are sent the
next time the visual is ticked; the [Observing Section](observing.md) covers this in
more detail.

Only values that have actually changed are sent, and only to the players they've changed
for. Setting a value to what it already is does nothing:

```kotlin
bossbar.color.set(BossBarColor.RED)
bossbar.color.set(BossBarColor.RED) // Nothing is sent, the value is the same
```

This means that we can recalculate and set all our values every tick without having to
worry about sending unnecessary packets to our players.
