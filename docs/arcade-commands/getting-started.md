# Commands

This module extends the functionality of vanilla command completely server-side, allowing
for custom argument types as well as providing the ability to add 'single use' commands
which are useful for clickable text components.

This module also provides kotlin type safe builders for building command trees.

## Adding to Dependencies

The extensions module depends on some other arcade modules; it's recommended that you
include all of these.

```kts
repositories {
    maven("https://maven.supersanta.me/snapshots")
}

dependencies {
    include(modImplementation("net.casual-championships:arcade-commands:0.3.0-alpha.2+1.21.1")!!)

    include(modImplementation("net.casual-championships:arcade-events:0.3.0-alpha.2+1.21.1")!!)
    include(modImplementation("net.casual-championships:arcade-utils:0.3.0-alpha.2+1.21.1")!!)
}
```
