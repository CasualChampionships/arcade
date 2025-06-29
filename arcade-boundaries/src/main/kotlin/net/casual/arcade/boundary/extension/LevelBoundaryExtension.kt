/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.boundary.extension

import net.casual.arcade.boundary.LevelBoundary
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.entity.EntityStartTrackingEvent
import net.casual.arcade.events.server.entity.EntityStopTrackingEvent
import net.casual.arcade.events.server.level.LevelTickEvent
import net.casual.arcade.extensions.DataExtension
import net.casual.arcade.extensions.event.LevelExtensionEvent
import net.casual.arcade.extensions.event.LevelExtensionEvent.Companion.getExtension
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import kotlin.jvm.optionals.getOrNull

public class LevelBoundaryExtension(
    private val level: ServerLevel
): DataExtension {
    private var boundary: LevelBoundary? = null

    private fun setBoundary(boundary: LevelBoundary) {
        this.removeBoundary()
        this.boundary = boundary
        for (player in this.level.players()) {
            boundary.addPlayer(player)
        }
    }

    private fun removeBoundary() {
        this.boundary?.clearPlayers()
        this.boundary = null
    }

    private fun startTrackingPlayer(player: ServerPlayer) {
        this.boundary?.addPlayer(player)
    }

    private fun stopTrackingPlayer(player: ServerPlayer) {
        this.boundary?.removePlayer(player)
    }

    override fun getId(): ResourceLocation {
        return ArcadeUtils.id("boundary")
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
        this.boundary = LevelBoundary(settings)
    }

    public companion object {
        /**
         * Gets and sets a [LevelBoundary] for a given [ServerLevel].
         * This may be `null` if no boundary has been set.
         */
        public var ServerLevel.levelBoundary: LevelBoundary?
            get() = this.getExtension<LevelBoundaryExtension>().boundary
            set(value) {
                val extension = this.getExtension<LevelBoundaryExtension>()
                if (value == null) extension.removeBoundary() else extension.setBoundary(value)
            }

        internal fun registerEvents() {
            GlobalEventHandler.Server.register<LevelExtensionEvent> {
                it.addExtension(::LevelBoundaryExtension)
            }
            GlobalEventHandler.Server.register<LevelTickEvent> {
                if (it.level.tickRateManager().runsNormally()) {
                    it.level.levelBoundary?.tick(it.level)
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