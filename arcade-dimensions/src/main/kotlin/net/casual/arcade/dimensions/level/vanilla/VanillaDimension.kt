package net.casual.arcade.dimensions.level.vanilla

import com.mojang.serialization.Codec
import net.casual.arcade.dimensions.level.spawner.*
import net.minecraft.resources.ResourceKey
import net.minecraft.util.StringRepresentable
import net.minecraft.world.level.Level
import net.minecraft.world.level.dimension.LevelStem

public enum class VanillaDimension: StringRepresentable {
    Overworld,
    Nether,
    End;

    public fun getDimensionKey(): ResourceKey<Level> {
        return when(this) {
            Overworld -> Level.OVERWORLD
            Nether -> Level.NETHER
            End -> Level.END
        }
    }

    public fun getStemKey(): ResourceKey<LevelStem> {
        return when(this) {
            Overworld -> LevelStem.OVERWORLD
            Nether -> LevelStem.NETHER
            End -> LevelStem.END
        }
    }

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