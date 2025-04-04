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
import net.minecraft.core.Registry
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import java.util.concurrent.CompletableFuture

public class RegistryElementArgument<T>(
    private val key: ResourceKey<Registry<T>>
): CustomArgumentType<ResourceKey<T>>() {
    override fun parse(reader: StringReader): ResourceKey<T> {
        return ResourceKey.create(this.key, ResourceLocation.read(reader))
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
                return SharedSuggestionProvider.suggestResource(registry.keySet(), builder)
            }
        }
        return super.listSuggestions(context, builder)
    }

    override fun getArgumentInfo(): CustomArgumentTypeInfo<*> {
        return CustomArgumentTypeInfo.of(ResourceLocationArgument::class.java)
    }

    public companion object {
        private val UNKNOWN_REGISTRY = DynamicCommandExceptionType { Component.translatable("commands.arguments.registry.unknown", it) }
        private val INVALID_ELEMENT = Dynamic2CommandExceptionType { a, b -> Component.translatable("commands.arguments.registry.element.unknown", a, b) }

        @JvmStatic
        public fun <T> element(key: ResourceKey<Registry<T>>): RegistryElementArgument<T> {
            return RegistryElementArgument(key)
        }

        @JvmStatic
        public fun <T> getElement(context: CommandContext<out SharedSuggestionProvider>, string: String): T {
            @Suppress("UNCHECKED_CAST")
            val key = context.getArgument(string, ResourceKey::class.java) as ResourceKey<T>
            val registry = context.source.registryAccess().lookup(key.registryKey())
            if (registry.isEmpty) {
                throw UNKNOWN_REGISTRY.create(key.registryKey().location())
            }
            val value = registry.get().getOptional(key)
            if (value.isEmpty) {
                throw INVALID_ELEMENT.create(key.location(), key.registryKey().location())
            }
            return value.get()
        }
    }
}