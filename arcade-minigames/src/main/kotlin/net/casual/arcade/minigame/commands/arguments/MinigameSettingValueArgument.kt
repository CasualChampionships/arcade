package net.casual.arcade.minigame.commands.arguments

import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.casual.arcade.commands.type.CustomArgumentType
import net.casual.arcade.utils.ComponentUtils.literal
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.ParserUtils
import net.minecraft.core.HolderLookup
import net.minecraft.core.RegistryAccess
import net.minecraft.util.ExtraCodecs

public class MinigameSettingValueArgument: CustomArgumentType<JsonElement>() {
    override fun parse(reader: StringReader): JsonElement {
        try {
            return ParserUtils.parseJson(RegistryAccess.EMPTY, reader, ExtraCodecs.JSON)
        } catch (e: JsonParseException) {
            throw INVALID_SETTING_VALUE.create()
        }
    }

    public companion object {
        public val INVALID_SETTING_VALUE: SimpleCommandExceptionType = SimpleCommandExceptionType("Invalid Settings Value".literal())

        @JvmStatic
        public fun value(): MinigameSettingValueArgument {
            return MinigameSettingValueArgument()
        }

        @JvmStatic
        public fun getSettingsValue(context: CommandContext<*>, string: String): JsonElement {
            return context.getArgument(string, JsonElement::class.java)
        }
    }
}