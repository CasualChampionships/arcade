package net.casualuhc.arcade.commands

import com.google.gson.JsonObject
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.synchronization.ArgumentTypeInfo
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.FriendlyByteBuf

class WordArgumentInfo: ArgumentTypeInfo<ArgumentType<Any>, WordArgumentInfo.Properties>, CustomArgumentTypeInfo {
    override fun serializeToNetwork(template: Properties, buffer: FriendlyByteBuf) {
        buffer.writeEnum(StringArgumentType.StringType.SINGLE_WORD)
    }

    override fun deserializeFromNetwork(buffer: FriendlyByteBuf): Properties? {
        return null
    }

    override fun unpack(argument: ArgumentType<Any>): Properties {
        return Properties()
    }

    override fun serializeToJson(template: Properties, json: JsonObject) {
        json.addProperty("type", "word")
    }

    override fun getFacadeId(existing: Map<Class<*>, ArgumentTypeInfo<*, *>>): Int {
        return BuiltInRegistries.COMMAND_ARGUMENT_TYPE.getId(existing[StringArgumentType::class.java])
    }

    inner class Properties: ArgumentTypeInfo.Template<ArgumentType<Any>> {
        override fun instantiate(context: CommandBuildContext): ArgumentType<Any>? {
            return null
        }

        override fun type(): ArgumentTypeInfo<ArgumentType<Any>, *> {
            return this@WordArgumentInfo
        }
    }

    companion object {
        fun isAllowedWord(word: String): Boolean {
            return word.all { StringReader.isAllowedInUnquotedString(it) }
        }
    }
}