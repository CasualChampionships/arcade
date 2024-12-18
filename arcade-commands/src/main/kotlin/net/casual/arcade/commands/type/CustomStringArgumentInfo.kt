/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.type

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.arguments.StringArgumentType.StringType
import net.minecraft.network.FriendlyByteBuf

public class CustomStringArgumentInfo(
    private val type: StringType
): CustomArgumentTypeInfo<ArgumentType<Any>>() {
    override fun serializeTypeData(buffer: FriendlyByteBuf) {
        buffer.writeEnum(this.type)
    }

    override fun getFacadeType(): Class<out ArgumentType<*>> {
        return StringArgumentType::class.java
    }

    public companion object {
        public fun isAllowedWord(word: String): Boolean {
            return word.all { StringReader.isAllowedInUnquotedString(it) }
        }
    }
}