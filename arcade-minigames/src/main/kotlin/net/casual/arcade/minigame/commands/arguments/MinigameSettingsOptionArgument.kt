/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.commands.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.casual.arcade.commands.type.ServerSideArgument
import net.minecraft.commands.arguments.SerializableArgument
import net.minecraft.commands.arguments.StringArgument
import net.minecraft.commands.suggestions.SuggestionContext
import net.minecraft.network.chat.Component
import java.util.concurrent.CompletableFuture

public class MinigameSettingsOptionArgument(
    private val minigameKey: String,
    private val settingsNameKey: String
): SerializableArgument.ContextFree<String>, ServerSideArgument<String> {
    override fun parse(reader: StringReader): String {
        return reader.readUnquotedString()
    }

    override fun listSuggestions(
        context: CommandContext<out SuggestionContext>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val minigame = MinigameArgument.getMinigame(context, this.minigameKey)
        val setting = MinigameSettingArgument.getSetting(context, this.settingsNameKey, minigame)
        return SuggestionContext.suggest(setting.options.map { it.id }, builder)
    }

    override fun asSerializableArgument(): SerializableArgument<*> {
        return StringArgument.Word.INSTANCE
    }

    public companion object {
        public val INVALID_SETTING_OPTION: SimpleCommandExceptionType = SimpleCommandExceptionType(Component.translatable("minigame.command.argument.settingOption.invalid"))

        @JvmStatic
        public fun option(minigameKey: String, settingsNameKey: String): ArgumentType<String> {
            return MinigameSettingsOptionArgument(minigameKey, settingsNameKey).toBrigadier()
        }

        @JvmStatic
        public fun getSettingsOption(context: CommandContext<*>, string: String): String {
            return context.getArgument(string, String::class.java)
        }
    }
}