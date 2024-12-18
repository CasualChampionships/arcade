/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.level.vanilla

import net.casual.arcade.dimensions.level.CustomLevel

/**
 * A collection of [CustomLevel]s which are vanilla-like.
 *
 * @see VanillaLikeLevelsBuilder
 */
public class VanillaLikeLevels internal constructor(
    private val map: Map<VanillaDimension, CustomLevel>
) {
    /**
     * Gets the [CustomLevel] for the given [dimension].
     *
     * @param dimension The dimension to get.
     * @return The level, or `null` if it does not exist.
     */
    public fun get(dimension: VanillaDimension): CustomLevel? {
        return this.map[dimension]
    }

    /**
     * Gets the [CustomLevel] for the given [dimension].
     *
     * @param dimension The dimension to get.
     * @return The level.
     * @throws IllegalStateException If the level does not exist.
     */
    public fun getOrThrow(dimension: VanillaDimension): CustomLevel {
        return this.get(dimension)
            ?: throw IllegalStateException("Expected dimension $dimension to exist")
    }

    /**
     * Gets all the [CustomLevel]s.
     *
     * @return The levels.
     */
    public fun all(): Collection<CustomLevel> {
        return this.map.values
    }
}