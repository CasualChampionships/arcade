package net.casual.arcade.minigame

import org.jetbrains.annotations.ApiStatus.OverrideOnly

interface MinigamePhase {
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
    fun start(minigame: Minigame) {

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
    fun initialise(minigame: Minigame) {

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
    fun end(minigame: Minigame) {

    }

    operator fun compareTo(other: MinigamePhase): Int {
        return this.ordinal.compareTo(other.ordinal)
    }

    private class None: MinigamePhase {
        override val id: String = "none"
        override val ordinal: Int = -1
    }

    companion object {
        val NONE: MinigamePhase = None()
    }
}