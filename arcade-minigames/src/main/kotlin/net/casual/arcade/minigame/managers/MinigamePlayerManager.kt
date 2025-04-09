/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.managers

import com.mojang.authlib.GameProfile
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerSaveEvent
import net.casual.arcade.events.server.player.PlayerLeaveEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.*
import net.casual.arcade.minigame.gamemode.ExtendedGameMode.Companion.extendedGameMode
import net.casual.arcade.minigame.mixins.PlayerListAccessor
import net.casual.arcade.minigame.utils.MinigameUtils.getMinigame
import net.casual.arcade.minigame.utils.MinigameUtils.minigame
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.PlayerUtils.player
import net.casual.arcade.utils.impl.ConcatenatedList.Companion.concat
import net.casual.arcade.utils.math.location.Location.Companion.location
import net.casual.arcade.utils.math.location.LocationWithLevel.Companion.asTeleportTransition
import net.casual.arcade.utils.math.location.LocationWithLevel.Companion.locationWithLevel
import net.casual.arcade.utils.teleportTo
import net.minecraft.Util
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtOps
import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.DefaultAttributes
import net.minecraft.world.level.Level
import net.minecraft.world.level.dimension.DimensionType
import net.minecraft.world.level.portal.TeleportTransition
import org.jetbrains.annotations.ApiStatus.Internal
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.stream.Stream
import kotlin.io.path.createDirectories
import kotlin.io.path.isRegularFile
import kotlin.jvm.optionals.getOrNull

