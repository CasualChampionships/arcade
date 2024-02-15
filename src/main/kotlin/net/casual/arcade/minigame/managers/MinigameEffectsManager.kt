package net.casual.arcade.minigame.managers

import net.casual.arcade.Arcade
import net.casual.arcade.events.minigame.MinigameAddPlayerEvent
import net.casual.arcade.events.minigame.MinigameRemovePlayerEvent
import net.casual.arcade.events.player.PlayerClientboundPacketEvent
import net.casual.arcade.events.player.PlayerDimensionChangeEvent
import net.casual.arcade.events.player.PlayerRespawnEvent
import net.casual.arcade.gui.predicate.EntityObserverPredicate
import net.casual.arcade.minigame.Minigame
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.network.syncher.SynchedEntityData.DataValue
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffectInstance.INFINITE_DURATION
import net.minecraft.world.effect.MobEffects.NIGHT_VISION
import net.minecraft.world.entity.Entity

public class
MinigameEffectsManager(
    private val owner: Minigame<*>
) {
    private var glowing = EntityObserverPredicate.never()
    private var invisible = EntityObserverPredicate.never()

    init {
        this.owner.events.register<PlayerClientboundPacketEvent> { this.onPlayerPacket(it) }
        this.owner.events.register<MinigameAddPlayerEvent> { this.updatePlayerFullbright(it.player) }
        this.owner.events.register<PlayerDimensionChangeEvent> { this.updatePlayerFullbright(it.player) }
        this.owner.events.register<PlayerRespawnEvent> { this.updatePlayerFullbright(it.player) }
        this.owner.events.register<MinigameRemovePlayerEvent> { this.removeFullbright(it.player) }
    }

    public fun addFullbright(player: ServerPlayer) {
        if (this.owner.tags.add(player, FULL_BRIGHT)) {
            player.connection.send(ClientboundUpdateMobEffectPacket(player.id, INFINITE_NIGHT_VISION))
        }
    }

    public fun hasFullbright(player: ServerPlayer): Boolean {
        return this.owner.tags.has(player, FULL_BRIGHT)
    }

    public fun removeFullbright(player: ServerPlayer) {
        if (this.owner.tags.remove(player, FULL_BRIGHT)) {
            player.connection.send(ClientboundRemoveMobEffectPacket(player.id, NIGHT_VISION))
            val instance = player.getEffect(NIGHT_VISION)
            if (instance != null) {
                player.connection.send(ClientboundUpdateMobEffectPacket(player.id, instance))
            }
        }
    }

    public fun setGlowingPredicate(predicate: EntityObserverPredicate) {
        this.glowing = predicate
        for (player in this.owner.getAllPlayers()) {
            // Mark entity data dirty
            player.setGlowingTag(!player.hasGlowingTag())
            player.setGlowingTag(!player.hasGlowingTag())
        }
    }

    public fun forceUpdate(
        observee: Entity,
        observer: ServerPlayer,
        consumer: (ClientboundSetEntityDataPacket) -> Unit = observer.connection::send
    ) {
        val flags = observee.entityData.get(Entity.DATA_SHARED_FLAGS_ID)
        val modified = this.modifySharedEntityFlags(observee, observer, flags)
        if (modified != flags) {
            val dirty = listOf(DataValue.create(Entity.DATA_SHARED_FLAGS_ID, modified))
            consumer(ClientboundSetEntityDataPacket(observee.id, dirty))
        }
    }

    private fun updatePlayerFullbright(player: ServerPlayer) {
        if (this.hasFullbright(player)) {
            player.connection.send(ClientboundUpdateMobEffectPacket(player.id, INFINITE_NIGHT_VISION))
        }
    }

    private fun onPlayerPacket(event: PlayerClientboundPacketEvent) {
        val (player, packet) = event

        val updated = this.updatePacket(player, packet)
        if (updated !== packet) {
            event.cancel(updated)
        }
    }

    private fun updatePacket(player: ServerPlayer, packet: Packet<*>): Packet<ClientGamePacketListener> {
        if (packet is ClientboundBundlePacket) {
            val updated = ArrayList<Packet<ClientGamePacketListener>>()
            for (sub in packet.subPackets()) {
                updated.add(this.updatePacket(player, sub))
            }
            return ClientboundBundlePacket(updated)
        }

        if (packet is ClientboundUpdateMobEffectPacket) {
            if (packet.entityId == player.id && packet.effect == NIGHT_VISION && this.hasFullbright(player)) {
                return ClientboundUpdateMobEffectPacket(player.id, INFINITE_NIGHT_VISION)
            }
            return packet
        }

        // We need to check this packet also, because data won't be
        // sent to the client if there is no dirty data
        if (packet is ClientboundAddEntityPacket) {
            val observee = player.level().getEntity(packet.id) ?: return packet

            val list = ArrayList<Packet<ClientGamePacketListener>>(2)
            this.forceUpdate(observee, player, list::add)
            if (list.isNotEmpty()) {
                list.add(0, packet)
                return ClientboundBundlePacket(list)
            }
            return packet
        }

        if (packet is ClientboundSetEntityDataPacket) {
            val observee = player.serverLevel().getEntity(packet.id) ?: return packet

            val items = packet.packedItems
            val data = ArrayList<DataValue<*>>()
            for (item in items) {
                if (item.id == Entity.DATA_SHARED_FLAGS_ID.id) {
                    val flags = item.value as Byte
                    val modified = this.modifySharedEntityFlags(observee, player, flags)
                    data.add(DataValue.create(Entity.DATA_SHARED_FLAGS_ID, modified))
                } else {
                    data.add(item)
                }
            }
            return ClientboundSetEntityDataPacket(packet.id, data)
        }

        @Suppress("UNCHECKED_CAST")
        return packet as Packet<ClientGamePacketListener>
    }

    private fun enableFlag(flags: Byte, flag: Int): Byte {
        return (flags.toInt() or (1 shl flag)).toByte()
    }

    private fun modifySharedEntityFlags(
        observee: Entity,
        observer: ServerPlayer,
        flags: Byte
    ): Byte {
        var modified = flags
        if (this.glowing.observable(observee, observer)) {
            modified = this.enableFlag(flags, Entity.FLAG_GLOWING)
        }
        if (this.invisible.observable(observee, observer)) {
            modified = this.enableFlag(flags, Entity.FLAG_INVISIBLE)
        }
        return modified
    }

    private companion object {
        val INFINITE_NIGHT_VISION = MobEffectInstance(NIGHT_VISION, INFINITE_DURATION, 0, false, false, false)

        val FULL_BRIGHT = Arcade.id("full_bright")
    }
}