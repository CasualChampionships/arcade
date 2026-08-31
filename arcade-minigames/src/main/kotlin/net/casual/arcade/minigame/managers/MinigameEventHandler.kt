/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.managers

import net.casual.arcade.events.EventListener
import net.casual.arcade.events.EventListenerHandle
import net.casual.arcade.events.ListenerProvider
import net.casual.arcade.events.ListenerRegistry
import net.casual.arcade.events.SimpleListenerRegistry
import net.casual.arcade.events.common.Event
import net.casual.arcade.events.common.ServerSideEvent
import net.casual.arcade.events.phase.BuiltInEventPhases
import net.casual.arcade.events.server.level.LevelEvent
import net.casual.arcade.events.server.level.LocatedLevelEvent
import net.casual.arcade.events.server.player.PlayerEvent
import net.casual.arcade.events.threading.ThreadingStrategy
import net.casual.arcade.events.threading.ThreadingTarget
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.annotation.ListenerFlags.DEFAULT
import net.casual.arcade.minigame.annotation.ListenerFlags.HAS_LEVEL
import net.casual.arcade.minigame.annotation.ListenerFlags.HAS_PLAYER
import net.casual.arcade.minigame.annotation.ListenerFlags.IN_LEVEL_BOUNDS
import net.casual.arcade.minigame.annotation.ListenerFlags.IS_ADMIN
import net.casual.arcade.minigame.annotation.ListenerFlags.IS_MINIGAME
import net.casual.arcade.minigame.annotation.ListenerFlags.IS_PLAYING
import net.casual.arcade.minigame.annotation.ListenerFlags.IS_SPECTATOR
import net.casual.arcade.minigame.events.MinigameEvent
import java.util.*
import java.util.function.Consumer

/**
 * This class is an implementation of [ListenerRegistry]
 * which handles [EventListener]s for a minigames.
 *
 * All events that are registered are **filtered** for
 * the given minigame. This means that only certain events
 * listeners may not be invoked since they are not
 * relevant to the minigame.
 *
 * For example, certain [PlayerEvent]s, will not invoke
 * listeners if the player that caused the event isn't
 * playing in that minigame, this also goes for [LevelEvent]s
 * and [MinigameEvent]s:
 * - The [PlayerEvent]s check using [MinigamePlayerManager.has]
 * - The [LevelEvent]s check using [MinigameLevelManager.has]
 * - The [MinigameEvent]s check simply check whether the
 * current minigame is the one that fired the event.
 *
 * If the event that you are registering doesn't implement
 * one of these interfaces, then it will register normally
 * and won't be filtered.
 *
 * @see Minigame
 * @see Minigame.events
 */
