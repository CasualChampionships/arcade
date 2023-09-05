package net.casual.arcade

import net.casual.arcade.commands.DebugCommand
import net.casual.arcade.commands.MinigameCommand
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerJoinEvent
import net.casual.arcade.events.server.ServerCreatedEvent
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.arcade.gui.nametag.ArcadeNameTag
import net.casual.arcade.gui.suppliers.ComponentSupplier
import net.casual.arcade.utils.*
import net.casual.arcade.utils.PlayerUtils.isSurvival
import net.fabricmc.api.ModInitializer
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.function.Predicate

class Arcade: ModInitializer {
    companion object {
        @JvmField
        val logger: Logger = LogManager.getLogger("Arcade")

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
            DebugCommand.register(it.dispatcher)
        }
    }
}