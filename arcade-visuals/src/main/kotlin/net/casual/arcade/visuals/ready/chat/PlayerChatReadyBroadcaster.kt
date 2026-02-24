/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.ready.chat

import net.casual.arcade.utils.component.lime
import net.casual.arcade.utils.component.red
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

public class PlayerChatReadyBroadcaster(
    unicast: (ServerPlayer, Component) -> Unit = PLAYER_UNICAST,
    multicast: (Component) -> Unit
): ChatReadyBroadcaster<ServerPlayer>(unicast, multicast) {
    override fun broadcastParticipantReady(participant: ServerPlayer) {
        this.multicast.invoke(Component.translatable("arcade.ready.player.ready", participant.displayName).lime())
    }

    override fun broadcastParticipantNotReady(participant: ServerPlayer) {
        this.multicast.invoke(Component.translatable("arcade.ready.player.notReady", participant.displayName).red())
    }

    override fun broadcastSuccess() {
        this.multicast.invoke(Component.translatable("arcade.ready.player.success").lime())
    }

    override fun broadcastFailure() {
        this.multicast.invoke(Component.translatable("arcade.ready.player.fail").red())
    }

    override fun getBroadcastComponent(yes: Component, no: Component): Component {
        return Component.translatable("arcade.ready.player.broadcast", yes, no)
    }
}