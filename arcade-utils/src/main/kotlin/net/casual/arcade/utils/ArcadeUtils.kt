/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.ProblemReporter.ScopedCollector
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.function.Consumer
import kotlin.io.path.createDirectories
import kotlin.jvm.optionals.getOrNull

public object ArcadeUtils {
    public const val MOD_ID: String = "arcade"

    @JvmField
    public val logger: Logger = LoggerFactory.getLogger(MOD_ID)

    @JvmField
    public val container: ModContainer? = FabricLoader.getInstance().getModContainer(MOD_ID).getOrNull()

    @JvmStatic
    public val path: Path by lazy {
        FabricLoader.getInstance().configDir.resolve(MOD_ID).apply { createDirectories() }
    }

    @JvmStatic
    public fun id(path: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
    }

    @JvmStatic
    public fun createProblemReporter(): ScopedCollector {
        return ScopedCollector(this.logger)
    }

    @JvmStatic
    public fun scopedProblemReporter(consumer: Consumer<ScopedCollector>) {
        this.createProblemReporter().use(consumer::accept)
    }
}