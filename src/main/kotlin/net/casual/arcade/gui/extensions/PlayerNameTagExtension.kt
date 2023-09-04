package net.casual.arcade.gui.extensions

import eu.pb4.polymer.virtualentity.api.ElementHolder
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData
import net.casual.arcade.extensions.Extension
import net.casual.arcade.gui.nametag.ArcadeNameTag
import net.casual.arcade.gui.nametag.PredicatedElementHolder
import net.casual.arcade.gui.suppliers.ComponentSupplier
import net.casual.arcade.utils.NameTagUtils.isWatching
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket
import net.minecraft.network.syncher.SynchedEntityData.DataValue
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.entity.Display
import org.joml.Vector3f
import java.util.function.Consumer
import java.util.function.Predicate

class PlayerNameTagExtension(
    private val owner: ServerPlayer
): Extension {
    private val tags = LinkedHashMap<ArcadeNameTag, SingleElementHolder>()

    internal fun addNameTag(tag: ArcadeNameTag) {
        val display = NameTagDisplay(tag.tag)
        display.billboardMode = Display.BillboardConstraints.VERTICAL
        val holder = SingleElementHolder(display, tag.predicate)
        EntityAttachment.ofTicking(holder, this.owner)
        VirtualEntityUtils.addVirtualPassenger(this.owner, *holder.entityIds.toIntArray())
        this.tags[tag] = holder
    }

    internal fun removeNameTag(tag: ArcadeNameTag) {
        val removed = this.tags.remove(tag) ?: return
        VirtualEntityUtils.removeVirtualPassenger(this.owner, *removed.entityIds.toIntArray())
        removed.destroy()

        this.updateNameTags()
    }

    internal fun updateNameTags() {
        for (holder in this.tags.values) {
            for (connection in holder.watchingPlayers) {
                holder.element.sendChangedTrackerEntries(connection.player, connection::send)
            }
        }
    }

    internal fun sneak() {
        for (element in this.getElements()) {
            element.seeThrough = false
        }
    }

    internal fun unsneak() {
        for (element in this.getElements()) {
            element.seeThrough = true
        }
    }

    private fun getElements(): List<NameTagDisplay> {
        return this.tags.values.map { it.element }
    }

    companion object {
        private const val DEFAULT = 0.7F
        private const val SHIFT = 0.3F
    }

    private inner class NameTagDisplay(private val generator: ComponentSupplier): TextDisplayElement() {
        init {
            this.ignorePositionUpdates()
        }

        override fun tick() {
            val text = this.generator.getComponent(owner)
            this.text = text
            super.tick()
        }

        public override fun sendChangedTrackerEntries(
            player: ServerPlayer,
            consumer: Consumer<Packet<ClientGamePacketListener>>
        ) {
            val changed = this.dataTracker.changedEntries ?: return

            val modifier = this.getTranslationModifier(changed)
            modifier(changed, DataValue.create(DisplayTrackedData.TRANSLATION, this.getTranslationFor(player)))
            consumer.accept(ClientboundSetEntityDataPacket(this.entityId, changed))
        }

        override fun sendTrackerUpdates() {
            if (this.dataTracker.isDirty) {
                val dirty = this.dataTracker.dirtyEntries
                val holder = this.holder
                if (dirty != null && holder != null) {
                    this.sendModifiedData(holder ,dirty)
                }
            }
        }

        private fun sendModifiedData(holder: ElementHolder, values: List<DataValue<*>>) {
            val modifier = this.getTranslationModifier(values)
            for (connection in holder.watchingPlayers) {
                val copy = ArrayList(values)
                modifier(copy, DataValue.create(DisplayTrackedData.TRANSLATION, this.getTranslationFor(connection.player)))
                connection.send(ClientboundSetEntityDataPacket(this.entityId, copy))
            }
        }

        private fun getTranslationFor(player: ServerPlayer): Vector3f {
            val translation = Vector3f(0.0F, DEFAULT, 0.0F)
            for (holder in tags.values) {
                if (!holder.isWatching(player)) {
                    continue
                }
                translation.add(holder.element.translation)
                if (holder.element == this) {
                    break
                }
                translation.add(0.0F, SHIFT, 0.0F)
            }
            return translation
        }

        private fun getTranslationModifier(values: List<DataValue<*>>): (MutableList<DataValue<*>>, DataValue<*>) -> Unit {
            for ((i, value) in values.withIndex()) {
                if (value.id == DisplayTrackedData.TRANSLATION.id) {
                    return { data, translation ->
                        data[i] = translation
                    }
                }
            }
            return { data, translation ->
                data.add(translation)
            }
        }
    }

    private inner class SingleElementHolder(
        val element: NameTagDisplay,
        predicate: Predicate<ServerPlayer>
    ): PredicatedElementHolder(predicate) {
        init {
            this.addElement(this.element)
        }

        override fun startWatching(connection: ServerGamePacketListenerImpl): Boolean {
            if (super.startWatching(connection)) {
                connection.send(ClientboundSetPassengersPacket(owner))
                for (holder in tags.values) {
                    holder.element.sendChangedTrackerEntries(connection.player, connection::send)
                }
                return true
            }
            return false
        }

        override fun stopWatching(player: ServerGamePacketListenerImpl): Boolean {
            if (super.stopWatching(player)) {
                updateNameTags()
                return true
            }
            return false
        }
    }
}