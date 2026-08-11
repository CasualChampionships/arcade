/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.gametest

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.tests.server.ArcadeTestSuite
import net.fabricmc.fabric.api.gametest.v1.GameTest

/**
 * Some simple smoke tests.
 */
@Suppress("FunctionName", "Unused")
object ServerBootTests: ArcadeTestSuite() {
    override val namespace: String = "arcade-gametest"

    @GameTest
    fun `server has booted`(context: TestContext) = context.test {
        check(server.allLevels.contains(level)) { "Test level is not one of the server's levels" }
    }
}
