/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.camera

import net.casual.arcade.utils.ClientboundPlayerInfoUpdatePacket
import net.casual.arcade.utils.asClientGamePacket
import net.casual.arcade.utils.entity.teleportTo
import net.casual.arcade.utils.math.location.Location
import net.casual.arcade.utils.math.location.Location.Companion.withRotation
import net.casual.arcade.utils.math.location.LocationWithLevel
import net.casual.arcade.utils.player.getGameMode
import net.casual.arcade.virtual.entity.attachment.SimpleVirtualEntityAttachment
import net.casual.arcade.virtual.entity.attachment.anchor.AttachmentAnchor
import net.casual.arcade.virtual.entity.display.SimpleVirtualItemDisplay
import net.casual.arcade.virtual.entity.utils.asObserver
import net.casual.arcade.virtual.entity.utils.attachWithParentObservers
import net.casual.arcade.virtual.entity.utils.sendBundledSpawnPackets
import net.casual.arcade.visuals.core.TickableVisualElement
import net.casual.arcade.visuals.core.TrackingVisualElement
import net.casual.arcade.visuals.extensions.PlayerCameraExtension.Companion.cameraExtension
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundGameEventPacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import java.util.*
import java.util.function.Consumer
import net.casual.arcade.utils.ClientboundSetCameraPacket as createSetCameraPacket

public class PlayerCamera(
    private val level: ServerLevel,
    private var location: Location
): TrackingVisualElement(), TickableVisualElement, AttachmentAnchor {
    private val attachment = SimpleVirtualEntityAttachment(this)
    private val camera = this.attachment.attachWithParentObservers(::SimpleVirtualItemDisplay)

    private var path: CameraPath? = null
    private var loop: Boolean = false
    private var progress = -1

    public constructor(location: LocationWithLevel<ServerLevel>): this(location.level, location.location)

    init {
        this.camera.setTeleportationInterpolation(3)
        this.camera.setStartInterpolation(0)
    }

    public fun setPosition(position: Vec3) {
        this.location = position.withRotation(this.location.rotation)
    }

    public fun setRotation(rotation: Vec2) {
        this.location = this.location.position.withRotation(rotation)
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

    public fun removePlayer(player: ServerPlayer, location: LocationWithLevel<ServerLevel>) {
        this.removePlayer(player)
        player.teleportTo(location)
    }

    override fun location(): Location {
        return this.location
    }

    override fun level(): ServerLevel {
        return this.level
    }

    override fun tick(server: MinecraftServer) {
        this.attachment.tick()

        this.tickPath()
    }

    override fun onAddPlayer(player: ServerPlayer) {
        player.cameraExtension.set(this)
        player.setCamera(player)
        player.teleportTo(this.location().with(this.level))
        this.sendGamemodePacket(player, GameType.SPECTATOR, player.connection::send)

        this.attachment.startObservingAttached(player.asObserver())
        player.connection.send(createSetCameraPacket(this.camera.id))
    }

    override fun onRemovePlayer(player: ServerPlayer) {
        player.cameraExtension.remove()
        this.sendGamemodePacket(player, player.getGameMode(), player.connection::send)
        player.connection.send(ClientboundSetCameraPacket(player))
        this.attachment.stopObservingAttached(player.asObserver())
    }

    override fun resendTo(player: ServerPlayer, sender: Consumer<Packet<ClientGamePacketListener>>) {
        this.sendGamemodePacket(player, GameType.SPECTATOR, sender)
        this.camera.sendBundledSpawnPackets(player.asObserver()) { sender.accept(it.asClientGamePacket()) }
        sender.accept(createSetCameraPacket(this.camera.id))
    }

    private fun sendGamemodePacket(player: ServerPlayer, gamemode: GameType, sender: Consumer<Packet<ClientGamePacketListener>>) {
        val packet = ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, gamemode.id.toFloat())
        sender.accept(packet)
        val action = EnumSet.of(Action.UPDATE_GAME_MODE)
        val entry = listOf(
            Entry(player.uuid, null, false, 0, gamemode, null, false, 0, null)
        )
        sender.accept(ClientboundPlayerInfoUpdatePacket(action, entry))
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
}