package net.casual.arcade.dimensions.utils.impl

import net.casual.arcade.dimensions.level.LevelProperties
import net.minecraft.world.Difficulty
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.storage.DerivedLevelData
import net.minecraft.world.level.storage.ServerLevelData
import net.minecraft.world.level.storage.WorldData
import org.jetbrains.annotations.ApiStatus.Internal

@Internal
internal class DerivedLevelData(
    val properties: LevelProperties,
    worldData: WorldData,
    levelData: ServerLevelData
): DerivedLevelData(worldData, levelData) {
    override fun getDayTime(): Long {
        return this.properties.dayTime.map { it.value }
            .orElseGet { super.getDayTime() }
    }

    override fun setDayTime(time: Long) {
        this.properties.dayTime.ifPresentOrElse(
            { it.setValue(time) },
            { super.setDayTime(time) }
        )
    }

    override fun isRaining(): Boolean {
        return this.properties.weather.map { it.raining }
            .orElseGet { super.isRaining() }
    }

    override fun setRaining(raining: Boolean) {
        this.properties.weather.ifPresentOrElse(
            { it.raining = raining },
            { super.setRaining(raining) }
        )
    }

    override fun getRainTime(): Int {
        return this.properties.weather.map { it.rainTime }
            .orElseGet { super.getRainTime() }
    }

    override fun setRainTime(time: Int) {
        this.properties.weather.ifPresentOrElse(
            { it.rainTime = time },
            { super.setRainTime(time) }
        )
    }

    override fun isThundering(): Boolean {
        return this.properties.weather.map { it.thundering }
            .orElseGet { super.isThundering() }
    }

    override fun setThundering(thundering: Boolean) {
        this.properties.weather.ifPresentOrElse(
            { it.thundering = thundering },
            { super.setThundering(thundering) }
        )
    }

    override fun getThunderTime(): Int {
        return this.properties.weather.map { it.thunderTime }
            .orElseGet { super.getThunderTime() }
    }

    override fun setThunderTime(time: Int) {
        this.properties.weather.ifPresentOrElse(
            { it.thunderTime = time },
            { super.setThunderTime(time) }
        )
    }

    override fun getClearWeatherTime(): Int {
        return this.properties.weather.map { it.clearWeatherTime }
            .orElseGet { super.getClearWeatherTime() }
    }

    override fun setClearWeatherTime(time: Int) {
        this.properties.weather.ifPresentOrElse(
            { it.clearWeatherTime = time },
            { super.setClearWeatherTime(time) }
        )
    }

    override fun getDifficulty(): Difficulty {
        return this.properties.difficulty.map { it.value }
            .orElseGet { super.getDifficulty() }
    }

    override fun isDifficultyLocked(): Boolean {
        return this.properties.difficulty.map { it.locked }
            .orElseGet { super.isDifficultyLocked() }
    }

    override fun getGameRules(): GameRules {
        return this.properties.gameRules.orElseGet { super.getGameRules() }
    }
}