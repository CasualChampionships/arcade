package net.casual.arcade.minigame.annotation

import net.casual.arcade.events.core.Event
import net.casual.arcade.minigame.phase.Phase
import kotlin.reflect.KClass

public const val NONE: Int = 0
public const val HAS_PLAYER: Int = 1 shl 1
public const val IS_PLAYING: Int = 1 shl 2
public const val IS_SPECTATOR: Int = 1 shl 3
public const val IS_ADMIN: Int = 1 shl 4
public const val HAS_LEVEL: Int = 1 shl 5
public const val IS_MINIGAME: Int = 1 shl 6
public const val HAS_PLAYER_PLAYING: Int = HAS_PLAYER or IS_PLAYING
public const val HAS_PLAYER_SPECTATING: Int = HAS_PLAYER or IS_SPECTATOR
public const val DEFAULT: Int = HAS_PLAYER or HAS_LEVEL or IS_MINIGAME

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
    val flags: Int = DEFAULT,

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
     * handler should be invoked in, this is inclusive.
     *
     * By default, if not specified will default to
     * [Phase.end].
     */
    val before: String = ""
)
