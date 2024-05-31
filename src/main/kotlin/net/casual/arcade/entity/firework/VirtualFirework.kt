package net.casual.arcade.entity.firework

import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.projectile.FireworkRocketEntity

public class VirtualFirework internal constructor(
    private val entity: FireworkRocketEntity,
    private val duration: MinecraftTimeDuration
) {
    public fun sendTo(player: ServerPlayer) {
        player.connection.send(this.entity.addEntityPacket)
        val changed = this.entity.entityData.nonDefaultValues
        if (changed != null) {
            player.connection.send(ClientboundSetEntityDataPacket(this.entity.id, changed))
        }
        GlobalTickedScheduler.schedule(this.duration) {
            player.connection.send(ClientboundEntityEventPacket(this.entity, 17))
        }
    }

    public fun sendTo(players: List<ServerPlayer>) {
        val spawn = this.entity.addEntityPacket
        for (player in players) {
            player.connection.send(spawn)
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
            for (player in players) {
                player.connection.send(event)
            }
        }
    }

    public companion object {
        public fun build(block: VirtualFireworkBuilder.() -> Unit): VirtualFirework {
            val builder = VirtualFireworkBuilder()
            builder.block()
            return builder.build()
        }
    }
}