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
    include(modImplementation("net.casualchampionships:arcade-scheduler:0.5.1-beta.1+1.21.6")!!)

    include(modImplementation("net.casualchampionships:arcade-event-registry:0.5.1-beta.1+1.21.6")!!)
    include(modImplementation("net.casualchampionships:arcade-events-server:0.5.1-beta.1+1.21.6")!!)
    include(modImplementation("net.casualchampionships:arcade-utils:0.5.1-beta.1+1.21.6")!!)
}
```

> ### [Usage](./usage.md)