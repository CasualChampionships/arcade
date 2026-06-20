/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.ready.chat

import net.casual.arcade.utils.component.event.ClickEventCallback
import net.casual.arcade.utils.component.function
import net.casual.arcade.utils.component.lime
import net.casual.arcade.utils.component.red
import net.casual.arcade.utils.player.broadcast
import net.casual.arcade.utils.scoreboard.getOnlinePlayers
import net.casual.arcade.utils.server.players
import net.casual.arcade.visuals.ready.ReadyBroadcaster
import net.casual.arcade.visuals.ready.ReadyParticipantState
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam

public abstract class ChatReadyBroadcaster<P>(
    protected val unicast: (P, Component) -> Unit,
    protected val multicast: (Component) -> Unit
): ReadyBroadcaster<P> {
    override fun broadcastReadyCheck(participant: P, state: ReadyParticipantState) {
        val yes = this.getYesComponent().function { player ->
            state.markReady {
                this.broadcastParticipantReady(this.getContextualParticipant(player, participant))
            }
            ClickEventCallback.Result.Success
        }
        val no = this.getNoComponent().function { player ->
            state.markNotReady {
                this.broadcastParticipantNotReady(this.getContextualParticipant(player, participant))
            }
            ClickEventCallback.Result.Success
        }
        this.unicast.invoke(participant, this.getBroadcastComponent(yes, no))
    }

    protected abstract fun broadcastParticipantReady(participant: P)

    protected abstract fun broadcastParticipantNotReady(participant: P)

    protected open fun getContextualParticipant(player: ServerPlayer, participant: P): P {
        return participant
    }

    protected open fun getYesComponent(): MutableComponent {
        return Component.translatable("arcade.ready.yes").lime()
    }

    protected open fun getNoComponent(): MutableComponent {
        return Component.translatable("arcade.ready.no").red()
    }

    protected abstract fun getBroadcastComponent(yes: Component, no: Component): Component

    public companion object {
        public val PLAYER_UNICAST: (ServerPlayer, Component) -> Unit = ServerPlayer::sendSystemMessage
        public val TEAM_UNICAST: (PlayerTeam, Component) -> Unit = { t, c -> t.getOnlinePlayers().broadcast(c) }

        public fun createGlobalMulticast(server: MinecraftServer): (Component) -> Unit {
            return { component -> server.players.broadcast(component) }
        }
    }
}