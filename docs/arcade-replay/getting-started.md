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
    include(modImplementation("net.casualchampionships:arcade-replay:0.5.2-beta.3+1.21.8")!!)

    include(modImplementation("net.casualchampionships:arcade-commands:0.5.2-beta.3+1.21.8")!!)
    include(modImplementation("net.casualchampionships:arcade-event-registry:0.5.2-beta.3+1.21.8")!!)
    include(modImplementation("net.casualchampionships:arcade-events-server:0.5.2-beta.3+1.21.8")!!)
    include(modImplementation("net.casualchampionships:arcade-resource-pack-host:0.5.2-beta.3+1.21.8")!!)
    include(modImplementation("net.casualchampionships:arcade-utils:0.5.2-beta.3+1.21.8")!!)
}
```
