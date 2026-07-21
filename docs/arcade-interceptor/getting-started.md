# Interceptor

The interceptor module allows you to intercept inbound traffic on a connection's
Netty pipeline before it reaches Minecraft's own handlers. This can be used, for
example, to serve HTTP responses over the same port as the Minecraft server.

## Adding to Dependencies

```kts
repositories {
    maven("https://maven.casualchampionships.net/snapshots")
}

dependencies {
    include(implementation("net.casualchampionships:arcade-interceptor:0.11.0-beta.3+26.2")!!)
}
```
