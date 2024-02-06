package net.casual.arcade.utils

import com.mojang.brigadier.builder.ArgumentBuilder
import net.casual.arcade.Arcade
import net.casual.arcade.commands.hidden.HiddenCommand
import net.casual.arcade.events.EventListener
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.core.Event
import net.casual.arcade.events.level.LevelCreatedEvent
import net.casual.arcade.events.player.PlayerCreatedEvent
import net.casual.arcade.events.player.PlayerJoinEvent
import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigamePhase
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.annotation.MinigameEventListener
import net.casual.arcade.minigame.events.lobby.ReadyChecker
import net.casual.arcade.minigame.extensions.LevelMinigameExtension
import net.casual.arcade.minigame.extensions.PlayerMinigameExtension
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.scheduler.MinecraftScheduler
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.task.Completable
import net.casual.arcade.task.Task
import net.casual.arcade.utils.LevelUtils.addExtension
import net.casual.arcade.utils.LevelUtils.getExtension
import net.casual.arcade.utils.PlayerUtils.addExtension
import net.casual.arcade.utils.PlayerUtils.getExtension
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.casual.arcade.utils.TimeUtils.Seconds
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method
import java.lang.reflect.Modifier

public object MinigameUtils {
    internal val ServerPlayer.minigame
        get() = this.getExtension(PlayerMinigameExtension::class.java)
    internal val ServerLevel.minigame
        get() = this.getExtension(LevelMinigameExtension::class.java)

    @JvmStatic
    public fun ServerPlayer.getMinigame(): Minigame<*>? {
        return this.minigame.getMinigame()
    }

    @JvmStatic
    public fun ServerLevel.getMinigame(): Minigame<*>? {
        return this.minigame.getMinigame()
    }

