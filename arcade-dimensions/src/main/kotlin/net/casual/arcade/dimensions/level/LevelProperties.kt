package net.casual.arcade.dimensions.level

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.utils.codec.ArcadeExtraCodecs
import net.minecraft.world.Difficulty
import net.minecraft.world.level.GameRules
import org.apache.commons.lang3.mutable.MutableLong
import java.util.*

/**
 * This class contains all the *mutable* properties of the level.
 *
 * If a property is set to [Optional.empty] then it will inherit
 * the properties from the primary level (usually the overworld).
 */
public class LevelProperties(
    public var dayTime: Optional<MutableLong> = Optional.empty(),
    public var weather: Optional<WeatherProperties> = Optional.empty(),
    public var difficulty: Optional<DifficultyProperties> = Optional.empty(),
    public var gameRules: Optional<GameRules> = Optional.empty()
) {
    public class WeatherProperties(
        public var clearWeatherTime: Int = 0,
        public var raining: Boolean = false,
        public var rainTime: Int = 0,
        public var thundering: Boolean = false,
        public var thunderTime: Int = 0,
    )

    public class DifficultyProperties(
        public var value: Difficulty = Difficulty.NORMAL,
        public var locked: Boolean = false,
    )

    public companion object {
        private val WEATHER_CODEC = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.INT.fieldOf("clear_weather_time").forGetter(WeatherProperties::clearWeatherTime),
                Codec.BOOL.fieldOf("raining").forGetter(WeatherProperties::raining),
                Codec.INT.fieldOf("rain_time").forGetter(WeatherProperties::rainTime),
                Codec.BOOL.fieldOf("thundering").forGetter(WeatherProperties::thundering),
                Codec.INT.fieldOf("thunder_time").forGetter(WeatherProperties::thunderTime),
            ).apply(instance, ::WeatherProperties)
        }

        private val DIFFICULTY_CODEC = RecordCodecBuilder.create { instance ->
            instance.group(
                Difficulty.CODEC.fieldOf("value").forGetter(DifficultyProperties::value),
                Codec.BOOL.fieldOf("locked").forGetter(DifficultyProperties::locked),
            ).apply(instance, ::DifficultyProperties)
        }

        @JvmField
        public val CODEC: Codec<LevelProperties> = RecordCodecBuilder.create { instance ->
            instance.group(
                ArcadeExtraCodecs.MUTABLE_LONG.optionalFieldOf("day_time").forGetter(LevelProperties::dayTime),
                WEATHER_CODEC.optionalFieldOf("weather").forGetter(LevelProperties::weather),
                DIFFICULTY_CODEC.optionalFieldOf("difficulty").forGetter(LevelProperties::difficulty),
                ArcadeExtraCodecs.GAMERULES.optionalFieldOf("game_rules").forGetter(LevelProperties::gameRules)
            ).apply(instance, ::LevelProperties)
        }
    }
}