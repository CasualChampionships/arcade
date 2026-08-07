# Client Events

This module provides client-side events, built on top of the
[Event Registry](../arcade-event-registry/getting-started.md).

As these events are client-side, this module should only be used in a
client environment.

## Adding to Dependencies

The client events module depends on some other arcade modules; it's recommended that you
include all of these.

```kts
repositories {
    maven("https://maven.casualchampionships.net/snapshots")
}

dependencies {
    include(implementation("net.casualchampionships:arcade-events-client:0.13.0-beta.1+26.2")!!)

    include(implementation("net.casualchampionships:arcade-event-registry:0.13.0-beta.1+26.2")!!)
    include(implementation("net.casualchampionships:arcade-utils:0.13.0-beta.1+26.2")!!)
}
```
