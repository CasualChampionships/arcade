/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.type

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.commands.ServerPlayerSuggestionContext
import net.minecraft.commands.arguments.SerializableArgument
import net.minecraft.commands.suggestions.SuggestionContext
import org.jetbrains.annotations.ApiStatus.NonExtendable
import java.util.concurrent.CompletableFuture

public interface ServerSideArgument<T: Any>: SerializableArgument<T> {
    public fun asSerializableArgument(): SerializableArgument<*>

    public fun listServerSuggestions(
        context: CommandContext<out ServerPlayerSuggestionContext>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return Suggestions.empty()
    }

    // The type may *not* be context free, we just cast it so we can use this
    // with SerializableArgument.ContextFree without needing another class.
    @NonExtendable
    override fun type(): SerializableArgument.Type<out SerializableArgument.ContextFree<T>> {
        throw UnsupportedOperationException("ServerSideArguments don't have explicit types, instead they mimic existing ones")
    }

    @NonExtendable
    override fun listSuggestions(
        context: CommandContext<out SuggestionContext>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        if (context.source !is ServerPlayerSuggestionContext) {
            ArcadeUtils.logger.warn("ServerSideArgument ${this::class.simpleName} tried to call listSuggestions on the client-side!?")
            return Suggestions.empty()
        }
        @Suppress("UNCHECKED_CAST")
        return this.listServerSuggestions(context as CommandContext<ServerPlayerSuggestionContext>, builder)
    }
}