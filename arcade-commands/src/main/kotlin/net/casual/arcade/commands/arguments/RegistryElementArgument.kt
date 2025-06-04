/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.casual.arcade.commands.type.CustomArgumentType
import net.casual.arcade.commands.type.CustomArgumentTypeInfo
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.core.RegistryAccess
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import java.util.concurrent.CompletableFuture

public class RegistryElementArgument<T>(
    private val key: ResourceKey<Registry<T>>,
    private val filter: (ResourceKey<T>, T) -> Boolean
): CustomArgumentType<RegistryElementArgument.FilterableResourceKey<T>>() {
    override fun parse(reader: StringReader): FilterableResourceKey<T> {
        return FilterableResourceKey(
            ResourceKey.create(this.key, ResourceLocation.read(reader)),
            this.filter
        )
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val source = context.source
        if (source is SharedSuggestionProvider) {
            val optional = source.registryAccess().lookup(this.key)
            if (optional.isPresent) {
                val registry = optional.get()
                return SharedSuggestionProvider.suggestResource(
                    registry.entrySet().filter { (key, value) ->
                        this.filter.invoke(key, value)
                    }.map { it.key.location() }, builder
                )
            }
        }
        return super.listSuggestions(context, builder)
    }

    override fun getArgumentInfo(): CustomArgumentTypeInfo<*> {
        return CustomArgumentTypeInfo.of(ResourceLocationArgument::class.java)
    }

    public data class FilterableResourceKey<T>(
        private val key: ResourceKey<T>,
        private val filter: (ResourceKey<T>, T) -> Boolean
    ) {
        public fun getElement(access: RegistryAccess): T {
            return this.getHolder(access).value()
        }

        public fun getHolder(access: RegistryAccess): Holder.Reference<T> {
            val registry = access.lookup(this.key.registryKey())
            if (registry.isEmpty) {
                throw UNKNOWN_REGISTRY.create(this.key.registryKey().location())
            }
            val holder = registry.get().get(this.key)
            if (holder.isEmpty || !this.filter.invoke(this.key, holder.get().value())) {
                throw INVALID_ELEMENT.create(this.key.location(), this.key.registryKey().location())
            }
            return holder.get()
        }
    }

    public companion object {
        private val UNKNOWN_REGISTRY = DynamicCommandExceptionType { Component.translatable("commands.arguments.registry.unknown", it) }
        private val INVALID_ELEMENT = Dynamic2CommandExceptionType { a, b -> Component.translatable("commands.arguments.registry.element.unknown", a, b) }

        @JvmStatic
        @JvmOverloads
        public fun <T> element(
            key: ResourceKey<Registry<T>>,
            filter: (ResourceKey<T>, T) -> Boolean = { _, _ -> true }
        ): RegistryElementArgument<T> {
            return RegistryElementArgument(key, filter)
        }

        @JvmStatic
        public fun <T> getElement(context: CommandContext<out SharedSuggestionProvider>, string: String): T {
            return this.getHolder<T>(context, string).value()
        }

        @JvmStatic
        public fun <T> getHolder(context: CommandContext<out SharedSuggestionProvider>, string: String): Holder.Reference<T> {
            @Suppress("UNCHECKED_CAST")
            val filterable = context.getArgument(string, FilterableResourceKey::class.java) as FilterableResourceKey<T>
            return filterable.getHolder(context.source.registryAccess())
        }
    }
}