/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.annotation

import net.casual.arcade.events.phase.BuiltInEventPhases
import net.casual.arcade.events.common.Event
import net.casual.arcade.events.threading.ThreadingTarget
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
    val phase: Int = BuiltInEventPhases.DEFAULT,

    /**
     * The target threading target for the listener to run on.
     */
    val strategy: ThreadingTarget = ThreadingTarget.Default
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
    public const val DEFAULT: Int = HAS_PLAYER or IN_LEVEL_BOUNDS or HAS_LEVEL or IS_MINIGAME
}