package net.casual.arcade.utils

import net.casual.arcade.commands.hidden.HiddenCommand
import net.casual.arcade.events.EventListener
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.core.Event
import net.casual.arcade.events.player.PlayerCreatedEvent
import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.gui.ready.ReadyChecker
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigamePhase
import net.casual.arcade.minigame.annotation.MinigameEvent
import net.casual.arcade.minigame.extensions.PlayerMinigameExtension
import net.casual.arcade.minigame.lobby.LobbyMinigame
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.task.Completable
import net.casual.arcade.utils.MinigameUtils.areTeamsReady
import net.casual.arcade.utils.PlayerUtils.addExtension
import net.casual.arcade.utils.PlayerUtils.getExtension
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import java.lang.IllegalArgumentException
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method

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
                    context.removeCommand {
                        if (unready.contains(team)) this.getAlreadyNotReadyMessage() else this.getAlreadyReadyMessage()
                    }
                }
                val notReady = HiddenCommand { context ->
                    if (context.player.team == team && unready.contains(team)) {
                        this.broadcast(Component.empty().append(team.formattedDisplayName).append(this.getNotReadyMessage()))
                    }
                    context.removeCommand {
                        if (unready.contains(team)) this.getAlreadyNotReadyMessage() else this.getAlreadyReadyMessage()
                    }
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
                context.removeCommand {
                    if (unready.contains(it)) this.getAlreadyNotReadyMessage() else this.getAlreadyReadyMessage()
                }
            }
            val notReady = HiddenCommand { context ->
                if (context.player == player && unready.contains(player)) {
                    this.broadcast(Component.empty().append(player.displayName).append(this.getNotReadyMessage()))
                }
                context.removeCommand {
                    if (unready.contains(it)) this.getAlreadyNotReadyMessage() else this.getAlreadyReadyMessage()
                }
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

    @JvmStatic
    public fun <M: Minigame<M>> Minigame<M>.getPhase(id: String): MinigamePhase<M>? {
        for (phase in this.phases) {
            if (phase.id == id) {
                return phase
            }
        }
        return null
    }

    internal fun parseMinigameEvents(minigame: Minigame<*>) {
        var type: Class<*> = minigame::class.java
        while (type != Minigame::class.java) {
            for (method in type.declaredMethods) {
                this.parseMinigameEventMethod(minigame, method)
            }
            type = type.superclass
        }
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<PlayerCreatedEvent> { (player) ->
            player.addExtension(PlayerMinigameExtension(player))
        }
    }

    private fun <M: Minigame<M>> parseMinigameEventMethod(minigame: Minigame<M>, method: Method) {
        if (method.parameterCount != 1) {
            return
        }

        val parameter = method.parameterTypes[0]
        if (!Event::class.java.isAssignableFrom(parameter)) {
            return
        }
        @Suppress("UNCHECKED_CAST")
        parameter as Class<Event>

        val event = method.getAnnotation(MinigameEvent::class.java) ?: return
        val priority = event.priority

        method.isAccessible = true
        val handle = MethodHandles.lookup().unreflect(method)
        val listener = EventListener.of<Event>(priority) { handle.invoke(minigame, it) }

        if (event.phases.isNotEmpty()) {
            val phases = event.phases.map { id ->
                val phase = minigame.getPhase(id)
                phase ?: throw IllegalArgumentException("Phase with id $id does not exist")
            }
            minigame.events.registerInPhases(
                type = parameter,
                phases = phases.toTypedArray(),
                listener = listener
            )
            return
        }
        if (event.start != "" && event.end != "") {
            val start = minigame.getPhase(event.start) ?: throw IllegalArgumentException("Start phase does not exist")
            val end = minigame.getPhase(event.end) ?: throw IllegalArgumentException("End phase does not exist")
            minigame.events.registerBetweenPhases(
                type = parameter,
                start = start,
                end = end,
                listener = listener
            )
            return
        }
        minigame.events.register(type = parameter, listener = listener)
    }
}