    /**
     * This counts down for the specified duration, and sends
     * the countdown to all players for a given minigame.
     *
     * @param minigame The minigame to send the countdown to.
     * @param players The players function, the players to send the countdown to.
     * @return A [Completable] that will complete when the countdown is finished.
     */
    @JvmStatic
    public fun <M: Minigame<M>> Countdown.countdown(
        minigame: Minigame<M>,
        duration: MinecraftTimeDuration = 10.Seconds,
        interval: MinecraftTimeDuration = 1.Seconds,
        scheduler: MinecraftScheduler = minigame.scheduler.asPhasedScheduler(),
        players: () -> Collection<ServerPlayer> = minigame::getAllPlayers
    ): Completable {
        return this.countdown(duration, interval, scheduler, players)
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

    public fun <T: ArgumentBuilder<CommandSourceStack, T>> T.requiresAdminOrPermission(level: Int = 4): T {
        return this.requires { source ->
            if (source.isPlayer) {
                source.playerOrException.isMinigameAdminOrHasPermission(level)
            } else source.hasPermission(level)
        }
    }

    public fun ServerPlayer.isMinigameAdminOrHasPermission(level: Int = 4): Boolean {
        val minigame = this.getMinigame()
        if (minigame != null && minigame.isAdmin(this)) {
            return true
        }
        return this.hasPermissions(level)
    }

    public fun Minigame<*>.addEventListener(listener: MinigameEventListener) {
        if (listener is Minigame<*>) {
            throw IllegalArgumentException("Cannot parse Minigame as ${listener::class.java}")
        }
        parseMinigameEvents(this, listener)
    }

    public fun Minigame<*>.transferTo(next: Minigame<*>) {
        if (next.closed) {
            throw IllegalArgumentException("Cannot transfer to a closed minigame")
        }

        if (this.teams.hasSpectatorTeam()) {
            next.teams.setSpectatorTeam(this.teams.getSpectatorTeam())
        }
        if (this.teams.hasAdminTeam()) {
            next.teams.setAdminTeam(this.teams.getAdminTeam())
        }

        val players = this.getAllPlayers()
        val delayed = ArrayList<() -> Unit>()
        for (player in players) {
            val wasAdmin = this.isAdmin(player)
            val wasSpectating = this.isSpectating(player)
            delayed.add {
                next.addPlayer(player)
                if (wasAdmin) {
                    next.makeAdmin(player)
                }
                if (wasSpectating) {
                    next.makeSpectator(player)
                }
            }
        }
        this.close()
        delayed.forEach { it() }

        next.start()
    }

    @JvmStatic
    public fun ServerLevel.isTicking(): Boolean {
        val ticking = this.tickRateManager().runsNormally()
        if (!ticking) {
            return false
        }
        val minigame = this.getMinigame()
        if (minigame != null && minigame.settings.tickFreezeOnPause.get()) {
            return !minigame.paused
        }
        return true
    }

    @JvmStatic
    public fun ServerPlayer.isTicking(): Boolean {
        val minigame = this.getMinigame()
        if (minigame != null && minigame.settings.tickFreezeOnPause.get(this)) {
            return !minigame.paused
        }
        return true
    }

    internal fun parseMinigameEvents(minigame: Minigame<*>, declarer: Any = minigame) {
        var type: Class<*> = declarer::class.java
        while (type != Any::class.java) {
            for (method in type.declaredMethods) {
                this.parseMinigameEventMethod(minigame, declarer, method)
            }
            type = type.superclass
        }
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<PlayerCreatedEvent> { (player) ->
            player.addExtension(PlayerMinigameExtension(player.connection))
        }
        GlobalEventHandler.register<PlayerJoinEvent>(Int.MIN_VALUE) { (player) ->
            player.getMinigame()?.addPlayer(player)
        }
        GlobalEventHandler.register<LevelCreatedEvent> { (level) ->
            level.addExtension(LevelMinigameExtension(level))
        }
    }

    private fun <M: Minigame<M>> parseMinigameEventMethod(
        minigame: Minigame<M>,
        declarer: Any,
        method: Method
    ) {
        val event = method.getAnnotation(Listener::class.java) ?: return
        if (!Modifier.isPrivate(method.modifiers)) {
            Arcade.logger.warn("MinigameEventListener was declared non-private, it should be private!")
        }
        val (type, listener) = this.createEventListener(declarer, method, event)

        if (event.phases.isNotEmpty()) {
            val phases = event.phases.map { id ->
                val phase = minigame.getPhase(id)
                phase ?: throw IllegalArgumentException("Phase with id $id does not exist")
            }
            minigame.events.registerInPhases(
                type = type,
                phases = phases.toTypedArray(),
                flags = event.flags,
                listener = listener
            )
            return
        }
        if (event.after != "" || event.before != "") {
            val start = if (event.after != "") {
                minigame.getPhase(event.after) ?: throw IllegalArgumentException("Start phase does not exist")
            } else {
                MinigamePhase.none()
            }
            val end = if (event.after != "") {
                minigame.getPhase(event.before) ?: throw IllegalArgumentException("End phase does not exist")
            } else {
                MinigamePhase.end()
            }
            minigame.events.registerBetweenPhases(
                type = type,
                start = start,
                end = end,
                flags = event.flags,
                listener = listener
            )
            return
        }
        minigame.events.register(type = type, flags = event.flags, listener = listener)
    }

    private fun createEventListener(
        declarer: Any,
        method: Method,
        event: Listener
    ): Pair<Class<Event>, EventListener<Event>> {
        if (method.parameterCount != 1) {
            throw IllegalArgumentException("Minigame Listener ($method) has unexpected parameter count, should be 1")
        }

        val type = method.parameterTypes[0]
        if (!Event::class.java.isAssignableFrom(type)) {
            val message = "Minigame Listener ($method) only accepts parameter type $type but should accept Event"
            throw IllegalArgumentException(message)
        }
        @Suppress("UNCHECKED_CAST")
        type as Class<Event>

        val priority = event.priority

        method.isAccessible = true
        val handle = MethodHandles.lookup().unreflect(method)
        return type to EventListener.of(priority) { handle.invoke(declarer, it) }
    }
}