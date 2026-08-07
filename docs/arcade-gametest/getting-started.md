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
    include(implementation("net.casualchampionships:arcade-gametest:0.13.0-beta.1+26.2")!!)

    include(implementation("net.casualchampionships:arcade-npcs:0.13.0-beta.1+26.2")!!)
    include(implementation("net.casualchampionships:arcade-scheduler:0.13.0-beta.1+26.2")!!)
    include(implementation("net.casualchampionships:arcade-utils:0.13.0-beta.1+26.2")!!)
}
```

> [!NOTE]
> Its timeout must fit inside the test's `maxTicks`, otherwise the test times out before the assertion 
> can report a failure.

## Test Players

`createTestPlayer(name)` takes an explicit name. Since tests run concurrently and the name determines
the profile UUID, two tests using the same name will fail the second join with a duplicate login. When
the name does not matter, use the no-argument overload, which generates one unique per server run:

```kotlin
val player = createTestPlayer()
```

