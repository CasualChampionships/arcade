package net.casual.arcade.minigame.commands.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.casual.arcade.commands.type.CustomArgumentType
import net.casual.arcade.commands.type.CustomArgumentTypeInfo
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.minigame.utils.MinigameUtils.getMinigame
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.commands.arguments.UuidArgument
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import java.util.concurrent.CompletableFuture

public class MinigameArgument: CustomArgumentType<MinigameArgument.Result>() {
    private val uuid = UuidArgument.uuid()

    override fun parse(reader: StringReader): Result {
        val start = reader.cursor
        if (reader.readString() == "-") {
            return Result(null)
        } else {
            reader.cursor = start
        }

        try {
            val uuid = this.uuid.parse(reader)
            val minigame = Minigames.get(uuid)
            if (minigame != null) {
                return Result(minigame)
            }
        } catch (_: CommandSyntaxException) {

        }

        val id = ResourceLocation.read(reader)
        val minigames = Minigames.get(id)
        if (minigames.size > 1) {
            throw TOO_MANY_MINIGAMES.create()
        } else if (minigames.isEmpty()) {
            throw INVALID_MINIGAME.create()
        }
        return Result(minigames[0])
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
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

        val source = context.source
        if (source is CommandSourceStack && source.player?.getMinigame() != null) {
            suggestions.add("-")
        }

        return SharedSuggestionProvider.suggest(suggestions, builder)
    }

    override fun getArgumentInfo(): CustomArgumentTypeInfo<*> {
        return CustomArgumentTypeInfo.of(ResourceLocationArgument::class.java)
    }

    public class Result(private val minigame: Minigame?) {
        public fun getMinigame(context: CommandContext<*>): Minigame {
            if (this.minigame != null) {
                return this.minigame
            }
            val source = context.source
            if (source is CommandSourceStack && source.isPlayer) {
                val minigame = source.player?.getMinigame()
                return minigame ?: throw NOT_PARTICIPATING.create()
            }
            throw NOT_PARTICIPATING.create()
        }
    }

    public companion object {
        public val INVALID_MINIGAME: SimpleCommandExceptionType = SimpleCommandExceptionType(Component.translatable("minigame.command.argument.minigame.invalidUUID"))
        public val TOO_MANY_MINIGAMES: SimpleCommandExceptionType = SimpleCommandExceptionType(Component.translatable("minigame.command.argument.minigame.resolutionFailed"))
        public val NOT_PARTICIPATING: SimpleCommandExceptionType = SimpleCommandExceptionType(Component.translatable("minigame.command.argument.minigame.noContext"))

        @JvmStatic
        public fun minigame(): MinigameArgument {
            return MinigameArgument()
        }

        @JvmStatic
        public fun getMinigame(context: CommandContext<*>, string: String): Minigame {
            val result = context.getArgument(string, Result::class.java)
            return result.getMinigame(context)
        }
    }
}

