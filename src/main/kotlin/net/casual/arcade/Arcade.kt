package net.casual.arcade

import net.casual.arcade.resources.creator.NamedResourcePackCreator
import net.casual.arcade.resources.utils.ResourcePackUtils.addLangsFromData
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ResourceUtils
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.ModContainer
import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.Logger
import java.nio.file.Path

/**
 * Arcade initializer class.
 */
public object Arcade: ModInitializer {
    /**
     * The mod identifier for Arcade.
     */
    public const val MOD_ID: String = ArcadeUtils.MOD_ID

    public val logger: Logger
        get() = ArcadeUtils.logger

    public val container: ModContainer
        get() = ArcadeUtils.container

    public val path: Path
        get() = ArcadeUtils.path

    /**
     * Creates a [ResourceLocation] with the namespace of [MOD_ID].
     *
     * @param path The path of the [ResourceLocation].
     * @return The created [ResourceLocation].
     */
    @JvmStatic
    public fun id(path: String): ResourceLocation {
        return ResourceUtils.arcade(path)
    }

    override fun onInitialize() {
        this.logger.info("Initializing Arcade!")
    }
}
