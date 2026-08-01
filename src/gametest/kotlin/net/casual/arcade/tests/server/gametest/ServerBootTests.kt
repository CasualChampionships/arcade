/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.gametest

import net.casual.arcade.gametest.ArcadeTestContext
import net.casual.arcade.gametest.ArcadeTestSuite
import net.fabricmc.fabric.api.gametest.v1.GameTest

/**
 * Some simple smoke tests.
 */
object ServerBootTests: ArcadeTestSuite() {
    @GameTest
    fun serverHasBooted(context: ArcadeTestContext) = context.test {
        check(server.allLevels.contains(level)) { "Test level is not one of the server's levels" }
    }
}
