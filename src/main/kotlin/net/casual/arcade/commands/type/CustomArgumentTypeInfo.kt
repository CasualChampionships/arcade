package net.casual.arcade.commands.type

import com.mojang.brigadier.arguments.ArgumentType

public interface CustomArgumentTypeInfo {
    public fun getFacadeType(): Class<out ArgumentType<*>>
}