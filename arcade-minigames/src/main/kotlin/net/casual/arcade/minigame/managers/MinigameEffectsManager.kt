package net.casual.arcade.minigame.managers

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData
import net.casual.arcade.events.player.PlayerClientboundPacketEvent
import net.casual.arcade.events.player.PlayerDimensionChangeEvent
import net.casual.arcade.events.player.PlayerRespawnEvent
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.visuals.predicate.PlayerObserverPredicate
import net.casual.arcade.visuals.predicate.PlayerObserverPredicate.Companion.toPlayer
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.annotation.ListenerFlags
import net.casual.arcade.minigame.events.MinigameAddPlayerEvent
import net.casual.arcade.minigame.events.MinigameRemovePlayerEvent
import net.casual.arcade.minigame.utils.modifySharedFlags
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.modify
import net.casual.arcade.visuals.predicate.EntityObserverPredicate
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.network.syncher.SynchedEntityData.DataValue
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffectInstance.INFINITE_DURATION
import net.minecraft.world.effect.MobEffects.NIGHT_VISION
import net.minecraft.world.entity.Entity
import java.util.*

/**
 * This class manages custom effects on players in a minigame.
 *
 * This includes whether entities are glowing or invisible,
 * as well as whether a player has fullbright (night vision).
 *
 * @see Minigame.effects
 */
public class MinigameEffectsManager(
    private val minigame: Minigame
) {
    private var glowingTracker: Multimap<UUID, UUID>? = null
    private var invisibleTracker: Multimap<UUID, UUID>? = null

    private var glowing = EntityObserverPredicate.never().toPlayer()
    private var invisible = EntityObserverPredicate.never().toPlayer()

    private val frozen = HashSet<UUID>()

    init {
        this.minigame.events.register<PlayerClientboundPacketEvent>(1_000, flags = ListenerFlags.HAS_PLAYER) { this.onPlayerPacket(it) }
        this.minigame.events.register<PlayerDimensionChangeEvent>(1_000, flags = ListenerFlags.HAS_PLAYER) { this.updatePlayerFullbright(it.player) }
        this.minigame.events.register<MinigameAddPlayerEvent> { this.updatePlayerFullbright(it.player) }
        this.minigame.events.register<PlayerRespawnEvent> { this.updatePlayerFullbright(it.player) }
        this.minigame.events.register<MinigameRemovePlayerEvent> { this.removeFullbright(it.player) }
        this.minigame.events.register<ServerTickEvent> { this.tickTrackers() }
    }

    /**
     * This marks a player as having fullbright (night vision).
     *
     * @param player The player to mark as having fullbright.
     */
    public fun addFullbright(player: ServerPlayer) {
        if (this.minigame.tags.add(player, FULL_BRIGHT)) {
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
        return this.minigame.tags.has(player, FULL_BRIGHT)
    }

    /**
     * This removes fullbright (night vision) from a player.
     *
     * @param player The player to remove fullbright from.
     */
    public fun removeFullbright(player: ServerPlayer) {
        if (this.minigame.tags.remove(player, FULL_BRIGHT)) {
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
    public fun setGlowingPredicate(predicate: PlayerObserverPredicate, tick: Boolean = false) {
        this.glowing = predicate
        for (player in this.minigame.players) {
            // Mark entity data dirty
            player.setGlowingTag(!player.hasGlowingTag())
            player.setGlowingTag(!player.hasGlowingTag())
        }
        if (tick) {
            this.glowingTracker = HashMultimap.create()
        } else {
            this.glowingTracker = null
        }
    }

    /**
     * This sets the predicate for whether an entity is invisible.
     *
     * @param predicate The predicate to set for invisible entities.
     */
    public fun setInvisiblePredicate(predicate: PlayerObserverPredicate, tick: Boolean = false) {
        this.invisible = predicate
        for (player in this.minigame.players) {
            // Mark entity data dirty
            player.isInvisible = !player.isInvisible
            player.isInvisible = !player.isInvisible
        }
        if (tick) {
            this.invisibleTracker = HashMultimap.create()
        } else {
            this.invisibleTracker = null
        }
    }

    public fun freeze(entity: Entity) {
        this.frozen.add(entity.uuid)
    }

    public fun unfreeze(entity: Entity) {
        this.frozen.remove(entity.uuid)
    }

    public fun isFrozen(entity: Entity): Boolean {
        return this.frozen.contains(entity.uuid)
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
        val flags = observee.entityData.get(EntityTrackedData.FLAGS)
        val modified = this.modifySharedEntityFlags(observee, observer, flags)
        val dirty = listOf(DataValue.create(EntityTrackedData.FLAGS, modified))
        consumer(ClientboundSetEntityDataPacket(observee.id, dirty))
    }

    private fun tickTrackers() {
        val glowing = this.glowingTracker
        val players = lazy { this.minigame.players.all }
        val updated = HashMultimap.create<UUID, UUID>()
        if (glowing != null) {
            this.glowingTracker = this.tickTracker(this.glowing, glowing, players, updated)
        }
        val invisible = this.invisibleTracker
        if (invisible != null) {
            this.invisibleTracker = this.tickTracker(this.invisible, invisible, players, updated)
        }
    }

    private fun tickTracker(
        predicate: PlayerObserverPredicate,
        previous: Multimap<UUID, UUID>,
        players: Lazy<List<ServerPlayer>>,
        updated: Multimap<UUID, UUID>
    ): HashMultimap<UUID, UUID> {
        val next = HashMultimap.create<UUID, UUID>()
        for (observee in players.value) {
            for (observer in players.value) {
                val bool = predicate.observable(observee, observer)
                if (bool) {
                    next.put(observee.uuid, observer.uuid)
                }
                if (bool != previous.containsEntry(observee.uuid, observer.uuid)) {
                    if (!updated.containsEntry(observee.uuid, observer.uuid)) {
                        this.forceUpdate(observee, observer)
                        updated.put(observee.uuid, observer.uuid)
                    }
                }
            }
        }
        return next
    }

    private fun updatePlayerFullbright(player: ServerPlayer) {
        if (this.hasFullbright(player)) {
            player.connection.send(ClientboundUpdateMobEffectPacket(player.id, INFINITE_NIGHT_VISION, false))
        }
    }

    private fun onPlayerPacket(event: PlayerClientboundPacketEvent) {
        event.packet = this.updatePacket(event.player, event.packet)
    }

    private fun updatePacket(player: ServerPlayer, packet: Packet<*>): Packet<ClientGamePacketListener> {
        if (packet is ClientboundBundlePacket) {
            return packet.modify(player, this::updatePacket)
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
            return packet.modifySharedFlags(player, this::modifySharedEntityFlags)
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
            modified = this.enableFlag(flags, EntityTrackedData.GLOWING_FLAG_INDEX)
        }
        if (this.invisible.observable(observee, observer)) {
            modified = this.enableFlag(flags, EntityTrackedData.INVISIBLE_FLAG_INDEX)
        }
        return modified
    }

    private companion object {
        val INFINITE_NIGHT_VISION = MobEffectInstance(NIGHT_VISION, INFINITE_DURATION, 0, false, false, false)

        val FULL_BRIGHT = ResourceUtils.arcade("full_bright")
    }
}