package net.casual.arcade.commands.type

import com.mojang.brigadier.arguments.ArgumentType

interface CustomArgumentTypeInfo {
    fun getFacadeType(): Class<out ArgumentType<*>>
}