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
    "gametestImplementation"("net.casualchampionships:arcade-gametest:0.11.0-beta.7+26.2")
}
```

If you use Loom's test source set, create it first:

```kts
fabricApi {
    configureTests {
        createSourceSet.set(true)
        modId.set("your-mod-tests")
    }
}
```

## Writing a Test

Test suites are objects extending `ArcadeTestSuite`. Each `@GameTest` method takes an
`ArcadeTestContext` and passes its body to `context.test`, which runs it as a coroutine and passes the
test when it returns, or fails it if it throws.

```kotlin
object ExampleTests: ArcadeTestSuite() {
    @GameTest(maxTicks = 400)
    fun playerReceivesMessage(context: ArcadeTestContext) = context.test {
        val player = createTestPlayer()

        val message = Component.literal("hello")
        server.playerList.broadcastSystemMessage(message, false)
        delay(1.Ticks)

        player.assertSent<ClientboundSystemChatPacket> { it.content().string == "hello" }
    }
}
```

Register each suite in your test mod's `fabric.mod.json`:

```json
{
  "entrypoints": {
    "fabric-gametest": [
      {
        "adapter": "kotlin",
        "value": "com.example.ExampleTests"
      }
    ]
  }
}
```

Run them with `./gradlew runGameTest`.

## Assertions

`ArcadeTestContext` provides assertion functions, examples can be seem below:

```kotlin
assertTrue(condition, "optional message")
assertFalse(condition)
assertEquals(expected, actual)
assertNotEquals(illegal, actual)
assertNull(value)
val nonNullValue = assertNotNull(value)
fail("give up")

val thrown = assertThrows<IllegalStateException> { throw IllegalStateException() }
```

Equality also has an infix form, where the receiver is the actual value:

```kotlin
result shouldEqual 5
result shouldNotEqual 0
```

Because the world is tick-based, most assertions need the game to advance first. `assertEventually`
re-checks a condition once per tick until it holds, or until it timeouts at which point it will
consider the test failed:

```kotlin
assertEventually(2.Seconds) { entity.isRemoved }
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

