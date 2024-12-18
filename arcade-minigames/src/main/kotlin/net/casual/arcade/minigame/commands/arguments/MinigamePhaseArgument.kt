/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.commands.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.casual.arcade.commands.type.CustomArgumentType
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.phase.Phase
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import java.util.concurrent.CompletableFuture

public class MinigamePhaseArgument(private val minigameKey: String): CustomArgumentType<String>() {
    override fun parse(reader: StringReader): String {
        return reader.readString()
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val minigame = MinigameArgument.getMinigame(context, this.minigameKey)
        return SharedSuggestionProvider.suggest(minigame.phases.map { it.id }, builder)
    }

    public companion object {
        public val INVALID_PHASE_NAME: SimpleCommandExceptionType = SimpleCommandExceptionType(Component.translatable("minigame.command.argument.phase.invalid"))

        @JvmStatic
        public fun name(minigameKey: String): MinigamePhaseArgument {
            return MinigamePhaseArgument(minigameKey)
        }

        @JvmStatic
        public fun getPhase(
            context: CommandContext<*>,
            string: String,
            minigame: Minigame
        ): Phase<Minigame> {
            val phaseName = context.getArgument(string, String::class.java)
            return minigame.getPhase(phaseName) ?: throw INVALID_PHASE_NAME.create()
        }
    }
}