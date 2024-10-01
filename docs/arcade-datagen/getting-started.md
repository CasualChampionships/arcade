# Datagen

Arcade's datagen api currently is aimed towards generating data which will be used in
resource packs which can't be done at the server's runtime, this mainly includes handling
translations.

## Adding to Dependencies

The datagen module depends on some other arcade modules; it's recommended that you
include all of these.

You probably do not want to depend on this in your main server project but instead
have a separate project dedicated to data generation as this api contains client code.

```kts
repositories {
    maven("https://maven.supersanta.me/snapshots")
}

dependencies {
    include(modImplementation("net.casual-championships:arcade-datagen:0.3.0-alpha.11+1.21.1")!!)

    include(modImplementation("net.casual-championships:arcade-resource-pack:0.3.0-alpha.11+1.21.1")!!)
    include(modImplementation("net.casual-championships:arcade-utils:0.3.0-alpha.11+1.21.1")!!)
}
```
