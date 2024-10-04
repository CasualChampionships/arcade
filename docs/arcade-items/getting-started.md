# Items

The items module is a small module which uses [Polymer](https://github.com/Patbox/polymer) 
to help create custom-modelled items. 
This is intended to be used alongside the [resource pack module](../arcade-resource-pack/getting-started.md).

## Adding to Dependencies

The extensions module depends on some other arcade modules; it's recommended that you
include all of these.

```kts
repositories {
    maven("https://maven.supersanta.me/snapshots")
}

dependencies {
    include(modImplementation("net.casual-championships:arcade-items:0.3.0-alpha.20+1.21.1")!!)

    include(modImplementation("eu.pb4:polymer-core:0.9.12+1.21.1")!!)
    include(modImplementation("net.casual-championships:arcade-resource-pack:0.3.0-alpha.20+1.21.1")!!)
    include(modImplementation("net.casual-championships:arcade-utils:0.3.0-alpha.20+1.21.1")!!)
}
```
