# Arcade

Arcade is a server-side Minecraft api made in Kotlin, providing
a wide array of functionality primarily aimed at server-sided
minigame development.

Arcade has been broken down into separate modules, the documentation
for each can be found below:

## Modules

> ### [Commands](./docs/arcade-commands/getting-started.md)
> ### [Datagen]()
> ### [Dimensions]()
> ### [Events]()
> ### [Extensions]()
> ### [Items]()
> ### [Minigames]()
> ### [Resource Packs]()
> ### [Resource Pack Hosting]()
> ### [Scheduling]()
> ### [Utilities]()
> ### [Visuals]()
> ### [World Border]()

## Adding to dependencies

If you are developing minigames using arcade, you will want to include
all the modules, you can do this by adding the following to your
`build.gradle.kts`.

```kts
repositories {
    maven("https://maven.supersanta.me/snapshots")
}

dependencies {
    include(modImplementation("net.casual-championships:arcade:0.3.0-alpha.1+1.21.1")!!)
}
```



