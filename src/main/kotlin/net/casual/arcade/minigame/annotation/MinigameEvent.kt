package net.casual.arcade.minigame.annotation

import net.casual.arcade.events.core.Event
import net.casual.arcade.minigame.managers.MinigameEventHandler
import net.casual.arcade.minigame.managers.MinigameEventHandler.Companion.HAS_LEVEL
import net.casual.arcade.minigame.managers.MinigameEventHandler.Companion.HAS_PLAYER
import net.casual.arcade.minigame.managers.MinigameEventHandler.Companion.IS_MINIGAME
import kotlin.reflect.KClass

/**
 * This annotation is used to mark a method as an event handler.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
public annotation class MinigameEvent(
    /**
     * This is the type of the event, this does not need
     * to be explicitly specified.
     */
    val type: KClass<out Event> = Event::class,

    /**
     * This is the priority of the event.
     * Higher values will be called later.
     */
    val priority: Int = 1_000,

    /**
     * The flags for setting the minigame event.
     */
    val flags: Int = MinigameEventHandler.DEFAULT,

    /**
     * These will be the phases of the minigame that
     * this event handler will be invoked in.
     *
     * If this array is empty, the event handler will
     * either use the [start] and [end] to determine
     * the events that it will be invoked in, or it will
     * be invoked in all phases.
     */
    val phases: Array<String> = [],

    /**
     * This is the id of the start phase that this
     * handler should be invoked in.
     *
     * This **must** be accompanied by an [end].
     *
     * Together with the [end], this will be used
     * to invoke this handler when the minigame phase
     * is between the given phases.
     */
    val start: String = "",

    /**
     * This is the id of the end phase that this
     * handler should be invoked in.
     *
     * This **must** be accompanied by a [start].
     *
     * Together with the [start], this will be used
     * to invoke this handler when the minigame phase
     * is between the given phases.
     */
    val end: String = ""
)
