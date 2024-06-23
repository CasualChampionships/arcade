package net.casual.arcade.entity.firework

import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.minecraft.network.protocol.game.*
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.projectile.FireworkRocketEntity

public class VirtualFirework internal constructor(
    private val entity: FireworkRocketEntity,
    private val duration: MinecraftTimeDuration
) {
    public fun sendTo(player: ServerPlayer) {
        player.connection.send(this.createSpawnPacket())
        player.connection.send(ClientboundSetEntityMotionPacket(this.entity))
        val changed = this.entity.entityData.nonDefaultValues
        if (changed != null) {
            player.connection.send(ClientboundSetEntityDataPacket(this.entity.id, changed))
        }
        GlobalTickedScheduler.schedule(this.duration) {
            player.connection.send(ClientboundEntityEventPacket(this.entity, 17))
            player.connection.send(ClientboundRemoveEntitiesPacket(this.entity.id))
        }
    }

    public fun sendTo(players: List<ServerPlayer>) {
        val spawn = this.createSpawnPacket()
        val motion = ClientboundSetEntityMotionPacket(this.entity)
        for (player in players) {
            player.connection.send(spawn)
            player.connection.send(motion)
        }
        val changed = this.entity.entityData.nonDefaultValues
        if (changed != null) {
            val data = ClientboundSetEntityDataPacket(this.entity.id, changed)
            for (player in players) {
                player.connection.send(data)
            }
        }
        GlobalTickedScheduler.schedule(this.duration) {
            val event = ClientboundEntityEventPacket(this.entity, 17)
            val remove = ClientboundRemoveEntitiesPacket(this.entity.id)
            for (player in players) {
                player.connection.send(event)
                player.connection.send(remove)
            }
        }
    }

    private fun createSpawnPacket(): ClientboundAddEntityPacket {
        return ClientboundAddEntityPacket(
            this.entity.id,
            this.entity.uuid,
            this.entity.x,
            this.entity.y,
            this.entity.z,
            this.entity.xRot,
            this.entity.yRot,
            this.entity.type,
            0,
            this.entity.deltaMovement,
            this.entity.yHeadRot.toDouble()
        )
    }

    public companion object {
        public fun build(block: VirtualFireworkBuilder.() -> Unit): VirtualFirework {
            val builder = VirtualFireworkBuilder()
            builder.block()
            return builder.build()
        }
    }
}