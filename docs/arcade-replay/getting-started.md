# Replay

This module provides an api for recording replays in both the flashback
and replay mod formats.

## Adding to Dependencies

The extensions module depends on some other arcade modules; it's recommended that you
include all of these.

```kts
repositories {
    maven("https://maven.supersanta.me/snapshots")
}

dependencies {
    include(implementation("net.casualchampionships:arcade-replay:0.9.0-beta.8+26.1")!!)

    include(implementation("net.casualchampionships:arcade-commands:0.9.0-beta.8+26.1")!!)
    include(implementation("net.casualchampionships:arcade-event-registry:0.9.0-beta.8+26.1")!!)
    include(implementation("net.casualchampionships:arcade-events-server:0.9.0-beta.8+26.1")!!)
    include(implementation("net.casualchampionships:arcade-resource-pack-host:0.9.0-beta.8+26.1")!!)
    include(implementation("net.casualchampionships:arcade-utils:0.9.0-beta.8+26.1")!!)
}
```
