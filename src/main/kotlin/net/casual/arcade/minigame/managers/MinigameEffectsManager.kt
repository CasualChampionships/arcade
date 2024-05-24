package net.casual.arcade.minigame.managers

import net.casual.arcade.Arcade
import net.casual.arcade.events.minigame.MinigameAddPlayerEvent
import net.casual.arcade.events.minigame.MinigameRemovePlayerEvent
import net.casual.arcade.events.player.PlayerClientboundPacketEvent
import net.casual.arcade.events.player.PlayerDimensionChangeEvent
import net.casual.arcade.events.player.PlayerRespawnEvent
import net.casual.arcade.gui.predicate.EntityObserverPredicate
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.annotation.ListenerFlags
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.network.syncher.SynchedEntityData.DataValue
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffectInstance.INFINITE_DURATION
import net.minecraft.world.effect.MobEffects.NIGHT_VISION
import net.minecraft.world.entity.Entity

/**
 * This class manages custom effects on players in a minigame.
 *
 * This includes whether entities are glowing or invisible,
 * as well as whether a player has fullbright (night vision).
 *
 * @see Minigame.effects
 */
public class MinigameEffectsManager(
    private val owner: Minigame<*>
) {
    private var glowing = EntityObserverPredicate.never()
    private var invisible = EntityObserverPredicate.never()

    init {
        this.owner.events.register<PlayerClientboundPacketEvent>(1_000, flags = ListenerFlags.HAS_PLAYER) { this.onPlayerPacket(it) }
        this.owner.events.register<PlayerDimensionChangeEvent>(1_000, flags = ListenerFlags.HAS_PLAYER) { this.updatePlayerFullbright(it.player) }
        this.owner.events.register<MinigameAddPlayerEvent> { this.updatePlayerFullbright(it.player) }
        this.owner.events.register<PlayerRespawnEvent> { this.updatePlayerFullbright(it.player) }
        this.owner.events.register<MinigameRemovePlayerEvent> { this.removeFullbright(it.player) }
    }

    /**
     * This marks a player as having fullbright (night vision).
     *
     * @param player The player to mark as having fullbright.
     */
    public fun addFullbright(player: ServerPlayer) {
        if (this.owner.tags.add(player, FULL_BRIGHT)) {
            player.connection.send(ClientboundUpdateMobEffectPacket(player.id, INFINITE_NIGHT_VISION, false))
        }
    }

    /**
     * This checks whether a player has fullbright (night vision).
     *
     * @param player The player to check whether it has fullbright.
     * @return Whether the player has fullbright.
     */
    public fun hasFullbright(player: ServerPlayer): Boolean {
        return this.owner.tags.has(player, FULL_BRIGHT)
    }

    /**
     * This removes fullbright (night vision) from a player.
     *
     * @param player The player to remove fullbright from.
     */
    public fun removeFullbright(player: ServerPlayer) {
        if (this.owner.tags.remove(player, FULL_BRIGHT)) {
            player.connection.send(ClientboundRemoveMobEffectPacket(player.id, NIGHT_VISION))
            val instance = player.getEffect(NIGHT_VISION)
            if (instance != null) {
                player.connection.send(ClientboundUpdateMobEffectPacket(player.id, instance, false))
            }
        }
    }

    /**
     * This sets the predicate for whether an entity is glowing.
     *
     * @param predicate The predicate to set for glowing entities.
     */
    public fun setGlowingPredicate(predicate: EntityObserverPredicate) {
        this.glowing = predicate
        for (player in this.owner.players) {
            // Mark entity data dirty
            player.setGlowingTag(!player.hasGlowingTag())
            player.setGlowingTag(!player.hasGlowingTag())
        }
    }

    /**
     * This sets the predicate for whether an entity is invisible.
     *
     * @param predicate The predicate to set for invisible entities.
     */
    public fun setInvisiblePredicate(predicate: EntityObserverPredicate) {
        this.invisible = predicate
        for (player in this.owner.players) {
            // Mark entity data dirty
            player.isInvisible = !player.isInvisible
            player.isInvisible = !player.isInvisible
        }
    }

    /**
     * This force updates the glowing and invisible flags for an entity for
     * a specific observer.
     *
     * @param observee The entity to update the flags for.
     * @param observer The observer to update the flags for.
     * @param consumer The consumer to send the updated flags to the observer, by
     *   default this just sends the packets to the observer.
     */
    public fun forceUpdate(
        observee: Entity,
        observer: ServerPlayer,
        consumer: (ClientboundSetEntityDataPacket) -> Unit = observer.connection::send
    ) {
        val flags = observee.entityData.get(Entity.DATA_SHARED_FLAGS_ID)
        val modified = this.modifySharedEntityFlags(observee, observer, flags)
        val dirty = listOf(DataValue.create(Entity.DATA_SHARED_FLAGS_ID, modified))
        consumer(ClientboundSetEntityDataPacket(observee.id, dirty))
    }

    private fun updatePlayerFullbright(player: ServerPlayer) {
        if (this.hasFullbright(player)) {
            player.connection.send(ClientboundUpdateMobEffectPacket(player.id, INFINITE_NIGHT_VISION, false))
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
            val updated = ArrayList<Packet<in ClientGamePacketListener>>()
            for (sub in packet.subPackets()) {
                val new = this.updatePacket(player, sub)
                if (new is ClientboundBundlePacket) {
                    updated.addAll(new.subPackets())
                } else {
                    updated.add(new)
                }
            }
            return ClientboundBundlePacket(updated)
        }

        if (packet is ClientboundUpdateMobEffectPacket) {
            if (packet.entityId == player.id && packet.effect.value() == NIGHT_VISION.value() && this.hasFullbright(player)) {
                return ClientboundUpdateMobEffectPacket(player.id, INFINITE_NIGHT_VISION, false)
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