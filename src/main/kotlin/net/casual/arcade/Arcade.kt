/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade

import net.casual.arcade.Arcade.MOD_ID
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.IdentifierUtils
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.ModContainer
import net.minecraft.resources.Identifier
import org.slf4j.Logger
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
        get() = ArcadeUtils.container!!

    public val path: Path
        get() = ArcadeUtils.path

    /**
     * Creates a [Identifier] with the namespace of [MOD_ID].
     *
     * @param path The path of the [Identifier].
     * @return The created [Identifier].
     */
    @JvmStatic
    public fun id(path: String): Identifier {
        return IdentifierUtils.arcade(path)
    }

    override fun onInitialize() {
        this.logger.info("Initializing Arcade!")
    }
}
