# Extensions

Extensions are a powerful tool allowing you to store additional data for existing
Minecraft classes without the need to use your own mixins and duck interfaces.

The extension api is built on-top of the events api to register extensions.

## Adding to Dependencies

The extensions module depends on some other arcade modules; it's recommended that you
include all of these.

```kts
repositories {
    maven("https://maven.supersanta.me/snapshots")
}

dependencies {
    include(modImplementation("net.casual-championships:arcade-extensions:0.4.0-alpha.7+1.21.4")!!)

    include(modImplementation("net.casual-championships:arcade-event-registry:0.4.0-alpha.7+1.21.4")!!)
    include(modImplementation("net.casual-championships:arcade-events-server:0.4.0-alpha.7+1.21.4")!!)
    include(modImplementation("net.casual-championships:arcade-utils:0.4.0-alpha.7+1.21.4")!!)
}
```
