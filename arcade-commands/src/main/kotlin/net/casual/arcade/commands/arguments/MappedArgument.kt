/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.casual.arcade.commands.type.ServerSideArgument
import net.minecraft.commands.arguments.PatternStringArgument
import net.minecraft.commands.arguments.SerializableArgument
import net.minecraft.commands.arguments.StringArgument
import net.minecraft.commands.suggestions.SuggestionContext
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import java.util.Optional
import java.util.concurrent.CompletableFuture

/**
 * Argument type for any values of [T], provided
 * as [options] with a [String] key.
 *
 * Only valid keys will be permitted and this
 * argument will autocomplete valid keys.
 *
 * @param T The type of the argument value.
 * @param options The valid argument options.
 */
public open class MappedArgument<T: Any>(
    private val options: Map<String, T>
): SerializableArgument.ContextFree<T>, ServerSideArgument<T> {
    private val client: SerializableArgument.ContextFree<*>
    private val quoted: Boolean

    init {
        val regex = this.options.keys.joinToString("|") { key -> Regex.escape(key) }
        if (regex.length >= FriendlyByteBuf.MAX_STRING_LENGTH) {
            this.client = StringArgument.Quoted.INSTANCE
            this.quoted = true
        } else {
            this.client = PatternStringArgument(
                regex.toPattern(), Optional.of(Component.translatable("commands.arguments.mapped.invalid"))
            )
            this.quoted = false
        }
    }

    override fun parse(reader: StringReader): T {
        val name = this.client.parse(reader)
        val enumeration = this.options[name]
        if (enumeration != null) {
            return enumeration
        }
        throw INVALID_ELEMENT.create(name)
    }

    override fun listSuggestions(
        context: CommandContext<out SuggestionContext>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        if (this.quoted) {
            SuggestionContext.suggest(this.options.keys.map { key -> "\"$key\"" }, builder)
        }
        return SuggestionContext.suggest(this.options.keys, builder)
    }

    override fun asSerializableArgument(): SerializableArgument<*> {
        return this.client
    }

    public companion object {
        private val INVALID_ELEMENT = DynamicCommandExceptionType { Component.translatable("commands.arguments.mapped.invalid", it) }

        @JvmStatic
        public fun <T: Any> mapped(map: Map<String, T>): MappedArgument<T> {
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