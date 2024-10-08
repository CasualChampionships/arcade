package net.casual.arcade.minigame.utils

import com.mojang.brigadier.builder.ArgumentBuilder
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.casual.arcade.events.EventListener
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.core.Event
import net.casual.arcade.events.level.LevelEvent
import net.casual.arcade.events.player.PlayerEvent
import net.casual.arcade.events.player.PlayerJoinEvent
import net.casual.arcade.extensions.event.ExtensionEvent
import net.casual.arcade.extensions.event.LevelExtensionEvent
import net.casual.arcade.extensions.event.LevelExtensionEvent.Companion.getExtension
import net.casual.arcade.extensions.event.PlayerExtensionEvent
import net.casual.arcade.extensions.event.PlayerExtensionEvent.Companion.getExtension
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.settings.MinigameSettings
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.annotation.MinigameEventListener
import net.casual.arcade.minigame.events.MinigameEvent
import net.casual.arcade.minigame.extensions.LevelMinigameExtension
import net.casual.arcade.minigame.extensions.PlayerMinigameExtension
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.settings.GameSetting
import net.casual.arcade.scheduler.MinecraftScheduler
import net.casual.arcade.scheduler.task.Completable
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.ComponentUtils.gold
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.arcade.visuals.countdown.Countdown
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.function.Predicate

