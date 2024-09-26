package net.casual.arcade.dimensions.level.spawner

import com.mojang.serialization.MapCodec
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.village.VillageSiege
import net.minecraft.world.entity.npc.CatSpawner
import net.minecraft.world.entity.npc.WanderingTraderSpawner
import net.minecraft.world.level.CustomSpawner
import net.minecraft.world.level.levelgen.PatrolSpawner
import net.minecraft.world.level.levelgen.PhantomSpawner
import net.minecraft.world.level.storage.ServerLevelData

// For some reason, making this class sealed makes intellij have a fit
public abstract class SingletonSpawnerFactory(name: String): CustomSpawnerFactory, CodecProvider<SingletonSpawnerFactory> {
    override val ID: ResourceLocation = ResourceUtils.arcade(name)

    @Suppress("LeakingThis")
    override val CODEC: MapCodec<SingletonSpawnerFactory> = MapCodec.unit(this)

    override fun codec(): MapCodec<out CustomSpawnerFactory> {
        return CODEC
    }
}

public object CatSpawnerFactory: SingletonSpawnerFactory("cat_spawner") {
    override fun create(level: ServerLevel): CustomSpawner {
        return CatSpawner()
    }
}

public object PhantomSpawnerFactory: SingletonSpawnerFactory("phantom_spawner") {
    override fun create(level: ServerLevel): CustomSpawner {
        return PhantomSpawner()
    }
}

public object PatrolSpawnerFactory: SingletonSpawnerFactory("patrol_spawner") {
    override fun create(level: ServerLevel): CustomSpawner {
        return PatrolSpawner()
    }
}

public object VillageSiegeFactory: SingletonSpawnerFactory("village_siege") {
    override fun create(level: ServerLevel): CustomSpawner {
        return VillageSiege()
    }
}

public object WanderingTraderSpawnerFactory: SingletonSpawnerFactory("wandering_trader_spawner") {
    override fun create(level: ServerLevel): CustomSpawner {
        return WanderingTraderSpawner(level.levelData as ServerLevelData)
    }
}