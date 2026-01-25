# Virtual Entities

Arcade's virtual entity api adds support for creating 'fake' entities that
don't really in the server-side world, but does properly emulate 'real'
entities on the client.

Virtual entities are essentially just a lightweight shell that allow for
full customizability, perfect for non-persisting visual entities,
also allowing for per-player customization.

This api has many overlapping features with [Polymer](https://github.com/Patbox/polymer)'s 
virtual entity api, but aims to fix some of the complexities with polymer's implementation.

## Adding to Dependencies

```kts
repositories {
    maven("https://maven.supersanta.me/snapshots")
}

dependencies {
    include(modImplementation("net.casualchampionships:arcade-virtual-entities:0.8.1-beta.3+1.21.11")!!)

    include(modImplementation("net.casualchampionships:arcade-event-registry:0.8.1-beta.3+1.21.11")!!)
    include(modImplementation("net.casualchampionships:arcade-events-server:0.8.1-beta.3+1.21.11")!!)
    include(modImplementation("net.casualchampionships:arcade-extensions:0.8.1-beta.3+1.21.11")!!)
    include(modImplementation("net.casualchampionships:arcade-utils:0.8.1-beta.3+1.21.11")!!)
}
```
