/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.level.spawner

import com.mojang.serialization.MapCodec
import net.casual.arcade.utils.arcade
import net.casual.arcade.utils.serialization.codec.CodecProvider
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.village.VillageSiege
import net.minecraft.world.entity.npc.CatSpawner
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner
import net.minecraft.world.level.CustomSpawner
import net.minecraft.world.level.levelgen.PatrolSpawner
import net.minecraft.world.level.levelgen.PhantomSpawner
import org.jetbrains.annotations.ApiStatus.Internal

// IntelliJ hates the file and has a fit anytime I try to open it

@Internal
public sealed class SingletonSpawnerFactory(name: String): CustomSpawnerFactory,
    CodecProvider<SingletonSpawnerFactory> {
    override val id: Identifier = arcade(name)

    @Suppress("LeakingThis")
    override val codec: MapCodec<SingletonSpawnerFactory> = MapCodec.unit(this)

    override fun codec(): MapCodec<out CustomSpawnerFactory> {
        return codec
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
        return WanderingTraderSpawner(level.dataStorage)
    }
}