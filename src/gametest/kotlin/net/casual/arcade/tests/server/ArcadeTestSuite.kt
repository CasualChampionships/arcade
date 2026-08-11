package net.casual.arcade.tests.server

import net.casual.arcade.gametest.TestSuite
import net.casual.arcade.utils.ArcadeUtils

abstract class ArcadeTestSuite: TestSuite() {
    abstract override val namespace: String
    final override val prefix: String = ""
}