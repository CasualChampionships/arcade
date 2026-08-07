/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals.ready.chat

import net.minecraft.network.chat.Component
import net.minecraft.world.scores.PlayerTeam

public class TeamChatReadyBroadcaster(
    unicast: (PlayerTeam, Component) -> Unit = TEAM_UNICAST,
    multicast: (Component) -> Unit
): ChatReadyBroadcaster<PlayerTeam>(unicast, multicast) {
    override fun broadcastParticipantReady(participant: PlayerTeam) {
        this.multicast.invoke(Component.translatable("arcade.ready.team.ready", participant.formattedDisplayName))
    }

    override fun broadcastParticipantNotReady(participant: PlayerTeam) {
        this.multicast.invoke(Component.translatable("arcade.ready.team.notReady", participant.formattedDisplayName))
    }

    override fun broadcastSuccess() {
        this.multicast.invoke(Component.translatable("arcade.ready.team.success"))
    }

    override fun broadcastFailure() {
        this.multicast.invoke(Component.translatable("arcade.ready.team.fail"))
    }

    override fun getBroadcastComponent(yes: Component, no: Component): Component {
        return Component.translatable("arcade.ready.team.broadcast", yes, no)
    }
}