package net.casualuhc.arcade.commands

import net.minecraft.commands.synchronization.ArgumentTypeInfo

interface CustomArgumentTypeInfo {
    fun getFacadeId(existing: Map<Class<*>, ArgumentTypeInfo<*, *>>): Int
}