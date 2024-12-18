# Dimensions

Arcade's dimension api adds support for creating worlds dynamically, allowing for
on the fly world creation and deletion.

Another commonly used api for creating worlds dynamically is [Fantasy](https://github.com/NucleoidMC/fantasy),
however, arcade's api puts more of a focus on being able to create a persistent world
as well as changing the structure in which the user handles custom worlds.

## Adding to Dependencies

The dimensions module depends on some other arcade modules; it's recommended that you
include all of these.

```kts
repositories {
    maven("https://maven.supersanta.me/snapshots")
}

dependencies {
    include(modImplementation("net.casualchampionships:arcade-dimensions:0.4.0-alpha.16+1.21.4")!!)

    include(modImplementation("net.casualchampionships:arcade-event-registry:0.4.0-alpha.16+1.21.4")!!)
    include(modImplementation("net.casualchampionships:arcade-events-server:0.4.0-alpha.16+1.21.4")!!)
    include(modImplementation("net.casualchampionships:arcade-extensions:0.4.0-alpha.16+1.21.4")!!)
    include(modImplementation("net.casualchampionships:arcade-utils:0.4.0-alpha.16+1.21.4")!!)
}
```

## Usage

> ### [Basic Usage](basic-usage.md)