# Commands

This module extends the functionality of vanilla command completely server-side, allowing
for custom argument types as well as providing the ability to add 'single use' commands
which are useful for clickable text components.

This module also provides kotlin type safe builders for building command trees.

## Adding to Dependencies

The extensions module depends on some other arcade modules; it's recommended that you
include all of these.

```kts
repositories {
    maven("https://maven.supersanta.me/snapshots")
}

dependencies {
    include(implementation("net.casualchampionships:arcade-commands:0.11.0-beta.3+26.2")!!)

    include(implementation("net.casualchampionships:arcade-event-registry:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-events-server:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-utils:0.11.0-beta.3+26.2")!!)
}
```

> ### [Usage](usage.md)