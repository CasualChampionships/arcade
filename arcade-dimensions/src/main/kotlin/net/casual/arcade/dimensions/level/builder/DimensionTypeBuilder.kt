/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.level.builder

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.util.valueproviders.IntProvider
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import net.minecraft.world.level.dimension.DimensionType
import net.minecraft.world.level.dimension.DimensionType.MonsterSettings
import java.util.*

/**
 * Builder class for [DimensionType].
 */
public class DimensionTypeBuilder {
    public var fixedTime: OptionalLong = OptionalLong.empty()
    public var hasSkyLight: Boolean = true
    public var hasCeiling: Boolean = false
    public var ultraWarm: Boolean = false
    public var natural: Boolean = true
    public var coordinateScale: Double = 1.0
    public var bedWorks: Boolean = true
    public var respawnAnchorWorks: Boolean = false
    public var minY: Int = -64
    public var height: Int = 384
    public var logicalHeight: Int = 384
    public var infiniburn: TagKey<Block> = BlockTags.INFINIBURN_OVERWORLD
    public var effects: ResourceLocation = BuiltinDimensionTypes.OVERWORLD_EFFECTS
    public var ambientLight: Float = 0.0F
    public var cloudHeight: Int? = null

    public var piglinSafe: Boolean = false
    public var hasRaids: Boolean = true
    public var monsterSpawnLightLevel: IntProvider = UniformInt.of(0, 7)
    public var monsterSpawnBlockLightLimit: Int = 0

    public fun fixedTime(fixedTime: Long): DimensionTypeBuilder {
        this.fixedTime = OptionalLong.of(fixedTime)
        return this
    }

    public fun hasSkyLight(hasSkyLight: Boolean): DimensionTypeBuilder {
        this.hasSkyLight = hasSkyLight
        return this
    }

    public fun hasCeiling(hasCeiling: Boolean): DimensionTypeBuilder {
        this.hasCeiling = hasCeiling
        return this
    }

    public fun ultraWarm(ultraWarm: Boolean): DimensionTypeBuilder {
        this.ultraWarm = ultraWarm
        return this
    }

    public fun natural(natural: Boolean): DimensionTypeBuilder {
        this.natural = natural
        return this
    }

    public fun coordinateScale(coordinateScale: Double): DimensionTypeBuilder {
        this.coordinateScale = coordinateScale
        return this
    }

    public fun bedWorks(bedWorks: Boolean): DimensionTypeBuilder {
        this.bedWorks = bedWorks
        return this
    }

    public fun respawnAnchorWorks(respawnAnchorWorks: Boolean): DimensionTypeBuilder {
        this.respawnAnchorWorks = respawnAnchorWorks
        return this
    }

    public fun minY(minY: Int): DimensionTypeBuilder {
        this.minY = minY
        return this
    }

    public fun height(height: Int): DimensionTypeBuilder {
        this.height = height
        return this
    }

    public fun logicalHeight(logicalHeight: Int): DimensionTypeBuilder {
        this.logicalHeight = logicalHeight
        return this
    }

    public fun infiniburn(infiniburn: TagKey<Block>): DimensionTypeBuilder {
        this.infiniburn = infiniburn
        return this
    }

    public fun effects(effects: ResourceLocation): DimensionTypeBuilder {
        this.effects = effects
        return this
    }

    public fun ambientLight(ambientLight: Float): DimensionTypeBuilder {
        this.ambientLight = ambientLight
        return this
    }

    public fun cloudHeight(height: Int): DimensionTypeBuilder {
        this.cloudHeight = height
        return this
    }

    public fun piglinSafe(piglinSafe: Boolean): DimensionTypeBuilder {
        this.piglinSafe = piglinSafe
        return this
    }

    public fun hasRaids(hasRaids: Boolean): DimensionTypeBuilder {
        this.hasRaids = hasRaids
        return this
    }

    public fun monsterSpawnLightLevel(light: IntProvider): DimensionTypeBuilder {
        this.monsterSpawnLightLevel = light
        return this
    }

    public fun monsterSpawnBlockLightLimit(light: Int): DimensionTypeBuilder {
        this.monsterSpawnBlockLightLimit = light
        return this
    }

    public fun build(): DimensionType {
        return DimensionType(
            this.fixedTime,
            this.hasSkyLight,
            this.hasCeiling,
            this.ultraWarm,
            this.natural,
            this.coordinateScale,
            this.bedWorks,
            this.respawnAnchorWorks,
            this.minY,
            this.height,
            this.logicalHeight,
            this.infiniburn,
            this.effects,
            this.ambientLight,
            Optional.ofNullable(this.cloudHeight),
            MonsterSettings(
                this.piglinSafe,
                this.hasRaids,
                this.monsterSpawnLightLevel,
                this.monsterSpawnBlockLightLimit
            )
        )
    }

    public companion object {
        public fun build(block: DimensionTypeBuilder.() -> Unit): DimensionType {
            val builder = DimensionTypeBuilder()
            builder.block()
            return builder.build()
        }
    }
}