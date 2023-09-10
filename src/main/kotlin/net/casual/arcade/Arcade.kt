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

/**
 * Arcade initializer class.
 */
class Arcade: ModInitializer {
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

    companion object {
        private var server: MinecraftServer? = null
        internal const val debug = false

        @JvmField
        internal val logger: Logger = LogManager.getLogger("Arcade")

        /**
         * The mod identifier for Arcade.
         */
        const val MOD_ID = "arcade"

        /**
         * Gets the [MinecraftServer] instance, this should only
         * be called after the server has been created otherwise
         * this method will throw an [IllegalStateException].
         *
         * @return The [MinecraftServer] instance.
         * @see getServerOrNull
         */
        @JvmStatic
        fun getServer(): MinecraftServer {
            return this.server ?: throw IllegalStateException("Called Arcade.getServer before server was created")
        }

        /**
         * Gets the [MinecraftServer] instance, this may be `null`
         * if the server has not been created yet.
         *
         * @return The [MinecraftServer] instance, or null.
         * @see getServer
         */
        @JvmStatic
        fun getServerOrNull(): MinecraftServer? {
            return this.server
        }

        /**
         * Creates a [ResourceLocation] with the namespace of [MOD_ID].
         *
         * @param path The path of the [ResourceLocation].
         * @return The created [ResourceLocation].
         */
        @JvmStatic
        fun id(path: String): ResourceLocation {
            return ResourceLocation(MOD_ID, path)
        }

        init {
            GlobalEventHandler.register<ServerCreatedEvent> {
                this.server = it.server
            }
        }
    }
}