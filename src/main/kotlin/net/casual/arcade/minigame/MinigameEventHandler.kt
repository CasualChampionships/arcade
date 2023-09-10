package net.casual.arcade.minigame

import net.casual.arcade.events.EventHandler
import net.casual.arcade.events.EventListener
import net.casual.arcade.events.EventRegisterer
import net.casual.arcade.events.core.Event
import net.casual.arcade.events.level.LevelEvent
import net.casual.arcade.events.minigame.MinigameEvent
import net.casual.arcade.events.player.PlayerEvent
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
class MinigameEventHandler(
    private val owner: Minigame<*>
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
    inline fun <reified T: Event> register(priority: Int = 1_000, listener: Consumer<T>) {
        this.register(T::class.java, priority, listener)
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
     * Registers an event listener with a given priority for phased events.
     *
     * This means that the event listener may be removed after the phase
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
    inline fun <reified T: Event> registerPhased(priority: Int = 1_000, listener: Consumer<T>) {
        this.registerPhased(T::class.java, priority, listener)
    }

    /**
     * Registers an event listener with a given priority for phased events.
     *
     * This means that the event listener may be removed after the phase
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
    fun <T: Event> registerPhased(type: Class<T>, priority: Int = 1_000, listener: Consumer<T>) {
        this.registerPhased(type, EventListener.of(priority, listener))
    }

    /**
     * Registers an event listener for phased events.
     *
     * This means that the event listener may be removed after the phase
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
    fun <T: Event> registerPhased(type: Class<T>, listener: EventListener<T>) {
        return this.registerFiltered(type, listener, this.phased)
    }

    private fun <T: Event> registerFiltered(type: Class<T>, listener: EventListener<T>, handler: EventRegisterer) {
        val predicates = LinkedList<(T) -> Boolean>()
        if (PlayerEvent::class.java.isAssignableFrom(type)) {
            predicates.add { this.owner.hasPlayer((it as PlayerEvent).player) }
        }
        if (LevelEvent::class.java.isAssignableFrom(type)) {
            predicates.add { this.owner.hasLevel((it as LevelEvent).level) }
        }
        if (MinigameEvent::class.java.isAssignableFrom(type)) {
            predicates.add { (it as MinigameEvent).minigame === this.owner }
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
}