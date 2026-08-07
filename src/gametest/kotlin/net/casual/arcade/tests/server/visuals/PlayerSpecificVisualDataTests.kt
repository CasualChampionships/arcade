/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.visuals

import net.casual.arcade.gametest.ArcadeTestContext
import net.casual.arcade.gametest.ArcadeTestSuite
import net.casual.arcade.virtual.visuals.data.PlayerSpecificVisualData
import net.fabricmc.fabric.api.gametest.v1.GameTest
import java.util.*

object PlayerSpecificVisualDataTests: ArcadeTestSuite() {
    @GameTest
    fun observersWithoutOverridesAreShownTheBase(context: ArcadeTestContext) = context.test {
        val data = PlayerSpecificVisualData()
        val value = data.register("base")

        value.set("updated")

        value.get() shouldEqual "updated"
        value.get(UUID.randomUUID()) shouldEqual "updated"
    }

    @GameTest
    fun overrideIsOnlyShownToThatPlayer(context: ArcadeTestContext) = context.test {
        val data = PlayerSpecificVisualData()
        val value = data.register("base")

        val overridden = UUID.randomUUID()
        val other = UUID.randomUUID()
        value.set(overridden, "override")

        value.get(overridden) shouldEqual "override"
        value.get(other) shouldEqual "base"
        value.get() shouldEqual "base"

        assertTrue(value.isOverridden(overridden), "Expected the player to override the value")
        assertFalse(value.isOverridden(other), "Expected the other player not to override the value")
    }

    @GameTest
    fun changingTheBaseDoesNotClobberAnOverride(context: ArcadeTestContext) = context.test {
        val data = PlayerSpecificVisualData()
        val value = data.register("base")

        val player = UUID.randomUUID()
        value.set(player, "override")
        value.set("updated")

        value.get(player) shouldEqual "override"
        value.get() shouldEqual "updated"
    }

    @GameTest
    fun settingToBaseFollowsTheBaseAgain(context: ArcadeTestContext) = context.test {
        val data = PlayerSpecificVisualData()
        val value = data.register("base")

        val player = UUID.randomUUID()
        value.set(player, "override")
        value.setToBase(player)

        assertFalse(value.isOverridden(player), "Expected the override to have been removed")
        value.get(player) shouldEqual "base"

        // The player should now follow any future change to the base
        value.set("updated")
        value.get(player) shouldEqual "updated"
    }

    @GameTest
    fun baseIsOnlyReportedDirtyOnce(context: ArcadeTestContext) = context.test {
        val data = PlayerSpecificVisualData()
        val value = data.register("base")

        value.set("updated")

        (data.clean() and value.bit) shouldNotEqual 0
        data.clean() shouldEqual 0
    }

    @GameTest
    fun settingAnUnchangedValueIsNotDirty(context: ArcadeTestContext) = context.test {
        val data = PlayerSpecificVisualData()
        val value = data.register("base")
        data.clean()

        assertFalse(value.set("base"), "Expected setting the same value to report no change")
        data.clean() shouldEqual 0
    }

    @GameTest
    fun dirtyBaseIsMaskedFromPlayersWhoOverrideIt(context: ArcadeTestContext) = context.test {
        val data = PlayerSpecificVisualData()
        val value = data.register("base")

        val overridden = UUID.randomUUID()
        val other = UUID.randomUUID()
        value.set(overridden, "override")
        data.clean(overridden, data.clean())

        value.set("updated")
        val base = data.clean()

        (data.clean(overridden, base) and value.bit) shouldEqual 0
        (data.clean(other, base) and value.bit) shouldNotEqual 0
    }

    @GameTest
    fun overrideIsReportedDirtyForThatPlayerOnly(context: ArcadeTestContext) = context.test {
        val data = PlayerSpecificVisualData()
        val value = data.register("base")

        val overridden = UUID.randomUUID()
        val other = UUID.randomUUID()
        value.set(overridden, "override")

        val base = data.clean()
        base shouldEqual 0

        (data.clean(overridden, base) and value.bit) shouldNotEqual 0
        (data.clean(other, base) and value.bit) shouldEqual 0

        // The dirty bit is consumed by the first clean
        (data.clean(overridden, base) and value.bit) shouldEqual 0
    }

    @GameTest
    fun settingToBaseIsDirtySoTheBaseIsResent(context: ArcadeTestContext) = context.test {
        val data = PlayerSpecificVisualData()
        val value = data.register("base")

        val player = UUID.randomUUID()
        value.set(player, "override")
        data.clean(player, data.clean())

        value.setToBase(player)

        (data.clean(player, 0) and value.bit) shouldNotEqual 0
    }

    @GameTest
    fun eachValueHasItsOwnBit(context: ArcadeTestContext) = context.test {
        val data = PlayerSpecificVisualData()
        val first = data.register("first")
        val second = data.register("second")

        first.bit shouldNotEqual second.bit

        second.set("updated")
        val base = data.clean()

        (base and first.bit) shouldEqual 0
        (base and second.bit) shouldNotEqual 0
    }

    @GameTest
    fun removingAPlayerDiscardsTheirOverrides(context: ArcadeTestContext) = context.test {
        val data = PlayerSpecificVisualData()
        val value = data.register("base")

        val player = UUID.randomUUID()
        value.set(player, "override")
        data.remove(player)

        value.get(player) shouldEqual "base"
        assertFalse(value.isOverridden(player), "Expected the override to have been discarded")
    }
}
