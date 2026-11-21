/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.commands.arguments

import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.casual.arcade.commands.type.ServerSideArgument
import net.casual.arcade.utils.JsonUtils
import net.minecraft.commands.arguments.SerializableArgument
import net.minecraft.commands.arguments.StringArgument
import net.minecraft.core.RegistryAccess
import net.minecraft.network.chat.Component
import net.minecraft.util.ExtraCodecs

public object MinigameSettingValueArgument: SerializableArgument.ContextFree<JsonElement>, ServerSideArgument<JsonElement> {
    public val INVALID_SETTING_VALUE: SimpleCommandExceptionType = SimpleCommandExceptionType(Component.translatable("minigame.command.argument.settingValue.invalid"))

    override fun parse(reader: StringReader): JsonElement {
        try {
            return JsonUtils.parseJson(RegistryAccess.EMPTY, reader, ExtraCodecs.JSON)
        } catch (_: JsonParseException) {
            throw INVALID_SETTING_VALUE.create()
        }
    }

    override fun asSerializableArgument(): SerializableArgument<*> {
        return StringArgument.Greedy.INSTANCE
    }

    @JvmStatic
    public fun value(): ArgumentType<JsonElement> {
        return this.toBrigadier()
    }

    @JvmStatic
    public fun getSettingsValue(context: CommandContext<*>, string: String): JsonElement {
        return context.getArgument(string, JsonElement::class.java)
    }
}