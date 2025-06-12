/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.entity.display

import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import net.casual.arcade.utils.createValue
import net.casual.arcade.visuals.elements.PlayerSpecificElement
import net.casual.arcade.visuals.elements.UniversalElement
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import java.util.function.Consumer

public open class SpecificComponentDisplayElement(
    private var component: PlayerSpecificElement<Component>
): TextDisplayElement() {
    private val components = Reference2ObjectOpenHashMap<ServerGamePacketListenerImpl, Component>()

    public fun setComponent(component: PlayerSpecificElement<Component>) {
        this.component = component
    }

    override fun tick() {
        this.updateSpecificComponents()
        super.tick()
    }

    override fun stopWatching(player: ServerPlayer, packetConsumer: Consumer<Packet<ClientGamePacketListener>>) {
        super.stopWatching(player, packetConsumer)
        this.components.remove(player.connection)
    }

    @Deprecated(
        "You should use the element setter instead", ReplaceWith(
            "this.setComponent(UniversalElement.constant(text))",
            "net.casual.arcade.visuals.elements.UniversalElement"
        )
    )
    override fun setText(text: Component) {
        this.setComponent(UniversalElement.constant(text))
    }

    @Deprecated("Each component is specific to a player, cannot get text without context")
    override fun getText(): Component {
        throw UnsupportedOperationException("This method should not be called")
    }

    private fun updateSpecificComponents() {
        val holder = this.holder
        if (holder == null) {
            this.components.clear()
            return
        }

        for (connection in holder.watchingPlayers) {
            val previous = this.components[connection]
            val updated = this.component.get(connection.player)
            if (previous != updated) {
                this.components[connection] = updated
                val entry = DisplayTrackedData.Text.TEXT.createValue(updated)
                val packet = ClientboundSetEntityDataPacket(this.entityId, listOf(entry))
                connection.send(packet)
            }
        }
    }
}