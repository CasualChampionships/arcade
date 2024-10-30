package net.casual.arcade.minigame.commands.arguments

import com.google.gson.JsonObject
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.StringArgumentType.StringType.GREEDY_PHRASE
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.MapCodec
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.casual.arcade.commands.type.CustomArgumentType
import net.casual.arcade.commands.type.CustomArgumentTypeInfo
import net.casual.arcade.commands.type.CustomStringArgumentInfo
import net.casual.arcade.minigame.mixins.ParserUtilsAccessor
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.minecraft.commands.ParserUtils
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.core.RegistryAccess
import net.minecraft.network.chat.Component
import net.minecraft.util.ExtraCodecs
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.jvm.optionals.getOrNull

public class MinigameFactoryDataArgument(
    private val factoryCodecKey: String
): CustomArgumentType<JsonObject>() {
    override fun parse(reader: StringReader): JsonObject {
        val json = ParserUtils.parseJson(RegistryAccess.EMPTY, reader, ExtraCodecs.JSON)
        if (json !is JsonObject) {
            throw INVALID_FACTORY_DATA.create()
        }
        return json
    }

    override fun <S: Any?> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val codec = MinigameFactoryCodecArgument.getCodec(context, this.factoryCodecKey)
        val keys = codec.keys(JsonOps.INSTANCE).map { it.asString }.toList().toMutableSet()
        val existing = ObjectOpenHashSet<String>()
        val typing = this.completions(builder, existing)
        keys.removeAll(existing)
        if (typing == null) {
            return Suggestions.empty()
        }
        val beginning = typing.beginning()
        return SharedSuggestionProvider.suggest(keys.filter { it.startsWith(beginning) }, builder)
    }

    public override fun getArgumentInfo(): CustomArgumentTypeInfo<*> {
        return CustomStringArgumentInfo(GREEDY_PHRASE)
    }

    private fun completions(
        builder: SuggestionsBuilder,
        existing: MutableSet<String>
    ): StringWithPosition? {
        val reader = JsonReader(java.io.StringReader(builder.input))
        var typing: Optional<StringWithPosition>? = null
        try {
            reader.beginObject()

            while (reader.hasNext()) {
                val peek = reader.peek()
                val key = when (peek) {
                    JsonToken.END_DOCUMENT -> break
                    JsonToken.NAME -> {
                        val name = reader.nextName()
                        existing.add(name)
                        name
                    }
                    else -> {
                        reader.skipValue()
                        null
                    }
                }
                if (typing == null) {
                    val pos = ParserUtilsAccessor.getReaderPos(reader)
                    if (pos < builder.start) {
                        continue
                    }
                    typing = if (key != null) {
                        Optional.of(StringWithPosition(key, key.length + 1 - (pos - builder.start)))
                    } else {
                        Optional.empty<StringWithPosition>()
                    }
                }
            }
        } catch (_: Exception) {

        } finally {
            reader.close()
        }
        return typing?.getOrNull()
    }

    private data class StringWithPosition(val string: String, val pos: Int) {
        fun beginning(): String {
            return this.string.substring(0, this.pos + 1)
        }
    }

    public companion object {
        public val INVALID_FACTORY_DATA: SimpleCommandExceptionType = SimpleCommandExceptionType(
            Component.translatable("minigame.command.factoryData.invalid")
        )

        @JvmStatic
        public fun data(factoryCodecKey: String): MinigameFactoryDataArgument {
            return MinigameFactoryDataArgument(factoryCodecKey)
        }

        @JvmStatic
        public fun getData(context: CommandContext<*>, string: String): JsonObject {
            return context.getArgument(string, JsonObject::class.java)
        }
    }
}