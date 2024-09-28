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
    include(modImplementation("net.casual-championships:arcade-visuals:0.3.0-alpha.10+1.21.1")!!)

    include(modImplementation("net.casual-championships:arcade-events:0.3.0-alpha.10+1.21.1")!!)
    include(modImplementation("net.casual-championships:arcade-extensions:0.3.0-alpha.10+1.21.1")!!)
    include(modImplementation("net.casual-championships:arcade-resource-pack:0.3.0-alpha.10+1.21.1")!!)
    include(modImplementation("net.casual-championships:arcade-scheduler:0.3.0-alpha.10+1.21.1")!!)
    include(modImplementation("net.casual-championships:arcade-utils:0.3.0-alpha.10+1.21.1")!!)
}
```
