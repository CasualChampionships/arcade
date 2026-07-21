# Basic Extensions

To create a basic extension we just need to create a class that
implements the `Extension` interface:
```kotlin
class MyExtension: Extension {

}
```

From here we can add any fields that we want to store in our extension:
```kotlin
class MyExtension: Extension {
    var foo: String = "example"
    var bar: Int = 0xDEADBEEF
    var baz: Identifier = Identifier("modid", "path")
}
```

We then need to register our extension to the type that we want to 
extend. Currently, arcade supports extending `ServerPlayer`, `Entity`, 
`ServerLevel`, and `PlayerTeam`. Each of which have an event which
we can hook into to add our extension:
```kotlin
class MyExtension: Extension {
    // ...
    companion object {
        // Call this from your mod initializer
        internal fun registerEvents() {
            GlobalEventHandler.Server.register<LevelExtensionEvent> {
                it.addExtension(MyExtension())
            }
        }
    }
}
```

Typically, we want to always register our extension to a class meaning
there will never be an instance of the class without the extension.

To then access our extension we can use the `getExtension` utility 
extension functions defined in `ExtensionUtils`, but a nicer way
to access your extension is to define an extension property:
```kotlin
class MyExtension: Extension {
    // ...
    companion object {
        @JvmStatic // If we want to access the extension from Java
        val ServerLevel.myExtension: MyExtension
            get() = this.getExtension()
        
        // ...
    }
}

fun foo(level: ServerLevel) {
    // Using the getExtension function
    val a = level.getExtension<MyExtension>()
    // Using our extension property, which is nicer
    val b = level.myExtension
}
```

It may also be useful for our extension to have context of its owner.
When registering your extension the event will provide access to the
owner, you can pass this reference into your extension, except 
extensions for entities and players which work slightly differently. 
This will be covered in the 
[Entities and Players Section](entity-player-extensions.md).

We can also register other events to update our extension, lets for
example say we wanted to keep track of the last modified block position
in our world:
```kotlin
class MyLevelExtension: Extension {
    var lastModifiedBlockPos: BlockPos? = null

    companion object {
        val ServerLevel.myExtension: MyLevelExtension
            get() = this.getExtension()

        internal fun registerEvents() {
            GlobalEventHandler.Server.register<LevelExtensionEvent> { event ->
                event.addExtension(MyLevelExtension())
            }
            GlobalEventHandler.Server.register<LevelBlockChangedEvent> { (level, pos, _, _) ->
                level.myExtension.lastModifiedBlockPos = pos
            }
        }
    }
}
```
