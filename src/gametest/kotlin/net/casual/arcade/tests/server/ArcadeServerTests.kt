package net.casual.arcade.tests.server

import net.casual.arcade.tests.server.scheduler.TestRoutines
import net.fabricmc.api.ModInitializer

object ArcadeServerTests: ModInitializer {
    override fun onInitialize() {
        TestRoutines.registerRoutines()
    }
}