# Event Registry

The event registry is the foundation of Arcade's event system. It provides the
`GlobalEventHandler`, the `ListenerRegistry`, and the core `Event` types that the
rest of Arcade's modules build upon.

This module only contains the event dispatching infrastructure, the actual
server and client events are provided by the
[Server Events](../arcade-events-server/getting-started.md) and
[Client Events](../arcade-events-client/getting-started.md) modules respectively.

## Adding to Dependencies

The event registry module depends on some other arcade modules; it's recommended that you
include all of these.

```kts
repositories {
    maven("https://maven.casualchampionships.net/snapshots")
}

dependencies {
    include(implementation("net.casualchampionships:arcade-event-registry:0.13.0-beta.1+26.2")!!)

    include(implementation("net.casualchampionships:arcade-utils:0.13.0-beta.1+26.2")!!)
}
```
