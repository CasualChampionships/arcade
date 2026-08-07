# GUIs


## Adding to Dependencies

The extensions module depends on some other arcade modules; it's recommended that you
include all of these.

```kts
repositories {
    maven("https://maven.casualchampionships.net/snapshots")
}

dependencies {
    include(implementation("net.casualchampionships:arcade-guis:0.13.0-beta.1+26.2")!!)

    include(implementation("net.casualchampionships:arcade-event-registry:0.13.0-beta.1+26.2")!!)
    include(implementation("net.casualchampionships:arcade-events-server:0.13.0-beta.1+26.2")!!)
    include(implementation("net.casualchampionships:arcade-extensions:0.13.0-beta.1+26.2")!!)
    include(implementation("net.casualchampionships:arcade-utils:0.13.0-beta.1+26.2")!!)
}
```
