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
    include(modImplementation("net.casualchampionships:arcade-datagen:0.5.2-beta.1+1.21.8")!!)

    include(modImplementation("net.casualchampionships:arcade-resource-pack:0.5.2-beta.1+1.21.8")!!)
    include(modImplementation("net.casualchampionships:arcade-utils:0.5.2-beta.1+1.21.8")!!)
}
```
