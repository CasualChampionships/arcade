/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.level.factory

import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.LevelGenerationOptions
import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.level.LevelProperties
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder

/**
 * This is a functional interface for constructing a
 * [CustomLevelFactory] used in [CustomLevelBuilder.constructor].
 *
 * The purpose of this is to create a factory which can construct
 * instances of [CustomLevel]s.
 *
 * @see CustomLevelFactory
 */
public fun interface CustomLevelFactoryConstructor {
    /**
     * Constructs a [CustomLevelFactory] implementation.
     *
     * @param properties The level properties.
     * @param options The generation options.
     * @param persistence The persistence level.
     * @return The factory implementation.
     */
    public fun construct(
        properties: LevelProperties,
        options: LevelGenerationOptions,
        persistence: LevelPersistence
    ): CustomLevelFactory

    public companion object {
        @JvmField
        public val DEFAULT: CustomLevelFactoryConstructor = CustomLevelFactoryConstructor(::SimpleCustomLevelFactory)
    }
}