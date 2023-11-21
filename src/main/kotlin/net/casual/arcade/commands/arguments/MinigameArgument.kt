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
import net.casual.arcade.minigame.MinigameFactory
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.MinigameUtils.getMinigame
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.resources.ResourceLocation
import java.util.*
import java.util.concurrent.CompletableFuture

public class MinigameArgument: CustomArgumentType(), ArgumentType<MinigameArgument.ParsedMinigame> {
    override fun parse(reader: StringReader): ParsedMinigame {
        val string = reader.readString()
        if (string == "-") {
            return ParsedMinigame()
        }

        val id = ResourceLocation.tryParse(string.replace('+', ':'))
        if (id != null) {
            val minigames = Minigames.get(id)
            if (minigames.size > 1) {
                throw TOO_MANY_MINIGAMES.create()
            } else if (minigames.size == 1) {
                return ParsedMinigame(minigames[0])
            }
        }

        val uuid: UUID
        try {
            uuid = UUID.fromString(string)
        } catch (e: IllegalArgumentException) {
            throw INVALID_MINIGAME.create()
        }
        return ParsedMinigame(Minigames.get(uuid) ?: throw INVALID_MINIGAME.create())
    }

    override fun <S: Any?> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val suggestions = LinkedList<String>()

        for ((id, minigames) in Minigames.allById()) {
            if (minigames.size == 1) {
                suggestions.add("${id.namespace}+${id.path}")
            }
            for (minigame in minigames) {
                suggestions.add(minigame.uuid.toString())
            }
        }

        val source = context.source
        if (source is CommandSourceStack && source.player?.getMinigame() != null) {
            suggestions.add("-")
        }
        return SharedSuggestionProvider.suggest(suggestions, builder)
    }

    public class ParsedMinigame(
        internal val minigame: Minigame<*>? = null
    )

    public companion object {
        public val INVALID_MINIGAME: SimpleCommandExceptionType = SimpleCommandExceptionType("Invalid Minigame UUID".literal())
        public val TOO_MANY_MINIGAMES: SimpleCommandExceptionType = SimpleCommandExceptionType("Too many Minigames with given id".literal())
        public val NOT_PARTICIPATING: SimpleCommandExceptionType = SimpleCommandExceptionType("You are not part of a minigame".literal())

        @JvmStatic
        public fun minigame(): MinigameArgument {
            return MinigameArgument()
        }

        @JvmStatic
        public fun getMinigame(context: CommandContext<*>, string: String): Minigame<*> {
            val parsed = context.getArgument(string, ParsedMinigame::class.java)
            if (parsed.minigame != null) {
                return parsed.minigame
            }

            val source = context.source
            if (source !is CommandSourceStack) {
                throw NOT_PARTICIPATING.create()
            }
            return source.player?.getMinigame() ?: throw NOT_PARTICIPATING.create()
        }
    }

    public class Factory: CustomArgumentType(), ArgumentType<MinigameFactory> {
        override fun parse(reader: StringReader): MinigameFactory {
            val formatted = reader.readString().replace('+', ':')
            val id = ResourceLocation.tryParse(formatted) ?: throw INVALID_FACTORY.create()
            return Minigames.getFactory(id) ?: throw INVALID_FACTORY.create()
        }

        override fun <S: Any?> listSuggestions(
            context: CommandContext<S>,
            builder: SuggestionsBuilder
        ): CompletableFuture<Suggestions> {
            return SharedSuggestionProvider.suggest(
                Minigames.getAllFactoryIds().stream().map { "${it.namespace}+${it.path}" },
                builder
            )
        }

        public companion object {
            public val INVALID_FACTORY: SimpleCommandExceptionType = SimpleCommandExceptionType("Invalid Minigame Factory".literal())

            @JvmStatic
            public fun factory(): Factory {
                return Factory()
            }

            @JvmStatic
            public fun getFactory(context: CommandContext<*>, string: String): MinigameFactory {
                return context.getArgument(string, MinigameFactory::class.java)
            }
        }
    }

    public class PhaseName(private val minigameKey: String): CustomArgumentType(), ArgumentType<String> {
        override fun parse(reader: StringReader): String {
            return reader.readString()
        }

        override fun <S: Any?> listSuggestions(
            context: CommandContext<S>,
            builder: SuggestionsBuilder
        ): CompletableFuture<Suggestions> {
            val minigame = getMinigame(context, this.minigameKey)
            return SharedSuggestionProvider.suggest(minigame.phases.map { it.id }, builder)
        }

        public companion object {
            public val INVALID_PHASE_NAME: SimpleCommandExceptionType = SimpleCommandExceptionType("Invalid Settings Name".literal())

            @JvmStatic
            public fun name(minigameKey: String): PhaseName {
                return PhaseName(minigameKey)
            }

            @JvmStatic
            public fun getPhaseName(context: CommandContext<*>, string: String): String {
                return context.getArgument(string, String::class.java)
            }
        }
    }

    public class SettingsName(private val minigameKey: String): CustomArgumentType(), ArgumentType<String> {
        override fun parse(reader: StringReader): String {
            return reader.readUnquotedString()
        }

        override fun <S: Any?> listSuggestions(
            context: CommandContext<S>,
            builder: SuggestionsBuilder
        ): CompletableFuture<Suggestions> {
            val minigame = getMinigame(context, this.minigameKey)
            return SharedSuggestionProvider.suggest(minigame.getSettings().map { it.name }, builder)
        }

        public companion object {
            public val INVALID_SETTING_NAME: SimpleCommandExceptionType = SimpleCommandExceptionType("Invalid Settings Name".literal())

            @JvmStatic
            public fun name(minigameKey: String): SettingsName {
                return SettingsName(minigameKey)
            }

            @JvmStatic
            public fun getSettingsName(context: CommandContext<*>, string: String): String {
                return context.getArgument(string, String::class.java)
            }
        }
    }

    public class SettingsOption(
        private val minigameKey: String,
        private val settingsNameKey: String
    ): CustomArgumentType(), ArgumentType<String> {
        override fun parse(reader: StringReader): String {
            return reader.readString()
        }

        override fun <S: Any?> listSuggestions(
            context: CommandContext<S>,
            builder: SuggestionsBuilder
        ): CompletableFuture<Suggestions> {
            val minigame = getMinigame(context, this.minigameKey)
            val name = SettingsName.getSettingsName(context, this.settingsNameKey)
            val setting = minigame.getSetting(name) ?: return Suggestions.empty()
            return SharedSuggestionProvider.suggest(setting.getOptions().keys, builder)
        }

        public companion object {
            public val INVALID_SETTING_OPTION: SimpleCommandExceptionType = SimpleCommandExceptionType("Invalid Settings Option".literal())

            @JvmStatic
            public fun option(minigameKey: String, settingsNameKey: String): SettingsOption {
                return SettingsOption(minigameKey, settingsNameKey)
            }

            @JvmStatic
            public fun getSettingsOption(context: CommandContext<*>, string: String): String {
                return context.getArgument(string, String::class.java)
            }
        }
    }

    public class SettingsValue: CustomArgumentType(), ArgumentType<JsonElement> {
        override fun parse(reader: StringReader): JsonElement {
            return CustomisableConfig.GSON.fromJson(reader.readString(), JsonElement::class.java)
        }

        public companion object {
            @JvmStatic
            public fun value(): SettingsValue {
                return SettingsValue()
            }

            @JvmStatic
            public fun getSettingsValue(context: CommandContext<*>, string: String): JsonElement {
                return context.getArgument(string, JsonElement::class.java)
            }
        }
    }
}