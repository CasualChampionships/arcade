package net.casualuhc.arcade

import net.casualuhc.arcade.events.GlobalEventHandler
import net.casualuhc.arcade.events.server.ServerCreatedEvent
import net.casualuhc.arcade.utils.BossbarUtils
import net.casualuhc.arcade.utils.NameDisplayUtils
import net.casualuhc.arcade.utils.SidebarUtils
import net.fabricmc.api.ModInitializer
import net.minecraft.server.MinecraftServer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class Arcade: ModInitializer {
    companion object {
        @JvmField
        val logger: Logger = LogManager.getLogger("Arcade")

        @JvmStatic
        lateinit var server: MinecraftServer
            private set

        init {
            GlobalEventHandler.register<ServerCreatedEvent> {
                this.server = it.server
            }
        }
    }

    override fun onInitialize() {
        SidebarUtils.registerEvents()
        BossbarUtils.registerEvents()
        NameDisplayUtils.registerEvents()
    }
}