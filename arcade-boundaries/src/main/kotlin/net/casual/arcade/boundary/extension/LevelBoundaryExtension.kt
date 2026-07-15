/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.boundary.extension

import net.casual.arcade.boundary.LevelBoundary
import net.casual.arcade.boundary.utils.levelBoundary
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.entity.EntityStartTrackingEvent
import net.casual.arcade.events.server.entity.EntityStopTrackingEvent
import net.casual.arcade.events.server.level.LevelTickEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.extensions.SerializableExtension
import net.casual.arcade.extensions.event.LevelExtensionEvent
import net.casual.arcade.extensions.utils.getExtension
import net.casual.arcade.networking.utils.asObserver
import net.casual.arcade.utils.arcade
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import kotlin.jvm.optionals.getOrNull

internal class LevelBoundaryExtension(
    private val level: ServerLevel
): SerializableExtension {
    private var boundary: LevelBoundary? = null

    fun setBoundary(boundary: LevelBoundary) {
        this.removeBoundary()
        this.boundary = boundary
        for (player in this.level.players()) {
            boundary.addObserver(player.asObserver())
        }
    }

    fun removeBoundary() {
        this.boundary?.remove()
        this.boundary = null
    }

    fun getBoundary(): LevelBoundary? {
        return this.boundary
    }

    private fun startTrackingPlayer(player: ServerPlayer) {
        this.boundary?.addObserver(player.asObserver())
    }

    private fun stopTrackingPlayer(player: ServerPlayer) {
        this.boundary?.removeObserver(player.asObserver())
    }

    override fun id(): Identifier {
        return arcade("boundary")
    }

    override fun serialize(output: ValueOutput) {
        val border = this.boundary ?: return
        output.store("boundary", LevelBoundary.Settings.CODEC, border.createSettings())
    }

    override fun deserialize(input: ValueInput) {
        val settings = input.read("boundary", LevelBoundary.Settings.CODEC).getOrNull()
        if (settings == null) {
            this.boundary = null
            return
        }
        this.boundary = LevelBoundary(this.level, settings)
    }

    companion object {
        val ServerLevel.levelBoundaryExtension: LevelBoundaryExtension
            get() = this.getExtension()

        fun registerEvents() {
            GlobalEventHandler.Server.register<LevelExtensionEvent> {
                it.addExtension(::LevelBoundaryExtension)
            }
            GlobalEventHandler.Server.register<LevelTickEvent> {
                if (it.level.tickRateManager().runsNormally()) {
                    it.level.levelBoundary?.tick()
                }
            }
            GlobalEventHandler.Server.register<EntityStartTrackingEvent> { (entity, level) ->
                if (entity is ServerPlayer) {
                    level.getExtension<LevelBoundaryExtension>().startTrackingPlayer(entity)
                }
            }
            GlobalEventHandler.Server.register<EntityStopTrackingEvent> { (entity, level) ->
                if (entity is ServerPlayer) {
                    level.getExtension<LevelBoundaryExtension>().stopTrackingPlayer(entity)
                }
            }
        }
    }
}