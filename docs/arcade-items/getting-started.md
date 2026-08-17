# Items

The items module is a small module which helps create custom-modelled items. 
This is intended to be used alongside the 
[resource pack generation module](../arcade-resource-pack-generation/getting-started.md), 
which can generate the models and model definitions our items point at.

## Adding to Dependencies

The extensions module depends on some other arcade modules; it's recommended that you
include all of these.

```kts
repositories {
    maven("https://maven.casualchampionships.net/snapshots")
}

dependencies {
    include(implementation("net.casualchampionships:arcade-items:0.13.0-beta.1+26.2")!!)
}
```
