# Boundaries

This module provides an alternative to vanilla's world borders, which
can be extremely challenging to use, and not flexible.

The boundaries module was written for server-side support in mind, providing 
the ability to use custom boundary shapes and custom boundary renderers to 
suit your use case.

## Adding to Dependencies

The extensions module depends on some other arcade modules; it's recommended that you
include all of these.

```kts
repositories {
    maven("https://maven.supersanta.me/snapshots")
}

dependencies {
    include(implementation("net.casualchampionships:arcade-boundaries:0.9.0-beta.8+26.1")!!)

    include(implementation("net.casualchampionships:arcade-event-registry:0.9.0-beta.8+26.1")!!)
    include(implementation("net.casualchampionships:arcade-events-server:0.9.0-beta.8+26.1")!!)
    include(implementation("net.casualchampionships:arcade-extensions:0.9.0-beta.8+26.1")!!)
    include(implementation("net.casualchampionships:arcade-utils:0.9.0-beta.8+26.1")!!)
    include(implementation("net.casualchampionships:arcade-visuals:0.9.0-beta.8+26.1")!!)
}
```

> ### [Usage](usage.md)
