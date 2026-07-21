# Resource Pack Host

A small and simple api for hosting resource packs on the server. It creates a simple
http server where you can register all the packs you wish to host, these can then
be transformed into resource pack packets which are sent to players.

Intended for use with the [resource pack module](../arcade-resource-pack/getting-started.md) 
but can be used standalone too.

## Adding to Dependencies

```kts
repositories {
    maven("https://maven.casualchampionships.net/snapshots")
}

dependencies {
    include(implementation("net.casualchampionships:arcade-resource-pack-host:0.11.0-beta.3+26.2")!!)

    include(implementation("net.casualchampionships:arcade-interceptor:0.11.0-beta.3+26.2")!!)
    include(implementation("net.casualchampionships:arcade-utils:0.11.0-beta.3+26.2")!!)
}
```
