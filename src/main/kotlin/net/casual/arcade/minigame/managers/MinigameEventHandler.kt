package net.casual.arcade.minigame.managers

import net.casual.arcade.events.EventHandler
import net.casual.arcade.events.EventListener
import net.casual.arcade.events.EventRegisterer
import net.casual.arcade.events.core.Event
import net.casual.arcade.events.level.LevelEvent
import net.casual.arcade.events.minigame.MinigameEvent
import net.casual.arcade.events.player.PlayerEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigamePhase
import net.casual.arcade.minigame.annotation.*
import net.casual.arcade.utils.impl.ConcatenatedList.Companion.concat
import java.util.*
import java.util.function.Consumer

/**
 * This class is an implementation of [EventRegisterer]
 * which handles [EventListener]s for a minigames.
 *
 * This event handler splits events up, minigame events,
 * and minigame phased events.
 * This separation is done so each minigame phase can
 * register its own logic, and once that phase is
 * over, the events can be cleared - removing the logic.
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
 * - The [PlayerEvent]s check using [Minigame.hasPlayer]
 * - The [LevelEvent]s check using [Minigame.hasLevel]
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
public class MinigameEventHandler<M: Minigame<M>>(
    private val owner: Minigame<M>
): EventRegisterer {
    internal val minigame = EventHandler()
    internal val phased = EventHandler()

    /**
     * This method gets all the [EventListener]s for a given
     * [Event] type, given by [type].
     *
     * @param type The type of the [Event] to get listeners for.
     * @return The list of [EventListener]s for the given [type].
     */
    override fun <T: Event> getListenersFor(type: Class<T>): List<EventListener<*>> {
        return this.minigame.getListenersFor(type).concat(this.phased.getListenersFor(type))
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
    public inline fun <reified T: Event> register(priority: Int, flags: Int = DEFAULT, listener: Consumer<T>) {
        this.register(T::class.java, priority, flags, listener)
    }

    /**
     * Registers an event listener with a given priority.
     *
     * This allows you to register a callback to a specific event type.
     * This callback will **only** fire when instances of the given type
     * are fired.
     *
     * This will filter events for the given minigame, see
     * [MinigameEventHandler] documentation for more details.
     *
     * @param T The type of event.
     * @param listener The callback which will be invoked when the event is fired.
     */
    public inline fun <reified T: Event> register(listener: Consumer<T>) {
        this.register(T::class.java, 1_000, listener)
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
    override fun <T: Event> register(type: Class<T>, listener: EventListener<T>) {
        this.registerFiltered(type, listener, this.minigame)
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
    public fun <T: Event> register(
        type: Class<T>,
        priority: Int = 1_000,
        flags: Int = DEFAULT,
        listener: Consumer<T>
    ) {
        this.register(type, flags, EventListener.of(priority, listener))
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
    public fun <T: Event> register(type: Class<T>, flags: Int = DEFAULT, listener: EventListener<T>) {
        this.registerFiltered(type, listener, this.minigame, flags = flags)
    }

    /**
     * Registers an event listener with a given priority for phased events.
     *
     * This means that the event listener may be removed after the current phase
     * of a minigame changes.
     *
     * This will filter events for the given minigame, see
     * [MinigameEventHandler] documentation for more details.
     *
     * See [register] for more information
     *
     * @param T The type of event.
     * @param priority The priority of your event listener.
     * @param listener The callback which will be invoked when the event is fired.
     */
    public inline fun <reified T: Event> registerPhased(priority: Int, flags: Int = DEFAULT, listener: Consumer<T>) {
        this.registerPhased(T::class.java, priority, flags, listener)
    }

    /**
     * Registers an event listener with a given priority for phased events.
     *
     * This means that the event listener may be removed after the current phase
     * of a minigame changes.
     *
     * This will filter events for the given minigame, see
     * [MinigameEventHandler] documentation for more details.
     *
     * See [register] for more information
     *
     * @param T The type of event.
     * @param listener The callback which will be invoked when the event is fired.
     */
    public inline fun <reified T: Event> registerPhased(listener: Consumer<T>) {
        this.registerPhased(T::class.java, 1_000, DEFAULT, listener)
    }

    /**
     * Registers an event listener with a given priority for phased events.
     *
     * This means that the event listener may be removed after the current phase
     * of a minigame changes.
     *
     * This will filter events for the given minigame, see
     * [MinigameEventHandler] documentation for more details.
     *
     * See [register] for more information
     *
     * @param T The type of event.
     * @param type The class of the event that you want to listen to.
     * @param priority The priority of your event listener.
     * @param listener The callback which will be invoked when the event is fired.
     */
    public fun <T: Event> registerPhased(
        type: Class<T>,
        priority: Int = 1_000,
        flags: Int = DEFAULT,
        listener: Consumer<T>
    ) {
        this.registerPhased(type, flags, EventListener.of(priority, listener))
    }

    /**
     * Registers an event listener for phased events.
     *
     * This means that the event listener may be removed after the current phase
     * of a minigame changes.
     *
     * This will filter events for the given minigame, see
     * [MinigameEventHandler] documentation for more details.
     *
     * See [register] for more information
     *
     * @param T The type of event.
     * @param type The class of the event that you want to listen to.
     * @param listener The callback which will be invoked when the event is fired.
     */
    public fun <T: Event> registerPhased(type: Class<T>, flags: Int = DEFAULT, listener: EventListener<T>) {
        return this.registerFiltered(type, listener, this.phased, flags = flags)
    }

    /**
     * Registers an event listener for phased events.
     *
     * This implementation, however, allows you to specify the phases and the
     * event listener will **NOT** be removed if the phase changes, meaning
     * if the phase returns to a previous phase, the events will still be triggered.
     *
     * @param T The type of event.
     * @param phases The phases that you want this listener to trigger in.
     * @param listener The callback which will be invoked when the event is fired.
     */
    public inline fun <reified T: Event> registerInPhases(vararg phases: MinigamePhase<M>, listener: Consumer<T>) {
        this.registerInPhases(1_000, phases = phases, listener = listener)
    }

    /**
     * Registers an event listener for phased events.
     *
     * This implementation, however, allows you to specify the phases and the
     * event listener will **NOT** be removed if the phase changes, meaning
     * if the phase returns to a previous phase, the events will still be triggered.
     *
     * @param T The type of event.
     * @param priority The priority of your event listener.
     * @param phases The phases that you want this listener to trigger in.
     * @param listener The callback which will be invoked when the event is fired.
     */
    public inline fun <reified T: Event> registerInPhases(
        priority: Int,
        flags: Int = DEFAULT,
        vararg phases: MinigamePhase<M>,
        listener: Consumer<T>
    ) {
        this.registerInPhases(T::class.java, flags, phases = phases, listener = EventListener.of(priority, listener))
    }

    /**
     * Registers an event listener for phased events.
     *
     * This implementation, however, allows you to specify the phases and the
     * event listener will **NOT** be removed if the phase changes, meaning
     * if the phase returns to a previous phase, the events will still be triggered.
     *
     * @param T The type of event.
     * @param type The class of the event that you want to listen to.
     * @param phases The phases that you want this listener to trigger in.
     * @param listener The callback which will be invoked when the event is fired.
     */
    public fun <T: Event> registerInPhases(
        type: Class<T>,
        flags: Int = DEFAULT,
        vararg phases: MinigamePhase<M>,
        listener: EventListener<T>
    ) {
        if (phases.isEmpty()) {
            this.register(type, listener)
        }
        val predicates = LinkedList<(T) -> Boolean>()
        if (phases.size == 1) {
            predicates.add { this.owner.isPhase(phases[0]) }
            return this.registerFiltered(type, listener, this.minigame, predicates)
        }
        predicates.add {
            for (phase in phases) {
                if (this.owner.isPhase(phase)) {
                    return@add true
                }
            }
            false
        }

        return this.registerFiltered(type, listener, this.minigame, predicates, flags)
    }

    /**
     * Registers an event listener for phased events, this allows you
     * to specify phase ranges in which to trigger your listener.
     *
     * @param T The type of event.
     * @param start The start phase of the range, inclusive.
     * @param end The end phase of the range, inclusive.
     * @param listener The callback which will be invoked when the event is fired.
     */
    public inline fun <reified T: Event> registerBetweenPhases(
        start: MinigamePhase<M>,
        end: MinigamePhase<M>,
        flags: Int = DEFAULT,
        listener: Consumer<T>
    ) {
        this.registerBetweenPhases(1_000, start, end, flags, listener)
    }

    /**
     * Registers an event listener for phased events, this allows you
     * to specify phase ranges in which to trigger your listener.
     *
     * @param T The type of event.
     * @param priority The priority of your event listener.
     * @param start The start phase of the range, inclusive.
     * @param end The end phase of the range, inclusive.
     * @param listener The callback which will be invoked when the event is fired.
     */
    public inline fun <reified T: Event> registerBetweenPhases(
        priority: Int,
        start: MinigamePhase<M>,
        end: MinigamePhase<M>,
        flags: Int = DEFAULT,
        listener: Consumer<T>
    ) {
        this.registerBetweenPhases(T::class.java, start, end, flags, EventListener.of(priority, listener))
    }

    /**
     * Registers an event listener for phased events, this allows you
     * to specify phase ranges in which to trigger your listener.
     *
     * @param T The type of event.
     * @param type The class of the event that you want to listen to.
     * @param start The start phase of the range, inclusive.
     * @param end The end phase of the range, inclusive.
     * @param listener The callback which will be invoked when the event is fired.
     */
    public fun <T: Event> registerBetweenPhases(
        type: Class<T>,
        start: MinigamePhase<M>,
        end: MinigamePhase<M>,
        flags: Int = DEFAULT,
        listener: EventListener<T>
    ) {
        val predicates = ArrayList<(T) -> Boolean>()
        predicates.add {
            (this.owner.isAfterPhase(start) || this.owner.isPhase(start)) &&
            (this.owner.isBeforePhase(end) || this.owner.isPhase(end))
        }
        return this.registerFiltered(type, listener, this.minigame, predicates, flags)
    }

    private fun <T: Event> registerFiltered(
        type: Class<T>,
        listener: EventListener<T>,
        handler: EventRegisterer,
        predicates: MutableList<(T) -> Boolean> = LinkedList(),
        flags: Int = DEFAULT
    ) {
        if (PlayerEvent::class.java.isAssignableFrom(type)) {
            if (this.hasFlag(flags, HAS_PLAYER)) {
                predicates.add { this.owner.hasPlayer((it as PlayerEvent).player) }
            }
            if (this.hasFlag(flags, IS_PLAYING)) {
                predicates.add { this.owner.isPlaying((it as PlayerEvent).player) }
            }
            if (this.hasFlag(flags, IS_SPECTATOR)) {
                predicates.add { this.owner.isSpectating((it as PlayerEvent).player) }
            }
            if (this.hasFlag(flags, IS_ADMIN)) {
                predicates.add { this.owner.isAdmin((it as PlayerEvent).player) }
            }
        }
        if (LevelEvent::class.java.isAssignableFrom(type)) {
            if (this.hasFlag(flags, HAS_LEVEL)) {
                predicates.add { this.owner.hasLevel((it as LevelEvent).level) }
            }
        }
        if (MinigameEvent::class.java.isAssignableFrom(type)) {
            if (this.hasFlag(flags, IS_MINIGAME)) {
                predicates.add { (it as MinigameEvent).minigame === this.owner }
            }
        }
        if (predicates.isEmpty()) {
            handler.register(type, listener)
            return
        }
        handler.register(type, EventListener.of(listener.priority) { event ->
            if (predicates.all { it(event) }) {
                listener.invoke(event)
            }
        })
    }

    private fun hasFlag(flags: Int, flag: Int): Boolean {
        return flags and flag == flag
    }
}