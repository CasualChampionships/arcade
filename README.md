# Arcade

Arcade is a server-side Minecraft api made in Kotlin, providing
a wide array of functionality primarily aimed at server-sided
minigame development.

Arcade has been broken down into separate modules, the documentation
for each can be found below:

## Modules

> ### [Commands](./docs/arcade-commands/getting-started.md)
> ### [Datagen](./docs/arcade-datagen/getting-started.md)
> ### [Dimensions](./docs/arcade-dimensions/getting-started.md)
> ### [Events](./docs/arcade-events/getting-started.md)
> ### [Extensions](./docs/arcade-extensions/getting-started.md)
> ### [Items](./docs/arcade-items/getting-started.md)
> ### [Minigames](./docs/arcade-minigames/getting-started.md)
> ### [Resource Packs](./docs/arcade-resource-pack/getting-started.md)
> ### [Resource Pack Hosting](./docs/arcade-resource-pack-host/getting-started.md)
> ### [Scheduling](./docs/arcade-scheduler/getting-started.md)
> ### [Utilities](./docs/arcade-utils/getting-started.md)
> ### [Visuals](./docs/arcade-visuals/getting-started.md)
> ### [World Border](./docs/arcade-world-border/getting-started.md)

## Adding to dependencies

If you are developing minigames using arcade, you will want to include
all the modules, you can do this by adding the following to your
`build.gradle.kts`.

```kts
repositories {
    maven("https://maven.supersanta.me/snapshots")
}

dependencies {
    include(modImplementation("net.casual-championships:arcade:0.3.0-alpha.32+1.21.1")!!)

    include(modImplementation("net.casual-championships:arcade-commands:0.3.0-alpha.32+1.21.1")!!)
    include(modImplementation("net.casual-championships:arcade-dimensions:0.3.0-alpha.32+1.21.1")!!)
    include(modImplementation("net.casual-championships:arcade-events:0.3.0-alpha.32+1.21.1")!!)
    include(modImplementation("net.casual-championships:arcade-extensions:0.3.0-alpha.32+1.21.1")!!)
    include(modImplementation("net.casual-championships:arcade-items:0.3.0-alpha.32+1.21.1")!!)
    include(modImplementation("net.casual-championships:arcade-minigames:0.3.0-alpha.32+1.21.1")!!)
    include(modImplementation("net.casual-championships:arcade-resource-pack:0.3.0-alpha.32+1.21.1")!!)
    include(modImplementation("net.casual-championships:arcade-resource-pack-host:0.3.0-alpha.32+1.21.1")!!)
    include(modImplementation("net.casual-championships:arcade-scheduler:0.3.0-alpha.32+1.21.1")!!)
    include(modImplementation("net.casual-championships:arcade-utils:0.3.0-alpha.32+1.21.1")!!)
    include(modImplementation("net.casual-championships:arcade-visuals:0.3.0-alpha.32+1.21.1")!!)
    include(modImplementation("net.casual-championships:arcade-world-border:0.3.0-alpha.32+1.21.1")!!)
}
```



