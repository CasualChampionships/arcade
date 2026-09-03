/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.annotation

import net.casual.arcade.events.common.Event
import net.casual.arcade.events.phase.BuiltInEventPhases
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
    val filters: Array<ListenerFilter> = [
        ListenerFilter.HasPlayer,
        ListenerFilter.HasLevel,
        ListenerFilter.InLevelBounds,
        ListenerFilter.IsMinigame
    ],

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