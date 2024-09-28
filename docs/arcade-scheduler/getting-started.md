# Scheduler

This module adds the ability to schedule tasks using Minecraft's game tick time.

You are able to make your tasks serializable, and they can be rescheduled, for example,
after a server restart.

## Adding to Dependencies

The extensions module depends on some other arcade modules; it's recommended that you
include all of these.

```kts
repositories {
    maven("https://maven.supersanta.me/snapshots")
}

dependencies {
    include(modImplementation("net.casual-championships:arcade-scheduler:0.3.0-alpha.10+1.21.1")!!)

    include(modImplementation("net.casual-championships:arcade-events:0.3.0-alpha.10+1.21.1")!!)
    include(modImplementation("net.casual-championships:arcade-utils:0.3.0-alpha.10+1.21.1")!!)
}
```
