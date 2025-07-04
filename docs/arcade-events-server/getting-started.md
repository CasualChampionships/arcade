# Events

Arcade implements a simple and modular events system for commonly used events.

Most of the built-in events are aimed towards minigame development; most of the 
events are player related events.

The modular design easily allows you to implement your own events which can be used
alongside the built-in events' system.

## Adding to Dependencies

The events api depends on the arcade utilities module; it's recommended that you
include both of these.

```kts
repositories {
    maven("https://maven.supersanta.me/snapshots")
}

dependencies {
    include(modImplementation("net.casualchampionships:arcade-events-server:0.5.1-beta.1+1.21.6")!!)

    include(modImplementation("net.casualchampionships:arcade-event-registry:0.5.1-beta.1+1.21.6")!!)
    include(modImplementation("net.casualchampionships:arcade-utils:0.5.1-beta.1+1.21.6")!!)
}
```

## Usage

> ### [Basic Usage](./basic-usage.md)
> ### [Advanced Usage](./advanced-usage.md)
