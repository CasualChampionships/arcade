package net.casual.arcade.commands.type

import net.minecraft.commands.synchronization.ArgumentTypeInfo

interface CustomArgumentTypeInfo {
    fun getFacadeId(existing: Map<Class<*>, ArgumentTypeInfo<*, *>>): Int
}