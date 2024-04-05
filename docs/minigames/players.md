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