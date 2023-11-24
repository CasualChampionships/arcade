package net.casual.arcade.utils

import net.casual.arcade.commands.hidden.HiddenCommand
import net.casual.arcade.events.EventListener
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.core.Event
import net.casual.arcade.events.player.PlayerCreatedEvent
import net.casual.arcade.events.player.PlayerJoinEvent
import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.minigame.lobby.ReadyChecker
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigamePhase
import net.casual.arcade.minigame.annotation.MinigameEvent
import net.casual.arcade.minigame.extensions.PlayerMinigameExtension
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.task.Completable
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
        GlobalEventHandler.register<PlayerJoinEvent>(Int.MIN_VALUE) { (player) ->
            player.getMinigame()?.addPlayer(player)
        }
    }

    private fun <M: Minigame<M>> parseMinigameEventMethod(minigame: Minigame<M>, method: Method) {
        val event = method.getAnnotation(MinigameEvent::class.java) ?: return
        val (type, listener) = this.createEventListener(minigame, method, event)

        if (event.phases.isNotEmpty()) {
            val phases = event.phases.map { id ->
                val phase = minigame.getPhase(id)
                phase ?: throw IllegalArgumentException("Phase with id $id does not exist")
            }
            minigame.events.registerInPhases(
                type = type,
                phases = phases.toTypedArray(),
                listener = listener
            )
            return
        }
        if (event.start != "" && event.end != "") {
            val start = minigame.getPhase(event.start) ?: throw IllegalArgumentException("Start phase does not exist")
            val end = minigame.getPhase(event.end) ?: throw IllegalArgumentException("End phase does not exist")
            minigame.events.registerBetweenPhases(
                type = type,
                start = start,
                end = end,
                listener = listener
            )
            return
        }
        minigame.events.register(type = type, listener = listener)
    }

    private fun createEventListener(
        minigame: Minigame<*>,
        method: Method,
        event: MinigameEvent
    ): Pair<Class<Event>, EventListener<Event>> {
        if (method.parameterCount != 1) {
            throw IllegalArgumentException("MinigameEvent ($method) has unexpected parameter count, should be 1")
        }

        val type = method.parameterTypes[0]
        if (!Event::class.java.isAssignableFrom(type)) {
            val message = "MinigameEvent ($method) only accepts parameter type $type but should accept Event"
            throw IllegalArgumentException(message)
        }
        @Suppress("UNCHECKED_CAST")
        type as Class<Event>

        val priority = event.priority

        method.isAccessible = true
        val handle = MethodHandles.lookup().unreflect(method)
        return type to EventListener.of(priority) { handle.invoke(minigame, it) }
    }
}