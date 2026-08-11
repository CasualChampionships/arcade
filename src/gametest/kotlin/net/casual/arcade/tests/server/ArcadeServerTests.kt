package net.casual.arcade.tests.server

import net.casual.arcade.gametest.TestSuite
import net.casual.arcade.gametest.TestSuiteProvider
import net.casual.arcade.gametest.utils.TestUtils
import net.casual.arcade.tests.server.scheduler.TestRoutines
import net.fabricmc.api.ModInitializer

object ArcadeServerTests: ModInitializer, TestSuiteProvider {
    override fun onInitialize() {
        TestRoutines.registerRoutines()
    }

    override fun getTestSuites(): Set<TestSuite> {
        return TestUtils.findTestSuiteObjects("net.casual.arcade.tests.server")
    }
}