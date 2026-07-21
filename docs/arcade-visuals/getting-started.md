# Visuals

This module provides loads of utilities for displaying things to players, which includes
bossbars, sidebars, tab displays, nametags, guis, particles, and more. 

This module makes use of [sgui](https://github.com/Patbox/sgui) and 
[custom nametags](https://github.com/senseiwells/CustomNameTags)!

## Adding to Dependencies

The extensions module depends on some other arcade modules; it's recommended that you
include all of these.

```kts
repositories {
    maven("https://maven.casualchampionships.net/snapshots")
}

dependencies {
    include(implementation("net.casualchampionships:arcade-visuals:0.11.0-beta.3+26.2")!!)

    include(implementation("net.casualchampionships:arcade-commands:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-event-registry:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-events-server:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-extensions:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-nametags:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-resource-pack:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-scheduler:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-utils:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-virtual-entities:0.11.0-beta.3+26.2")!!)
}
```
