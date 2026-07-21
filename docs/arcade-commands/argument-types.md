# Argument Types

Implementing custom argument types yourself is a pain as they usually have
to be synchronized with the client. Arcade works around this with lots of
trickery and allows you to easily implement your own argument types as
well as implementing some basic ones.

By default, Arcade implements `ChunkPosArgument`, `EnumArgument`, `MappedArgument`, 
`RegionPosArgument`, and `RegistryElementArgument`.

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

Further documentation of the argument types can be found in the KDocs. 

## Implementing Your Own Argument Type

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
        // We're now pretending to be a IdentifierArgument
        return CustomArgumentTypeInfo.of(IdentifierArgument::class.java)
    }
}
```

For more complex argument types (that require arguments), such as `StringArgumentType`
you will need to implement `CustomArgumentTypeInfo` yourself.
