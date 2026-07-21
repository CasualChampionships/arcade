# Command Trees

The `CommandTree` type and utilities in `CommandUtils` allow for creating 
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
        requiresPermission(PermissionLevel.GAMEMASTERS)
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
