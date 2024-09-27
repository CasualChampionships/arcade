package net.casual.arcade.border.utils

import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.border.BorderChangeListener
import net.minecraft.world.level.border.WorldBorder

public class LevelSpecificBorderBroadcaster(private val level: ServerLevel): BorderChangeListener {
    override fun onBorderSizeSet(border: WorldBorder, size: Double) {
        this.broadcast(ClientboundSetBorderSizePacket(border))
    }

    override fun onBorderSizeLerping(border: WorldBorder, oldSize: Double, newSize: Double, time: Long) {
        this.broadcast(ClientboundSetBorderLerpSizePacket(border))
    }

    override fun onBorderCenterSet(border: WorldBorder, x: Double, z: Double) {
        // We need to copy the border with the corrected center
        val scale = this.level.dimensionType().coordinateScale
        if (scale != 0.0) {
            val copy = WorldBorder()
            copy.setCenter(x * scale, z * scale)
            this.broadcast(ClientboundSetBorderCenterPacket(copy))
            return
        }
        this.broadcast(ClientboundSetBorderCenterPacket(border))
    }

    override fun onBorderSetWarningTime(border: WorldBorder, warningTime: Int) {
        this.broadcast(ClientboundSetBorderWarningDelayPacket(border))
    }

    override fun onBorderSetWarningBlocks(border: WorldBorder, warningBlocks: Int) {
        this.broadcast(ClientboundSetBorderWarningDistancePacket(border))
    }

    override fun onBorderSetDamagePerBlock(border: WorldBorder, damagePerBlock: Double) {

    }

    override fun onBorderSetDamageSafeZOne(border: WorldBorder, damageSafeZone: Double) {

    }

    private fun broadcast(packet: Packet<*>) {
        for (player in this.level.players()) {
            player.connection.send(packet)
        }
    }
}