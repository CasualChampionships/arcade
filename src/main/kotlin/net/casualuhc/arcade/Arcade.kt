package net.casualuhc.arcade

import net.casualuhc.arcade.events.EventHandler
import net.casualuhc.arcade.events.player.PlayerChatEvent
import net.casualuhc.arcade.events.player.PlayerJoinEvent
import net.casualuhc.arcade.events.server.ServerCreatedEvent
import net.casualuhc.arcade.scoreboards.ArcadeSidebar
import net.casualuhc.arcade.scoreboards.ConstantRow
import net.casualuhc.arcade.scoreboards.SidebarRow
import net.casualuhc.arcade.utils.ComponentUtils.bold
import net.casualuhc.arcade.utils.ComponentUtils.gold
import net.casualuhc.arcade.utils.SidebarUtils
import net.fabricmc.api.ModInitializer
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.random.Random

class Arcade: ModInitializer {
    companion object {
        @JvmField
        val logger: Logger = LogManager.getLogger("Arcade")

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
        SidebarUtils.registerEvents()
    }
}