public class MinigamePlayerManager(
    private val minigame: Minigame
): Iterable<ServerPlayer> {
    private val connections: MutableSet<ServerGamePacketListenerImpl> = ReferenceLinkedOpenHashSet()

    private val data = DataManager(this.minigame.getSavePath().resolve("player-data"))

    internal val adminUUIDs = ObjectLinkedOpenHashSet<UUID>()
    internal val spectatorUUIDs = ObjectLinkedOpenHashSet<UUID>()
    internal val offlineGameProfiles = ObjectLinkedOpenHashSet<GameProfile>()

    /**
     * This gets all the tracked players in this minigame.
     * This includes spectating and playing players.
     */
    public val all: List<ServerPlayer>
        get() = this.connections.map { it.player }

    /**
     * This gets all the players that are currently
     * playing in the minigame, i.e. not spectating.
     *
     * @return The list of playing players.
     */
    public val playing: List<ServerPlayer>
        get() = this.streamPlayers().filter(this::isPlaying).toList()

    /**
     * This gets all the players that are currently
     * spectating in the minigame.
     */
    public val spectating: List<ServerPlayer>
        get() = this.streamPlayers().filter(this::isSpectating).toList()

    /**
     * This gets a list of all the players that are
     * admins, they may be either spectating or playing.
     */
    public val admins: List<ServerPlayer>
        get() = this.streamPlayers().filter(this::isAdmin).toList()

    /**
     * Gets a list of all the non-admin players,
     * they may be either spectating or playing.
     */
    public val nonAdmins: List<ServerPlayer>
        get() = this.streamPlayers().filter { !this.isAdmin(it) }.toList()

    /**
     * This gets all the player profiles that are playing this minigame,
     * whether online or offline.
     *
     * @return All the player's profiles.
     */
    public val allProfiles: List<GameProfile>
        get() = this.streamPlayers().map { it.gameProfile }.toList().concat(this.offlineProfiles)

    /**
     * This gets all profiles of the player's that
     * were playing the minigame but are now offline.
     *
     * @return All the offline player's profiles.
     */
    public val offlineProfiles: List<GameProfile>
        get() = this.offlineGameProfiles.toList()


    public val totalPlayerCount: Int
        get() = this.onlinePlayerCount + this.offlinePlayerCount
    public val onlinePlayerCount: Int
        get() = this.connections.size
    public val offlinePlayerCount: Int
        get() = this.offlineGameProfiles.size

    public val playingPlayerCount: Int
        get() = this.connections.count { this.isPlaying(it.player) }
    public val spectatingPlayerCount: Int
        get() = this.connections.count { this.isSpectating(it.player) }
    public val adminPlayerCount: Int
        get() = this.connections.count { this.isAdmin(it.player) }

    public var keepPlayerData: Boolean = true

    init {
        this.minigame.events.register<PlayerLeaveEvent>(Int.MAX_VALUE) { this.onPlayerLeave(it) }
        this.minigame.events.register<ServerSaveEvent> { this.onServerSave() }
    }

    /**
     * This adds a player to the minigame.
     * In order for this method to be successful, the minigame must be
     * initialized, and must not yet be tracking the given player.
     * The player may be rejected by the [MinigameAddNewPlayerEvent],
     * in which case this method will also return `false`.
     *
     * If the player is accepted this method will return `true`.
     *
     * If the player previously logged out (or the server restarted),
     * the player will automatically rejoin the minigame and the
     * [MinigameAddExistingPlayerEvent] will be broadcast instead.
     *
     * In both cases of the player joining a singular other event
     * will be broadcasted [MinigameAddPlayerEvent].
     *
     * You can specify whether the player should join as a spectator.
     * If the player was not previously part of the minigame then
     * if [spectating] is `null` or `false` then they will be marked
     * as playing, if `true` then they will join as a spectator.
     * If the player was peviously part of the minigame then if
     * [spectating] is `null` then the player will keep their previous
     * state, if `true` then they will try to start spectating and if
     * `false` then they will be removed from the spectators.
     *
     * @param player The player to add to the minigame.
     * @param spectating Whether the player should be spectating, `null` for default.
     * @param admin Whether the player should be an admin, `null` for default
     * @return Whether the player was successfully accepted.
     */
    public fun add(
        player: ServerPlayer,
        spectating: Boolean? = null,
        admin: Boolean? = null
    ): Boolean {
        this.minigame.tryInitialize()
        if (this.has(player)) {
            return false
        }

        val hasMinigame = player.getMinigame() === this.minigame
        if (this.offlineGameProfiles.remove(player.gameProfile) || hasMinigame) {
            val newPlayer = this.loadMinigamePlayer(player)
            if (!hasMinigame) {
                ArcadeUtils.logger.warn("Player's minigame UUID didn't work?!")
                newPlayer.minigame.setMinigame(this.minigame)
            }

            this.connections.add(newPlayer.connection)
            val existing = MinigameAddExistingPlayerEvent(this.minigame, newPlayer, spectating, admin)
            GlobalEventHandler.Server.broadcast(existing)
            var isSpectating = existing.spectating
            var isAdmin = existing.admin
            val default = MinigameAddPlayerEvent(this.minigame, newPlayer, isSpectating, isAdmin)
            GlobalEventHandler.Server.broadcast(default)
            isSpectating = default.spectating
            isAdmin = default.admin

            when (isSpectating) {
                true -> if (!this.setSpectating(newPlayer)) {
                    GlobalEventHandler.Server.broadcast(MinigameLoadSpectatingEvent(this.minigame, newPlayer))
                }
                false -> if (!this.setPlaying(newPlayer)) {
                    GlobalEventHandler.Server.broadcast(MinigameLoadPlayingEvent(this.minigame, newPlayer))
                }
                null -> if (this.isSpectating(newPlayer)) {
                    GlobalEventHandler.Server.broadcast(MinigameLoadSpectatingEvent(this.minigame, newPlayer))
                } else {
                    GlobalEventHandler.Server.broadcast(MinigameLoadPlayingEvent(this.minigame, newPlayer))
                }
            }
            when (isAdmin) {
                true -> this.addAdmin(newPlayer)
                false -> this.removeAdmin(newPlayer)
                null -> { }
            }
            return true
        }

        this.connections.add(player.connection)
        val event = MinigameAddNewPlayerEvent(this.minigame, player, spectating, admin)
        GlobalEventHandler.Server.broadcast(event)
        if (!event.isCancelled()) {
            val newPlayer = this.loadMinigamePlayer(player)

            newPlayer.minigame.setMinigame(this.minigame)
            val default = MinigameAddPlayerEvent(this.minigame, newPlayer, event.spectating, event.admin)
            GlobalEventHandler.Server.broadcast(default)
            val isSpectating = default.spectating
            val isAdmin = default.admin

            if (isSpectating != null && isSpectating) {
                this.setSpectating(newPlayer)
            } else {
                GlobalEventHandler.Server.broadcast(MinigameSetPlayingEvent(this.minigame, newPlayer))
                GlobalEventHandler.Server.broadcast(MinigameLoadPlayingEvent(this.minigame, newPlayer))
            }
            if (isAdmin != null && isAdmin) {
                this.addAdmin(newPlayer)
            }
            return true
        }
        this.connections.remove(player.connection)
        return false
    }

    /**
     * This removes a given player from the minigame.
     * This also removes the player from all the minigame UI.
     *
     * If successful then the [MinigameRemovePlayerEvent] is
     * broadcast.
     *
     * @param player The player to remove.
     */
    public fun remove(player: ServerPlayer): Boolean {
        this.minigame.tryInitialize()

        val wasOffline = this.offlineGameProfiles.remove(player.gameProfile)
        if (wasOffline || this.connections.contains(player.connection)) {
            if (wasOffline) {
                ArcadeUtils.logger.warn("Removed offline player?!")
            }
            this.minigame.data.updatePlayer(player)
            this.spectatorUUIDs.remove(player.uuid)
            this.removeAdmin(player)

            GlobalEventHandler.Server.broadcast(MinigameRemovePlayerEvent(this.minigame, player))
            this.connections.remove(player.connection)
            player.minigame.removeMinigame()
            this.restoreServerPlayer(player)
            return true
        }
        return false
    }

    public fun setSpectating(player: ServerPlayer): Boolean {
        if (this.has(player) && this.spectatorUUIDs.add(player.uuid)) {
            GlobalEventHandler.Server.broadcast(MinigameSetSpectatingEvent(this.minigame, player))
            GlobalEventHandler.Server.broadcast(MinigameLoadSpectatingEvent(this.minigame, player))
            return true
        }
        return false
    }

    public fun setPlaying(player: ServerPlayer): Boolean {
        if (this.spectatorUUIDs.remove(player.uuid)) {
            GlobalEventHandler.Server.broadcast(MinigameSetPlayingEvent(this.minigame, player))
            GlobalEventHandler.Server.broadcast(MinigameLoadPlayingEvent(this.minigame, player))
            return true
        }
        return false
    }

    public fun addAdmin(player: ServerPlayer): Boolean {
        if (this.has(player) && this.adminUUIDs.add(player.uuid)) {
            GlobalEventHandler.Server.broadcast(MinigameAddAdminEvent(this.minigame, player))
            return true
        }
        return false
    }

    public fun removeAdmin(player: ServerPlayer): Boolean {
        if (this.adminUUIDs.remove(player.uuid)) {
            GlobalEventHandler.Server.broadcast(MinigameRemoveAdminEvent(this.minigame, player))
            return true
        }
        return false
    }

    /**
     * This gets whether a given player is playing in the minigame.
     *
     * @param player The player to check whether they are playing.
     * @return Whether the player is playing.
     */
    public fun has(player: ServerPlayer): Boolean {
        return this.connections.contains(player.connection)
    }

    public operator fun contains(player: ServerPlayer): Boolean {
        return this.has(player)
    }

    /**
     * This checks whether a given player uuid is playing in the minigame.
     *
     * @param uuid The uuid of the player you want to check.
     * @return Whether the player is playing.
     */
    public fun has(uuid: UUID): Boolean {
        val player = this.minigame.server.player(uuid)
        if (player != null && this.has(player)) {
            return true
        }
        return this.offlineGameProfiles.any { it.id == uuid }
    }

    public fun isPlaying(player: ServerPlayer): Boolean {
        return this.has(player) && !this.isSpectating(player)
    }

    public fun isSpectating(player: ServerPlayer): Boolean {
        return this.spectatorUUIDs.contains(player.uuid)
    }

    public fun isAdmin(player: ServerPlayer): Boolean {
        return this.adminUUIDs.contains(player.uuid)
    }

    public fun transferTo(
        next: Minigame,
        players: Iterable<ServerPlayer> = this,
        keepSpectating: Boolean = true,
        keepAdmin: Boolean = true,
    ) {
        if (next === this.minigame) {
            return
        }

        for (player in players) {
            this.transferTo(next, player, keepSpectating, keepAdmin)
        }
    }

    public fun transferTo(
        next: Minigame,
        player: ServerPlayer,
        keepSpectating: Boolean = true,
        keepAdmin: Boolean = true,
    ): Boolean {
        if (next === this.minigame) {
            return false
        }

        if (!this.has(player)) {
            return false
        }
        val spectating = if (keepSpectating) this.isSpectating(player) else null
        val admin = if (keepAdmin) this.isAdmin(player) else null
        return next.players.add(player, spectating, admin)
    }

    public fun broadcast(packet: Packet<*>) {
        for (connection in this.connections) {
            connection.send(packet)
        }
    }

    internal fun close() {
        // We copy the players to avoid CME
        for (player in this.all) {
            this.remove(player)
        }
    }

    private fun streamPlayers(): Stream<ServerPlayer> {
        return this.connections.stream().map { it.player }
    }

    override fun iterator(): Iterator<ServerPlayer> {
        return this.all.iterator()
    }

    private fun onPlayerLeave(event: PlayerLeaveEvent) {
        val (player) = event

        if (this.connections.remove(player.connection)) {
            this.offlineGameProfiles.add(player.gameProfile)

            this.minigame.data.updatePlayer(player)
            this.data.save(player)
        }
    }

    private fun onServerSave() {
        if (!this.keepPlayerData) {
            this.streamPlayers().forEach(this.data::save)
        }
    }

    private fun loadMinigamePlayer(existing: ServerPlayer): ServerPlayer {
        if (this.keepPlayerData) {
            return existing
        }
        val playerList = this.minigame.server.playerList
        (playerList as PlayerListAccessor).playerIo.save(existing)

        val copy = this.createNewPlayer(existing)
        val data = this.data.load(copy)
        this.updatePlayerLocation(copy, data)
        return copy
    }

    private fun restoreServerPlayer(existing: ServerPlayer): ServerPlayer {
        if (this.keepPlayerData) {
            return existing
        }
        this.data.save(existing)

        val playerList = this.minigame.server.playerList
        val copy = this.createNewPlayer(existing)
        val data = (playerList as PlayerListAccessor).playerIo.load(copy)
        this.updatePlayerLocation(copy, data.getOrNull())
        return copy
    }

    private fun updatePlayerLocation(player: ServerPlayer, data: CompoundTag?) {
        if (data != null) {
            val key = Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, data.get("Dimension")).resultOrPartial().getOrNull()
            if (key != null) {
                val level = player.server.getLevel(key)
                if (level != null) {
                    player.teleportTo(player.location.with(level))
                    return
                }
            }
        }
        player.teleportTo(player.location)
    }

    private fun createNewPlayer(existing: ServerPlayer): ServerPlayer {
        val playerList = this.minigame.server.playerList
        val copy = try {
            LOCAL_TRANSITION.set(existing.locationWithLevel.asTeleportTransition())
            playerList.respawn(existing, false, Entity.RemovalReason.CHANGED_DIMENSION)
        } finally {
            LOCAL_TRANSITION.remove()
        }
        copy.connection.player = copy
        return copy
    }

    private class DataManager(
        private val path: Path
    ) {
        init {
            this.path.createDirectories()
        }

        fun save(player: ServerPlayer) {
            try {
                val tag = player.saveWithoutId(CompoundTag())
                val temp = Files.createTempFile(this.path, player.stringUUID + "-", ".dat")
                NbtIo.writeCompressed(tag, temp)
                val current = this.path.resolve(player.stringUUID + ".dat")
                val old = this.path.resolve(player.stringUUID + ".dat_old")
                Util.safeReplaceFile(current, temp, old)
            } catch (e: Exception) {
                ArcadeUtils.logger.warn("Failed to save player data for ${player.scoreboardName}")
            }
        }

        fun load(player: ServerPlayer): CompoundTag? {
            val optional = this.load(player, ".dat")
            val tag = optional ?: this.load(player, ".dat_old") ?: return null
            player.load(tag)
            return tag
        }

        private fun load(player: ServerPlayer, suffix: String): CompoundTag? {
            val path = this.path.resolve(player.stringUUID + suffix)
            if (path.isRegularFile()) {
                try {
                    return NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap())
                } catch (e: Exception) {
                    ArcadeUtils.logger.warn("Failed to load player data for ${player.scoreboardName}")
                }
            }
            return null
        }
    }

    public companion object {
        @Internal
        @JvmField
        public val LOCAL_TRANSITION: ThreadLocal<TeleportTransition> = ThreadLocal<TeleportTransition>()
    }
}