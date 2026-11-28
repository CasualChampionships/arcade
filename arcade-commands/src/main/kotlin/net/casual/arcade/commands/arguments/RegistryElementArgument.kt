/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.casual.arcade.commands.type.ServerSideArgument
import net.casual.arcade.utils.registries.id
import net.casual.arcade.utils.registries.toIdString
import net.casual.arcade.utils.registries.toKey
import net.minecraft.commands.ParseContext
import net.minecraft.commands.ServerPlayerSuggestionContext
import net.minecraft.commands.arguments.IdentifierArgument
import net.minecraft.commands.arguments.ResourceArgument
import net.minecraft.commands.arguments.SerializableArgument
import net.minecraft.commands.suggestions.SuggestionContext
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import java.util.concurrent.CompletableFuture
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

/**
 * Argument type that allows specifying any registry
 * element for the given registries.
 *
 * You may want to opt for vanilla's [ResourceArgument],
 * this argument type works similar, but allows for specifying
 * multiple registry keys as well as an optional filter
 * to filter results by.
 *
 * @param T The type of the registry.
 * @param registryKeys The set of registry keys to allow.
 * @param filter A filter for selectable registry elements.
 */
public class RegistryElementArgument<T: Any>(
    private val registryKeys: Set<ResourceKey<Registry<T>>>,
    private val filter: (ResourceKey<T>, T) -> Boolean
): ServerSideArgument<Holder.Reference<T>> {
    override fun parse(reader: StringReader, context: ParseContext): Holder.Reference<T> {
        val id = Identifier.read(reader)
        val access = context.holderGetters()
        for (registryKey in this.registryKeys) {
            val resourceKey = id.toKey(registryKey)
            val holder = access.get(resourceKey).getOrNull() ?: continue
            if (this.filter.invoke(holder.key(), holder.value())) {
                return holder
            }
        }

        throw INVALID_ELEMENT.create(id.toString(), this.registryKeys.joinToString(" | ") { key -> key.toIdString() })
    }

    override fun listServerSuggestions(
        context: CommandContext<out ServerPlayerSuggestionContext>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val lookup = context.source.holderLookups()
        val suggestions = this.registryKeys.flatMap { key ->
            lookup.lookup(key).map { lookup ->
                lookup.listElements().filter { holder ->
                    this.filter.invoke(holder.key(), holder.value())
                }.map { entry -> entry.id() }.toList()
            }.getOrElse { emptyList() }
        }
        if (suggestions.isNotEmpty()) {
            return SuggestionContext.suggestResource(suggestions, builder)
        }
        return super.listSuggestions(context, builder)
    }

    override fun asSerializableArgument(): SerializableArgument<*> {
        return IdentifierArgument.INSTANCE
    }

    public companion object {
        private val INVALID_ELEMENT = Dynamic2CommandExceptionType { a, b -> Component.translatable("commands.arguments.registry.element.unknown", a, b) }

        @JvmStatic
        @JvmOverloads
        public fun <T: Any> element(
            key: ResourceKey<Registry<T>>,
            filter: (ResourceKey<T>, T) -> Boolean = { _, _ -> true }
        ): ArgumentType<Holder.Reference<T>> {
            return RegistryElementArgument(setOf(key), filter).toBrigadier()
        }

        @JvmStatic
        @JvmOverloads
        public fun <T: Any> element(
            keys: Set<ResourceKey<Registry<T>>>,
            filter: (ResourceKey<T>, T) -> Boolean = { _, _ -> true }
        ): ArgumentType<Holder.Reference<T>> {
            return RegistryElementArgument(keys, filter).toBrigadier()
        }

        public fun <T: Any> element(
            vararg keys: ResourceKey<Registry<T>>,
            filter: (ResourceKey<T>, T) -> Boolean = { _, _ -> true }
        ): ArgumentType<Holder.Reference<T>> {
            return RegistryElementArgument(keys.toSet(), filter).toBrigadier()
        }

        @JvmStatic
        public fun <T: Any> getElement(context: CommandContext<*>, string: String): T {
            return this.getHolder<T>(context, string).value()
        }

        @JvmStatic
        public fun <T: Any> getHolder(context: CommandContext<*>, string: String): Holder.Reference<T> {
            @Suppress("UNCHECKED_CAST")
            return context.getArgument(string, Holder.Reference::class.java) as Holder.Reference<T>
        }
    }
}