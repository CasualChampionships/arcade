package net.casual.arcade.virtual.entity

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.casual.arcade.virtual.entity.data.PlayerSpecificEntityData
import net.casual.arcade.virtual.entity.utils.VirtualPosition
import net.casual.arcade.virtual.entity.utils.VirtualRotation
import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.entity.EntityType
import java.util.*

/**
 * A [VirtualEntity] implementation which keeps track
 * of the players which are currently observing it.
 *
 * @see VirtualEntity
 */
public abstract class TrackingVirtualEntity: VirtualEntity {
    /**
     * The set of [ServerGamePacketListenerImpl] connections, each
     * corresponding to a unique player.
     */
    protected val connections: MutableSet<ServerGamePacketListenerImpl> = ReferenceOpenHashSet()

    override val id: Int = VirtualEntity.getNextEntityId()
    override val uuid: UUID = UUID.randomUUID()
    override var position: VirtualPosition = VirtualPosition.DEFAULT
    override var rotation: VirtualRotation = VirtualRotation.DEFAULT

    /**
     * Gets the players observing this virtual entity.
     *
     * @return The observers.
     */
    public fun observers(): List<ServerPlayer> {
        return this.connections.map { connection -> connection.player }
    }

    /**
     * Broadcasts a packet to all observers.
     *
     * @param packet The packet to broadcast.
     */
    public fun broadcast(packet: Packet<*>) {
        for (connection in this.connections) {
            connection.send(packet)
        }
    }

    override fun startObserving(observer: ServerPlayer) {
        this.connections.add(observer.connection)
    }

    override fun stopObserving(observer: ServerPlayer) {
        this.connections.remove(observer.connection)
    }
}