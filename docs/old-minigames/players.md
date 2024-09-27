# Players

> Return to [table of contents](../old-minigames)

Managing players is an extremely important task in minigames. Arcade's minigames make it extremely easy to do this.

## Adding Players

From the [User Commands Section](../old-commands) we know that we can add and remove players from minigames using the commands, but we can also do it programmatically.

We can add players to our minigame using the `MinigamePlayerManager#add` method:

```kotlin
val minigame: Minigame<*> = // ...
val player: ServerPlayer = // ...
    
val success: Boolean = minigame.players.add(player)
```

The method returns whether the player was added successfully; `true` if the player was added, `false` if not.

The player may be rejected from joining, either because the minigame has already added the player previously or because the implementation of the minigame didn't allow the player to join.

If a player logged out (or the server was restarted, the player will automatically rejoin the minigame.

--- 

This method also allows us to specify whether we want to add the player as a spectator, this is a nullable boolean. `true` will make the player a spectator, `false` will remove the player from spectating, and `null` will preserve whether the player was previously a spectator (or default to not making them a spectator if they weren't previously part of the minigame).

```kotlin
val minigame: Minigame<*> = // ...
val player: ServerPlayer = // ...
    
minigame.players.add(player, spectating = true)
```

We will discuss more about spectating players in the [Spectators Section](#spectators).

## Removing Players

We can remove players from our minigame using the `MinigamePlayerManager#remove` method:

```kotlin
val minigame: Minigame<*> = // ...
val player: ServerPlayer = // ...
    
minigame.players.remove(player)
```

Similarly to the `MinigamePlayerManager#add` method this returns whether the player was successfully removed. The player will only be removed if they are part of the minigame.

## Spectators

It is likely that you want to allow players to spectate your minigame, in which case their behaviours in the minigame will differ from those who are not spectating.

Arcade lets you differentiate between these two types of players, it keeps track of spectating players for you. 
It allows you to designate a spectating team as well as a spectating chat for you, these will be documented in the [Teams Section](teams.md) and the [Chat Section](chat.md) respectively.

A player **must** either be spectating or playing. You can specify this when adding the player to the minigame as discussed in the [Adding Players Section](#adding-players).
You can also do this later by calling `MinigamePlayerManager#setSpectating` and `MinigamePlayerManager#setPlaying`. These methods will return true of the player successfully changed their type. 

> [!NOTE]
> You can only set this for players who are part of the minigame.

```kotlin
val minigame: Minigame<*> = // ...
val player: ServerPlayer = // ...
    
minigame.players.setSpectating(player)
minigame.players.setPlaying(player)
```

An event will be broadcast when a player's type is changed so your minigame can handle and change state for the player.

## Admins

Admins in a minigame are useful as they provide higher privileges,
for example, they can bypass certain minigame settings.
More details about this in the [Minigame Settings Section](settings.md) but a simple example is if you mute the chat admins will still be able to send messages. 

An admin can be either spectating or playing. You can specify whether a player is admin by using the `MinigamePlayerManager#addAdmin` and `MinigamePlayerManager#removeAdmin`

```kotlin
val minigame: Minigame<*> = // ...
val player: ServerPlayer = // ...
    
minigame.players.addAdmin(player)
minigame.players.removeAdmin(player)
```

These will also be broadcast so your minigame can also change state for the player.

## Querying Players

There are quite a few different ways to get players in a minigame.

These methods only return the only players of the minigame:
```kotlin
val minigame: Minigame<*> = // ...

// Gets all online players, including spectators and admins
val allPlayers: List<ServerPlayer> = minigame.players.all
// Gets only non-spectating players
val playingPlayers: List<ServerPlayer> = minigame.players.playing
// Gets only spectating players
val spectatingPlayers: List<ServerPlayer> = minigame.players.spectating
// Gets only admin players
val adminPlayers: List<ServerPlayer> = minigame.players.admin
// Gets only non-admin players
val nonAdminPlayers: List<ServerPlayer> = minigame.players.nonAdmin
```

You may also query the player profiles if you need to access offline players.
```kotlin
val minigame: Minigame<*> = // ...

// Gets all player profiles, both online and offline
val allProfiles: List<GameProfile> = minigame.players.allProfiles
// Gets all player profiles for those players who are offline
val offlineProfiles: List<GameProfile> = minigame.players.offlineProfiles
```

You can also do checks on a player to check whether they are part of the minigame,
whether they're playing, spectating, or an admin.

```kotlin
val minigame: Minigame<*> = // ...
val player: ServerPlayer = // ...

// Checks whether the player is part of the minigame
var isPartOfMinigame: Boolean = minigame.players.has(player)
// Or you can do this instead
isPartOfMinigame = player in minigame.players

// Checks whether the player is playing in the minigame
val isPlaying = minigame.players.isPlaying(player)
// Checks whether the player is spectating in the minigame
val isSpectating = minigame.players.isSpectating(player)
// Checks whether the player is an admin in the minigame
val isAdmin = minigame.players.isAdmin(player)
```

> See the next section on [Worlds](worlds.md)
