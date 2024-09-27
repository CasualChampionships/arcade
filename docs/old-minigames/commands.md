# Commands

> Return to [table of contents](../old-minigames)

Minigames provide a nice way of registering commands that are completely sandboxed from the rest of the server commands. Arcade allows you to register minigame-specific commands that work locally to each minigame, meaning that only players in that minigame can execute those commands.

The commands system is quite simple; we will just be looking at the `MinigameCommandManager` class, which can be accessed though the `Minigame#commands` field.

## Registering

We can register our custom command using the `MinigameCommandManager#register` method. This is the same as registering a command via a `CommandDispatcher` using brigadier.

```kotlin
val minigame: Minigame<*> = // ...
minigame.commands.register(Commands.literal("example").executes { context ->
    context.source.sendSystemMessage("Example command!".literal())
    Command.SINGLE_SUCCESS
})
```

You can register as many commands as you wish to use the `MinigameCommandManager`.

> [!NOTE]
> Any commands registered in the `MinigameCommandManager` will take precedence over vanilla commands! Be aware of command conflicts. Commands registered in one minigame will not conflict with other commands registered in other minigames (even of the same type).

## Unregistering

We can unregister any commands that we've previously registered using the `MinigameCommandManager#unregister` method which takes in the literal of the command that you want to unregister. Alternatively we can unregister all command with `MinigameCommandManager#registerAll`:
```kotlin
val minigame: Minigame<*> = // ...
minigame.commands.unregister("example")

minigame.commands.unregisterAll()
```

> See the next section on [GUI](gui.md)