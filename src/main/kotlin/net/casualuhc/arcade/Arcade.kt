package net.casualuhc.arcade

import net.casualuhc.arcade.events.EventHandler
import net.casualuhc.arcade.events.level.LevelCreatedEvent
import net.casualuhc.arcade.events.server.ServerCreatedEvent
import net.casualuhc.arcade.extensions.Extension
import net.casualuhc.arcade.utils.LevelUtils.addExtension
import net.fabricmc.api.ModInitializer
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
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
            EventHandler.register<ServerCreatedEvent> {
                this.server = it.server
            }
        }
    }

    override fun onInitialize() {
        EventHandler.register<LevelCreatedEvent> {
            it.level.addExtension(PlayerExtension())
        }
    }

    class PlayerExtension: Extension {
        override fun getName(): String {
            return "Testing"
        }

        override fun serialize(): Tag {
            return CompoundTag()
        }

        override fun deserialize(element: Tag) {

        }
    }
}