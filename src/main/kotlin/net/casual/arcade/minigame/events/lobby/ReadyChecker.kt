package net.casual.arcade.minigame.events.lobby

import net.casual.arcade.commands.hidden.HiddenCommand
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
    public fun getIsReadyMessage(): Component {
        return " is ready!".literal().lime()
    }

    @OverrideOnly
    public fun getNotReadyMessage(): Component {
        return " is not ready!".literal().red()
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
    public fun onReady()

    /**
     * This allows you to check if all teams are ready.
     *
     * This will broadcast a message to all players asking if
     * their team is ready, once all teams confirm they are
     * ready [ReadyChecker.onReady] will be called.
     *
     * @param teams The teams to check.
     * @return The teams that are not ready, this collection is mutable,
     * and may be updated in the future.
     */
    @NonExtendable
    public fun areTeamsReady(teams: Collection<PlayerTeam>): Collection<PlayerTeam> {
        val unready = HashSet<PlayerTeam>()
        for (team in teams) {
            val players = team.getOnlinePlayers()
            if (players.isEmpty()) {
                continue
            }
            unready.add(team)

            val ready = HiddenCommand { context ->
                if (context.player.team == team && unready.remove(team)) {
                    this.broadcast(
                        Component.empty().append(team.formattedDisplayName).append(this.getIsReadyMessage())
                    )
                    if (unready.isEmpty()) {
                        this.onReady()
                    }
                }
                context.removeCommand {
                    if (unready.contains(team)) this.getAlreadyNotReadyMessage() else this.getAlreadyReadyMessage()
                }
            }
            val notReady = HiddenCommand { context ->
                if (context.player.team == team && unready.contains(team)) {
                    this.broadcast(
                        Component.empty().append(team.formattedDisplayName).append(this.getNotReadyMessage())
                    )
                }
                context.removeCommand {
                    if (unready.contains(team)) this.getAlreadyNotReadyMessage() else this.getAlreadyReadyMessage()
                }
            }

            for (player in players) {
                player.sendSystemMessage(this.getReadyMessage(ready, notReady))
            }
        }
        return unready
    }

    /**
     * This allows you to check if all players are ready.
     *
     * This will broadcast a message to all players asking if
     * they are ready, once all players confirm they are
     * ready [ReadyChecker.onReady] will be called.
     *
     * @param players The players to check.
     * @return The players that are not ready, this collection is mutable,
     * and may be updated in the future.
     */
    @NonExtendable
    public fun arePlayersReady(players: Collection<ServerPlayer>): Collection<ServerPlayer> {
        val unready = HashSet<ServerPlayer>(players)
        for (player in players) {
            val ready = HiddenCommand { context ->
                if (context.player == player && unready.remove(player)) {
                    this.broadcast(Component.empty().append(player.displayName!!).append(this.getIsReadyMessage()))
                    if (unready.isEmpty()) {
                        this.onReady()
                    }
                }
                context.removeCommand {
                    if (unready.contains(it)) this.getAlreadyNotReadyMessage() else this.getAlreadyReadyMessage()
                }
            }
            val notReady = HiddenCommand { context ->
                if (context.player == player && unready.contains(player)) {
                    this.broadcast(Component.empty().append(player.displayName!!).append(this.getNotReadyMessage()))
                }
                context.removeCommand {
                    if (unready.contains(it)) this.getAlreadyNotReadyMessage() else this.getAlreadyReadyMessage()
                }
            }
            player.sendSystemMessage(this.getReadyMessage(ready, notReady))
        }
        return unready
    }
}