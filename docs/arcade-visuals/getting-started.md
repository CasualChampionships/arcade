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
    maven("https://maven.supersanta.me/snapshots")
}

dependencies {
    include(implementation("net.casualchampionships:arcade-visuals:0.9.0-beta.1+26.1-pre-2")!!)

    include(modImplementation("net.casualchampionships:arcade-commands:0.9.0-beta.1+26.1-pre-2")!!)
    include(modImplementation("net.casualchampionships:arcade-event-registry:0.9.0-beta.1+26.1-pre-2")!!)
    include(modImplementation("net.casualchampionships:arcade-events-server:0.9.0-beta.1+26.1-pre-2")!!)
    include(modImplementation("net.casualchampionships:arcade-extensions:0.9.0-beta.1+26.1-pre-2")!!)
    include(modImplementation("net.casualchampionships:arcade-nametags:0.9.0-beta.1+26.1-pre-2")!!)
    include(modImplementation("net.casualchampionships:arcade-resource-pack:0.9.0-beta.1+26.1-pre-2")!!)
    include(modImplementation("net.casualchampionships:arcade-scheduler:0.9.0-beta.1+26.1-pre-2")!!)
    include(modImplementation("net.casualchampionships:arcade-utils:0.9.0-beta.1+26.1-pre-2")!!)
    include(modImplementation("net.casualchampionships:arcade-virtual-entities:0.9.0-beta.1+26.1-pre-2")!!)
    include(modImplementation("org.jetbrains.kotlin:kotlin-stdlib:2.3.10")!!)
}
```

> ### [Usage](./usage.md)
