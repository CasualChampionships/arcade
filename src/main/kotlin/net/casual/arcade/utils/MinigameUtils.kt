package net.casual.arcade.utils

import net.casual.arcade.commands.hidden.HiddenCommand
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerCreatedEvent
import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.gui.ready.ReadyChecker
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.extensions.PlayerMinigameExtension
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.task.Completable
import net.casual.arcade.utils.PlayerUtils.addExtension
import net.casual.arcade.utils.PlayerUtils.getExtension
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam

public object MinigameUtils {
    internal val ServerPlayer.minigame
        get() = this.getExtension(PlayerMinigameExtension::class.java)

    @JvmStatic
    public fun ServerPlayer.getMinigame(): Minigame<*>? {
        return this.minigame.getMinigame()
    }

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
    @JvmStatic
    public fun ReadyChecker.areTeamsReady(teams: Collection<PlayerTeam>): Collection<PlayerTeam> {
        val unready = HashSet<PlayerTeam>()
        for (team in teams) {
            val players = team.getOnlinePlayers()
            if (players.isNotEmpty()) {
                unready.add(team)

                val ready = HiddenCommand { context ->
                    if (context.player.team == team && unready.remove(team)) {
                        this.broadcast(Component.empty().append(team.formattedDisplayName).append(this.getIsReadyMessage()))
                        if (unready.isEmpty()) {
                            this.onReady()
                        }
                    }
                    context.deleteCommand()
                }
                val notReady = HiddenCommand { context ->
                    if (context.player.team == team && unready.contains(team)) {
                        this.broadcast(Component.empty().append(team.formattedDisplayName).append(this.getNotReadyMessage()))
                    }
                    context.deleteCommand()
                }

                for (player in players) {
                    player.sendSystemMessage(this.getReadyMessage(ready, notReady))
                }
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
    @JvmStatic
    public fun ReadyChecker.arePlayersReady(players: Collection<ServerPlayer>): Collection<ServerPlayer> {
        val unready = HashSet<ServerPlayer>(players)
        for (player in players) {
            val ready = HiddenCommand { context ->
                if (context.player == player && unready.remove(player)) {
                    this.broadcast(Component.empty().append(player.displayName).append(this.getIsReadyMessage()))
                    if (unready.isEmpty()) {
                        this.onReady()
                    }
                }
                context.deleteCommand()
            }
            val notReady = HiddenCommand { context ->
                if (context.player == player && unready.contains(player)) {
                    this.broadcast(Component.empty().append(player.displayName).append(this.getNotReadyMessage()))
                }
                context.deleteCommand()
            }
            player.sendSystemMessage(this.getReadyMessage(ready, notReady))
        }
        return unready
    }

    /**
     * This counts down for the specified duration, and sends
     * the countdown to all players for a given minigame.
     *
     * @param minigame The minigame to send the countdown to.
     * @return A [Completable] that will complete when the countdown is finished.
     */
    @JvmStatic
    public fun Countdown.countdown(minigame: Minigame<*>): Completable {
        val post = Completable.Impl()
        var remaining = this.getDuration()
        val interval = this.getInterval()
        var current = remaining / interval
        minigame.scheduler.schedulePhasedInLoop(MinecraftTimeDuration.ZERO, interval, remaining) {
            this.sendCountdown(minigame.getPlayers(), current--, remaining)
            remaining -= interval
        }
        minigame.scheduler.schedulePhased(remaining) {
            this.afterCountdown(minigame.getPlayers())
            post.complete()
        }
        return post
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<PlayerCreatedEvent> { (player) ->
            player.addExtension(PlayerMinigameExtension(player))
        }
    }
}