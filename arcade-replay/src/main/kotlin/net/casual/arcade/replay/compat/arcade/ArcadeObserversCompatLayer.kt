/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.compat.arcade

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.observer.ArcadeObservers
import net.casual.arcade.observer.events.ObserverClientboundPacketEvent
import net.casual.arcade.observer.Observer
import net.casual.arcade.observer.utils.startObserving
import net.casual.arcade.observer.utils.stopObserving
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorder
import net.casual.arcade.utils.math.location.LocationWithLevel
import net.casual.arcade.utils.math.location.with
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec2

internal object ArcadeObserversCompatLayer {
    val loaded: Boolean = FabricLoader.getInstance().isModLoaded(ArcadeObservers.MOD_ID)

    fun modifyPacketForObserver(recorder: ReplayChunkRecorder, packet: Packet<*>): Packet<*> {
        if (this.loaded) {
            val event = ObserverClientboundPacketEvent(this.observerFor(recorder), packet)
            GlobalEventHandler.Server.broadcast(event)
            return event.packet
        }
        return packet
    }

    fun observerFor(recorder: ReplayChunkRecorder): Observer {
        require(this.loaded) {
            "Cannot create observer for recorder as ${ArcadeObservers.MOD_ID} is not loaded"
        }
        return recorder.observer as Observer
    }

    @JvmStatic
    fun startObservingLevel(recorder: ReplayChunkRecorder) {
        if (this.loaded) {
            val observer = this.observerFor(recorder)
            observer.startObserving(recorder.level)
        }
    }

    @JvmStatic
    fun stopObservingLevel(recorder: ReplayChunkRecorder) {
        if (this.loaded) {
            val observer = this.observerFor(recorder)
            observer.stopObserving(recorder.level)
        }
    }

    @JvmStatic
    fun startObservingEntity(recorder: ReplayChunkRecorder, entity: Entity) {
        if (this.loaded) {
            val observer = this.observerFor(recorder)
            observer.startObserving(entity)
        }
    }

    @JvmStatic
    fun stopObservingEntity(recorder: ReplayChunkRecorder, entity: Entity) {
        if (this.loaded) {
            val observer = this.observerFor(recorder)
            observer.stopObserving(entity)
        }
    }

    internal fun createObserver(recorder: ReplayChunkRecorder): Any? {
        if (this.loaded) {
            return ChunkRecorderObserver(recorder)
        }
        return null
    }

    private class ChunkRecorderObserver(
        private val recorder: ReplayChunkRecorder
    ): Observer {
        override val context = Observer.Context()

        override val location: LocationWithLevel<ServerLevel>
            get() = this.recorder.position.with(Vec2.ZERO).with(this.recorder.level)

        override fun send(packet: Packet<*>) {
            this.recorder.record(packet)
        }
    }
}