public class MinigameEventHandler(
    private val minigame: Minigame
): ListenerRegistry<ServerSideEvent> {
    private val global = SimpleListenerRegistry<ServerSideEvent>()
    private val injected = SimpleListenerRegistry<ServerSideEvent>()

    /**
     * This method gets all the [EventListener]s for a given
     * [Event] type, given by [type].
     *
     * @param type The type of the [Event] to get listeners for.
     * @return The list of [EventListener]s for the given [type].
     */
    override fun <T: Event> getListenersFor(type: Class<T>): List<EventListener<*>> {
        return this.global.getListenersFor(type)
    }

    /**
     * Registers an event listener with a given priority.
     *
     * This allows you to register a callback to a specific event type.
     * This callback will **only** fire when instances of the given type
     * are fired.
     *
     * The priority that you register the event with determines
     * in what order the listener will be invoked. Lower values
     * of [priority] will result in being invoked earlier.
     *
     * This will filter events for the given minigame, see
     * [MinigameEventHandler] documentation for more details.
     *
     * @param T The type of event.
     * @param priority The priority of your event listener.
     * @param listener The callback which will be invoked when the event is fired.
     */
    public inline fun <reified T: ServerSideEvent> register(
        priority: Int,
        phase: Int = BuiltInEventPhases.DEFAULT,
        flags: Int = DEFAULT,
        strategy: ThreadingStrategy = ThreadingTarget.Default,
        listener: Consumer<T>
    ): EventListenerHandle {
        return this.register(T::class.java, priority, phase, flags, strategy, listener)
    }

    /**
     * Registers an event listener.
     *
     * This allows you to register a callback to a specific event type.
     * This callback will **only** fire when instances of the given type
     * are fired.
     *
     * This will filter events for the given minigame, see
     * [MinigameEventHandler] documentation for more details.
     *
     * @param T The type of event.
     * @param type The class of the event that you want to listen to.
     * @param listener The callback which will be invoked when the event is fired.
     */
    override fun <T: ServerSideEvent> register(type: Class<T>, listener: EventListener<T>): EventListenerHandle {
        return this.registerFiltered(type, listener)
    }

    /**
     * Registers an event listener with a given priority.
     *
     * This allows you to register a callback to a specific event type.
     * This callback will **only** fire when instances of the given type
     * are fired.
     *
     * The priority that you register the event with determines
     * in what order the listener will be invoked. Lower values
     * of [priority] will result in being invoked earlier.
     *
     * This will filter events for the given minigame, see
     * [MinigameEventHandler] documentation for more details.
     *
     * @param T The type of event.
     * @param priority The priority of your event listener.
     * @param listener The callback which will be invoked when the event is fired.
     */
    public fun <T: ServerSideEvent> register(
        type: Class<T>,
        priority: Int = 1_000,
        phase: Int = BuiltInEventPhases.DEFAULT,
        flags: Int = DEFAULT,
        strategy: ThreadingStrategy = ThreadingTarget.Default,
        listener: Consumer<T>
    ): EventListenerHandle {
        return this.register(type, flags, EventListener.of(priority, phase, strategy, listener))
    }

    /**
     * Registers an event listener.
     *
     * This allows you to register a callback to a specific event type.
     * This callback will **only** fire when instances of the given type
     * are fired.
     *
     * This will filter events for the given minigame, see
     * [MinigameEventHandler] documentation for more details.
     *
     * @param T The type of event.
     * @param type The class of the event that you want to listen to.
     * @param listener The callback which will be invoked when the event is fired.
     */
    public fun <T: ServerSideEvent> register(
        type: Class<T>,
        flags: Int = DEFAULT,
        listener: EventListener<T>
    ): EventListenerHandle {
        return this.registerFiltered(type, listener, flags)
    }


    internal fun getInjectedProvider(): ListenerProvider {
        return this.injected
    }

    internal fun clear() {
        this.global.clear()
        this.injected.clear()
    }

    private fun <T: ServerSideEvent> registerFiltered(
        type: Class<T>,
        listener: EventListener<T>,
        flags: Int = DEFAULT
    ): EventListenerHandle {
        val predicates = LinkedList<(T) -> Boolean>()
        var registry = this.global
        if (PlayerEvent::class.java.isAssignableFrom(type)) {
            if (this.hasFlag(flags, HAS_PLAYER)) {
                registry = this.injected
                predicates.add { this.minigame.players.has((it as PlayerEvent).player) }
            }
            if (this.hasFlag(flags, IS_PLAYING)) {
                registry = this.injected
                predicates.add { this.minigame.players.isPlaying((it as PlayerEvent).player) }
            }
            if (this.hasFlag(flags, IS_SPECTATOR)) {
                registry = this.injected
                predicates.add { this.minigame.players.isSpectating((it as PlayerEvent).player) }
            }
            if (this.hasFlag(flags, IS_ADMIN)) {
                registry = this.injected
                predicates.add { this.minigame.players.isAdmin((it as PlayerEvent).player) }
            }
        }
        if (LocatedLevelEvent::class.java.isAssignableFrom(type)) {
            if (this.hasFlag(flags, IN_LEVEL_BOUNDS)) {
                registry = this.injected
                predicates.add {
                    val casted = it as LocatedLevelEvent
                    this.minigame.levels.has(casted.level, casted.pos)
                }
            }
        }
        if (LevelEvent::class.java.isAssignableFrom(type)) {
            if (this.hasFlag(flags, HAS_LEVEL)) {
                registry = this.injected
                predicates.add { this.minigame.levels.has((it as LevelEvent).level) }
            }
        }
        if (MinigameEvent::class.java.isAssignableFrom(type)) {
            if (this.hasFlag(flags, IS_MINIGAME)) {
                registry = this.injected
                predicates.add { this.minigame === (it as MinigameEvent).minigame }
            }
        }
        if (predicates.isEmpty()) {
            return registry.register(type, listener)
        }
        return registry.register(type, EventListener.of(listener.priority, listener.phase) { event ->
            if (predicates.all { it(event) }) {
                listener.invoke(event)
            }
        })
    }

    private fun hasFlag(flags: Int, flag: Int): Boolean {
        return (flags and flag) == flag
    }
}