package net.casual.arcade.dimensions.level.spawner

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.dimensions.utils.DimensionRegistries
import net.casual.arcade.utils.codec.CodecProvider.Companion.register
import net.minecraft.core.Registry
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.CustomSpawner
import java.util.function.Function

public interface CustomSpawnerFactory {
    public fun create(level: ServerLevel): CustomSpawner

    public fun codec(): MapCodec<out CustomSpawnerFactory>

    public companion object {
        public val CODEC: Codec<CustomSpawnerFactory> by lazy {
            DimensionRegistries.CUSTOM_SPAWNER_FACTORY.byNameCodec()
                .dispatch(CustomSpawnerFactory::codec, Function.identity())
        }

        internal fun bootstrap(registry: Registry<MapCodec<out CustomSpawnerFactory>>) {
            CatSpawnerFactory.register(registry)
            PhantomSpawnerFactory.register(registry)
            PatrolSpawnerFactory.register(registry)
            VillageSiegeFactory.register(registry)
            WanderingTraderSpawnerFactory.register(registry)
        }
    }
}