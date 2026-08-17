# Resource Pack Generation

Resource packs can be difficult to manage with Mojang changing the specification
almost every version. Arcade provides a solution by allowing you to define
resource packs in code then building them at runtime, generating all the files
automatically.

Custom fonts, sounds, translations, item models, and some shaders are supported.
Additionally, many built-in resource packs are provided for commonly used features,
e.g. negative spacing fonts.

This module is heavily complemented by the [resource pack module](../arcade-resource-pack/getting-started.md)
and the [resource pack host module](../arcade-resource-pack-host/getting-started.md).

## Adding to Dependencies

```kts
repositories {
    maven("https://maven.casualchampionships.net/snapshots")
}

dependencies {
    include(implementation("net.casualchampionships:arcade-resource-pack-generation:0.13.0-beta.7+26.2")!!)

    include(implementation("net.casualchampionships:arcade-resource-pack:0.13.0-beta.7+26.2")!!)
    include(implementation("net.casualchampionships:arcade-resource-pack-host:0.13.0-beta.7+26.2")!!)
    include(implementation("net.casualchampionships:arcade-utils:0.13.0-beta.7+26.2")!!)
}
```
