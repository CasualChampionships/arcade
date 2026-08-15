# Resource Pack Generation

<!-- TODO: Describe what the resource-pack-generation module provides. -->

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
