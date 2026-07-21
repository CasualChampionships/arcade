/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.observer.extensions

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.entity.EntityStartTrackingEvent
import net.casual.arcade.events.server.entity.EntityStopTrackingEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.event.LevelExtensionEvent
import net.casual.arcade.extensions.utils.getExtension
import net.casual.arcade.observer.Observer
import net.casual.arcade.observer.events.ObserverStartObservingLevelEvent
import net.casual.arcade.observer.events.ObserverStopObservingLevelEvent
import net.casual.arcade.observer.tracker.ObserverTracker
import net.casual.arcade.observer.tracker.SimpleObserverTracker
import net.casual.arcade.observer.utils.asObserver
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

internal class LevelObserversExtension(private val level: ServerLevel): Extension {
    private val observers = SimpleObserverTracker()

    fun startObserving(observer: Observer) {
        if (this.observers.startObserving(observer)) {
            val event = ObserverStartObservingLevelEvent(observer, this.level)
            GlobalEventHandler.Server.broadcast(event)
        }
    }

    fun stopObserving(observer: Observer) {
        if (this.observers.isObserving(observer)) {
            val event = ObserverStopObservingLevelEvent(observer, this.level)
            GlobalEventHandler.Server.broadcast(event)
            this.observers.stopObserving(observer)
        }
    }

    fun getObservers(): ObserverTracker {
        return this.observers
    }

    companion object {
        @JvmStatic
        val ServerLevel.observersExtension: LevelObserversExtension
            get() = this.getExtension()

        fun registerEvents() {
            GlobalEventHandler.Server.register<LevelExtensionEvent>(priority = 2) {
                it.addExtension(::LevelObserversExtension)
            }
            GlobalEventHandler.Server.register<EntityStartTrackingEvent>(phase = EntityStartTrackingEvent.PHASE_POST) { (entity, level) ->
                if (entity is ServerPlayer) {
                    level.observersExtension.startObserving(entity.asObserver())
                }
            }
            GlobalEventHandler.Server.register<EntityStopTrackingEvent>(phase = EntityStartTrackingEvent.PHASE_PRE) { (entity, level) ->
                if (entity is ServerPlayer) {
                    level.observersExtension.stopObserving(entity.asObserver())
                }
            }
        }
    }
}