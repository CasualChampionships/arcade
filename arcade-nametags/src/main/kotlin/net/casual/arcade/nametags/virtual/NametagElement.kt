/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.nametags.virtual

import eu.pb4.polymer.virtualentity.api.elements.AbstractElement
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement
import it.unimi.dsi.fastutil.ints.IntList
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.casual.arcade.nametags.Nametag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import java.util.function.Consumer

public class NametagElement(
    private val entity: Entity,
    public val nametag: Nametag
): AbstractElement() {
    private val watching = ReferenceOpenHashSet<ServerGamePacketListenerImpl>()
    private val listeners = ObjectOpenHashSet<Consumer<Packet<ClientGamePacketListener>>>()

    private val background = TextDisplayElement()
    private val foreground = TextDisplayElement()

    private val shift = NametagHeightElement(this.nametag.height)

    private var ticks = 0
    private var sneaking = false

    init {
        this.initializeElement(this.background)
        this.initializeElement(this.foreground)

        this.background.seeThrough = this.nametag.isVisibleThroughWalls(this.entity)
        this.background.textOpacity = 30.toByte()
        this.foreground.seeThrough = false
        this.foreground.textOpacity = 255.toByte()
        this.foreground.background = 0
    }

    public fun getMountingId(): Int {
        return this.shift.id
    }

    public fun getObservers(): Set<ServerGamePacketListenerImpl> {
        return this.watching
    }

    public fun sneak() {
        // When the player sneaks, the background becomes
        // non-see-through and the foreground becomes invisible
        this.background.seeThrough = false
        this.foreground.textOpacity = -127

        this.sendDirtyPackets()
        this.sneaking = true
    }

    public fun unsneak() {
        // When the player un-sneaks, we return to default
        this.background.seeThrough = this.nametag.isVisibleThroughWalls(this.entity)
        // Not sure why 255 is required here, 128 doesn't work.
        this.foreground.textOpacity = 255.toByte()

        this.sendDirtyPackets()
        this.sneaking = false
    }

    public fun sendSpawnPackets(player: ServerPlayer, consumer: Consumer<Packet<ClientGamePacketListener>>) {
        this.background.startWatching(player, consumer)
        this.foreground.startWatching(player, consumer)
        this.shift.startWatching(player, consumer)
    }

    public fun sendRemovePackets(player: ServerPlayer, consumer: Consumer<Packet<ClientGamePacketListener>>) {
        this.background.stopWatching(player, consumer)
        this.foreground.stopWatching(player, consumer)
        this.shift.stopWatching(player, consumer)
        consumer.accept(ClientboundRemoveEntitiesPacket(this.entityIds))
    }

    public fun addDirtyListener(listener: Consumer<Packet<ClientGamePacketListener>>) {
        this.listeners.add(listener)
    }

    override fun tick() {
        val interval = this.nametag.updateInterval.ticks
        if (interval > 0 && this.ticks++ % interval == 0) {
            this.update()
        }
    }

    public fun update() {
        val updated = this.nametag.getComponent(this.entity)
        this.foreground.text = updated
        this.background.text = updated

        this.background.seeThrough = !this.sneaking && this.nametag.isVisibleThroughWalls(this.entity)

        this.sendDirtyPackets()
    }

    override fun startWatching(player: ServerPlayer, consumer: Consumer<Packet<ClientGamePacketListener>>) {
        if (this.watching.add(player.connection)) {
            this.background.startWatching(player, consumer)
            this.foreground.startWatching(player, consumer)
            this.shift.startWatching(player, consumer)
        }
    }

    override fun stopWatching(player: ServerPlayer, consumer: Consumer<Packet<ClientGamePacketListener>>) {
        if (this.watching.remove(player.connection)) {
            this.background.stopWatching(player, consumer)
            this.foreground.stopWatching(player, consumer)
            this.shift.stopWatching(player, consumer)
        }
    }

    override fun getEntityIds(): IntList {
        return IntList.of(this.background.entityId, this.foreground.entityId, this.shift.id)
    }

    override fun notifyMove(oldPos: Vec3, currentPos: Vec3, delta: Vec3) {

    }

    private fun initializeElement(element: TextDisplayElement) {
        // Our nametags are going to be 'riding' the player,
        // so we ignore all position updates
        element.ignorePositionUpdates()
        // The nametag rotates with the player's camera.
        element.billboardMode = Display.BillboardConstraints.CENTER
        element.translation = Vector3f(0.0F, -0.2F, 0.0F)
        element.isInvisible = true

        // We spawn the entity so low that the lerp animation
        // happens so fast it's basically instant
        element.offset = Vec3(0.0, -500.0, 0.0)
    }

    private fun sendDirtyPackets() {
        this.sendDirtyPacketsFor(this.foreground)
        this.sendDirtyPacketsFor(this.background)
    }

    private fun sendDirtyPacketsFor(element: TextDisplayElement) {
        val dirty = element.dataTracker.dirtyEntries
        if (dirty != null) {
            val packet = ClientboundSetEntityDataPacket(element.entityId, dirty)
            for (watcher in this.watching) {
                watcher.send(packet)
            }

            for (listener in this.listeners) {
                listener.accept(packet)
            }
        }
    }
}