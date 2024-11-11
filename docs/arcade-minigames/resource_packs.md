# Resource Packs

> Return to [table of contents](getting-started.md)

To read about how to generate and host resource packs read the section on 
[Resources](../arcade-resource-pack/getting-started.md), it will be useful to 
read that before continuing on with this section.

Minigame's can handle resource packs for us, they will ensure that new incoming
players will be prompted to download the correct packs, and when they leave the
minigame the players will remove the respective packs.

Adding resource packs to minigame's is trivial, first we must create some 
`MinigameResources`, if you just have a few pre-determined packs that you want 
to send to all players then we can use the utility method 
`MinigameResources#of`:
```kotlin
val packA: PackInfo = // ...
val packB: PackInfo = // ...
val packC: PackInfo = // ...

val resources: MinigameResources = MinigameResources.of(packA, packB, packC)
```

Alternatively `MinigameResources` allows you to generate a list of packs on a 
per-player basis. We can make our own implementation by implementing the 
`MinigameResources` interface:
```kotlin
object MyResources: MinigameResources {
    override fun getPacks(): Collection<PackInfo> {
        return super.getPacks()
    }

    override fun getPacks(player: ServerPlayer): Collection<PackInfo> {
        return super.getPacks(player)
    }
}
```

Once we have our resources we can simply add them to our minigame using the 
`Minigame#resources` field. This will keep track of existing resources and 
ensure to broadcast changes to players in the minigame.
```kotlin
val minigame: Minigame = // ...
    
minigame.resources.add(MyResources)

// We can also remove resources
minigame.resources.remove(MyResources)
```

> See the next section on [Chat](chat.md)