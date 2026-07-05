/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.phase

/**
 * This object contains the built-in event phases.
 *
 * @see net.casual.arcade.events.ListenerRegistry
 * @see net.casual.arcade.events.EventListener
 */
public object BuiltInEventPhases {
    /**
     * This is the default event phase, used for events that do not
     * have a specific phase.
     */
    public const val DEFAULT: Int = 0

    /**
     * This event phase is used for events that have side effects.
     * For example, a ticking event, [PRE] would be invoked before
     * the tick.
     *
     * This usually is the default phase out of [PRE] and [POST].
     */
    public const val PRE: Int = 1

    /**
     * This event phase is used for events that have side effects.
     * For example, a ticking event, [POST] would be invoked after
     * the tick.
     */
    public const val POST: Int = 2

    /**
     * The set containing the [DEFAULT] phase.
     */
    public val DEFAULT_PHASES: EventPhases = EventPhases.of(DEFAULT)

    /**
     * The set containing the [PRE] and [DEFAULT] phases.
     */
    public val PRE_PHASES: EventPhases = EventPhases.of(PRE, DEFAULT)

    /**
     * The set containing the [POST] phase.
     */
    public val POST_PHASES: EventPhases = EventPhases.of(POST)

    /**
     * The set containing the [PRE] phase.
     */
    public val ALT_PRE_PHASES: EventPhases = EventPhases.of(PRE)

    /**
     * The set containing the [POST] and [DEFAULT] phases.
     */
    public val ALT_POST_PHASES: EventPhases = EventPhases.of(POST, DEFAULT)

    /**
     * The set containing the [PRE], [DEFAULT], and [POST] phases.
     */
    public val PRE_POST_PHASES: EventPhases = EventPhases.of(PRE, DEFAULT, POST)

    @JvmField public val DEFAULT_PHASES_RAW: Long = DEFAULT_PHASES.bits
    @JvmField public val PRE_PHASES_RAW: Long = PRE_PHASES.bits
    @JvmField public val POST_PHASES_RAW: Long = POST_PHASES.bits
    @JvmField public val ALT_PRE_PHASES_RAW: Long = ALT_PRE_PHASES.bits
    @JvmField public val ALT_POST_PHASES_RAW: Long = ALT_POST_PHASES.bits
    @JvmField public val PRE_POST_PHASES_RAW: Long = PRE_POST_PHASES.bits
}