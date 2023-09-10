package net.casual.arcade.minigame

import org.jetbrains.annotations.ApiStatus.OverrideOnly

/**
 * This interface represents a phase of a given [Minigame] of type [M].
 *
 * This allows you to implement different logic for different phases of
 * the minigame.
 * Each phase is notified when it is starting, see [start], when it is
 * being initialized, see [initialise], or when it's ending, see [end].
 *
 * Here's an example of an implementation of some [MinigamePhase]s:
 * ```kotlin
 * enum class MyMinigamePhases(override val id: String): MinigamePhase<MyMinigame> {
 *     Grace("grace") {
 *         override fun initialise(minigame: MyMinigame) {
 *             minigame.server.isPvpAllowed = false
 *         }
 *
 *         override fun end(minigame: MyMinigame) {
 *             minigame.server.isPvpAllowed = true
 *         }
 *     },
 *     Active("active") {
 *         override fun initialise(minigame: MyMinigame) {
 *             minigame.registerPhaseMinigameEvent<PlayerDeathEvent> {
 *                 // ...
 *             }
 *         }
 *     },
 *     DeathMatch("death_match") {
 *         override fun start(minigame: MyMinigame) {
 *             for (player in minigame.getPlayers()) {
 *                 player.teleportTo(/* ... */)
 *             }
 *         }
 *     }
 * }
 *
 * class MyMinigame: Minigame<MyMinigame>(/* ... */) {
 *     override fun getPhases(): Collection<MinigamePhase<MyMinigame>> {
 *         return MyMinigamePhases.values().toList()
 *     }
 *
 *     // ...
 * }
 * ```
 */
interface MinigamePhase<M: Minigame<M>> {
    /**
     * The identifier for the phase, this should be unique
     * to avoid overlapping phase names.
     *
     * Generally the id should follow `snake_case`.
     */
    val id: String

    /**
     * The ordinal of the phase.
     * This will be used to compare where the phase is in
     * relation to other phases.
     *
     * If you use an [Enum] to represent your [MinigamePhase]s
     * then you do not need to override this field.
     */
    val ordinal: Int

    /**
     * This method is called when the [minigame]s
     * phase is set to `this`.
     * Here you may schedule tasks for the [minigame]
     * or run specific code that **only** runs when
     * the phase is **set**.
     *
     * You **MUST NOT** register events in this
     * method.
     * That should instead be done in
     * [initialise], see documentation for more
     * information.
     *
     * @param minigame The minigame which as set its phase to `this`.
     * @see initialise
     */
    @OverrideOnly
    fun start(minigame: M) {

    }

    /**
     * This method is called either when a phase is set
     * **or** when a phase is re-set (for example, when
     * the minigame restarts, see [SavableMinigame]).
     * This method will **always** be invoked after
     * [start] has been invoked.
     *
     * This **SHOULD NOT** be used to run code that
     * is only meant to be run when a phase is set,
     * instead use [start].
     *
     * Instead, this method should be used for setting
     * state in the [minigame], for example registering
     * phase events, or setting the UI:
     * ```kotlin
     * fun initialise(minigame: Minigame) {
     *     minigame.registerPhaseMinigameEvent<PlayerDeathEvent> { (player) ->
     *         val message = Component.literal("You died in the phase ${this.id}!")
     *         player.sendSystemMessage(message)
     *     }
     *
     *     val bossbar = CustomBossBar.of(Component.literal("My BossBar"))
     *     minigame.addBossbar(bossbar)
     * }
     * ```
     *
     * @param minigame The minigame which is initializing its phase.
     * @see start
     */
    @OverrideOnly
    fun initialise(minigame: M) {

    }

    /**
     * This method is called when the [minigame] is changing phase
     * from the current one.
     * This is called after the minigame has already changed phase,
     * so you may do [Minigame.phase] to get the phase that it is
     * being set to, however, the new phase has **NOT** yet started
     * or initialized.
     *
     * This should reset any state that was changed in [start] or
     * [initialise], for example, removing UI that was set by this
     * phase.
     *
     * @param minigame The minigame that is changing phase.
     * @see start
     */
    @OverrideOnly
    fun end(minigame: M) {

    }

    operator fun compareTo(other: MinigamePhase<*>): Int {
        return this.ordinal.compareTo(other.ordinal)
    }

    private class None<M: Minigame<M>>: MinigamePhase<M> {
        override val id: String = "none"
        override val ordinal: Int = Int.MIN_VALUE
    }

    private class End<M: Minigame<M>>: MinigamePhase<M> {
        override val id: String = "end"
        override val ordinal: Int = Int.MAX_VALUE
    }

    companion object {
        private val NONE: MinigamePhase<*> = None()
        private val END: MinigamePhase<*> = End()

        fun <M: Minigame<M>> none(): MinigamePhase<M> {
            @Suppress("UNCHECKED_CAST")
            return NONE as MinigamePhase<M>
        }

        fun <M: Minigame<M>> end(): MinigamePhase<M> {
            @Suppress("UNCHECKED_CAST")
            return END as MinigamePhase<M>
        }
    }
}