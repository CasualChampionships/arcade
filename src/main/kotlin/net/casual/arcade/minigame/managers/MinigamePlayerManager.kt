package net.casual.arcade.minigame.managers

import com.mojang.authlib.GameProfile
import net.casual.arcade.Arcade
import net.casual.arcade.events.minigame.*
import net.casual.arcade.events.player.PlayerLeaveEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.EventUtils.broadcast
import net.casual.arcade.utils.MinigameUtils.getMinigame
import net.casual.arcade.utils.MinigameUtils.minigame
import net.casual.arcade.utils.PlayerUtils
import net.casual.arcade.utils.impl.ConcatenatedList.Companion.concat
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import java.util.*
import java.util.stream.Stream

public class MinigamePlayerManager(
    private val minigame: Minigame<*>
): Iterable<ServerPlayer> {
    private val connections: MutableSet<ServerGamePacketListenerImpl> = LinkedHashSet()

    internal val adminUUIDs = LinkedHashSet<UUID>()
    internal val spectatorUUIDs = LinkedHashSet<UUID>()
    internal val offlineGameProfiles = LinkedHashSet<GameProfile>()

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

    init {
        this.minigame.events.register<PlayerLeaveEvent>(Int.MAX_VALUE) { this.onPlayerLeave(it) }
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
     * @return Whether the player was successfully accepted.
     */
    public fun add(player: ServerPlayer, spectating: Boolean? = null): Boolean {
        this.minigame.tryInitialize()
        if (this.has(player)) {
            return false
        }

        val hasMinigame = player.getMinigame() === this.minigame
        if (this.offlineGameProfiles.remove(player.gameProfile) || hasMinigame) {
            if (!hasMinigame) {
                Arcade.logger.warn("Player's minigame UUID didn't work?!")
                player.minigame.setMinigame(this.minigame)
            }

            this.connections.add(player.connection)
            MinigameAddExistingPlayerEvent(this.minigame, player).broadcast()
            MinigameAddPlayerEvent(this.minigame, player).broadcast()

            if (spectating != null) {
                if (spectating) this.setSpectating(player) else this.setPlaying(player)
            }
            return true
        }

        this.connections.add(player.connection)
        val event = MinigameAddNewPlayerEvent(this.minigame, player).broadcast()
        if (!event.isCancelled()) {
            player.minigame.setMinigame(this.minigame)
            MinigameAddPlayerEvent(this.minigame, player).broadcast()

            if (spectating != null && spectating) {
                this.setSpectating(player)
            } else {
                MinigameSetPlayingEvent(this.minigame, player).broadcast()
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
                Arcade.logger.warn("Removed offline player?!")
            }
            this.minigame.data.updatePlayer(player)
            this.spectatorUUIDs.remove(player.uuid)
            this.removeAdmin(player)

            MinigameRemovePlayerEvent(this.minigame, player).broadcast()
            this.connections.remove(player.connection)
            player.minigame.removeMinigame()
            return true
        }
        return false
    }

    public fun setSpectating(player: ServerPlayer): Boolean {
        if (this.has(player) && this.spectatorUUIDs.add(player.uuid)) {
            MinigameSetSpectatingEvent(this.minigame, player).broadcast()
            return true
        }
        return false
    }

    public fun setPlaying(player: ServerPlayer): Boolean {
        if (this.spectatorUUIDs.remove(player.uuid)) {
            MinigameSetPlayingEvent(this.minigame, player).broadcast()
            return true
        }
        return false
    }

    public fun addAdmin(player: ServerPlayer): Boolean {
        if (this.has(player) && this.adminUUIDs.add(player.uuid)) {
            MinigameAddAdminEvent(this.minigame, player).broadcast()
            return true
        }
        return false
    }

    public fun removeAdmin(player: ServerPlayer): Boolean {
        if (this.adminUUIDs.remove(player.uuid)) {
            MinigameRemoveAdminEvent(this.minigame, player).broadcast()
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
        val player = PlayerUtils.player(uuid)
        if (player != null) {
            return this.has(player)
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

    private fun streamPlayers(): Stream<ServerPlayer> {
        return this.connections.stream().map { it.player }
    }

    override fun iterator(): Iterator<ServerPlayer> {
        return this.streamPlayers().iterator()
    }

    private fun onPlayerLeave(event: PlayerLeaveEvent) {
        val (player) = event

        if (this.connections.remove(player.connection)) {
            this.offlineGameProfiles.add(player.gameProfile)

            this.minigame.data.updatePlayer(player)
        }
    }
}