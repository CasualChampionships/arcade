/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.level.vanilla

import com.mojang.serialization.Codec
import net.casual.arcade.dimensions.level.spawner.*
import net.minecraft.resources.ResourceKey
import net.minecraft.util.StringRepresentable
import net.minecraft.world.level.Level
import net.minecraft.world.level.dimension.LevelStem

/**
 * Enum representing the default dimensions in vanilla Minecraft.
 */
public enum class VanillaDimension: StringRepresentable {
    /**
     * The `minecraft:overworld` dimension.
     */
    Overworld,

    /**
     * The `minecraft:the_nether` dimension.
     */
    Nether,

    /**
     * The `minecraft:the_end` dimension.
     */
    End;

    /**
     * Gets the dimension key for this dimension.
     *
     * @return The dimension key.
     */
    public fun getDimensionKey(): ResourceKey<Level> {
        return when(this) {
            Overworld -> Level.OVERWORLD
            Nether -> Level.NETHER
            End -> Level.END
        }
    }

    /**
     * Gets the stem key for this dimension.
     *
     * @return The stem key.
     */
    public fun getStemKey(): ResourceKey<LevelStem> {
        return when(this) {
            Overworld -> LevelStem.OVERWORLD
            Nether -> LevelStem.NETHER
            End -> LevelStem.END
        }
    }

    /**
     * Gets the custom spawners for this dimension.
     *
     * @return The custom spawners.
     */
    public fun getCustomSpawners(): List<CustomSpawnerFactory> {
        if (this != Overworld) {
            return listOf()
        }
        return listOf(
            PhantomSpawnerFactory,
            PatrolSpawnerFactory,
            CatSpawnerFactory,
            VillageSiegeFactory,
            WanderingTraderSpawnerFactory
        )
    }

    /**
     * Whether time ticks in this dimension.
     *
     * @return Whether time ticks.
     */
    public fun doesTimeTick(): Boolean {
        return this == Overworld
    }

    override fun getSerializedName(): String {
        return this.getDimensionKey().location().toString()
    }

    public companion object {
        @JvmField
        public val CODEC: Codec<VanillaDimension> = StringRepresentable.fromEnum(VanillaDimension::values)

        @JvmStatic
        public fun fromDimensionKey(key: ResourceKey<Level>?): VanillaDimension? {
            return when (key) {
                Level.OVERWORLD -> Overworld
                Level.NETHER -> Nether
                Level.END -> End
                else -> null
            }
        }
    }
}