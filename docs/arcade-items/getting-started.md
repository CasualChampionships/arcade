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
    include(implementation("net.casualchampionships:arcade-items:0.9.0-beta.1+26.1-pre-2")!!)

    include(modImplementation("org.jetbrains.kotlin:kotlin-stdlib:2.3.10")!!)
}
```
