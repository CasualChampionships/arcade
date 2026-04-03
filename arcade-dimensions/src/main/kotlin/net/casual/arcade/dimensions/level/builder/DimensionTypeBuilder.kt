/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.level.builder

import net.minecraft.core.Holder
import net.minecraft.core.HolderGetter
import net.minecraft.core.HolderSet
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.util.valueproviders.IntProvider
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.attribute.EnvironmentAttributeMap
import net.minecraft.world.clock.WorldClock
import net.minecraft.world.clock.WorldClocks
import net.minecraft.world.level.CardinalLighting
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.dimension.DimensionType
import net.minecraft.world.level.dimension.DimensionType.MonsterSettings
import net.minecraft.world.level.dimension.DimensionType.Skybox
import net.minecraft.world.timeline.Timeline
import java.util.*

/**
 * Builder class for [DimensionType].
 */
public class DimensionTypeBuilder {
    public var hasFixedTime: Boolean = false
    public var hasSkyLight: Boolean = true
    public var hasCeiling: Boolean = false
    public var hasEnderDragonFight: Boolean = false
    public var coordinateScale: Double = 1.0
    public var minY: Int = -64
    public var height: Int = 384
    public var logicalHeight: Int = 384
    public var infiniburn: TagKey<Block> = BlockTags.INFINIBURN_OVERWORLD
    public var ambientLight: Float = 0.0F

    public var monsterSpawnLightLevel: IntProvider = UniformInt.of(0, 7)
    public var monsterSpawnBlockLightLimit: Int = 0

    public var skybox: Skybox = Skybox.OVERWORLD
    public var cardinalLightType: CardinalLighting.Type = CardinalLighting.Type.DEFAULT
    public var attributes: EnvironmentAttributeMap = EnvironmentAttributeMap.EMPTY
    public var timelines: HolderSet<Timeline> = HolderSet.empty()
    public var defaultClock: Holder<WorldClock>? = null

    public fun hasFixedTime(hasFixedTime: Boolean): DimensionTypeBuilder {
        this.hasFixedTime = hasFixedTime
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

    public fun hasEnderDragonFight(hasEnderDragonFight: Boolean): DimensionTypeBuilder {
        this.hasEnderDragonFight = hasEnderDragonFight
        return this
    }

    public fun coordinateScale(coordinateScale: Double): DimensionTypeBuilder {
        this.coordinateScale = coordinateScale
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

    public fun ambientLight(ambientLight: Float): DimensionTypeBuilder {
        this.ambientLight = ambientLight
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

    public fun skybox(skybox: Skybox): DimensionTypeBuilder {
        this.skybox = skybox
        return this
    }

    public fun cardinalLightType(cardinalLightType: CardinalLighting.Type): DimensionTypeBuilder {
        this.cardinalLightType = cardinalLightType
        return this
    }

    public fun attributes(attributes: EnvironmentAttributeMap): DimensionTypeBuilder {
        this.attributes = attributes
        return this
    }

    public fun attributes(block: EnvironmentAttributeMap.Builder.() -> Unit): DimensionTypeBuilder {
        val builder = EnvironmentAttributeMap.builder()
        builder.putAll(this.attributes)
        builder.block()
        return this.attributes(builder.build())
    }

    public fun timelines(timelines: HolderSet<Timeline>): DimensionTypeBuilder {
        this.timelines = timelines
        return this
    }

    public fun defaultClock(clock: Holder<WorldClock>): DimensionTypeBuilder {
        this.defaultClock = clock
        return this
    }

    public fun overworldClock(access: HolderGetter.Provider): DimensionTypeBuilder {
        this.defaultClock = access.getOrThrow(WorldClocks.OVERWORLD)
        return this
    }

    public fun build(): DimensionType {
        return DimensionType(
            this.hasFixedTime,
            this.hasSkyLight,
            this.hasCeiling,
            this.hasEnderDragonFight,
            this.coordinateScale,
            this.minY,
            this.height,
            this.logicalHeight,
            this.infiniburn,
            this.ambientLight,
            MonsterSettings(
                this.monsterSpawnLightLevel,
                this.monsterSpawnBlockLightLimit
            ),
            this.skybox,
            this.cardinalLightType,
            this.attributes,
            this.timelines,
            Optional.ofNullable(this.defaultClock)
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