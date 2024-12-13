package net.casual.arcade.utils

import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.jvm.optionals.getOrNull

public object ArcadeUtils {
    public const val MOD_ID: String = "arcade"

    @JvmField
    public val logger: Logger = LogManager.getLogger(MOD_ID)

    @JvmField
    public val container: ModContainer? = FabricLoader.getInstance().getModContainer(MOD_ID).getOrNull()

    @JvmStatic
    public val path: Path by lazy {
        FabricLoader.getInstance().configDir.resolve(MOD_ID).apply { createDirectories() }
    }
}