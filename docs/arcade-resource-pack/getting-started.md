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
    include(modImplementation("net.casualchampionships:arcade-resource-pack:0.4.0-beta.1+1.21.4")!!)

    include(modImplementation("eu.pb4:polymer-core:0.11.2+1.21.4")!!)
    include(modImplementation("eu.pb4:polymer-resource-pack:0.11.2+1.21.4")!!)
    include(modImplementation("net.casualchampionships:arcade-event-registry:0.4.0-beta.1+1.21.4")!!)
    include(modImplementation("net.casualchampionships:arcade-events-server:0.4.0-beta.1+1.21.4")!!)
    include(modImplementation("net.casualchampionships:arcade-extensions:0.4.0-beta.1+1.21.4")!!)
    include(modImplementation("net.casualchampionships:arcade-resource-pack-host:0.4.0-beta.1+1.21.4")!!)
    include(modImplementation("net.casualchampionships:arcade-utils:0.4.0-beta.1+1.21.4")!!)
}
```
