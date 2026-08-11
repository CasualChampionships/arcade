/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.gametest.fabric

import net.casual.arcade.gametest.TestSuite
import net.casual.arcade.gametest.TestSuiteProvider
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.fabricmc.loader.api.entrypoint.EntrypointContainer
import java.util.stream.Stream

public object InjectedTestSuites {
    private const val ENTRYPOINT_KEY = "arcade-gametest"

    public fun getEntrypoints(): Stream<EntrypointContainer<TestSuite>> {
        val providers = FabricLoader.getInstance().getEntrypointContainers(ENTRYPOINT_KEY, TestSuiteProvider::class.java)
        return providers.stream().flatMap { provider ->
            provider.entrypoint.getTestSuites().stream().map { suite -> SuiteEntrypoint(suite, provider.provider) }
        }
    }

    private class SuiteEntrypoint(
        private val suite: TestSuite,
        private val container: ModContainer
    ): EntrypointContainer<TestSuite> {
        override fun getEntrypoint(): TestSuite {
            return this.suite
        }

        override fun getProvider(): ModContainer {
            return this.container
        }
    }
}