/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.phase

import net.casual.arcade.utils.string.PascalCase
import net.casual.arcade.utils.string.ScreamingSnakeCase
import net.casual.arcade.utils.string.SnakeCase
import net.casual.arcade.utils.string.convertCasing
import org.jetbrains.annotations.ApiStatus.NonExtendable

public interface MinigamePhase {
    public val id: String
        get() {
            check(this is Enum<*>) { "Phase ${this.javaClass.name} must be an enum constant" }
            return this.name.toPhaseId()
        }

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
