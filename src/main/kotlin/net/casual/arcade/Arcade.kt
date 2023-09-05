package net.casual.arcade

import net.casual.arcade.commands.DebugCommand
import net.casual.arcade.commands.MinigameCommand
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerCreatedEvent
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.arcade.utils.*
import net.fabricmc.api.ModInitializer
import net.minecraft.server.MinecraftServer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class Arcade: ModInitializer {
    companion object {
        @JvmField
        val logger: Logger = LogManager.getLogger("Arcade")

        @JvmField
        internal val debug = false

        private var server: MinecraftServer? = null

        @JvmStatic
        fun getServer(): MinecraftServer {
            return this.server ?: throw IllegalStateException("Called Arcade.getServer before Server was created")
        }

        @JvmStatic
        fun getServerOrNull(): MinecraftServer? {
            return this.server
        }

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
        MinigameUtils.registerEvents()
        TabUtils.registerEvents()
        NameTagUtils.registerEvents()

        this.registerCommands()
    }

    private fun registerCommands() {
        GlobalEventHandler.register<ServerRegisterCommandEvent> {
            MinigameCommand.register(it.dispatcher)

            if (debug) {
                DebugCommand.register(it.dispatcher)
            }
        }
    }
}