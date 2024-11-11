# Teams

> Return to [table of contents](getting-started.md)

This section covers how Arcade handles teams, even if your minigame isn't 
team-based, it's likely you still want to use teams to differentiate between 
admins and spectators too.

## Player's Teams

Unlike most other managers in Arcade the `MinigameTeamManager` doesn't manage 
teams for players entirely. Instead, you should add players to teams as you 
normally would. The goal of the team manager is to add behaviours for specific 
teams.

It also provides utilities for getting all the teams that are part of the 
minigame, this is equivalent to iterating over all players and collecting all 
their teams.

```kotlin
val minigame: Minigame = // ...

// All teams that are part of the minigame (including players that are offline)
val allTeams = minigame.teams.getAllTeams()

// All teams (that have online players) that are part of the minigame
// This may also include the spectator and admin team
val onlineTeams = minigame.teams.getOnlineTeams()

// All teams that have playing players (doesn't include spectator and admin teams)
val playingTeams = minigame.teams.getPlayingTeams()

// All teams (including offline) that are not the spectator or admin team
val nonSpectatorOrAdminTeams = minigame.teams.getAllNonSpectatorOrAdminTeams()
```

### Spectators and Admins

You are able to configure your minigame such that spectators and admins join a 
dedicated team. You can do this via the `MinigameTeamManager` class:

```kotlin
val minigame: Minigame = // ...
    
val spectators: PlayerTeam = // ...
minigame.teams.setSpectatorTeam(spectators)

val admins: PlayerTeam = // ...
minigame.teams.setAdminTeam(admins)
```

Once set, any spectators or admins will automatically join these specified 
teams and any players on those teams will be marked as admins and spectators in
the minigame.

You can check whether a given team is the admin or spectator team:
```kotlin
val minigame: Minigame = // ...
val team: Team = // ...

minigame.teams.isSpectatorTeam(team)
minigame.teams.isAdminTeam(team)
```

You can also check whether these teams have been assigned and if they have, you
can get the team. If you try to get the team when it hasn't been set, an 
exception will be thrown.
```kotlin
val minigame: Minigame = // ...

if (minigame.teams.hasSpectatorTeam()) {
    val team: PlayerTeam = minigame.teams.getSpectatorTeam()
}
if (minigame.teams.hasAdminTeam()) {
    val team: PlayerTeam = minigame.teams.getAdminTeam()
}
```


### Team-Based Minigames

The team manager, however, can track any "eliminated" teams.

```kotlin
val minigame: Minigame = // ...
val eliminated: PlayerTeam = // ...
    
minigame.teams.addEliminatedTeam(eliminated)
minigame.teams.removeEliminatedTeam(eliminated)
```

You can then later query whether a team has been eliminated, or query all the 
eliminated teams.

```kotlin
val minigame: Minigame = // ...
val team: PlayerTeam = // ...
    
val isEliminated: Boolean = minigame.teams.isTeamEliminated(team)

val eliminated: Collection<PlayerTeam> = minigame.teams.getEliminatedTeams()
```

And finally, you can query whether a term is ignored; whether the team has been
eliminated, or whether it is the admin or spectator team.
```kotlin
val minigame: Minigame = // ...
val team: PlayerTeam = // ...

val ignored: Boolean = minigame.teams.isTeamIgnored(team)
```

> See the next section on [Events](events.md)