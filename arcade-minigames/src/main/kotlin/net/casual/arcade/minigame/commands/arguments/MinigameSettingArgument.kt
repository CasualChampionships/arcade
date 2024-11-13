package net.casual.arcade.minigame.commands.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.casual.arcade.commands.type.CustomArgumentType
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.settings.GameSetting
import net.casual.arcade.utils.ComponentUtils.literal
import net.minecraft.commands.SharedSuggestionProvider
import java.util.concurrent.CompletableFuture

public class MinigameSettingArgument(private val minigameKey: String): CustomArgumentType<String>() {
    override fun parse(reader: StringReader): String {
        return reader.readUnquotedString()
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val minigame = MinigameArgument.getMinigame(context, this.minigameKey)
        return SharedSuggestionProvider.suggest(minigame.settings.all().map { it.name }, builder)
    }

    public companion object {
        public val INVALID_SETTING_NAME: SimpleCommandExceptionType = SimpleCommandExceptionType("Invalid Settings Name".literal())

        @JvmStatic
        public fun setting(minigameKey: String): MinigameSettingArgument {
            return MinigameSettingArgument(minigameKey)
        }

        @JvmStatic
        public fun getSetting(
            context: CommandContext<*>,
            string: String,
            minigame: Minigame
        ): GameSetting<*> {
            val name = context.getArgument(string, String::class.java)
            return minigame.settings.get(name) ?: throw INVALID_SETTING_NAME.create()
        }
    }
}