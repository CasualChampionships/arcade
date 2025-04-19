/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.commands.arguments

import com.google.gson.JsonObject
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.MalformedJsonException
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.StringArgumentType.StringType.GREEDY_PHRASE
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.serialization.JsonOps
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.casual.arcade.commands.type.CustomArgumentType
import net.casual.arcade.commands.type.CustomArgumentTypeInfo
import net.casual.arcade.commands.type.CustomStringArgumentInfo
import net.casual.arcade.utils.ParserUtils
import net.casual.arcade.utils.ParserUtils.getPos
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.core.RegistryAccess
import net.minecraft.network.chat.Component
import net.minecraft.util.ExtraCodecs
import java.util.concurrent.CompletableFuture

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

    override fun <S> listSuggestions(
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
        val start = builder.remaining.removeSuffix(typing)
        return SharedSuggestionProvider.suggest(keys.filter { it.startsWith(typing) }.map { start + it }, builder)
    }

    public override fun getArgumentInfo(): CustomArgumentTypeInfo<*> {
        return CustomStringArgumentInfo(GREEDY_PHRASE)
    }

    private fun completions(
        builder: SuggestionsBuilder,
        existing: MutableSet<String>
    ): String? {
        val input = builder.remaining
        val reader = JsonReader(java.io.StringReader(input))
        var typing: String? = null
        try {
            reader.beginObject()

            while (reader.hasNext()) {
                val peek = reader.peek()
                val passed = when (peek) {
                    JsonToken.END_DOCUMENT -> break
                    JsonToken.NAME -> try {
                        val name = reader.nextName()
                        existing.add(name)
                        true
                    } catch (e: MalformedJsonException) {
                        false
                    }
                    else -> {
                        reader.skipValue()
                        true
                    }
                }
                if (!passed) {
                    val pos = reader.getPos()
                    val last = builder.remaining.lastIndexOf('"')
                    if (last != -1) {
                        typing = input.substring(last + 1, pos)
                    }
                    break
                }
            }
        } finally {
            reader.close()
        }
        return typing
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