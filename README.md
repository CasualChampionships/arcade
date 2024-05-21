# Arcade

Arcade is a server-side api, primarily aimed at minigame development.

Arcade does a lot of the heavy lifting behind the scenes to make developing minigames
much easier, having a wide range of features built-in.

## Getting Started

To implement the API into your project, you can add the
following to your `build.gradle.kts`

```kts
repositories {
    maven("https://jitpack.io")
}

dependencies {
    // For the most recent version, use the latest commit hash
    val arcadeVersion = "5275ccf13e"
    modImplementation("com.github.CasualChampionships:arcade:$arcadeVersion")
}
```

## Documentation

> ### [Minigames](./docs/minigames.md)
> ### [Commands](./docs/commands.md)
> ### [Events](./docs/events.md)
> ### [Scheduling](./docs/scheduling.md)
> ### [GUI](./docs/gui.md)
> ### [Resources](./docs/resources.md)

