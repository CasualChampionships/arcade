/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.level

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.utils.serialization.codec.ArcadeExtraCodecs
import net.minecraft.world.Difficulty
import net.minecraft.world.level.gamerules.GameRules
import net.minecraft.world.level.saveddata.WeatherData
import net.minecraft.world.level.storage.LevelData
import org.apache.commons.lang3.mutable.MutableLong
import java.util.*

/**
 * This class contains all the *mutable* properties of the level.
 *
 * If a property is set to [Optional.empty] then it will inherit
 * the properties from the primary level (usually the overworld).
 *
 * @param weather The weather properties.
 * @param difficulty The difficulty properties.
 * @param gameRules The game rules.
 * @param viewDistance The view distance in chunks.
 * @param simulationDistance The simulation distance in chunks.
 */
public class LevelProperties(
    public var weather: Optional<WeatherData> = Optional.empty(),
    public var difficulty: Optional<DifficultyProperties> = Optional.empty(),
    public var gameRules: Optional<GameRules> = Optional.empty(),
    public var viewDistance: Optional<Int> = Optional.empty(),
    public var simulationDistance: Optional<Int> = Optional.empty(),
    public var respawnData: Optional<LevelData.RespawnData> = Optional.empty()
) {
    public class DifficultyProperties(
        public var value: Difficulty = Difficulty.NORMAL,
        public var locked: Boolean = false,
    )

    public companion object {
        private val DIFFICULTY_CODEC = RecordCodecBuilder.create { instance ->
            instance.group(
                Difficulty.CODEC.fieldOf("value").forGetter(DifficultyProperties::value),
                Codec.BOOL.fieldOf("locked").forGetter(DifficultyProperties::locked),
            ).apply(instance, ::DifficultyProperties)
        }

        @JvmField
        public val CODEC: Codec<LevelProperties> = RecordCodecBuilder.create { instance ->
            instance.group(
                WeatherData.CODEC.optionalFieldOf("weather").forGetter(LevelProperties::weather),
                DIFFICULTY_CODEC.optionalFieldOf("difficulty").forGetter(LevelProperties::difficulty),
                ArcadeExtraCodecs.GAMERULES.optionalFieldOf("game_rules").forGetter(LevelProperties::gameRules),
                Codec.INT.optionalFieldOf("view_distance").forGetter(LevelProperties::viewDistance),
                Codec.INT.optionalFieldOf("simulation_distance").forGetter(LevelProperties::simulationDistance),
                LevelData.RespawnData.CODEC.optionalFieldOf("respawn_data").forGetter(LevelProperties::respawnData)
            ).apply(instance, ::LevelProperties)
        }
    }
}