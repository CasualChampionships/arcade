# Command Managers

Arcade provides dynamic command managers which allow you to register
commands which aren't registered directly to the `MinecraftServer`'s
command dispatcher.

The rationale for this is that it allows you to register and unregister
commands at runtime, as well as allowing for commands to be registered
at any time, not only during server start.

```kotlin
val server: MinecraftServer = // ...
val manager = ServerCommandManager(server)
manager.register(object: CommandTree<CommandSourceStack> {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        TODO("Not yet implemented")
    }
})
GlobalCommandManager.addManager(manager)

// Later...
GlobalCommandManager.removeManager(manager)
```
