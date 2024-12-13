# World Border

This module improves on the vanilla world border implementation, there are many issues
with how vanilla implements it, for example, all the vanilla world borders are linked,
and do not scale correctly per dimension. This api fixes many of those issues.

It also provides a way to keep track of border(s) which is useful for minigames.

## Adding to Dependencies

The extensions module depends on some other arcade modules; it's recommended that you
include all of these.

```kts
repositories {
    maven("https://maven.supersanta.me/snapshots")
}

dependencies {
    include(modImplementation("net.casual-championships:arcade-world-border:0.4.0-alpha.7+1.21.4")!!)

    include(modImplementation("net.casual-championships:arcade-commands:0.4.0-alpha.7+1.21.4")!!)
    include(modImplementation("net.casual-championships:arcade-event-registry:0.4.0-alpha.7+1.21.4")!!)
    include(modImplementation("net.casual-championships:arcade-events-server:0.4.0-alpha.7+1.21.4")!!)
    include(modImplementation("net.casual-championships:arcade-extensions:0.4.0-alpha.7+1.21.4")!!)
    include(modImplementation("net.casual-championships:arcade-scheduler:0.4.0-alpha.7+1.21.4")!!)
    include(modImplementation("net.casual-championships:arcade-utils:0.4.0-alpha.7+1.21.4")!!)
}
```
