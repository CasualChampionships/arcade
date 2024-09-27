# Resource Pack Host

A small and simple api for hosting resource packs on the server. It creates a simple
http server where you can register all the packs you wish to host, these can then
be transformed into resource pack packets which are sent to players.

Intended for use with the [resource pack module](../arcade-resource-pack/getting-started.md) 
but can be used standalone too.

## Adding to Dependencies

```kts
repositories {
    maven("https://maven.supersanta.me/snapshots")
}

dependencies {
    include(modImplementation("net.casual-championships:arcade-resource-pack-host:0.3.0-alpha.1+1.21.1")!!)
}
```
