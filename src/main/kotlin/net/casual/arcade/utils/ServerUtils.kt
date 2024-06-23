package net.casual.arcade.utils

import net.casual.arcade.ducks.CustomMOTD
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer

public object ServerUtils {
    public fun MinecraftServer.setMessageOfTheDay(message: Component) {
        (this as CustomMOTD).`arcade$setMOTD`(message)
    }

    public fun MinecraftServer.getMessageOfTheDay(): Component {
        val custom = (this as CustomMOTD).`arcade$getMOTD`()
        return custom ?: Component.nullToEmpty(this.motd)
    }
}