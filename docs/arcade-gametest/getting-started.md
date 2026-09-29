# Gametest

Helpers for writing automated in-game tests against a real `MinecraftServer`, built on Minecraft's
game test framework and `fabric-gametest-api-v1`.

## Adding to Dependencies

This is a test-only library. Add it to your test source set rather than `implementation`.

```kts
repositories {
    maven("https://maven.casualchampionships.net/snapshots")
}

dependencies {
    "gametestImplementation"("net.casualchampionships:arcade-gametest:__ARCADE_VERSION__")
}
```
