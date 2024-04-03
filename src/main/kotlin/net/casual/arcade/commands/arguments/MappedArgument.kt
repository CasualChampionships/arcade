package net.casual.arcade.commands.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.casual.arcade.commands.type.CustomArgumentType
import net.casual.arcade.commands.type.CustomStringArgumentInfo
import net.casual.arcade.utils.ComponentUtils.literal
import net.minecraft.commands.SharedSuggestionProvider
import java.util.concurrent.CompletableFuture

public open class MappedArgument<T>(
    private val options: Map<String, T>
): CustomArgumentType<T>() {
    init {
        for (name in this.options.keys) {
            if (!CustomStringArgumentInfo.isAllowedWord(name)) {
                throw IllegalArgumentException("Mapped key $name has invalid characters")
            }
        }
    }

    override fun parse(reader: StringReader): T {
        val name = reader.readUnquotedString()
        val enumeration = this.options[name]
        if (enumeration != null) {
            return enumeration
        }
        throw INVALID_ELEMENT.create(name)
    }

    override fun <S> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        return SharedSuggestionProvider.suggest(this.options.keys, builder)
    }

    public companion object {
        private val INVALID_ELEMENT = DynamicCommandExceptionType { "$it is not a valid argument option".literal() }

        @JvmStatic
        public fun <T> mapped(map: Map<String, T>): MappedArgument<T> {
            return MappedArgument(map)
        }

        public inline fun <reified T> getMapped(context: CommandContext<*>, string: String): T {
            return context.getArgument(string, T::class.java)
        }

        @JvmStatic
        public fun <T> getMapped(context: CommandContext<*>, string: String, clazz: Class<T>): T {
            return context.getArgument(string, clazz)
        }
    }
}