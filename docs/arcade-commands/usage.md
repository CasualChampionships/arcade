# Usage

> Return to [table of contents](getting-started.md)

## Creating Commands

The `CommandUtils` and `CommandTree` objects provide utilities for creating 
much more readable command trees in kotlin.

We start by calling `CommandTree.buildLiteral` or `CommandTree.createLiteral`
which return a `LiteralArgumentBuilder` or `LiteralCommandNode` respectively.
This method takes a lambda of `LiteralArgumentBuilder<S>.() -> Unit` which
allows us to put everything related to this tree inside the lambda.

```kotlin
fun createExampleCommand(): LiteralArgumentBuilder<CommandSourceStack> {
    return CommandTree.buildLiteral("example") {

    }
}
```

We can add suggestions, requirements, literal subcommands and add arguments:
```kotlin
fun createExampleCommand(): LiteralArgumentBuilder<CommandSourceStack> {
    return CommandTree.buildLiteral("example") {
        requiresPermission(2)
        argument("argument", StringArgumentType.string()) {
            requires { it.level.dimension() == Level.OVERWORLD }
            executes { /* Command logic */ }
        }
        
        literal("subcommand") {
            argument("subcommand-argument", StringArgumentType.string()) {
                suggests { _, b -> SharedSuggestionProvider.suggest(listOf("a", "b", "c"), b) }
                executes { /* Command logic */ }
            }
        }
        literal("other-subcommand") {
            // ...
        }
    }
}
```
Other than the structuring of the command tree, everything else is the same as
brigadier.

## Custom Argument Types

Implementing custom argument types yourself is a pain as they usually have
to be synchronized with the client. Arcade works around this with lots of
trickery and allows you to easily implement your own argument types as
well as implementing some basic ones.

By default, Arcade implements `EnumArgument`, `MappedArgument`, 
`RegistryElementArgument`, `TimeArgument`, and `TimeZoneArgument`.

`EnumArgument`s can be created with an Enum `Class` and it will
allow you to use the enum instances as arguments. Similarly,
`MappedArgument` lets you pass in a `Map<String, ?>` to specify the valid
options for your argument:

```kotlin
enum class MyEnum {
    Foo, Bar
}

fun createExampleCommand(): LiteralArgumentBuilder<CommandSourceStack> {
    return CommandTree.buildLiteral("example") {
        /* /example Foo, or /example Bar */
        argument("enum", EnumArgument.enumeration<MyEnum>()) {
            executes { 
                val myEnum = EnumArgument.getEnumeration<MyEnum>(it, "enum")
                Command.SINGLE_SUCCESS
            }
        }
        
        /* /example one, or /example two */
        val options = mapOf("one" to 1, "two" to 2)
        argument("mapped", MappedArgument.mapped(options)) {
            executes { 
                val option = MappedArgument.getMapped<Int>(it, "mapped")
                Command.SINGLE_SUCCESS
            }
        }
    }
}
```

The registry element argument lets you specify a registry entry as an argument
in a command by specifying its resource key.

```kotlin
fun createExampleCommand(): LiteralArgumentBuilder<CommandSourceStack> {
    return CommandTree.buildLiteral("example") {
        argument("element", RegistryElementArgument.element(Registries.COW_VARIANT)) {
            executes {
                val holder: Holder.Reference<CowVariant> = RegistryElementArgument.getHolder(it, "element")
                val variant: CowVariant = RegistryElementArgument.getElement(it, "element")
                Command.SINGLE_SUCCESS
            }
        }
    }
}
```

The other two built-in argument types are self-explanatory. 

### Implementing Your Own Argument Type

To implement your own `ArgumentType` you must extend the `CustomArgumentType<T>`
class. The `T` type is the type of your argument type, what you will be parsing into.

```kotlin
class ExampleArgumentType: CustomArgumentType<String>() {
    override fun parse(reader: StringReader): String {
        TODO("Not yet implemented")
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return super.listSuggestions(context, builder)
    }

    override fun getArgumentInfo(): CustomArgumentTypeInfo<*> {
        return super.getArgumentInfo()
    }
}
```

Here you can implement your `parse` method as you would if you were implementing
a vanilla argument type, and you can also provide suggestions.

By default, all `CustomArgumentTypes` are just disguising themselves as
`StringArgumentType.string()` - a quotable phrase. However, you can change
which argument type your custom argument type is shadowing:

```kotlin
class ExampleArgumentType: CustomArgumentType<String>() {
    // ...
    
    override fun getArgumentInfo(): CustomArgumentTypeInfo<*> {
        // We're now pretending to be a ResourceLocationArgument
        return CustomArgumentTypeInfo.of(ResourceLocationArgument::class.java)
    }
}
```

For more complex argument types (that require arguments), such as `StringArgumentType`
you will need to implement `CustomArgumentTypeInfo` yourself.

## Hidden Commands

Hidden commands are an invaluable tool, especially in combination with
`Component`s as they allow you to register temporary commands with any
callback for a player that runs that 'command'.

You can register a hidden command using the `HiddenCommandManager`:
```kotlin
fun registerMyHiddenCommand() {
    val command: String = HiddenCommandManager.register(
        timeout = 10.Seconds,
        command = { context ->
            println("${context.player.scoreboardName} ran my hidden command!")
        }
    )
}
```
This returns a string containing the command which can be run, the
format of which is undefined.

This is most useful as previously mentioned with `Components` as we can
make functions that get run whenever a player clicks on a chat message.
We do this by using the `function` extension function on `MutableComponent`s.

```kotlin
fun sendMyHiddenCommand(player: ServerPlayer) {
    player.sendSystemMessage(
        Component.literal("[CLICK HERE]").function {
            println("Player ${it.player.scoreboardName} clicked the message!")
        }
    )
}
```

We can also instead use `singleUseFunction` to denote that the function may
only be called once:
```kotlin
fun sendMyHiddenCommand(player: ServerPlayer) {
    player.sendSystemMessage(
        Component.literal("[CLICK HERE]").singleUseFunction {
            println("Player ${it.player.scoreboardName} clicked the message!")
        }
    )
}
```
Once the player has clicked the message, it will no longer function on later clicks.
If we send the same component to multiple players, it will still limit it to
the first player who clicked the message.

> [!IMPORTANT]
> Even if you send a component with a function to only a specific player, it is
> theoretically possible that another player may run the command given they know
> what it is.
> 
> Although unlikely, if your application requires only the player that was sent
> the message to be able to click it, you ***should*** add a check.