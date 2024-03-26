package net.casual.arcade.events

public object BuiltInEventPhases {
    public const val PRE: String = "pre"
    public const val POST: String = "post"
    public const val DEFAULT: String = "default"

    @JvmField
    public val DEFAULT_PHASES: Set<String> = setOf(DEFAULT)
    @JvmField
    public val PRE_PHASES: Set<String> = setOf(PRE, DEFAULT)
    @JvmField
    public val POST_PHASES: Set<String> = setOf(POST)
    @JvmField
    public val PRE_POST_PHASES: Set<String> = setOf(PRE, DEFAULT, POST)
}