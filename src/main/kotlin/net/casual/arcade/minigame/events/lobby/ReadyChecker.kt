package net.casual.arcade.minigame.events.lobby

import net.casual.arcade.commands.hidden.HiddenCommand
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.ComponentUtils.crimson
import net.casual.arcade.utils.ComponentUtils.function
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import org.jetbrains.annotations.ApiStatus.NonExtendable
import org.jetbrains.annotations.ApiStatus.OverrideOnly

public interface ReadyChecker {
    @OverrideOnly
    public fun getReadyMessage(ready: HiddenCommand, notReady: HiddenCommand): Component {
        return "Are you ready? ".literal()
            .append("[Yes]".literal().function(ready).lime())
            .append(" ")
            .append("[No]".literal().function(notReady).red())
    }

    @OverrideOnly
    public fun getIsReadyMessage(readier: Component): Component {
        return Component.empty().append(readier).append(" is ready!".literal().lime())
    }

    @OverrideOnly
    public fun getNotReadyMessage(readier: Component): Component {
        return Component.empty().append(readier).append(" is not ready!".literal().red())
    }

    @OverrideOnly
    public fun getAlreadyReadyMessage(): Component {
        return "You are already ready!".literal().crimson()
    }

    @OverrideOnly
    public fun getAlreadyNotReadyMessage(): Component {
        return "You are already marked as not ready!".literal().crimson()
    }

    @OverrideOnly
    public fun broadcast(message: Component)

    @OverrideOnly
    public fun broadcastTo(message: Component, player: ServerPlayer)

    /**
     * This allows you to check if all teams are ready.
     *
     * This will broadcast a message to all players asking if
     * their team is ready, once all teams confirm the callback
     * will be invoked.
     *
     * @param teams The teams to check.
     * @param callback The function called when all tean are ready.
     * @return The teams that are not ready, this collection is mutable,
     * and may be updated in the future.
     */
    @NonExtendable
    public fun areTeamsReady(
        teams: Collection<PlayerTeam>,
        callback: () -> Unit
    ): Collection<PlayerTeam> {
        val unready = HashSet<PlayerTeam>()
        for (team in teams) {
            val players = team.getOnlinePlayers()
            if (players.isEmpty()) {
                continue
            }
            unready.add(team)

            val ready = HiddenCommand { context ->
                if (context.player.team == team && unready.remove(team)) {
                    this.broadcast(this.getIsReadyMessage(team.formattedDisplayName))
                    if (unready.isEmpty()) {
                        callback()
                    }
                }
                context.removeCommand {
                    if (unready.contains(team)) this.getAlreadyNotReadyMessage() else this.getAlreadyReadyMessage()
                }
            }
            val notReady = HiddenCommand { context ->
                if (context.player.team == team && unready.contains(team)) {
                    this.broadcast(this.getNotReadyMessage(team.formattedDisplayName))
                }
                context.removeCommand {
                    if (unready.contains(team)) this.getAlreadyNotReadyMessage() else this.getAlreadyReadyMessage()
                }
            }

            for (player in players) {
                this.broadcastTo(this.getReadyMessage(ready, notReady), player)
            }
        }
        return unready
    }

    /**
     * This allows you to check if all players are ready.
     *
     * This will broadcast a message to all players asking if
     * they are ready, once all players confirm the callback
     * will be invoked.
     *
     * @param players The players to check.
     * @param callback The function called when all players are ready.
     * @return The players that are not ready, this collection is mutable,
     * and may be updated in the future.
     */
    @NonExtendable
    public fun arePlayersReady(
        players: Collection<ServerPlayer>,
        callback: () -> Unit
    ): Collection<ServerPlayer> {
        val unready = HashSet<ServerPlayer>(players)
        for (player in players) {
            val ready = HiddenCommand { context ->
                if (context.player == player && unready.remove(player)) {
                    this.broadcast(this.getIsReadyMessage(player.displayName!!))
                    if (unready.isEmpty()) {
                        callback()
                    }
                }
                context.removeCommand {
                    if (unready.contains(it)) this.getAlreadyNotReadyMessage() else this.getAlreadyReadyMessage()
                }
            }
            val notReady = HiddenCommand { context ->
                if (context.player == player && unready.contains(player)) {
                    this.broadcast(this.getNotReadyMessage(player.displayName!!))
                }
                context.removeCommand {
                    if (unready.contains(it)) this.getAlreadyNotReadyMessage() else this.getAlreadyReadyMessage()
                }
            }
            this.broadcastTo(this.getReadyMessage(ready, notReady), player)
        }
        return unready
    }

    public companion object {
        public fun of(minigame: Minigame<*>): ReadyChecker {
            return object: ReadyChecker {
                override fun broadcast(message: Component) {
                    minigame.chat.broadcast(message)
                }

                override fun broadcastTo(message: Component, player: ServerPlayer) {
                    minigame.chat.broadcastTo(message, player)
                }
            }
        }
    }
}