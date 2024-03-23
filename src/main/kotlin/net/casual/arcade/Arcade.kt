package net.casual.arcade

import net.casual.arcade.commands.ArcadeCommands
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerChatEvent
import net.casual.arcade.events.player.PlayerJoinEvent
import net.casual.arcade.events.server.ServerCreatedEvent
import net.casual.arcade.font.heads.PlayerHeadComponents
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.resources.ArcadePacks
import net.casual.arcade.utils.*
import net.casual.arcade.utils.ComponentUtils.greyscale
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Path
import kotlin.io.path.createDirectories

/**
 * Arcade initializer class.
 */
public object Arcade: ModInitializer {
    /**
     * The mod identifier for Arcade.
     */
    public const val MOD_ID: String = "arcade"

    /**
     * The mod container for Arcade.
     */
    public val container: ModContainer = FabricLoader.getInstance().getModContainer(MOD_ID).get()

    /**
     * The path to the arcade config directory.
     */
    @JvmStatic
    public val path: Path by lazy {
        FabricLoader.getInstance().configDir.resolve(MOD_ID).apply { createDirectories() }
    }

    @JvmField
    internal val logger: Logger = LogManager.getLogger("Arcade")

    private var server: MinecraftServer? = null

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
        MinigameUtils.registerEvents()
        Minigames.registerEvents()
        TabUtils.registerEvents()
        ResourcePackUtils.registerEvents()
        CommandUtils.registerEvents()
        BorderUtils.registerEvents()
        LevelUtils.registerEvents()

        ArcadeCommands.registerCommands()
    }
}
