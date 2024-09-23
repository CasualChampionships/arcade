# Arcade

Arcade is a server-side api, primarily aimed at minigame development.

Arcade does a lot of the heavy lifting behind the scenes to make developing minigames
much easier, having a wide range of features built-in.

## Getting Started

To implement the API into your project, you can add the
following to your `build.gradle.kts`

```kts
repositories {
    maven("https://maven.supersanta.me/snapshots")
}

dependencies {
    modImplementation("net.casual-championships:arcade:0.2.0-alpha.32+1.21.1")
}
```

## Documentation

> ### [Minigames](./docs/minigames.md)
> ### [Commands](./docs/commands.md)
> ### [Events](./docs/events.md)
> ### [Scheduling](./docs/scheduling.md)
> ### [GUI](./docs/gui.md)
> ### [Resources](./docs/resources.md)

