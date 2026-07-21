# Observers

The observers module keeps track of which "observers" are currently observing
a given entity or level. This includes all players who are within tracking
range, but also other observers who wish to listen in on packets being sent
by the target.
This module is useful when working with packet-based visuals (e.g. virtual entities).

## Adding to Dependencies

The observers module depends on some other arcade modules; it's recommended that you
include all of these.

```kts
repositories {
    maven("https://maven.casualchampionships.net/snapshots")
}

dependencies {
    include(implementation("net.casualchampionships:arcade-observers:0.11.0-beta.3+26.2")!!)

    include(implementation("net.casualchampionships:arcade-event-registry:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-events-server:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-extensions:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-utils:0.11.0-beta.3+26.2")!!)
}
```
