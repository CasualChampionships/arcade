package net.casual.arcade.events

/**
 * This object contains the built-in event phases.
 *
 * @see ListenerRegistry
 * @see EventListener
 */
public object BuiltInEventPhases {
    /**
     * This event phase is used for events that have side effects.
     * For example, a ticking event, [PRE] would be invoked before
     * the tick.
     *
     * This usually is the default phase out of [PRE] and [POST].
     */
    public const val PRE: String = "pre"

    /**
     * This event phase is used for events that have side effects.
     * For example, a ticking event, [POST] would be invoked after
     * the tick.
     */
    public const val POST: String = "post"

    /**
     * This is the default event phase, used for events that do not
     * have a specific phase.
     */
    public const val DEFAULT: String = "default"

    /**
     * The set containing the [DEFAULT] phase.
     */
    @JvmField
    public val DEFAULT_PHASES: Set<String> = setOf(DEFAULT)

    /**
     * The set containing the [PRE] and [DEFAULT] phases.
     */
    @JvmField
    public val PRE_PHASES: Set<String> = setOf(PRE, DEFAULT)

    /**
     * The set containing the [POST] phase.
     */
    @JvmField
    public val POST_PHASES: Set<String> = setOf(POST)

    /**
     * The set containing the [PRE], [DEFAULT], and [POST] phases.
     */
    @JvmField
    public val PRE_POST_PHASES: Set<String> = setOf(PRE, DEFAULT, POST)
}