package net.casual.arcade.minigame.annotation

import net.casual.arcade.events.BuiltInEventPhases
import net.casual.arcade.events.core.Event
import net.casual.arcade.minigame.phase.Phase
import kotlin.reflect.KClass

/**
 * This annotation is used to mark a method as an event handler.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
public annotation class Listener(
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
    val flags: Int = ListenerFlags.DEFAULT,

    /**
     * This is the phase of the event that this handler
     * will be invoked in.
     */
    val phase: String = BuiltInEventPhases.DEFAULT,

    /**
     * This specified during when this listener will be invoked.
     *
     * @see During
     */
    val during: During = During()
)

/**
 * This annotation is used to specify the minigame-phases that
 * the event handler will be invoked in.
 */
public annotation class During(
    /**
     * These will be the phases of the minigame that
     * this event handler will be invoked in.
     *
     * If this array is empty, the event handler will
     * either use the [after] and [before] to determine
     * the events that it will be invoked in, or it will
     * be invoked in all phases.
     */
    val phases: Array<String> = [],

    /**
     * This is the id of the start phase that this
     * handler should be invoked in, this is inclusive.
     *
     * By default, if not specified will default to
     * [Phase.none].
     */
    val after: String = "",

    /**
     * This is the id of the end phase that this
     * handler should be invoked in, this is exclusive.
     *
     * By default, if not specified will default to
     * [Phase.end].
     */
    val before: String = ""
)

public object ListenerFlags {
    public const val NONE: Int = 0
    public const val HAS_PLAYER: Int = 1 shl 1
    public const val IS_PLAYING: Int = 1 shl 2
    public const val IS_SPECTATOR: Int = 1 shl 3
    public const val IS_ADMIN: Int = 1 shl 4
    public const val HAS_LEVEL: Int = 1 shl 5
    public const val IN_LEVEL_BOUNDS: Int = 1 shl 6
    public const val IS_MINIGAME: Int = 1 shl 7
    public const val DEFAULT: Int = HAS_PLAYER or IN_LEVEL_BOUNDS or IS_MINIGAME
}