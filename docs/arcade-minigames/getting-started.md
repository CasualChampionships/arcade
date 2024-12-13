# Minigames

Arcade's minigame module provides you almost anything you would need to develop a minigame.

Minigames in arcade are completely sandboxed from the rest of the game allowing you to
run many of the same minigame at the same time. 
With support for all the other arcade modules, you're able to use custom dimensions,
have per minigame custom resource packs, and have customized guis, with all the heavy
lifting done for you!

## Adding to Dependencies

If you are implementing minigames, you probably want to bundle the entirety of the arcade,
read the [README](../../README.md) for more information.

```kts
repositories {
    maven("https://maven.supersanta.me/snapshots")
}

dependencies {
    include(modImplementation("net.casual-championships:arcade-minigames:0.4.0-alpha.7+1.21.4")!!)

    include(modImplementation("net.casual-championships:arcade-commands:0.4.0-alpha.7+1.21.4")!!)
    include(modImplementation("net.casual-championships:arcade-dimensions:0.4.0-alpha.7+1.21.4")!!)
    include(modImplementation("net.casual-championships:arcade-event-registry:0.4.0-alpha.7+1.21.4")!!)
    include(modImplementation("net.casual-championships:arcade-events-server:0.4.0-alpha.7+1.21.4")!!)
    include(modImplementation("net.casual-championships:arcade-extensions:0.4.0-alpha.7+1.21.4")!!)
    include(modImplementation("net.casual-championships:arcade-resource-pack:0.4.0-alpha.7+1.21.4")!!)
    include(modImplementation("net.casual-championships:arcade-scheduler:0.4.0-alpha.7+1.21.4")!!)
    include(modImplementation("net.casual-championships:arcade-utils:0.4.0-alpha.7+1.21.4")!!)
    include(modImplementation("net.casual-championships:arcade-visuals:0.4.0-alpha.7+1.21.4")!!)
}
```

> ### [Basic Usage](basic-usage.md)
> ### [Players](players.md)
> ### [Worlds](worlds.md)
> ### [Settings](settings.md)
> ### [Teams](teams.md)
> ### [Events](events.md)
> ### [Scheduling](scheduling.md)
> ### [Commands](commands.md)
> ### [GUI](gui.md)
> ### [Resource Packs](resource_packs.md)
 
### Under Construction 

> ### [Chat](chat.md)
> ### [Advancements](advancements.md)
> ### [Recipes](recipes.md)
> ### [Effects](effects.md)
> ### [Stats](stats.md)

> ### [Lobby](lobby.md)

> ### [Serialization](serialization.md)