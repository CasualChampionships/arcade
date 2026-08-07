# Minigames

Arcade's minigame module provides you almost anything you would need to develop a minigame.

Minigames in arcade are completely sandboxed from the rest of the game allowing you to
run many of the same minigame at the same time. 
With support for all the other arcade modules, you're able to use custom dimensions,
have per minigame custom resource packs, and have customized guis, with all the heavy
lifting done for you!

## Adding to Dependencies

If you are implementing minigames, you probably want to bundle the entirety of the arcade,
read the [README](https://github.com/CasualChampionships/arcade#readme) for more information.

```kts
repositories {
    maven("https://maven.casualchampionships.net/snapshots")
}

dependencies {
    include(implementation("net.casualchampionships:arcade-minigames:0.11.0-beta.3+26.2")!!)

    include(implementation("net.casualchampionships:arcade-commands:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-dimensions:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-event-registry:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-events-server:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-extensions:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-guis:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-replay:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-resource-pack:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-scheduler:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-utils:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-virtual-visuals:0.11.0-beta.3+26.2")!!)
}
```
