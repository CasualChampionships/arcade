/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.observer.extensions

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.utils.register
import net.casual.arcade.extensions.EntityExtension
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.event.EntityExtensionEvent
import net.casual.arcade.extensions.utils.getExtension
import net.casual.arcade.observer.Observer
import net.casual.arcade.observer.events.ObserverStartObservingEntityEvent
import net.casual.arcade.observer.events.ObserverStopObservingEntityEvent
import net.casual.arcade.utils.entity.EntityTransferReason
import net.casual.arcade.utils.impl.DelayedActions
import net.minecraft.world.entity.Entity

internal class EntityObserversExtension(entity: Entity): EntityExtension(entity) {
    private val observers = LinkedHashSet<Observer>()

    fun startObserving(observer: Observer) {
        if (this.observers.add(observer)) {
            val event = ObserverStartObservingEntityEvent(observer, this.entity)
            GlobalEventHandler.Server.broadcast(event)
        }
    }

    fun stopObserving(observer: Observer) {
        if (this.observers.remove(observer)) {
            val event = ObserverStopObservingEntityEvent(observer, this.entity)
            GlobalEventHandler.Server.broadcast(event)
        }
    }

    fun getObservers(): LinkedHashSet<Observer> {
        return this.observers
    }

    override fun transfer(
        entity: Entity,
        reason: EntityTransferReason,
        delayed: DelayedActions
    ): Extension {
        return EntityObserversExtension(entity)
    }

    companion object {
        @JvmStatic
        val Entity.observersExtension: EntityObserversExtension
            get() = this.getExtension()

        fun registerEvents() {
            GlobalEventHandler.Server.register<EntityExtensionEvent>(priority = 2) {
                it.addExtension(::EntityObserversExtension)
            }
        }
    }
}