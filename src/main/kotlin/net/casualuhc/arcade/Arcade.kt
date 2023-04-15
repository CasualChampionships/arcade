package net.casualuhc.arcade

import net.casualuhc.arcade.events.EventHandler
import net.casualuhc.arcade.events.player.PlayerChatEvent
import net.casualuhc.arcade.events.player.PlayerJoinEvent
import net.casualuhc.arcade.events.server.ServerCreatedEvent
import net.casualuhc.arcade.scoreboards.SimpleSidebar
import net.casualuhc.arcade.utils.ComponentUtils.gold
import net.fabricmc.api.ModInitializer
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
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
        var bar: SimpleSidebar? = null

        EventHandler.register<PlayerJoinEvent> { (player) ->
            bar = SimpleSidebar(player)
        }
        EventHandler.register<PlayerChatEvent> { (player, message) ->
            val content = message.signedContent()
            if (content == "show") {
                bar!!.show()
            } else if (content == "add") {
                bar!!.addRow(Component.literal("Wow this actually works!").gold())
            } else if (content == "space") {
                bar!!.addRow(0, Component.empty())
            } else if (content == "remove") {
                bar!!.removeRow(Random.nextInt(bar!!.size()))
            } else if (content == "hide") {
                bar!!.hide()
            } else if (content == "name") {
                bar!!.setName(Component.literal("Poggers"))
            } else if (content == "modify") {
                bar!!.setRow(Random.nextInt(bar!!.size()), Component.literal("Modified").withStyle(ChatFormatting.OBFUSCATED))
            }
        }
    }
}