package net.casual.arcade.minigame.phase

import net.casual.arcade.minigame.Minigame
import org.jetbrains.annotations.ApiStatus.NonExtendable
import org.jetbrains.annotations.ApiStatus.OverrideOnly

/**
 * This interface represents a phase of a given [Minigame] of type [P].
 *
 * This allows you to implement different logic for different phases of
 * the minigame.
 * Each phase is notified when it is starting, see [start], when it is
 * being initialized, see [initialize], or when it's ending, see [end].
 *
 * Here's an example of an implementation of some [Phase]s:
 * ```kotlin
 * enum class MyMinigamePhases(override val id: String): Phase<MyMinigame> {
 *     Grace("grace") {
 *         override fun initialize(minigame: MyMinigame) {
 *             minigame.settings.canPvp.set(false)
 *         }
 *
 *         override fun end(minigame: MyMinigame, next: Phase<MyMinigame>) {
 *             minigame.chat.broadcast(Component.literal("Grace period is over"))
 *         }
 *     },
 *     Active("active") {
 *         override fun initialize(minigame: MyMinigame) {
 *             minigame.settings.canPvp.set(true)
 *         }
 *     },
 *     DeathMatch("death_match") {
 *         override fun start(minigame: MyMinigame, previous: Phase<MyMinigame>) {
 *             for (player in minigame.players.playing) {
 *                 player.teleportTo(/* ... */)
 *             }
 *         }
 *     }
 * }
 *
 * class MyMinigame: Minigame(/* ... */) {
 *     override fun phases(): Collection<Phase<out Minigame>> {
 *         return MyMinigamePhases.entries
 *     }
 *
 *     // ...
 * }
 * ```
 */
public interface Phase<P> {
    /**
     * The identifier for the phase, this should be unique
     * to avoid overlapping phase names.
     *
     * Generally the id should follow `snake_case`.
     */
    public val id: String

    /**
     * The ordinal of the phase.
     * This will be used to compare where the phase is in
     * relation to other phases.
     *
     * If you use an [Enum] to represent your [Phase]s
     * then you do not need to override this field.
     */
    public val ordinal: Int

    /**
     * This method is called when the [minigame]s
     * phase is set to `this`.
     * Here you may schedule tasks for the [minigame]
     * or run specific code that **only** runs when
     * the phase is **set**, this method **WILL NOT**
     * be called when a minigame is reloaded from save.
     *
     * That should instead be done in
     * [initialize], see documentation for more
     * information.
     *
     * @param minigame The minigame which as set its phase to `this`.
     * @param previous The previous phase.
     * @see initialize
     */
    @OverrideOnly
    public fun start(minigame: P, previous: Phase<P>) {

    }

    /**
     * This method is called either when a phase is set
     * **or** when a phase is re-set (for example, when
     * the minigame reloads).
     * This method will **always** be invoked after
     * [start] has been invoked, however it does not necessarily
     * follow [start], it will be called by itself when a minigame
     * is reloaded.
     *
     * This will also be invoked for all phases that preceded the
     * current phase too.
     *
     * This **SHOULD NOT** be used to run code
     * only meant to be run when a phase is set,
     * instead use [start].
     *
     * Instead, this method should be used for setting
     * state in the [minigame], that is **not** serialized
     * and that you need to be set when the minigame reloads.
     *
     * For example, settings UI elements:
     * ```kotlin
     * fun initialize(minigame: Minigame) {
     *     val bossbar = CustomBossBar.of(Component.literal("My BossBar"))
     *     minigame.addBossbar(bossbar)
     * }
     * ```
     *
     * This method allows for more control when reloading
     * savable minigames, however, it is probably safest to
     * use [start] and [end].
     *
     * @param minigame The minigame which is initializing its phase.
     * @see start
     */
    @OverrideOnly
    public fun initialize(minigame: P) {

    }

    /**
     * This method is called when the [minigame] is changing phase
     * from the current one to the [next] phase.
     * This is called before the minigame has changed phase,
     * so [Minigame.phase] will still reference `this`.
     *
     * @param minigame The minigame that is changing phase.
     * @param next The next phase.
     * @see start
     */
    @OverrideOnly
    public fun end(minigame: P, next: Phase<P>) {

    }

    // We don't implement Comparable<Phase<M>>
    // because it causes conflicts when inheriting with Enum.
    @NonExtendable
    public operator fun compareTo(other: Phase<*>): Int {
        return this.ordinal.compareTo(other.ordinal)
    }

    private class None<P>: Phase<P> {
        override val id: String = "core_none"
        override val ordinal: Int = Int.MIN_VALUE

        override fun compareTo(other: Phase<*>): Int {
            return Int.MIN_VALUE
        }
    }

    private class End<P>: Phase<P> {
        override val id: String = "core_end"
        override val ordinal: Int = Int.MAX_VALUE

        override fun compareTo(other: Phase<*>): Int {
            return Int.MAX_VALUE
        }
    }

    public companion object {
        private val NONE: Phase<*> = None<Any>()
        private val END: Phase<*> = End<Any>()

        /**
         * This gets the none minigame phase.
         * This is the default phase before a minigame starts.
         *
         * @return The none minigame phase instance.
         */
        public fun <M> none(): Phase<M> {
            @Suppress("UNCHECKED_CAST")
            return NONE as Phase<M>
        }

        /**
         * This gets the end minigame phase.
         * Any minigame can be set to this phase,
         * usually denoting the minigame is over.
         *
         * @return The end minigame phase instance.
         */
        public fun <M> end(): Phase<M> {
            @Suppress("UNCHECKED_CAST")
            return END as Phase<M>
        }
    }
}