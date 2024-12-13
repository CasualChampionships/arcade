# Resource Packs

Arcade's resource pack api allows for creating resource packs dynamically, automatically
generating all the necessary files. This means you don't need to know the resource pack
format, and don't have to waste time updating them every new pack version. Instead, you
can declare what you want in your pack programmatically.

Currently, custom fonts, custom sounds, custom items (with the [items module](../arcade-items/getting-started.md))
are supported. Many built-in commonly used resource packs are also bundled and ready to use.

This api also improves on resource pack tracking, so you know which players have what
packs, allowing for better player handling.

It's recommended you also use the [resource pack hosting module](../arcade-resource-pack-host/getting-started.md)
to host your dynamic resource packs.

## Adding to Dependencies

The extensions module depends on some other arcade modules; it's recommended that you
include all of these.

```kts
repositories {
    maven("https://maven.supersanta.me/snapshots")
}

dependencies {
    include(modImplementation("net.casual-championships:arcade-resource-pack:0.4.0-alpha.7+1.21.4")!!)

    include(modImplementation("eu.pb4:polymer-core:0.11.0+1.21.4-rc1")!!)
    include(modImplementation("eu.pb4:polymer-resource-pack:0.11.0+1.21.4-rc1")!!)
    include(modImplementation("net.casual-championships:arcade-event-registry:0.4.0-alpha.7+1.21.4")!!)
    include(modImplementation("net.casual-championships:arcade-events-server:0.4.0-alpha.7+1.21.4")!!)
    include(modImplementation("net.casual-championships:arcade-extensions:0.4.0-alpha.7+1.21.4")!!)
    include(modImplementation("net.casual-championships:arcade-resource-pack-host:0.4.0-alpha.7+1.21.4")!!)
    include(modImplementation("net.casual-championships:arcade-utils:0.4.0-alpha.7+1.21.4")!!)
}
```
