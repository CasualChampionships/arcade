package net.casual.arcade

import net.casual.arcade.commands.MinigameCommand
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerCreatedEvent
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.arcade.utils.BossbarUtils
import net.casual.arcade.utils.NameDisplayUtils
import net.casual.arcade.utils.SidebarUtils
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

        this.registerCommands()
    }

    private fun registerCommands() {
        GlobalEventHandler.register<ServerRegisterCommandEvent> {
            MinigameCommand.register(it.dispatcher)
        }
    }
}