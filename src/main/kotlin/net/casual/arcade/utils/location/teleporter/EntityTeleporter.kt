package net.casual.arcade.utils.location.teleporter

import com.google.common.collect.Multimap
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.utils.TeamUtils
import net.casual.arcade.utils.location.template.ExactLocationTemplate
import net.casual.arcade.utils.location.template.LocationTemplate
import net.casual.arcade.utils.registries.ArcadeRegistries
import net.casual.arcade.utils.serialization.CodecProvider.Companion.register
import net.minecraft.core.Registry
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.PlayerTeam
import java.util.function.Function

public interface EntityTeleporter {
    public fun teleportEntities(level: ServerLevel, entities: List<Entity>)

    public fun teleportTeams(level: ServerLevel, teams: Multimap<PlayerTeam, Entity>)

    public fun codec(): MapCodec<out EntityTeleporter>

    public companion object {
        public val DEFAULT: LocationTeleporter = LocationTeleporter(ExactLocationTemplate(Vec3(0.0, 5.0, 0.0)))

        public val CODEC: Codec<EntityTeleporter> by lazy {
            ArcadeRegistries.ENTITY_TELEPORTER.byNameCodec()
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