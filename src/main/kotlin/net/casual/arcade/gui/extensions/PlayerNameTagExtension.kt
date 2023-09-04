package net.casual.arcade.gui.extensions

import eu.pb4.polymer.virtualentity.api.ElementHolder
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment
import eu.pb4.polymer.virtualentity.api.elements.AbstractElement
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData
import it.unimi.dsi.fastutil.ints.IntList
import net.casual.arcade.extensions.Extension
import net.casual.arcade.gui.nametag.ArcadeNameTag
import net.casual.arcade.gui.nametag.PredicatedElementHolder
import net.casual.arcade.gui.suppliers.ComponentSupplier
import net.casual.arcade.utils.NameTagUtils.isWatching
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket
import net.minecraft.network.syncher.SynchedEntityData.DataValue
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import java.util.function.Consumer
import java.util.function.Predicate

// This is low-key some of the most cursed shit
// I've ever written, lets hope I don't have to
// ever touch it again :)
class PlayerNameTagExtension(
    private val owner: ServerPlayer
): Extension {
    private val tags = LinkedHashMap<ArcadeNameTag, SingleElementHolder>()

    internal fun addNameTag(tag: ArcadeNameTag) {
        val display = NameTagDisplay(tag.tag)
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
            element.sneak()
        }
    }

    internal fun unsneak() {
        for (element in this.getElements()) {
            element.unsneak()
        }
    }

    private fun getElements(): List<NameTagDisplay> {
        return this.tags.values.map { it.element }
    }

    companion object {
        private const val DEFAULT = 0.7F
        private const val SHIFT = 0.3F
    }

    private inner class NameTagDisplay(private val generator: ComponentSupplier): AbstractElement() {
        private val background = TextDisplayElement()
        private val foreground = TextDisplayElement()

        init {
            this.initialiseDisplay(this.background)
            this.initialiseDisplay(this.foreground)

            this.background.seeThrough = true
            this.background.textOpacity = 30.toByte()
            this.foreground.seeThrough = false
            this.foreground.textOpacity = 255.toByte()
            this.foreground.setBackground(0)
        }

        fun sneak() {
            this.background.seeThrough = false
            this.foreground.textOpacity = -127
        }

        fun unsneak() {
            this.background.seeThrough = true
            this.foreground.textOpacity = 255.toByte()
        }

        private fun initialiseDisplay(display: TextDisplayElement) {
            display.ignorePositionUpdates()
            display.billboardMode = Display.BillboardConstraints.CENTER
        }

        override fun getEntityIds(): IntList {
            return IntList.of(this.background.entityId, this.foreground.entityId)
        }

        override fun tick() {
            val text = this.generator.getComponent(owner)
            this.foreground.text = text
            this.background.text = text
            this.sendTrackerUpdates()
        }

        override fun startWatching(player: ServerPlayer, consumer: Consumer<Packet<ClientGamePacketListener>>) {
            consumer.accept(this.createSpawnPacket(this.background))
            consumer.accept(this.createSpawnPacket(this.foreground))

            this.sendChangedTrackerEntries(player, consumer)
        }

        override fun stopWatching(player: ServerPlayer, packetConsumer: Consumer<Packet<ClientGamePacketListener>>) {

        }

        override fun notifyMove(oldPos: Vec3, currentPos: Vec3, delta: Vec3) {

        }

        private fun createSpawnPacket(display: TextDisplayElement): ClientboundAddEntityPacket {
            return ClientboundAddEntityPacket(
                display.entityId,
                display.uuid,
                owner.x,
                owner.y,
                owner.z,
                display.pitch,
                display.yaw,
                EntityType.TEXT_DISPLAY,
                0,
                Vec3.ZERO,
                display.yaw.toDouble()
            )
        }

        fun sendChangedTrackerEntries(
            player: ServerPlayer,
            consumer: Consumer<Packet<ClientGamePacketListener>>
        ) {
            val translation = this.getTranslationFor(player)
            this.sendChangedTrackerEntries(this.background, translation, consumer)
            this.sendChangedTrackerEntries(this.foreground, translation, consumer)
        }

        private fun sendChangedTrackerEntries(
            display: TextDisplayElement,
            translation: Vector3f,
            consumer: Consumer<Packet<ClientGamePacketListener>>
        ) {
            val changed = display.dataTracker.changedEntries ?: return

            val modifier = this.getTranslationModifier(changed)
            modifier(changed, DataValue.create(DisplayTrackedData.TRANSLATION, translation))
            consumer.accept(ClientboundSetEntityDataPacket(display.entityId, changed))
        }

        fun sendTrackerUpdates() {
            this.sendTrackerUpdates(this.background)
            this.sendTrackerUpdates(this.foreground)
        }

        private fun sendTrackerUpdates(display: TextDisplayElement) {
            if (display.dataTracker.isDirty) {
                val dirty = display.dataTracker.dirtyEntries
                val holder = this.holder
                if (dirty != null && holder != null) {
                    this.sendModifiedData(display, holder ,dirty)
                }
            }
        }

        private fun sendModifiedData(display: TextDisplayElement, holder: ElementHolder, values: List<DataValue<*>>) {
            val modifier = this.getTranslationModifier(values)
            for (connection in holder.watchingPlayers) {
                val copy = ArrayList(values)
                modifier(copy, DataValue.create(DisplayTrackedData.TRANSLATION, this.getTranslationFor(connection.player)))
                connection.send(ClientboundSetEntityDataPacket(display.entityId, copy))
            }
        }

        private fun getTranslationFor(player: ServerPlayer): Vector3f {
            val translation = Vector3f(0.0F, DEFAULT, 0.0F)
            for (holder in tags.values) {
                if (!holder.isWatching(player)) {
                    continue
                }
                translation.add(holder.element.background.translation)
                if (holder.element === this) {
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