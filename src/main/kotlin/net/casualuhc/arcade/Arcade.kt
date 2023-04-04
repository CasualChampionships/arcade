package net.casualuhc.arcade

import net.casualuhc.arcade.events.EventHandler
import net.casualuhc.arcade.events.server.ServerCreatedEvent
import net.casualuhc.arcade.events.server.ServerLoadedEvent
import net.fabricmc.api.ModInitializer
import net.minecraft.server.MinecraftServer

class Arcade: ModInitializer {
    companion object {
        @JvmStatic
        lateinit var server: MinecraftServer
            private set

        init {
            EventHandler.register<ServerCreatedEvent> {
                this.server = it.server
            }
        }
    }

    override fun onInitialize() {

    }
}