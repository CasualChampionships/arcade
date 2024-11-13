package net.casual.arcade.minigame.commands.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.casual.arcade.commands.type.CustomArgumentType
import net.casual.arcade.utils.ComponentUtils.literal
import net.minecraft.commands.SharedSuggestionProvider
import java.util.concurrent.CompletableFuture

public class MinigameSettingsOptionArgument(
    private val minigameKey: String,
    private val settingsNameKey: String
): CustomArgumentType<String>() {
    override fun parse(reader: StringReader): String {
        return reader.readString()
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val minigame = MinigameArgument.getMinigame(context, this.minigameKey)
        val setting = MinigameSettingArgument.getSetting(context, this.settingsNameKey, minigame)
        return SharedSuggestionProvider.suggest(setting.getOptions().keys, builder)
    }

    public companion object {
        public val INVALID_SETTING_OPTION: SimpleCommandExceptionType = SimpleCommandExceptionType("Invalid Settings Option".literal())

        @JvmStatic
        public fun option(minigameKey: String, settingsNameKey: String): MinigameSettingsOptionArgument {
            return MinigameSettingsOptionArgument(minigameKey, settingsNameKey)
        }

        @JvmStatic
        public fun getSettingsOption(context: CommandContext<*>, string: String): String {
            return context.getArgument(string, String::class.java)
        }
    }
}