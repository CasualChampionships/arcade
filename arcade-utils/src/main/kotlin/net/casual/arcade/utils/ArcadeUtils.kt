/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import net.casual.arcade.utils.registries.ArcadeUtilsRegistries
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.minecraft.resources.Identifier
import net.minecraft.util.ProblemReporter.ScopedCollector
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.function.Consumer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.io.path.createDirectories
import kotlin.jvm.optionals.getOrNull

public object ArcadeUtils: ModInitializer {
    public const val MOD_ID: String = "arcade"

    @JvmField
    public val logger: Logger = LoggerFactory.getLogger(MOD_ID)

    @JvmField
    public val container: ModContainer? = FabricLoader.getInstance().getModContainer(MOD_ID).getOrNull()

    @JvmStatic
    public val version: String by lazy {
        FabricLoader.getInstance().getModContainer("arcade-utils").get().metadata.version.friendlyString
    }

    @JvmStatic
    public val path: Path by lazy {
        FabricLoader.getInstance().configDir.resolve(MOD_ID).apply { createDirectories() }
    }

    @JvmStatic
    public fun id(path: String): Identifier {
        return Identifier.fromNamespaceAndPath(MOD_ID, path)
    }

    @JvmStatic
    public fun createProblemReporter(): ScopedCollector {
        return ScopedCollector(this.logger)
    }

    @JvmStatic
    public fun scopedProblemReporter(consumer: Consumer<ScopedCollector>) {
        this.createProblemReporter().use(consumer::accept)
    }

    @OptIn(ExperimentalContracts::class)
    public inline fun scopedProblemReporter(consumer: (ScopedCollector) -> Unit) {
        contract {
            callsInPlace(consumer, InvocationKind.EXACTLY_ONCE)
        }
        this.createProblemReporter().use(consumer)
    }

    override fun onInitialize() {
        ArcadeUtilsRegistries.load()
    }
}