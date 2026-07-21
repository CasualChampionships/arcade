# Debug

The debug module provides development utilities for inspecting and debugging a
running server, such as debuggable behaviors and broadcasting debug information
to connected clients.

> This module is primarily intended for development and debugging use.

## Adding to Dependencies

```kts
repositories {
    maven("https://maven.casualchampionships.net/snapshots")
}

dependencies {
    include(implementation("net.casualchampionships:arcade-debug:0.11.0-beta.3+26.2")!!)
}
```
