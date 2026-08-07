/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals.camera

import net.casual.arcade.observer.Observer
import net.casual.arcade.observer.tracker.ObserverTracker
import net.casual.arcade.observer.tracker.SimpleObserverTracker
import net.casual.arcade.observer.utils.asObserver
import net.casual.arcade.observer.utils.asPlayerOrNull
import net.casual.arcade.utils.ClientboundPlayerInfoUpdatePacket
import net.casual.arcade.utils.arcade
import net.casual.arcade.utils.entity.teleportTo
import net.casual.arcade.utils.math.location.Location
import net.casual.arcade.utils.math.location.LocationWithLevel
import net.casual.arcade.utils.math.location.with
import net.casual.arcade.utils.network.PacketSender
import net.casual.arcade.utils.player.getGameMode
import net.casual.arcade.virtual.entity.attachment.SimpleVirtualEntityAttachment
import net.casual.arcade.virtual.entity.attachment.anchor.AttachmentAnchor
import net.casual.arcade.virtual.entity.display.SimpleVirtualItemDisplay
import net.casual.arcade.virtual.entity.utils.attachWithParentObservers
import net.casual.arcade.virtual.visuals.VirtualVisual
import net.casual.arcade.virtual.visuals.utils.stopObservingAndSendPackets
import net.minecraft.network.protocol.game.ClientboundGameEventPacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import java.util.*
import net.casual.arcade.utils.ClientboundSetCameraPacket as createSetCameraPacket

public class VirtualCamera(
    private val level: ServerLevel,
    private var location: Location
): VirtualVisual, AttachmentAnchor {
    private val attachment = SimpleVirtualEntityAttachment(this)
    private val camera = this.attachment.attachWithParentObservers(::SimpleVirtualItemDisplay)

    private var path: CameraPath? = null
    private var loop: Boolean = false
    private var progress = -1

    override val observers: ObserverTracker = SimpleObserverTracker()

    public constructor(location: LocationWithLevel<ServerLevel>): this(location.level, location.location)

    init {
        this.camera.setTeleportationInterpolation(3)
        this.camera.setStartInterpolation(0)

        this.camera.syncExactLocation = true
    }

    public fun setPosition(position: Vec3) {
        this.location = position.with(this.location.rotation)
    }

    public fun setRotation(rotation: Vec2) {
        this.location = this.location.position.with(rotation)
    }

    public fun setLocation(location: Location) {
        this.location = location
    }

    public fun setPath(path: CameraPath) {
        this.path = path
    }

    public fun startPath(loop: Boolean = this.loop) {
        this.progress = 0
        this.loop = loop
    }

    public fun stopPath() {
        this.progress = -1
    }

    override fun location(): Location {
        return this.location
    }

    override fun level(): ServerLevel {
        return this.level
    }

    override fun tick() {
        this.attachment.tick()

        this.tickPath()
    }

    override fun sendSpawnPackets(observer: Observer, sender: PacketSender) {
        val player = observer.asPlayerOrNull()
        if (player != null) {
            this.sendGamemodePacket(player, GameType.SPECTATOR, sender)
            sender.send(ClientboundSetCameraPacket(player))
        }

        this.attachment.sendObservingAttachedSpawnPackets(observer, sender)
        sender.send(createSetCameraPacket(this.camera.id))
    }

    override fun sendDespawnPackets(observer: Observer, sender: PacketSender) {
        this.attachment.sendObservingAttachedDespawnPackets(observer, sender)

        val player = observer.asPlayerOrNull()
        if (player != null) {
            this.sendGamemodePacket(player, player.getGameMode(), sender)
            sender.send(ClientboundSetCameraPacket(player))
        }
    }

    override fun onStartObserving(observer: Observer) {
        val previous = observer.context.get(CURRENT_CAMERA)
        previous?.stopObservingAndSendPackets(observer)

        observer.context.set(CURRENT_CAMERA, this)

        val player = observer.asPlayerOrNull()
        if (player != null) {
            player.setCamera(player)
            player.teleportTo(this.location().with(this.level))
        }

        this.attachment.startObservingAttached(observer, quietly = true)
    }

    override fun onStopObserving(observer: Observer) {
        observer.context.remove(CURRENT_CAMERA)

        this.attachment.stopObservingAttached(observer, quietly = true)
    }

    private fun sendGamemodePacket(player: ServerPlayer, gamemode: GameType, sender: PacketSender) {
        val packet = ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, gamemode.id.toFloat())
        sender.send(packet)
        val action = EnumSet.of(Action.UPDATE_GAME_MODE)
        val entry = listOf(
            Entry(player.uuid, null, false, 0, gamemode, null, false, 0, null)
        )
        sender.send(ClientboundPlayerInfoUpdatePacket(action, entry))
    }

    private fun tickPath() {
        val path = this.path ?: return
        if (this.progress < 0) {
            return
        }

        val keyframes = path.keyframes
        val times = path.times

        var index = 0
        while (index < times.size - 2) {
            if (this.progress < times[index + 1]) {
                break
            }
            index++
        }

        val nextIndex = index + 1
        val targetKeyframe = keyframes[nextIndex]

        val startTick = times[index]
        val segmentDuration = targetKeyframe.duration.ticks
        val ticksIntoSegment = this.progress - startTick
        val segmentProgress = if (segmentDuration <= 0) 1.0F else ticksIntoSegment.toFloat() / segmentDuration.toFloat()

        val target = path.interpolator.interpolate(path, index, this.progress.toDouble(), segmentProgress)

        this.setLocation(target)

        if (++this.progress >= path.duration.ticks) {
            if (this.loop) {
                this.progress = 0
            } else {
                this.stopPath()
            }
        }
    }

    public companion object {
        private val CURRENT_CAMERA = Observer.Context.Key<VirtualCamera>(arcade("virtual_camera"))

        /**
         * Gets the camera the given [player] is currently observing.
         *
         * @param player The player.
         * @return The camera being observed, or `null` if they aren't observing one.
         */
        @JvmStatic
        public fun getCurrentCamera(player: ServerPlayer): VirtualCamera? {
            return player.asObserver().context.get(CURRENT_CAMERA)
        }
    }
}