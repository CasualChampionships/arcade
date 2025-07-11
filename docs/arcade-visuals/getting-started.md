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
    include(modImplementation("net.casualchampionships:arcade-visuals:0.5.1-beta.1+1.21.6")!!)

    include(modImplementation("net.casualchampionships:arcade-event-registry:0.5.1-beta.1+1.21.6")!!)
    include(modImplementation("net.casualchampionships:arcade-events-server:0.5.1-beta.1+1.21.6")!!)
    include(modImplementation("net.casualchampionships:arcade-extensions:0.5.1-beta.1+1.21.6")!!)
    include(modImplementation("net.casualchampionships:arcade-nametags:0.5.1-beta.1+1.21.6")!!)
    include(modImplementation("net.casualchampionships:arcade-resource-pack:0.5.1-beta.1+1.21.6")!!)
    include(modImplementation("net.casualchampionships:arcade-scheduler:0.5.1-beta.1+1.21.6")!!)
    include(modImplementation("net.casualchampionships:arcade-utils:0.5.1-beta.1+1.21.6")!!)
}
```

> ### [Usage](./usage.md)
