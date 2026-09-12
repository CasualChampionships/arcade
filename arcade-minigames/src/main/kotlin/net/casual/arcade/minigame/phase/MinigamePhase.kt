/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.phase

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.managers.MinigamePhaseManager
import net.casual.arcade.utils.string.PascalCase
import net.casual.arcade.utils.string.ScreamingSnakeCase
import net.casual.arcade.utils.string.SnakeCase
import net.casual.arcade.utils.string.convertCasing
import org.jetbrains.annotations.ApiStatus.NonExtendable
import kotlin.enums.enumEntries

/**
 * This interface represents a phase of a [Minigame].
 *
 * This allows you to concretely define state for
 * your minigame where logic may differ, or may happen
 * on phase boundaries.
 *
 * Phases **must** be implemented as an enum. Subsequently,
 * the implemented enum's [enumEntries] is then passed to
 * the [Minigame] constructor. While phases are minigame
 * agnostic, it's typical for each minigame to have their
 * own set of phases, as the enum names typically differ
 * to more properly describe the lifecycle of a minigame.
 *
 * An example of a possible set of phases:
 * ```
 * enum class MyMinigamePhase: MinigamePhase {
 *     Grace,
 *     Active,
 *     DeathMatch,
 *     GameOver
 * }
 * ```
 *
 * @see MinigamePhaseManager
 */
public interface MinigamePhase {
    /**
     * The identifier for the phase, this must be unique
     * to all the other phases.
     *
     * Generally the id should follow `snake_case`.
     *
     * This is implemented by default by converting the
     * enum's name to a snake case name.
     */
    public val id: String
        get() {
            check(this is Enum<*>) { "Phase ${this.javaClass.name} must be an enum constant" }
            return this.name.toPhaseId()
        }

    /**
     * The ordinal of the phase.
     *
     * This will be used to compare where the phase is in
     * relation to other phases.
     *
     * This is implemented by default by the enum class.
     */
    public val ordinal: Int

    // We don't implement Comparable<Phase>
    // because it causes conflicts when inheriting with Enum.
    @NonExtendable
    public operator fun compareTo(other: MinigamePhase): Int {
        return this.ordinal.compareTo(other.ordinal)
    }

    private fun String.toPhaseId(): String {
        val from = if (this.none { it.isLowerCase() }) ScreamingSnakeCase else PascalCase
        return this.convertCasing(from, SnakeCase)
    }
}