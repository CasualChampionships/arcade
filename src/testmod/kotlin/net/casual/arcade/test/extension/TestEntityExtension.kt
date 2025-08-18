package net.casual.arcade.test.extension

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.entity.EntityTickEvent
import net.casual.arcade.extensions.EntityExtension
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.TransferableEntityExtension.TransferReason
import net.casual.arcade.extensions.event.EntityExtensionEvent
import net.casual.arcade.extensions.event.EntityExtensionEvent.Companion.getExtension
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.impl.DelayedInvokers
import net.minecraft.world.entity.Entity

class TestEntityExtension(entity: Entity): EntityExtension(entity) {
    var value: Int = 0

    override fun transfer(entity: Entity, reason: TransferReason, delayed: DelayedInvokers): Extension {
        return TestEntityExtension(entity)
    }

    companion object {
        internal fun registerEvents() {
            GlobalEventHandler.Server.register<EntityExtensionEvent> {
                it.addExtension(::TestEntityExtension)
            }
            GlobalEventHandler.Server.register<EntityTickEvent> { (entity) ->
                try {
                    val extension = entity.getExtension<TestEntityExtension>()
                    extension.value
                } catch (e: Exception) {
                    ArcadeUtils.logger.error("Failed to get extension", e)
                }
            }
        }
    }
}