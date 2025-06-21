/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.ready

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.casual.arcade.scheduler.task.Completable
import net.casual.arcade.utils.PlayerUtils.levelServer
import net.casual.arcade.utils.PlayerUtils.player
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import java.util.*

public class ReadyChecker(
    private val playerHandler: ReadyHandler<ServerPlayer>? = null,
    private val teamHandler: ReadyHandler<PlayerTeam>? = null
) {
    private val players = Object2ObjectOpenHashMap<UUID, ReadyState>()
    private val teams = Object2ObjectOpenHashMap<PlayerTeam, ReadyState>()

    private var current: Completable.Impl? = null
    private var readyId = 0

    public fun isRunning(): Boolean {
        val current = this.current ?: return false
        return !current.complete
    }

    public fun complete(): Boolean {
        if (this.isRunning()) {
            this.current!!.complete()
            return true
        }
        return false
    }

    public fun getUnreadyPlayers(server: MinecraftServer): List<ServerPlayer> {
        return this.players.mapNotNull { (uuid, state) ->
            if (state != ReadyState.Ready) server.player(uuid) else null
        }
    }

    public fun getUnreadyTeams(): List<PlayerTeam> {
        return this.teams.mapNotNull { (team, state) ->
            if (state != ReadyState.Ready) team else null
        }
    }

    public fun getUnreadyFormatted(server: MinecraftServer): List<Component> {
        val unready = ArrayList<Component>()
        if (!this.players.isEmpty() && this.playerHandler != null) {
            for ((uuid, state) in this.players) {
                val player = server.player(uuid)
                if (player != null && state != ReadyState.Ready) {
                    unready.add(this.playerHandler.format(player))
                }
            }
            return unready
        }
        if (!this.teams.isEmpty() && this.teamHandler != null) {
            for ((team, state) in this.teams) {
                if (state != ReadyState.Ready) {
                    unready.add(this.teamHandler.format(team))
                }
            }
            return unready
        }
        return unready
    }

    public fun areTeamsReady(teams: Collection<PlayerTeam>): Completable {
        if (this.teamHandler == null) {
            throw IllegalArgumentException("Cannot ready teams with no team handler")
        }

        val id = this.startNextReady()
        if (teams.isEmpty()) {
            return Completable.complete()
        }

        val completable = Completable.Impl()
        for (team in teams) {
            this.teams[team] = ReadyState.Awaiting
            this.teamHandler.broadcastReadyCheck(
                team,
                this.checkId(id) { this.onTeamReady(this.teamHandler, team, completable) },
                this.checkId(id) { this.onTeamNotReady(this.teamHandler, team) },
            )
        }

        this.current = completable
        return completable
    }

    public fun arePlayersReady(players: Collection<ServerPlayer>): Completable {
        if (this.playerHandler == null) {
            throw IllegalArgumentException("Cannot ready teams with no team handler")
        }

        val id = this.startNextReady()
        if (players.isEmpty()) {
            return Completable.complete()
        }

        val completable = Completable.Impl()
        for (player in players) {
            val uuid = player.uuid
            val server = player.levelServer
            this.players[uuid] = ReadyState.Awaiting
            this.playerHandler.broadcastReadyCheck(
                player,
                this.checkId(id) { this.onPlayerReady(this.playerHandler, server.player(uuid), completable) },
                this.checkId(id) { this.onPlayerNotReady(this.playerHandler, server.player(uuid)) },
            )
        }

        this.current = completable
        return completable
    }

    private fun onTeamReady(handler: ReadyHandler<PlayerTeam>, team: PlayerTeam, completable: Completable.Impl) {
        val state = this.teams[team] ?: return
        if (state != ReadyState.Ready && handler.onReady(team, state)) {
            this.teams[team] = ReadyState.Ready

            if (this.teams.values.all { it == ReadyState.Ready }) {
                handler.onAllReady()
                completable.complete()
            }
        }
    }

    private fun onTeamNotReady(handler: ReadyHandler<PlayerTeam>, team: PlayerTeam) {
        val state = this.teams[team] ?: return
        if (state != ReadyState.NotReady && handler.onNotReady(team, state)) {
            this.teams[team] = ReadyState.NotReady
        }
    }

    private fun onPlayerReady(handler: ReadyHandler<ServerPlayer>, player: ServerPlayer?, completable: Completable.Impl) {
        player ?: return
        val state = this.players[player.uuid] ?: return
        if (state != ReadyState.Ready && handler.onReady(player, state)) {
            this.players[player.uuid] = ReadyState.Ready

            if (this.players.values.all { it == ReadyState.Ready }) {
                handler.onAllReady()
                completable.complete()
            }
        }
    }

    private fun onPlayerNotReady(handler: ReadyHandler<ServerPlayer>, player: ServerPlayer?) {
        player ?: return
        val state = this.players[player.uuid] ?: return
        if (state != ReadyState.NotReady && handler.onNotReady(player, state)) {
            this.players[player.uuid] = ReadyState.NotReady
        }
    }

    private fun startNextReady(): Int {
        this.current = null
        this.players.clear()
        this.teams.clear()
        return ++this.readyId
    }

    private inline fun checkId(id: Int, crossinline callback: () -> Unit): () -> Unit {
        return {
            if (this.readyId == id && this.isRunning()) {
                callback.invoke()
            }
        }
    }
}