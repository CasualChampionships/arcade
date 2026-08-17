# Resource Packs

The module provides many resource pack utilities, such as keeping track of what
resources the client has loaded.

## Adding to Dependencies

The resource pack module depends on some other arcade modules; it's recommended that you
include all of these.

```kts
repositories {
    maven("https://maven.casualchampionships.net/snapshots")
}

dependencies {
    include(implementation("net.casualchampionships:arcade-resource-pack:0.13.0-beta.7+26.2")!!)

    include(implementation("net.casualchampionships:arcade-event-registry:0.13.0-beta.7+26.2")!!)
    include(implementation("net.casualchampionships:arcade-events-server:0.13.0-beta.7+26.2")!!)
    include(implementation("net.casualchampionships:arcade-extensions:0.13.0-beta.7+26.2")!!)
    include(implementation("net.casualchampionships:arcade-resource-pack-host:0.13.0-beta.7+26.2")!!)
    include(implementation("net.casualchampionships:arcade-utils:0.13.0-beta.7+26.2")!!)
}
```