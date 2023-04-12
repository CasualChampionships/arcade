package net.casualuhc.arcade.commands

import com.google.gson.JsonObject
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.synchronization.ArgumentTypeInfo
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import java.util.concurrent.CompletableFuture

class EnumArgument<T: Enum<T>>(
    clazz: Class<T>
): ArgumentType<T> {
    private var enums: HashMap<String, T>

    init {
        val enums = clazz.enumConstants
        this.enums = HashMap(enums.size)
        for (enumeration in enums) {
            this.enums[enumeration.name] = enumeration
        }
    }

    override fun parse(reader: StringReader): T {
        val name = reader.readString()
        val enumeration = this.enums[name]
        if (enumeration != null) {
            return enumeration
        }
        throw INVALID_ELEMENT.create(name)
    }

    override fun <S> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        return SharedSuggestionProvider.suggest(this.enums.keys, builder)
    }

    internal class Info: ArgumentTypeInfo<EnumArgument<*>, Info.Properties> {
        override fun serializeToNetwork(template: Properties, buffer: FriendlyByteBuf) {
            buffer.writeEnum(StringArgumentType.StringType.SINGLE_WORD)
        }

        override fun deserializeFromNetwork(buffer: FriendlyByteBuf): Properties? {
            return null
        }

        override fun unpack(argument: EnumArgument<*>): Properties {
            return Properties()
        }

        override fun serializeToJson(template: Properties, json: JsonObject) {
            json.addProperty("type", "word")
        }

        inner class Properties: ArgumentTypeInfo.Template<EnumArgument<*>> {
            override fun instantiate(context: CommandBuildContext): EnumArgument<*>? {
                return null
            }

            override fun type(): ArgumentTypeInfo<EnumArgument<*>, *> {
                return this@Info
            }
        }
    }

    companion object {
        private val INVALID_ELEMENT = DynamicCommandExceptionType { Component.literal("No such enumeration for $it found") }

        inline fun <reified T: Enum<T>> enumeration(): EnumArgument<T> {
            return EnumArgument(T::class.java)
        }

        @JvmStatic
        fun <T: Enum<T>> enumeration(clazz: Class<T>): EnumArgument<T> {
            return EnumArgument(clazz)
        }

        inline fun <reified T: Enum<T>> getEnumeration(context: CommandContext<CommandSourceStack>, string: String): T {
            return context.getArgument(string, T::class.java)
        }

        @JvmStatic
        fun <T: Enum<T>> getEnumeration(context: CommandContext<CommandSourceStack>, string: String, clazz: Class<T>): T {
            return context.getArgument(string, clazz)
        }
    }
}