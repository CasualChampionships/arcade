package net.casual.arcade.commands.arguments

import com.google.gson.JsonElement
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.casual.arcade.commands.type.CustomArgumentType
import net.casual.arcade.config.CustomisableConfig
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import java.util.*
import java.util.concurrent.CompletableFuture

class MinigameArgument: CustomArgumentType(), ArgumentType<Minigame<*>> {
    override fun parse(reader: StringReader): Minigame<*> {
        val uuid: UUID
        try {
            uuid = UUID.fromString(reader.readString())
        } catch (e: IllegalArgumentException) {
            throw INVALID_MINIGAME.create()
        }
        return Minigames.get(uuid) ?: throw INVALID_MINIGAME.create()
    }


    override fun <S: Any?> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        return SharedSuggestionProvider.suggest(Minigames.all().stream().map { it.uuid.toString() }, builder)
    }

    companion object {
        val INVALID_MINIGAME = SimpleCommandExceptionType(Component.literal("Invalid Minigame UUID"))

        @JvmStatic
        fun minigame(): MinigameArgument {
            return MinigameArgument()
        }

        @JvmStatic
        fun getMinigame(context: CommandContext<*>, string: String): Minigame<*> {
            return context.getArgument(string, Minigame::class.java)
        }
    }

    class PhaseName(private val minigameKey: String): CustomArgumentType(), ArgumentType<String> {
        override fun parse(reader: StringReader): String {
            return reader.readString()
        }

        override fun <S : Any?> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
            val minigame = getMinigame(context, this.minigameKey)
            return SharedSuggestionProvider.suggest(minigame.phases.map { it.id }, builder)
        }

        companion object {
            val INVALID_PHASE_NAME = SimpleCommandExceptionType(Component.literal("Invalid Settings Name"))

            @JvmStatic
            fun name(minigameKey: String): SettingsName {
                return SettingsName(minigameKey)
            }

            @JvmStatic
            fun getPhaseName(context: CommandContext<*>, string: String): String {
                return context.getArgument(string, String::class.java)
            }
        }
    }

    class SettingsName(private val minigameKey: String): CustomArgumentType(), ArgumentType<String> {
        override fun parse(reader: StringReader): String {
            return reader.readUnquotedString()
        }

        override fun <S: Any?> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
            val minigame = getMinigame(context, this.minigameKey)
            return SharedSuggestionProvider.suggest(minigame.getSettings().map { it.name }, builder)
        }

        companion object {
            val INVALID_SETTING_NAME = SimpleCommandExceptionType(Component.literal("Invalid Settings Name"))

            @JvmStatic
            fun name(minigameKey: String): SettingsName {
                return SettingsName(minigameKey)
            }

            @JvmStatic
            fun getSettingsName(context: CommandContext<*>, string: String): String {
                return context.getArgument(string, String::class.java)
            }
        }
    }

    class SettingsOption(
        private val minigameKey: String,
        private val settingsNameKey: String
    ): CustomArgumentType(), ArgumentType<String> {
        override fun parse(reader: StringReader): String {
            return reader.readString()
        }

        override fun <S: Any?> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
            val minigame = getMinigame(context, this.minigameKey)
            val name = SettingsName.getSettingsName(context, this.settingsNameKey)
            val setting = minigame.getSetting(name) ?: return Suggestions.empty()
            return SharedSuggestionProvider.suggest(setting.getOptions().keys, builder)
        }

        companion object {
            val INVALID_SETTING_OPTION = SimpleCommandExceptionType(Component.literal("Invalid Settings Option"))

            @JvmStatic
            fun option(minigameKey: String, settingsNameKey: String): SettingsOption {
                return SettingsOption(minigameKey, settingsNameKey)
            }

            @JvmStatic
            fun getSettingsOption(context: CommandContext<*>, string: String): String {
                return context.getArgument(string, String::class.java)
            }
        }
    }

    class SettingsValue: CustomArgumentType(), ArgumentType<JsonElement> {
        override fun parse(reader: StringReader): JsonElement {
            return CustomisableConfig.GSON.fromJson(reader.readString(), JsonElement::class.java)
        }

        companion object {
            @JvmStatic
            fun value(): SettingsValue {
                return SettingsValue()
            }

            @JvmStatic
            fun getSettingsValue(context: CommandContext<*>, string: String): JsonElement {
                return context.getArgument(string, JsonElement::class.java)
            }
        }
    }
}