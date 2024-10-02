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
    include(modImplementation("net.casual-championships:arcade-events:0.3.0-alpha.18+1.21.1")!!)

    include(modImplementation("net.casual-championships:arcade-utils:0.3.0-alpha.18+1.21.1")!!)
}
```

## Usage

> ### [Basic Usage](./basic-usage.md)
> ### [Advanced Usage](./advanced-usage.md)
