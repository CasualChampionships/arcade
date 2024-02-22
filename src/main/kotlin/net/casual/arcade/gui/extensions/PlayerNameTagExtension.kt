package net.casual.arcade.gui.extensions

import eu.pb4.polymer.virtualentity.api.ElementHolder
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment
import eu.pb4.polymer.virtualentity.api.elements.AbstractElement
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData
import it.unimi.dsi.fastutil.ints.IntList
import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.gui.nametag.ArcadeNameTag
import net.casual.arcade.gui.predicate.PlayerObserverPredicate
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

// This is low-key some of the most cursed code
// I've ever written, let's hope I don't have to
// ever touch it again :)
internal class PlayerNameTagExtension(
    owner: ServerGamePacketListenerImpl
): PlayerExtension(owner) {
    private val tags = LinkedHashMap<ArcadeNameTag, Holder>()

    fun respawn(player: ServerPlayer) {
        for (holder in this.tags.values) {
            EntityAttachment.ofTicking(holder, player)
            VirtualEntityUtils.addVirtualPassenger(player, *holder.entityIds.toIntArray())
        }
    }

    internal fun addNameTag(tag: ArcadeNameTag) {
        val display = NameTagDisplay(tag)
        val holder = Holder(display, tag.observable)
        EntityAttachment.ofTicking(holder, this.player)
        VirtualEntityUtils.addVirtualPassenger(this.player, *holder.entityIds.toIntArray())
        this.tags[tag] = holder
    }

    internal fun removeNameTag(tag: ArcadeNameTag) {
        val removed = this.tags.remove(tag) ?: return
        VirtualEntityUtils.removeVirtualPassenger(this.player, *removed.entityIds.toIntArray())
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

    internal fun getHolder(tag: ArcadeNameTag): Holder? {
        return this.tags[tag]
    }

    private fun getElements(): List<NameTagDisplay> {
        return this.tags.values.map { it.element }
    }

    companion object {
        private const val DEFAULT = 0.3F
        private const val SHIFT = 0.3F
    }

    // Okay, so I think I should write some implementation comments just
    // encase, because this might look confusing.
    // So a vanilla name tag is rendered twice - once for the 'background'
    // nametag (the see-through rectangle and text), and once for the
    // 'foreground' which is the non-see-through text, it has no rectangle.
    // This class handles both of these as separate Display entities.
    internal inner class NameTagDisplay(private val tag: ArcadeNameTag): AbstractElement() {
        private val background = TextDisplayElement()
        private val foreground = TextDisplayElement()

        private var ticks = 0

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
            // When the player sneaks, the background becomes
            // non-see-through and the foreground becomes invisible
            this.background.seeThrough = false
            this.foreground.textOpacity = -127

            this.sendTrackerUpdates()
        }

        fun unsneak() {
            // When the player un-sneaks, we return to default
            this.background.seeThrough = true
            // Not sure why 255 is required here, 128 doesn't work.
            this.foreground.textOpacity = 255.toByte()

            this.sendTrackerUpdates()
        }

        private fun initialiseDisplay(display: TextDisplayElement) {
            // Our nametags are going to be 'riding' the player,
            // so we ignore all position updates
            display.ignorePositionUpdates()
            // The nametag rotates with the player's camera.
            display.billboardMode = Display.BillboardConstraints.CENTER
        }

        override fun getEntityIds(): IntList {
            return IntList.of(this.background.entityId, this.foreground.entityId)
        }

        override fun tick() {
            if (this.ticks++ % this.tag.interval == 0) {
                val text = this.tag.tag.getComponent(player)
                this.foreground.text = text
                this.background.text = text
                this.sendTrackerUpdates()
            }
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
                player.x,
                player.y,
                player.z,
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

        // Okay, so this is where it gets REALLY jank
        // Essentially since certain nametags aren't visible to
        // some players, the heights of each nametag are going to
        // be different for every observer.
        private fun sendModifiedData(display: TextDisplayElement, holder: ElementHolder, values: List<DataValue<*>>) {
            // Either we have to add TRANSLATION or replace it
            val modifier = this.getTranslationModifier(values)
            for (connection in holder.watchingPlayers) {
                val copy = ArrayList(values)
                modifier(copy, DataValue.create(DisplayTrackedData.TRANSLATION, this.getTranslationFor(connection.player)))
                connection.send(ClientboundSetEntityDataPacket(display.entityId, copy))
            }
        }

        // We manually calculate the translation for every observer.
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

    internal inner class Holder(
        val element: NameTagDisplay,
        val predicate: PlayerObserverPredicate
    ): ElementHolder() {
        init {
            this.addElement(this.element)
        }

        override fun onTick() {
            this.attachment?.updateCurrentlyTracking(ArrayList(this.watchingPlayers))
        }

        override fun startWatching(connection: ServerGamePacketListenerImpl): Boolean {
            if (this.predicate.observable(player, connection.player)) {
                if (super.startWatching(connection)) {
                    this.sendDataTo(connection.player, connection::send)
                    return true
                }
            } else {
                this.stopWatching(connection)
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

        internal fun sendDataTo(observer: ServerPlayer, sender: Consumer<Packet<ClientGamePacketListener>>) {
            sender.accept(ClientboundSetPassengersPacket(player))
            for (holder in tags.values) {
                holder.element.sendChangedTrackerEntries(observer, sender)
            }
        }
    }
}