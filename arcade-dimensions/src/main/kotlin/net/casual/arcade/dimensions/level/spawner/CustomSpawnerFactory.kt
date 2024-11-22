package net.casual.arcade.dimensions.level.spawner

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.dimensions.utils.DimensionRegistries
import net.casual.arcade.utils.codec.CodecProvider.Companion.register
import net.minecraft.core.Registry
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.CustomSpawner
import java.util.function.Function

/**
 * Factory for creating custom spawners.
 *
 * Custom spawners are used to define custom mob spawning
 * behavior in a dimension.
 *
 * All [CustomSpawnerFactory]'s should be registered to
 * [DimensionRegistries.CUSTOM_SPAWNER_FACTORY] in your
 * mod initializer.
 *
 * Here's an example:
 * ```
 * class MyCustomSpawnerFactory: CustomSpawnerFactory {
 *     override fun create(level: ServerLevel): CustomSpawner {
 *         // ...
 *     }
 *
 *     override fun codec(): MapCodec<out MyCustomSpawnerFactory> {
 *         return CODEC
 *     }
 *
 *     companion object: CodecProvider<MyCustomSpawnerFactory> {
 *         override val ID: ResourceLocation = // ...
 *
 *         override val CODEC: MapCodec<out MyCustomSpawnerFactory> = // ...
 *     }
 * }
 *
 * class MyMod: ModInitializer {
 *     override fun onInitialize() {
 *         MyCustomSpawnerFactory.register(DimensionRegistries.CUSTOM_SPAWNER_FACTORY)
 *     }
 * }
 * ```
 *
 * @see CustomLevelBuilder.customSpawners
 */
public interface CustomSpawnerFactory {
    /**
     * Creates a [CustomSpawner] instance.
     */
    public fun create(level: ServerLevel): CustomSpawner

    /**
     * Returns the codec for this factory.
     */
    public fun codec(): MapCodec<out CustomSpawnerFactory>

    public companion object {
        public val CODEC: Codec<CustomSpawnerFactory> = Codec.lazyInitialized {
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