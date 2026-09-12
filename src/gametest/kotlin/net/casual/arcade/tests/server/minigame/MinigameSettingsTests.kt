/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.minigame

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.gametest.minigame.reload
import net.casual.arcade.tests.server.ArcadeTestSuite
import net.casual.arcade.tests.server.minigame.utils.minigame
import net.casual.arcade.utils.ArcadeUtils
import net.fabricmc.fabric.api.gametest.v1.GameTest

@Suppress("FunctionName", "Unused")
object MinigameSettingsTests: ArcadeTestSuite() {
    override val namespace: String = ArcadeUtils.MOD_ID

    @GameTest
    fun `settings are applied during initialization`(context: TestContext) = context.test {
        val minigame = minigame().create()

        minigame.settings.applied shouldEqual listOf(false)
    }

    @GameTest
    fun `setting a value invokes listeners and appliers`(context: TestContext) = context.test {
        val minigame = minigame().create()
        minigame.settings.clearRecords()

        minigame.settings.testToggle.set(true)

        minigame.settings.changes shouldEqual listOf(false to true)
        minigame.settings.applied shouldEqual listOf(true)
    }

    @GameTest
    fun `listeners observe the written value`(context: TestContext) = context.test {
        val minigame = minigame().create()
        minigame.settings.clearRecords()

        minigame.settings.testToggle.set(true)

        assertEquals(
            listOf(true), minigame.settings.observed, "A listener read the setting's previous value during onChange"
        )
    }

    @GameTest
    fun `setting an unchanged value does nothing`(context: TestContext) = context.test {
        val minigame = minigame().create()
        minigame.settings.clearRecords()

        minigame.settings.testToggle.set(false)

        assertTrue(minigame.settings.changes.isEmpty(), "An unchanged value fired the setting's listeners")
        assertTrue(minigame.settings.applied.isEmpty(), "An unchanged value applied the setting")
    }

    @GameTest
    fun `setting quietly doesnt invoke listeners or appliers`(context: TestContext) = context.test {
        val minigame = minigame().create()
        minigame.settings.clearRecords()

        minigame.settings.testToggle.setQuietly(true)

        minigame.settings.testToggle.get() shouldEqual true
        assertTrue(minigame.settings.changes.isEmpty(), "setQuietly fired the setting's listeners")
        assertTrue(minigame.settings.applied.isEmpty(), "setQuietly applied the setting")
    }

    @GameTest
    fun `settings are reapplied when reloaded`(context: TestContext) = context.test {
        val minigame = minigame().start()
        minigame.settings.testToggle.set(true)

        val restored = reload(minigame)

        restored.settings.testToggle.get() shouldEqual true
        assertEquals(
            listOf(true),
            restored.settings.applied,
            "A restored setting had its value restored but was never applied"
        )
        assertTrue(restored.settings.changes.isEmpty(), "A restored setting fired its listeners on load")
    }

    @GameTest
    fun `setting from an option sets its value`(context: TestContext) = context.test {
        val minigame = minigame().create()

        assertTrue(minigame.settings.testToggle.setFromOption("enabled"), "Known option was not accepted")
        minigame.settings.testToggle.get() shouldEqual true

        assertFalse(minigame.settings.testToggle.setFromOption("unknown"), "Unknown option was accepted")
        minigame.settings.testToggle.get() shouldEqual true
    }

    @GameTest
    fun `cycling moves through the options`(context: TestContext) = context.test {
        val minigame = minigame().create()
        val setting = minigame.settings.testToggle

        assertEquals("disabled", assertNotNull(setting.selected(), "Value has no matching option").id)
        setting.cycle(1)
        assertEquals("enabled", assertNotNull(setting.selected(), "Value has no matching option").id)
        setting.cycle(1)
        assertEquals("disabled", assertNotNull(setting.selected(), "Value has no matching option").id)
    }

    @GameTest
    fun `a setting without a display is still registered`(context: TestContext) = context.test {
        val minigame = minigame().create()

        val setting = assertNotNull(minigame.settings.get("test_undisplayed"), "Undisplayed setting was not registered")
        assertFalse(setting.hasDisplay(), "Undisplayed setting reported a display")
        assertNull(setting.display(), "Undisplayed setting created a display stack")
    }

    @GameTest
    fun `setting values survive a round trip`(context: TestContext) = context.test {
        val minigame = minigame().start()
        minigame.settings.canPvp.set(false)

        val restored = reload(minigame)

        restored.settings.canPvp.get() shouldEqual false
    }
}
