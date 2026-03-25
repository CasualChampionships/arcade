# <img src="./src/main/resources/assets/icon.png" align="center" width="64px"/> Arcade


Arcade is a server-side Minecraft api made in Kotlin, providing
a wide array of functionality primarily aimed at server-sided
minigame development.

Arcade has been broken down into separate modules, the documentation
for each can be found below:

## Modules

> ### [Boundaries](docs/arcade-boundaries/getting-started.md)
> ### [Commands](./docs/arcade-commands/getting-started.md)
> ### [Datagen](./docs/arcade-datagen/getting-started.md)
> ### [Dimensions](./docs/arcade-dimensions/getting-started.md)
> ### [Events](docs/arcade-events-server/getting-started.md)
> ### [Extensions](./docs/arcade-extensions/getting-started.md)
> ### [Guis](./docs/arcade-guis/getting-started.md)
> ### [Items](./docs/arcade-items/getting-started.md)
> ### [Minigames](./docs/arcade-minigames/getting-started.md)
> ### [Nametags](./docs/arcade-nametags/getting-started.md)
> ### [NPCs](./docs/arcade-npcs/getting-started.md)
> ### [Replay](./docs/arcade-replay/getting-started.md)
> ### [Resource Packs](./docs/arcade-resource-pack/getting-started.md)
> ### [Resource Pack Hosting](./docs/arcade-resource-pack-host/getting-started.md)
> ### [Scheduling](./docs/arcade-scheduler/getting-started.md)
> ### [Utilities](./docs/arcade-utils/getting-started.md)
> ### [Virtual Entities](./docs/arcade-virtual-entities/getting-started.md)
> ### [Visuals](./docs/arcade-visuals/getting-started.md)

## Adding to dependencies

If you are developing minigames using arcade, you will want to include
all the modules, you can do this by adding the following to your
`build.gradle.kts`.

```kts
repositories {
    maven("https://maven.supersanta.me/snapshots")
}

dependencies {
    include(implementation("net.casualchampionships:arcade:0.9.0-beta.5+26.1")!!)
}
```



