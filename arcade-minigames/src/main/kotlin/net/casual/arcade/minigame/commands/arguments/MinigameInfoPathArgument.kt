package net.casual.arcade.minigame.commands.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.casual.arcade.commands.type.CustomArgumentType
import net.minecraft.commands.SharedSuggestionProvider
import java.util.concurrent.CompletableFuture

public class MinigameInfoPathArgument(
    private val minigameKey: String
): CustomArgumentType<String>() {
    override fun parse(reader: StringReader): String {
        return reader.readString()
    }

    override fun <S: Any?> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val minigame = MinigameArgument.getMinigame(context, this.minigameKey)
        return SharedSuggestionProvider.suggest(minigame.getDebugInfo().keySet(), builder)
    }

    public companion object {
        @JvmStatic
        public fun path(minigameKey: String): MinigameInfoPathArgument {
            return MinigameInfoPathArgument(minigameKey)
        }

        @JvmStatic
        public fun getPath(context: CommandContext<*>, string: String): String {
            return context.getArgument(string, String::class.java)
        }
    }
}