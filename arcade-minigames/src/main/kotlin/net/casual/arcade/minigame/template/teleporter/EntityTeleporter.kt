/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.template.teleporter

import com.google.common.collect.Multimap
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.utils.TeamUtils
import net.casual.arcade.utils.codec.CodecProvider.Companion.register
import net.casual.arcade.utils.math.location.Location
import net.casual.arcade.utils.math.location.providers.ExactLocationProvider
import net.minecraft.core.Registry
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.PlayerTeam
import java.util.function.Function

public interface EntityTeleporter {
    public fun teleportEntities(level: ServerLevel, entities: List<Entity>)

    public fun teleportTeams(level: ServerLevel, teams: Multimap<PlayerTeam, Entity>)

    public fun codec(): MapCodec<out EntityTeleporter>

    public companion object {
        public val DEFAULT: LocationTeleporter = LocationTeleporter(ExactLocationProvider(Location(Vec3(0.0, 5.0, 0.0), Vec2.ZERO)))

        public val CODEC: Codec<EntityTeleporter> = Codec.lazyInitialized {
            MinigameRegistries.ENTITY_TELEPORTER.byNameCodec()
                .dispatch(EntityTeleporter::codec, Function.identity())
        }

        internal fun bootstrap(registry: Registry<MapCodec<out EntityTeleporter>>) {
            SpreadTeleporter.register(registry)
            SplitTeleporter.register(registry)
            LocationTeleporter.register(registry)
        }

        @JvmStatic
        public fun EntityTeleporter.teleport(level: ServerLevel, entities: List<Entity>, byTeams: Boolean) {
            if (byTeams) {
                this.teleportTeams(level, TeamUtils.getMappedTeamsFor(entities))
                return
            }
            this.teleportEntities(level, entities)
        }
    }
}