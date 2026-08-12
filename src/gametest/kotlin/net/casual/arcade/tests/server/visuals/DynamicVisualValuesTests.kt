/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.visuals

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.tests.server.ArcadeTestSuite
import net.casual.arcade.observer.tracker.SimpleObserverTracker
import net.casual.arcade.observer.utils.asObserver
import net.casual.arcade.utils.player.username
import net.casual.arcade.virtual.visuals.ArcadeVirtualVisuals
import net.casual.arcade.virtual.visuals.data.DynamicVisualValues
import net.casual.arcade.virtual.visuals.data.PlayerSpecificVisualData
import net.casual.arcade.virtual.visuals.elements.PlayerSpecificElement
import net.casual.arcade.virtual.visuals.elements.TickableElement
import net.casual.arcade.virtual.visuals.elements.UniversalElement
import net.fabricmc.fabric.api.gametest.v1.GameTest

@Suppress("FunctionName", "Unused")
object DynamicVisualValuesTests: ArcadeTestSuite() {
    override val namespace: String = ArcadeVirtualVisuals.MOD_ID

    @GameTest
    fun `universal element generates base value`(context: TestContext) = context.test {
        val data = PlayerSpecificVisualData()
        val value = data.register("base")

        val dynamic = DynamicVisualValues()
        dynamic.bind(value, UniversalElement { "generated" })

        dynamic.tick(server)
        dynamic.update(server, SimpleObserverTracker())

        value.get() shouldEqual "generated"
    }

    @GameTest(maxTicks = 400)
    fun `player specific element generates override`(context: TestContext) = context.test {
        val player = this.createTestPlayer()
        val observers = SimpleObserverTracker()
        observers.startObserving(player.asObserver())

        val data = PlayerSpecificVisualData()
        val value = data.register("base")

        val dynamic = DynamicVisualValues()
        dynamic.bind(value, PlayerSpecificElement { observee -> "for ${observee.username}" })

        dynamic.tick(server)
        dynamic.update(server, observers)

        value.get() shouldEqual "base"
        value.get(player) shouldEqual "for ${player.username}"
        assertTrue(value.isOverridden(player), "Expected the element to have generated an override")
    }

    @GameTest(maxTicks = 400)
    fun `dynamic value can have base and override`(context: TestContext) = context.test {
        val overridden = this.createTestPlayer()
        val observers = SimpleObserverTracker()
        observers.startObserving(overridden.asObserver())

        val data = PlayerSpecificVisualData()
        val value = data.register("initial")

        val dynamic = DynamicVisualValues()
        dynamic.bind(value, UniversalElement { "shared" })
        dynamic.bind(value, PlayerSpecificElement { "yours" })

        dynamic.tick(server)
        dynamic.update(server, observers)

        value.get() shouldEqual "shared"
        value.get(overridden) shouldEqual "yours"
    }

    @GameTest
    fun `binding element replaces previous of same kind`(context: TestContext) = context.test {
        val data = PlayerSpecificVisualData()
        val value = data.register("initial")

        val dynamic = DynamicVisualValues()
        dynamic.bind(value, UniversalElement { "first" })
        dynamic.bind(value, UniversalElement { "second" })

        dynamic.tick(server)
        dynamic.update(server, SimpleObserverTracker())

        value.get() shouldEqual "second"
    }

    @GameTest
    fun `unbinding element stops generating value`(context: TestContext) = context.test {
        val data = PlayerSpecificVisualData()
        val value = data.register("base")

        val dynamic = DynamicVisualValues()
        val element = UniversalElement { "generated" }
        dynamic.bind(value, element)
        dynamic.tick(server)
        dynamic.update(server, SimpleObserverTracker())

        dynamic.unbind(value)
        value.set("manual")
        dynamic.tick(server)
        dynamic.update(server, SimpleObserverTracker())

        value.get() shouldEqual "manual"
    }

    @GameTest
    fun `tickable elements tick once per tick`(context: TestContext) = context.test {
        val dynamic = DynamicVisualValues()

        var ticks = 0
        val tickable = TickableElement { ticks++ }
        dynamic.addTickable(tickable)
        // This should be idempotent
        dynamic.addTickable(tickable)

        dynamic.tick(server)
        ticks shouldEqual 1

        dynamic.tick(server)
        ticks shouldEqual 2
    }

    @GameTest
    fun `tickable elements tick before values generate`(context: TestContext) = context.test {
        val data = PlayerSpecificVisualData()
        val value = data.register(0)

        val dynamic = DynamicVisualValues()

        var state = 0
        dynamic.addTickable { state++ }
        dynamic.bind(value, UniversalElement { state })

        dynamic.tick(server)
        dynamic.update(server, SimpleObserverTracker())

        value.get() shouldEqual 1
    }

    @GameTest
    fun `removed tickable elements dont tick`(context: TestContext) = context.test {
        val dynamic = DynamicVisualValues()

        var ticks = 0
        val tickable = TickableElement { ticks++ }
        dynamic.addTickable(tickable)
        dynamic.tick(server)

        dynamic.removeTickable(tickable)
        dynamic.tick(server)

        ticks shouldEqual 1
    }
}
