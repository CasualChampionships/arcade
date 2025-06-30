/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.managers

import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.player.PlayerClientboundPacketEvent
import net.casual.arcade.events.server.player.PlayerClientboundPacketEvent.Companion.replacePacket
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.MinigamePauseEvent
import net.casual.arcade.minigame.events.MinigameUnpauseEvent
import net.minecraft.network.protocol.game.ClientboundTickingStatePacket
import net.minecraft.network.protocol.game.ClientboundTickingStepPacket
import net.minecraft.server.ServerTickRateManager
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.TimeUtil
import net.minecraft.world.entity.Entity
import kotlin.math.max

public class MinigameTickRateManager(
    private val minigame: Minigame
): ServerTickRateManager(minigame.server) {
    internal fun initialize() {
        this.minigame.events.register<MinigamePauseEvent> {
            if (this.minigame.settings.tickFreezeOnPause.get()) {
                this.setFrozen(true)
            }
        }
        this.minigame.events.register<MinigameUnpauseEvent> {
            if (this.minigame.settings.tickFreezeOnPause.get()) {
                this.setFrozen(false)
            }
        }
        this.minigame.events.register<PlayerClientboundPacketEvent>(::onPlayerClientboundPacketEvent)
    }

    override fun setTickRate(tickRate: Float) {
        this.tickrate = max(tickRate.toDouble(), 1.0).toFloat()
        this.nanosecondsPerTick = (TimeUtil.NANOSECONDS_PER_SECOND.toDouble() / this.tickrate).toLong()
        this.updateStateToClients()
    }

    override fun setFrozen(frozen: Boolean) {
        this.isFrozen = frozen
        this.updateStateToClients()
    }

    override fun stepGameIfPaused(ticks: Int): Boolean {
        if (this.isFrozen()) {
            this.frozenTicksToRun = ticks
            this.updateStepTicks()
            return true
        }
        return false
    }

    override fun stopStepping(): Boolean {
        if (this.frozenTicksToRun > 0) {
            this.frozenTicksToRun = 0
            this.updateStateToClients()
            return true
        }
        return false
    }

    override fun isEntityFrozen(entity: Entity): Boolean {
        if (entity is ServerPlayer) {
            return this.isPlayerFrozen(entity)
        }
        val isFrozen = super.isEntityFrozen(entity)
        if (isFrozen) {
            return true
        }
        if (this.minigame.settings.tickFreezeEntities.get()) {
            return true
        }
        if (this.minigame.effects.isTickFrozen(entity)) {
            return true
        }
        return false
    }

    private fun isPlayerFrozen(player: ServerPlayer): Boolean {
        if (this.minigame.settings.tickFreezeOnPause.get(player) && this.minigame.paused) {
            return true
        }
        if (this.minigame.settings.tickFreezeEntities.get(player)) {
            return true
        }
        if (this.minigame.effects.isTickFrozen(player)) {
            return true
        }
        return false
    }

    private fun updateStateToClients() {
        this.minigame.players.broadcast(ClientboundTickingStatePacket.from(this))
    }

    private fun updateStepTicks() {
        this.minigame.players.broadcast(ClientboundTickingStepPacket.from(this))
    }

    private fun onPlayerClientboundPacketEvent(event: PlayerClientboundPacketEvent) {
        event.replacePacket { _, packet ->
            when (packet) {
                is ClientboundTickingStatePacket -> ClientboundTickingStatePacket.from(this)
                is ClientboundTickingStepPacket -> ClientboundTickingStepPacket.from(this)
                else -> packet
            }
        }
    }
}