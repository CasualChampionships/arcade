/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals.ready

import net.casual.arcade.utils.player.displayName
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam

public object ReadyChecker {
    public suspend fun <P> check(broadcaster: ReadyBroadcaster<P>, participants: Collection<P>) {
        val tracker = this.track(broadcaster, participants) { CommonComponents.EMPTY }
        tracker.awaitSuccess()
    }

    public fun <P> track(
        broadcaster: ReadyBroadcaster<P>,
        participants: Collection<P>,
        pretty: (P) -> Component
    ): ReadyTracker<P> {
        val tracker = ReadyTracker(broadcaster)
        tracker.initialize(participants, pretty)
        return tracker
    }

    @JvmName("trackPlayers")
    public fun track(
        broadcaster: ReadyBroadcaster<ServerPlayer>,
        participants: Collection<ServerPlayer>
    ): ReadyTracker<ServerPlayer> {
        return this.track(broadcaster, participants) { player -> player.displayName() }
    }

    @JvmName("trackTeams")
    public fun track(
        broadcaster: ReadyBroadcaster<PlayerTeam>,
        participants: Collection<PlayerTeam>
    ): ReadyTracker<PlayerTeam> {
        return this.track(broadcaster, participants) { player -> player.formattedDisplayName }
    }
}