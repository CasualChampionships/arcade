# Guis


## Adding to Dependencies

The extensions module depends on some other arcade modules; it's recommended that you
include all of these.

```kts
repositories {
    maven("https://maven.supersanta.me/snapshots")
}

dependencies {
    include(implementation("net.casualchampionships:arcade-guis:0.9.0-beta.1+26.1-pre-2")!!)

    include(modImplementation("net.casualchampionships:arcade-event-registry:0.9.0-beta.1+26.1-pre-2")!!)
    include(modImplementation("net.casualchampionships:arcade-events-server:0.9.0-beta.1+26.1-pre-2")!!)
    include(modImplementation("net.casualchampionships:arcade-extensions:0.9.0-beta.1+26.1-pre-2")!!)
    include(modImplementation("net.casualchampionships:arcade-utils:0.9.0-beta.1+26.1-pre-2")!!)
    include(modImplementation("org.jetbrains.kotlin:kotlin-stdlib:2.3.10")!!)
}
```
