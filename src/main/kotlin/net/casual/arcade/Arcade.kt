package net.casual.arcade

import net.casual.arcade.commands.ArcadeCommands
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerCreatedEvent
import net.casual.arcade.utils.*
import net.fabricmc.api.ModInitializer
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class Arcade: ModInitializer {
    companion object {
        private var server: MinecraftServer? = null
        internal const val debug = false
        const val MOD_ID = "arcade"

        @JvmField
        val logger: Logger = LogManager.getLogger("Arcade")

        @JvmStatic
        fun getServer(): MinecraftServer {
            return this.server ?: throw IllegalStateException("Called Arcade.getServer before Server was created")
        }

        @JvmStatic
        fun getServerOrNull(): MinecraftServer? {
            return this.server
        }

        fun id(path: String): ResourceLocation {
            return ResourceLocation(MOD_ID, path)
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
        ResourcePackUtils.registerEvents()
        CommandUtils.registerEvents()

        ArcadeCommands.registerCommands()
    }
}