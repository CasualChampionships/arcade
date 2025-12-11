/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.casual.arcade.commands.type.CustomArgumentType
import net.casual.arcade.commands.type.CustomArgumentTypeInfo
import net.casual.arcade.utils.toIdString
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.IdentifierArgument
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.core.RegistryAccess
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import java.util.concurrent.CompletableFuture
import kotlin.jvm.optionals.getOrNull

public class RegistryElementArgument<T: Any>(
    private val keys: Set<ResourceKey<Registry<T>>>,
    private val filter: (ResourceKey<T>, T) -> Boolean
): CustomArgumentType<RegistryElementArgument.FilterableResourceKey<T>>() {
    override fun parse(reader: StringReader): FilterableResourceKey<T> {
        return FilterableResourceKey(
            this.keys, Identifier.read(reader), this.filter
        )
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val source = context.source
        if (source is SharedSuggestionProvider) {
            val suggestions = this.keys.flatMap { key ->
                source.registryAccess().lookup(key).map { registry ->
                    registry.entrySet().filter { (key, value) ->
                        this.filter.invoke(key, value)
                    }.map { entry -> entry.key.identifier() }
                }.orElseGet(::listOf)
            }
            if (suggestions.isNotEmpty()) {
                return SharedSuggestionProvider.suggestResource(suggestions, builder)
            }
        }
        return super.listSuggestions(context, builder)
    }

    override fun getArgumentInfo(): CustomArgumentTypeInfo<*> {
        return CustomArgumentTypeInfo.of(IdentifierArgument::class.java)
    }

    public data class FilterableResourceKey<T: Any>(
        private val keys: Set<ResourceKey<Registry<T>>>,
        private val id: Identifier,
        private val filter: (ResourceKey<T>, T) -> Boolean
    ) {
        public fun getElement(access: RegistryAccess): T {
            return this.getHolder(access).value()
        }

        public fun getHolder(access: RegistryAccess): Holder.Reference<T> {
            for (key in this.keys) {
                val registry = access.lookup(key).getOrNull() ?: continue
                val holder = registry.get(this.id).getOrNull() ?: continue
                if (this.filter.invoke(holder.key(), holder.value())) {
                    return holder
                }
            }

            throw INVALID_ELEMENT.create(this.id.toString(), this.keys.joinToString(" | ") { key -> key.toIdString() })
        }
    }

    public companion object {
        private val INVALID_ELEMENT = Dynamic2CommandExceptionType { a, b -> Component.translatable("commands.arguments.registry.element.unknown", a, b) }

        @JvmStatic
        @JvmOverloads
        public fun <T: Any> element(
            key: ResourceKey<Registry<T>>,
            filter: (ResourceKey<T>, T) -> Boolean = { _, _ -> true }
        ): RegistryElementArgument<T> {
            return RegistryElementArgument(setOf(key), filter)
        }

        @JvmStatic
        @JvmOverloads
        public fun <T: Any> element(
            keys: Set<ResourceKey<Registry<T>>>,
            filter: (ResourceKey<T>, T) -> Boolean = { _, _ -> true }
        ): RegistryElementArgument<T> {
            return RegistryElementArgument(keys, filter)
        }

        public fun <T: Any> element(
            vararg keys: ResourceKey<Registry<T>>,
            filter: (ResourceKey<T>, T) -> Boolean = { _, _ -> true }
        ): RegistryElementArgument<T> {
            return RegistryElementArgument(keys.toSet(), filter)
        }

        @JvmStatic
        public fun <T: Any> getElement(context: CommandContext<out SharedSuggestionProvider>, string: String): T {
            return this.getHolder<T>(context, string).value()
        }

        @JvmStatic
        public fun <T: Any> getHolder(context: CommandContext<out SharedSuggestionProvider>, string: String): Holder.Reference<T> {
            @Suppress("UNCHECKED_CAST")
            val filterable = context.getArgument(string, FilterableResourceKey::class.java) as FilterableResourceKey<T>
            return filterable.getHolder(context.source.registryAccess())
        }
    }
}