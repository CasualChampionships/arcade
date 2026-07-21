# Player Specific Elements

Before we continue looking at any more gui components, the majority of them rely on 
Arcade's `PlayerSpecificElement`s, so let's take a quick look at what they are and 
what purpose they serve.

Most GUI elements are player-dependent meaning that they can display different 
elements to different players, meaning if you want to display the stat for the 
viewing player, it's extremely easy to do.

We can create such an element like so:
```kotlin
// An element that returns the player's display name
val element = PlayerSpecificElement<Component> { player -> player.displayName }
```

For the example above the element generator will be run everytime we call 
`element.get(player)`, however, in a lot of cases it's likely that the element will 
not be changing between ticks. If your generator is expensive, it may be worth considering 
caching your element:
```kotlin
val element = PlayerSpecificElement<Component> { player ->
    /* Expensive function */
    Component.literal("Foo Bar")
}.cached()
```

This does mean you need to constantly tick your element to keep it updated but prevents 
you unnecessarily calling the expensive function multiple times.

Further, we can merge elements:
```kotlin
val teamElement: PlayerSpecificElement<PlayerTeam> = // ...
val messageElement: PlayerSpecificElement<String> = // ...

val componentElement: PlayerSpecificElement<Component> = teamElement.merge(messageElement) { team, message ->
    team.formattedDisplayName.append(" ").append(message)
}
```

## Other Type-Specific Elements

There are other type-specific elements: `LevelSpecficElement`, `TeamSpecificElement`, and `UniversalElement`, 
these all inherit from `PlayerSpecificElement`, let's also take a quick look at these:

- `LevelSpecificElement`: this is an element that displays based on the world. 
This may be useful if you want to display information about the current world a 
player is in, this can then be cached on a level basis.
- `TeamSpecificElement`: this is an element that displays based on a player team. 
Similarly to the level element, this may be useful if you want to display information 
about a player's team, which can then be cached on a team basis.
- `UniversalElement`: this is an element that displays the same universally. 
This may be useful for elements that will always be displayed the same to **all** 
players.

`UniversalElements` can also be constant, if there is no need to constantly update it's content:
```kotlin
val element: UniversalElement<Component> = UniversalElement.constant(Component.literal("This is constant!"))
```

And similarly to `PlayerSpecificElements` we can create a cached universal element 
that will be updated only once per tick:
```kotlin
val element: UniversalElement<Int> = UniversalElement { server ->
    /* Expensive function */
    server.tickCount
}.cached()
```
