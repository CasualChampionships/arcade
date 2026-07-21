# Player Interfaces

Most Visual elements that we're going to discuss will inherit the class `TrackingVisualElement`, 
this is a class that just manages what players are able to view a given gui component, 
keeps track of who is watching what and how often the ui should be updated.

Whether it's a sidebar, bossbar, or tab display, we can add, remove, and clear players:
```kotlin
val player: ServerPlayer = // ...
val visual: TrackingVisualElement = // ...

// Player can now view this ui component
visual.addPlayer(player)
// Player cannot view this ui component anymore
visual.removePlayer(player)
// Removes all watching players
visual.clearPlayers()
// Gets all watching players
val watching: List<ServerPlayer> = visual.getPlayers()
```

And we can change the update interval, this just specifies how many ticks should 
occur before the ui recalculates its state and sends updates to watching players. 
By default, this will happen every tick.
```kotlin
val visual: TrackingVisualElement = // ...
// An update will now happen every second
    visual.setInterval(20)
```

> [!NOTE]
> If you're using these visual components in a minigame, the minigame will handle all 
> the player management for you, see the section on [Minigame Visuals](../arcade-minigames/visuals.md) 
> for more information.
