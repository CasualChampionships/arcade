/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.commands.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.casual.arcade.commands.player
import net.casual.arcade.commands.type.ServerSideArgument
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.minigame.utils.MinigameUtils.getMinigame
import net.casual.arcade.minigame.utils.MinigameUtils.getMinigames
import net.casual.arcade.utils.IdentifierUtils
import net.casual.arcade.utils.string.UUID_REGEX
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.ServerPlayerSuggestionContext
import net.minecraft.commands.arguments.PatternStringArgument
import net.minecraft.commands.arguments.SerializableArgument
import net.minecraft.commands.arguments.UuidArgument
import net.minecraft.commands.suggestions.SuggestionContext
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import java.util.Optional
import java.util.concurrent.CompletableFuture

public object MinigameArgument: SerializableArgument.ContextFree<MinigameArgument.Result>, ServerSideArgument<MinigameArgument.Result> {
    public val INVALID_MINIGAME: SimpleCommandExceptionType = SimpleCommandExceptionType(Component.translatable("minigame.command.argument.minigame.invalidUUID"))
    public val TOO_MANY_MINIGAMES: SimpleCommandExceptionType = SimpleCommandExceptionType(Component.translatable("minigame.command.argument.minigame.resolutionFailed"))
    public val NOT_PARTICIPATING: SimpleCommandExceptionType = SimpleCommandExceptionType(Component.translatable("minigame.command.argument.minigame.noContext"))

    private val uuid = UuidArgument.uuid()

    override fun parse(reader: StringReader): Result {
        val start = reader.cursor
        if (reader.readString() == "-") {
            return Result.Contextual
        } else {
            reader.cursor = start
        }

        try {
            val uuid = this.uuid.parse(reader)
            val minigame = Minigames.get(uuid)
            if (minigame != null) {
                return Result.Reference(minigame)
            }
        } catch (_: CommandSyntaxException) {

        }

        val id = Identifier.read(reader)
        val minigames = Minigames.get(id)
        if (minigames.size > 1) {
            throw TOO_MANY_MINIGAMES.create()
        } else if (minigames.isEmpty()) {
            throw INVALID_MINIGAME.create()
        }
        return Result.Reference(minigames[0])
    }

    override fun listServerSuggestions(
        context: CommandContext<out ServerPlayerSuggestionContext>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val suggestions = ArrayList<String>()
        for ((id, minigames) in Minigames.allById()) {
            if (minigames.size == 1) {
                suggestions.add(id.toString())
                continue
            }
            suggestions.addAll(minigames.map { it.uuid.toString() })
        }

        if (context.source.player.getMinigame() != null) {
            suggestions.add("-")
        }
        return SuggestionContext.suggest(suggestions, builder)
    }

    override fun asSerializableArgument(): SerializableArgument<*> {
        return PatternStringArgument(
            "${IdentifierUtils.REGEX}|^-$|$UUID_REGEX".toPattern(),
            Optional.of(Component.translatable("minigame.command.argument.minigame.invalidUUID"))
        )
    }

    @JvmStatic
    public fun minigame(): ArgumentType<Result> {
        return this.toBrigadier()
    }

    @JvmStatic
    public fun getMinigame(context: CommandContext<*>, string: String): Minigame {
        val result = context.getArgument(string, Result::class.java)
        return result.getMinigame(context)
    }

    public sealed class Result {
        public abstract fun getMinigame(context: CommandContext<*>): Minigame

        internal class Reference(private val minigame: Minigame): Result() {
            override fun getMinigame(context: CommandContext<*>): Minigame {
                return this.minigame
            }
        }

        internal object Contextual: Result() {
            override fun getMinigame(context: CommandContext<*>): Minigame {
                val source = context.source
                if (source is CommandSourceStack) {
                    if (source.isPlayer) {
                        val minigame = source.player?.getMinigame()
                        return minigame ?: throw NOT_PARTICIPATING.create()
                    }
                    val minigames = source.level.getMinigames()
                    if (minigames.size == 1) {
                        return minigames.first()
                    }
                }
                throw NOT_PARTICIPATING.create()
            }
        }
    }
}

