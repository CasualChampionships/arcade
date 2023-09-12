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
public object Arcade: ModInitializer {
    private var server: MinecraftServer? = null

    @JvmField
    internal val logger: Logger = LogManager.getLogger("Arcade")

    /**
     * Whether arcade is in debug mode.
     */
    internal const val DEBUG: Boolean = false

    /**
     * The mod identifier for Arcade.
     */
    public const val MOD_ID: String = "arcade"

    init {
        GlobalEventHandler.register<ServerCreatedEvent> {
            this.server = it.server
        }
    }

    /**
     * Gets the [MinecraftServer] instance, this should only
     * be called after the server has been created otherwise
     * this method will throw an [IllegalStateException].
     *
     * @return The [MinecraftServer] instance.
     * @see getServerOrNull
     */
    @JvmStatic
    public fun getServer(): MinecraftServer {
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
    public fun getServerOrNull(): MinecraftServer? {
        return this.server
    }

    /**
     * Creates a [ResourceLocation] with the namespace of [MOD_ID].
     *
     * @param path The path of the [ResourceLocation].
     * @return The created [ResourceLocation].
     */
    @JvmStatic
    public fun id(path: String): ResourceLocation {
        return ResourceLocation(MOD_ID, path)
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