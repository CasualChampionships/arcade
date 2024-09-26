package net.casual.arcade.dimensions.level.factory

import net.casual.arcade.dimensions.level.LevelGenerationOptions
import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.level.LevelProperties

public fun interface CustomLevelFactoryConstructor {
    public fun construct(
        properties: LevelProperties,
        options: LevelGenerationOptions,
        persistence: LevelPersistence
    ): CustomLevelFactory

    public companion object {
        public val DEFAULT: CustomLevelFactoryConstructor = CustomLevelFactoryConstructor(::SimpleCustomLevelFactory)
    }
}