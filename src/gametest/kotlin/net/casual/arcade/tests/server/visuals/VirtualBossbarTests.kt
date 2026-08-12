/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.visuals

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.tests.server.ArcadeTestSuite
import net.casual.arcade.gametest.utils.TestFakePlayer
import net.casual.arcade.observer.utils.asObserver
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.coroutine.delay
import net.casual.arcade.virtual.visuals.ArcadeVirtualVisuals
import net.casual.arcade.virtual.visuals.bossbar.VirtualBossbar
import net.casual.arcade.virtual.visuals.utils.startObservingAndSendPackets
import net.casual.arcade.virtual.visuals.utils.stopObservingAndSendPackets
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundBossEventPacket
import net.minecraft.world.BossEvent.BossBarColor
import net.minecraft.world.BossEvent.BossBarOverlay
import java.util.*

@Suppress("FunctionName", "Unused")
object VirtualBossbarTests: ArcadeTestSuite() {
    override val namespace: String = ArcadeVirtualVisuals.MOD_ID

    @GameTest(maxTicks = 400)
    fun `bossbar is sent when observing`(context: TestContext) = context.test {
        val player = this.createTestPlayer()

        val bossbar = VirtualBossbar()
        bossbar.title.set(Component.literal("Hello!"))
        bossbar.startObservingAndSendPackets(player.asObserver())
        delay(1.Ticks)

        val added = assertNotNull(player.bossbarOperations().added, "Expected an add packet")
        added shouldEqual Component.literal("Hello!")
    }

    @GameTest(maxTicks = 400)
    fun `bossbar is removed when not observing`(context: TestContext) = context.test {
        val player = this.createTestPlayer()

        val bossbar = VirtualBossbar()
        bossbar.startObservingAndSendPackets(player.asObserver())
        player.clearPackets()

        bossbar.stopObservingAndSendPackets(player.asObserver())
        delay(1.Ticks)

        assertNotNull(player.bossbarOperations().removed, "Expected a remove packet")
    }

    @GameTest(maxTicks = 400)
    fun `changing bossbar base updates all observers`(context: TestContext) = context.test {
        val first = this.createTestPlayer()
        val second = this.createTestPlayer()

        val bossbar = VirtualBossbar()
        bossbar.startObservingAndSendPackets(first.asObserver())
        bossbar.startObservingAndSendPackets(second.asObserver())
        first.clearPackets()
        second.clearPackets()

        bossbar.title.set(Component.literal("Updated!"))
        bossbar.tick()
        delay(1.Ticks)

        for (player in listOf(first, second)) {
            val name = assertNotNull(player.bossbarOperations().updatedName, "Expected a name update")
            name shouldEqual Component.literal("Updated!")
        }
    }

    @GameTest(maxTicks = 400)
    fun `bossbar override is only sent to that player`(context: TestContext) = context.test {
        val overridden = this.createTestPlayer()
        val other = this.createTestPlayer()

        val bossbar = VirtualBossbar()
        bossbar.startObservingAndSendPackets(overridden.asObserver())
        bossbar.startObservingAndSendPackets(other.asObserver())
        overridden.clearPackets()
        other.clearPackets()

        bossbar.title.set(overridden, Component.literal("Just for you!"))
        bossbar.tick()
        delay(1.Ticks)

        val name = assertNotNull(overridden.bossbarOperations().updatedName, "Expected a name update")
        name shouldEqual Component.literal("Just for you!")

        other.assertNotSent<ClientboundBossEventPacket>()
    }

    @GameTest(maxTicks = 400)
    fun `changing bossbar base doesnt clobber override`(context: TestContext) = context.test {
        val overridden = this.createTestPlayer()
        val other = this.createTestPlayer()

        val bossbar = VirtualBossbar()
        bossbar.startObservingAndSendPackets(overridden.asObserver())
        bossbar.startObservingAndSendPackets(other.asObserver())
        bossbar.title.set(overridden, Component.literal("Just for you!"))
        bossbar.tick()
        delay(1.Ticks)
        overridden.clearPackets()
        other.clearPackets()

        bossbar.title.set(Component.literal("Updated!"))
        bossbar.tick()
        delay(1.Ticks)

        overridden.assertNotSent<ClientboundBossEventPacket>()
        assertNotNull(other.bossbarOperations().updatedName, "Expected a name update")
    }

    @GameTest(maxTicks = 400)
    fun `unchanged bossbar sends nothing`(context: TestContext) = context.test {
        val player = this.createTestPlayer()

        val bossbar = VirtualBossbar()
        bossbar.color.set(BossBarColor.RED)
        bossbar.startObservingAndSendPackets(player.asObserver())
        bossbar.tick()
        delay(1.Ticks)
        player.clearPackets()

        bossbar.color.set(BossBarColor.RED)
        bossbar.tick()
        delay(1.Ticks)

        player.assertNotSent<ClientboundBossEventPacket>()
    }

    @GameTest(maxTicks = 400)
    fun `bossbar overrides persist when observing again`(context: TestContext) = context.test {
        val player = this.createTestPlayer()

        val bossbar = VirtualBossbar()
        bossbar.title.set(Component.literal("Base"))
        bossbar.title.set(player, Component.literal("Override"))
        bossbar.startObservingAndSendPackets(player.asObserver())
        bossbar.stopObservingAndSendPackets(player.asObserver())
        player.clearPackets()

        bossbar.startObservingAndSendPackets(player.asObserver())
        delay(1.Ticks)

        val added = assertNotNull(player.bossbarOperations().added, "Expected an add packet")
        added shouldEqual Component.literal("Override")
    }

    @GameTest(maxTicks = 400)
    fun `bossbar style changes send single packet`(context: TestContext) = context.test {
        val player = this.createTestPlayer()

        val bossbar = VirtualBossbar()
        bossbar.startObservingAndSendPackets(player.asObserver())
        bossbar.tick()
        delay(1.Ticks)
        player.clearPackets()

        bossbar.color.set(BossBarColor.BLUE)
        bossbar.overlay.set(BossBarOverlay.NOTCHED_6)
        bossbar.tick()
        delay(1.Ticks)

        player.sent<ClientboundBossEventPacket>().size shouldEqual 1
        val style = assertNotNull(player.bossbarOperations().updatedStyle, "Expected a style update")
        style shouldEqual (BossBarColor.BLUE to BossBarOverlay.NOTCHED_6)
    }
}

private fun TestFakePlayer.bossbarOperations(): BossbarOperations {
    val operations = BossbarOperations()
    for (packet in this.sent<ClientboundBossEventPacket>()) {
        packet.dispatch(operations)
    }
    return operations
}

private class BossbarOperations: ClientboundBossEventPacket.Handler {
    var added: Component? = null
    var removed: UUID? = null
    var updatedName: Component? = null
    var updatedProgress: Float? = null
    var updatedStyle: Pair<BossBarColor, BossBarOverlay>? = null

    override fun add(
        uuid: UUID,
        name: Component,
        progress: Float,
        color: BossBarColor,
        overlay: BossBarOverlay,
        dark: Boolean,
        music: Boolean,
        fog: Boolean
    ) {
        this.added = name
    }

    override fun remove(uuid: UUID) {
        this.removed = uuid
    }

    override fun updateName(uuid: UUID, name: Component) {
        this.updatedName = name
    }

    override fun updateProgress(uuid: UUID, progress: Float) {
        this.updatedProgress = progress
    }

    override fun updateStyle(uuid: UUID, color: BossBarColor, overlay: BossBarOverlay) {
        this.updatedStyle = color to overlay
    }
}
