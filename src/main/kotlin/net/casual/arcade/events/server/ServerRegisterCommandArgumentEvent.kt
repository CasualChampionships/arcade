package net.casual.arcade.events.server

import net.casual.arcade.events.core.Event
import net.minecraft.commands.synchronization.ArgumentTypeInfo

class ServerRegisterCommandArgumentEvent(
    private val arguments: MutableMap<Class<*>, ArgumentTypeInfo<*, *>>
): Event {
    fun addArgument(type: Class<*>, info: ArgumentTypeInfo<*, *>) {
        this.arguments[type] = info
    }
}