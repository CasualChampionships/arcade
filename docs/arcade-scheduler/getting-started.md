# Scheduler

This module adds the ability to schedule tasks using Minecraft's game tick time.

Schedulers work on both the logical server and the logical client.

You are able to make your tasks serializable, and they can be rescheduled, for example,
after a server restart.

## Adding to Dependencies

The extensions module depends on some other arcade modules; it's recommended that you
include all of these.

```kts
repositories {
    maven("https://maven.casualchampionships.net/snapshots")
}

dependencies {
    include(implementation("net.casualchampionships:arcade-scheduler:0.11.0-beta.3+26.2")!!)

    include(implementation("net.casualchampionships:arcade-event-registry:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-events-server:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-events-client:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-utils:0.11.0-beta.3+26.2")!!)
}
```
