# Players

Managing players is an extremely important task in minigames. Arcade's minigames make it extremely easy to do this.

## Adding Players

From the [User Commands Section](../commands.md) we know that we can add and remove players from minigames using the commands, but we can also do it programmatically.

We can add players to our minigame using the `Minigame#addPlayer` method:

```kotlin
val minigame: Minigame<*> = // ...
val player: ServerPlayer = // ...
    
val success: Boolean = minigame.addPlayer(player)
```

The method returns whether the player was added successfully; `true` if the player was added, `false` if not.

The player may be rejected from joining, either because the minigame has already added the player previously or because the implementation of the minigame didn't allow the player to join.

If a player logged out (or the server was restarted, the player will automatically rejoin the minigame.

--- 

This method also allows us to specify whether we want to add the player as a spectator, this is a nullable boolean. `true` will make the player a spectator, `false` will remove the player from spectating, and `null` will preserve whether the player was previously a spectator (or default to not making them a spectator if they weren't previously part of the minigame).

```kotlin
val minigame: Minigame<*> = // ...
val player: ServerPlayer = // ...
    
minigame.addPlayer(player, spectating = true)
```

We will discuss more about spectating players in the [Spectators Section](#spectators).

## Removing Players

We can remove players from our minigame using the `Minigame#removePlayer` method:

```kotlin
val minigame: Minigame<*> = // ...
val player: ServerPlayer = // ...
    
minigame.removePlayer(player)
```

Similarly to the `Minigame#addPlayer` method this returns whether the player was successfully removed. The player will only be removed if they are part of the minigame.

## Querying Players

## Spectators

## Admins