public object MinigameUtils {
    internal val ServerPlayer.minigame
        get() = this.getExtension<PlayerMinigameExtension>()
    internal val ServerLevel.minigame
        get() = this.getExtension<LevelMinigameExtension>()

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
        players: () -> Collection<ServerPlayer> = minigame.players::all
    ): Completable {
        return this.countdown(duration, interval, scheduler, players)
    }

    @JvmStatic
    public fun <M: Minigame<M>> Minigame<M>.getPhase(id: String): Phase<M>? {
        for (phase in this.phases) {
            if (phase.id == id) {
                return phase
            }
        }
        return null
    }

    public fun <T: ArgumentBuilder<CommandSourceStack, T>> T.requiresAdminOrPermission(level: Int = 4): T {
        return this.requires { source -> source.isMinigameAdminOrHasPermission(level) }
    }

    public fun CommandSourceStack.isMinigameAdminOrHasPermission(level: Int = 4): Boolean {
        if (!this.isPlayer) {
            return this.hasPermission(level)
        }
        return this.playerOrException.isMinigameAdminOrHasPermission(level)
    }

    public fun CommandSourceStack.isPlayerAnd(predicate: Predicate<ServerPlayer>): Boolean {
        return this.isPlayer && predicate.test(this.playerOrException)
    }

    public fun ServerPlayer.isMinigameAdminOrHasPermission(level: Int = 4): Boolean {
        val minigame = this.getMinigame()
        if (minigame != null && minigame.players.isAdmin(this)) {
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

    @Deprecated("Use player manager instead", ReplaceWith(
        "this.players.transferTo(next, players, transferSpectatorStatus, transferAdminStatus)",
        "net.casual.arcade.utils.MinigameUtils.transferTo"
    ))
    public fun Minigame<*>.transferPlayersTo(
        next: Minigame<*>,
        players: Iterable<ServerPlayer> = this.players,
        transferAdminStatus: Boolean = true,
        transferSpectatorStatus: Boolean = true
    ) {
        this.players.transferTo(next, players, transferSpectatorStatus, transferAdminStatus)
    }
    public fun Minigame<*>.transferAdminAndSpectatorTeamsTo(next: Minigame<*>) {
        if (this.teams.hasSpectatorTeam()) {
            next.teams.setSpectatorTeam(this.teams.getSpectatorTeam())
        }
        if (this.teams.hasAdminTeam()) {
            next.teams.setAdminTeam(this.teams.getAdminTeam())
        }
    }

    public fun MinigameSettings.broadcastChangesToAdmin() {
        for (setting in this.all()) {
            @Suppress("UNCHECKED_CAST")
            (setting as GameSetting<Any>).addListener { _, previous, value ->
                this.minigame.chat.broadcastTo(
                    "Setting ".literal()
                        .append(setting.name.literal().gold())
                        .append(" changed from ")
                        .append(previous.toString().literal().red())
                        .append(" to ")
                        .append(value.toString().literal().lime()),
                    this.minigame.players.admins
                )
            }
        }
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
    public fun Entity.isTicking(): Boolean {
        if (this is ServerPlayer) {
            return this.isTicking()
        }
        val level = this.level()
        if (level is ServerLevel) {
            val minigame = level.getMinigame()
            if (minigame != null) {
                if (minigame.settings.freezeEntities.get()) {
                    return false
                }
                if (minigame.effects.isFrozen(this)) {
                    return false
                }
            }
            return level.isTicking()
        }
        return true
    }

    @JvmStatic
    public fun ServerPlayer.isTicking(): Boolean {
        val minigame = this.getMinigame()
        if (minigame != null) {
            if (minigame.settings.tickFreezeOnPause.get(this) && minigame.paused) {
                return false
            }
            if (minigame.settings.freezeEntities.get(this)) {
                return false
            }
            if (minigame.effects.isFrozen(this)) {
                return false
            }
        }
        return true
    }

    internal fun parseMinigameEvents(minigame: Minigame<*>, declarer: Any = minigame) {
        var type: Class<*> = declarer::class.java
        while (type != Any::class.java) {
            for (method in type.declaredMethods) {
                parseMinigameEventMethod(minigame, declarer, method)
            }
            type = type.superclass
        }
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<PlayerExtensionEvent> { event ->
            event.addExtension(::PlayerMinigameExtension)
        }
        GlobalEventHandler.register<PlayerJoinEvent>(Int.MIN_VALUE) { (player) ->
            player.getMinigame()?.players?.add(player)
        }
        GlobalEventHandler.register<LevelExtensionEvent> { event ->
            event.addExtension(::LevelMinigameExtension)
        }

        // This allows us to inject listener providers
        GlobalEventHandler.addInjectedProvider { event, consumer ->
            if (event is ExtensionEvent) {
                return@addInjectedProvider
            }
            val minigames = ObjectOpenHashSet<Minigame<*>>(3)
            if (event is PlayerEvent) {
                val minigame = event.player.getMinigame()
                if (minigame != null) {
                    minigames.add(minigame)
                }
            }
            if (event is LevelEvent) {
                val minigame = event.level.getMinigame()
                if (minigame != null) {
                    minigames.add(minigame)
                }
            }
            if (event is MinigameEvent) {
                minigames.add(event.minigame)
            }
            for (minigame in minigames) {
                consumer.accept(minigame.events.getInjectedProvider())
            }
        }
    }

    private fun <M: Minigame<M>> parseMinigameEventMethod(
        minigame: Minigame<M>,
        declarer: Any,
        method: Method
    ) {
        val event = method.getAnnotation(Listener::class.java) ?: return
        if (!Modifier.isPrivate(method.modifiers)) {
            ArcadeUtils.logger.warn("MinigameEventListener was declared non-private, it should be private!")
        }
        val (type, listener) = createEventListener(declarer, method, event)

        val during = event.during
        if (during.phases.isNotEmpty()) {
            val phases = during.phases.map { id ->
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
        if (during.after != "" || during.before != "") {
            val start = if (during.after != "") {
                minigame.getPhase(during.after) ?: throw IllegalArgumentException("Start phase does not exist")
            } else {
                Phase.none()
            }
            val end = if (during.before != "") {
                minigame.getPhase(during.before) ?: throw IllegalArgumentException("End phase does not exist")
            } else {
                Phase.end()
            }
            minigame.events.registerBetweenPhases(
                type = type,
                after = start,
                before = end,
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
        return type to EventListener.of(priority, event.phase) { handle.invoke(declarer, it) }
    }
}