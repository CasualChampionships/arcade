package net.casual.arcade.commands.type

import com.google.gson.JsonObject
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.arguments.StringArgumentType.StringType
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.synchronization.ArgumentTypeInfo
import net.minecraft.network.FriendlyByteBuf

public class CustomStringArgumentInfo(
    private val type: StringType
): ArgumentTypeInfo<ArgumentType<Any>, CustomStringArgumentInfo.Properties>, CustomArgumentTypeInfo {
    override fun serializeToNetwork(template: Properties, buffer: FriendlyByteBuf) {
        buffer.writeEnum(this.type)
    }

    override fun deserializeFromNetwork(buffer: FriendlyByteBuf): Properties? {
        return null
    }

    override fun unpack(argument: ArgumentType<Any>): Properties {
        return Properties()
    }

    override fun serializeToJson(template: Properties, json: JsonObject) {
        json.addProperty("type", when (this.type) {
            StringType.QUOTABLE_PHRASE -> "phrase"
            StringType.GREEDY_PHRASE -> "greedy"
            else -> "word"
        })
    }

    override fun getFacadeType(): Class<out ArgumentType<*>> {
        return StringArgumentType::class.java
    }

    public inner class Properties: ArgumentTypeInfo.Template<ArgumentType<Any>> {
        override fun instantiate(context: CommandBuildContext): ArgumentType<Any>? {
            return null
        }

        override fun type(): ArgumentTypeInfo<ArgumentType<Any>, *> {
            return this@CustomStringArgumentInfo
        }
    }

    public companion object {
        public fun isAllowedWord(word: String): Boolean {
            return word.all { StringReader.isAllowedInUnquotedString(it) }
        }
    }
}