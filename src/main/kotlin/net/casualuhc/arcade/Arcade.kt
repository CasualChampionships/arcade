package net.casualuhc.arcade

import net.casualuhc.arcade.events.EventHandler
import net.casualuhc.arcade.events.server.ServerLoadedEvent
import net.fabricmc.api.ModInitializer
import net.minecraft.server.MinecraftServer

class Arcade: ModInitializer {
    companion object {
        @JvmStatic
        lateinit var server: MinecraftServer

        init {
            EventHandler.register<ServerLoadedEvent> {
                server = it.server
            }
        }
    }

    override fun onInitialize() {

    }
}