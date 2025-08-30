/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.camera

import eu.pb4.polymer.virtualentity.api.ElementHolder
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils
import eu.pb4.polymer.virtualentity.api.attachment.ManualAttachment
import eu.pb4.polymer.virtualentity.api.elements.SimpleEntityElement
import net.casual.arcade.utils.ClientboundPlayerInfoUpdatePacket
import net.casual.arcade.utils.math.location.Location
import net.casual.arcade.utils.math.location.LocationWithLevel
import net.casual.arcade.utils.math.location.LocationWithLevel.Companion.asLocation
import net.casual.arcade.utils.teleportTo
import net.casual.arcade.visuals.core.TickableUI
import net.casual.arcade.visuals.core.TrackedPlayerUI
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
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import java.util.*
import java.util.function.Consumer

public class PlayerCamera(
    private val level: ServerLevel,
    private var position: Vec3,
    rotation: Vec2 = Vec2.ZERO
): TrackedPlayerUI(), TickableUI {
    private val holder = ElementHolder()
    private val attachment = ManualAttachment(this.holder, this.level, this::position)
    private val element = SimpleEntityElement(EntityType.ARMOR_STAND)

    public constructor(location: LocationWithLevel<ServerLevel>): this(location.level, location.position, location.rotation)

    init {
        this.holder.addElement(this.element)
        this.setRotation(rotation)
    }

    public fun setPosition(position: Vec3) {
        this.position = position
    }

    public fun setRotation(rotation: Vec2) {
        this.element.pitch = rotation.x
        this.element.yaw = rotation.y
    }

    public fun setLocation(location: Location) {
        this.setPosition(location.position)
        this.setRotation(location.rotation)
    }

    public fun destroy() {
        this.attachment.destroy()
    }

    public fun removePlayer(player: ServerPlayer, location: LocationWithLevel<ServerLevel>) {
        this.removePlayer(player)
        player.teleportTo(location)
    }

    override fun tick(server: MinecraftServer) {
        this.holder.tick()
    }

    override fun onAddPlayer(player: ServerPlayer) {
        player.cameraExtension.set(this)
        player.camera = player
        player.teleportTo(this.level.asLocation(this.position))
        this.sendGamemodePacket(player, GameType.SPECTATOR)

        this.attachment.startWatching(player)
        player.connection.send(VirtualEntityUtils.createSetCameraEntityPacket(this.element.entityId))
    }

    override fun onRemovePlayer(player: ServerPlayer) {
        player.cameraExtension.remove()
        this.sendGamemodePacket(player, player.gameMode())
        player.connection.send(ClientboundSetCameraPacket(player))
        this.attachment.stopWatching(player)
    }

    override fun resendTo(player: ServerPlayer, sender: Consumer<Packet<ClientGamePacketListener>>) {

    }

    private fun sendGamemodePacket(player: ServerPlayer, gamemode: GameType) {
        val packet = ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, gamemode.id.toFloat())
        player.connection.send(packet)
        val action = EnumSet.of(Action.UPDATE_GAME_MODE)
        val entry = listOf(
            Entry(player.uuid, null, false, 0, gamemode, null, false, 0, null)
        )
        player.connection.send(ClientboundPlayerInfoUpdatePacket(action, entry))
    }
}