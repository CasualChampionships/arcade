/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.gametest

import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.entrypoint.EntrypointContainer
import org.slf4j.LoggerFactory

public object ArcadeGametest: ModInitializer {
    private const val MOD_ID = "arcade-gametest"
    internal const val ENTRYPOINT_KEY = MOD_ID

    internal val logger = LoggerFactory.getLogger(MOD_ID)

    @JvmStatic
    public var suppressSystemMessages: Boolean = false
        private set

    override fun onInitialize() {
        val suppressors = this.getEntrypoints().filter { provider -> provider.entrypoint.suppressSystemChat() }
            .map { it.entrypoint::class.simpleName }
        if (suppressors.isNotEmpty()) {
            this.suppressSystemMessages = true
            logger.info("The following test suite providers opted to suppress system chat: $suppressors")
        }
    }

    internal fun getEntrypoints(): List<EntrypointContainer<TestSuiteProvider>> {
        return FabricLoader.getInstance().getEntrypointContainers(ENTRYPOINT_KEY, TestSuiteProvider::class.java)
    }
}