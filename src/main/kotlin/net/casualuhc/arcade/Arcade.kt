package net.casualuhc.arcade

import net.casualuhc.arcade.commands.EnumArgument
import net.casualuhc.arcade.events.EventHandler
import net.casualuhc.arcade.events.level.LevelCreatedEvent
import net.casualuhc.arcade.events.server.ServerCreatedEvent
import net.casualuhc.arcade.events.server.ServerRegisterCommandEvent
import net.casualuhc.arcade.extensions.Extension
import net.casualuhc.arcade.utils.LevelUtils.addExtension
import net.fabricmc.api.ModInitializer
import net.minecraft.commands.Commands
import net.minecraft.commands.synchronization.SuggestionProviders
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.GameType
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
            EventHandler.register<ServerCreatedEvent> {
                this.server = it.server
            }
        }
    }

    override fun onInitialize() {

    }
}