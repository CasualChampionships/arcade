package net.casual.arcade.minigame.template.minigame

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.minigame.lobby.LobbyMinigame
import net.casual.arcade.minigame.template.lobby.LobbyTemplate
import net.casual.arcade.resources.pack.PackInfo
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.utils.encodedOptionalFieldOf
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import xyz.nucleoid.fantasy.Fantasy
import xyz.nucleoid.fantasy.RuntimeWorldConfig
import xyz.nucleoid.fantasy.RuntimeWorldHandle
import xyz.nucleoid.fantasy.util.VoidChunkGenerator
import java.util.*
import kotlin.jvm.optionals.getOrNull

public open class SimpleMinigamesTemplate(
    override val name: String = "default",
    public val lobby: LobbyTemplate = LobbyTemplate.DEFAULT,
    public val dimension: Optional<ResourceKey<Level>> = Optional.empty(),
    public val operators: List<String> = listOf(),
    override val minigames: List<MinigameData> = listOf(),
    override val repeat: Boolean = true,
): MinigamesTemplate {
    override fun createLobby(server: MinecraftServer): LobbyMinigame {
        val dimension = this.dimension.getOrNull()
        val either: Either<RuntimeWorldHandle, ServerLevel> = if (dimension == null) {
            Either.left(createTemporaryLobbyLevel(server))
        } else {
            val level = server.getLevel(dimension)
            if (level == null) Either.left(createTemporaryLobbyLevel(server)) else Either.right(level)
        }
        val level = either.map({ it.asWorld() }, { it })
        val lobby = this.lobby.create(level).createMinigame(server)
        either.ifLeft(lobby.levels::add)
        return lobby
    }

    override fun isAdmin(player: ServerPlayer): Boolean {
        return this.operators.contains(player.scoreboardName)
    }

    override fun getAdditionalPacks(): Iterable<PackInfo> {
        return listOf()
    }

    override fun codec(): MapCodec<out MinigamesTemplate> {
        return CODEC
    }

    protected open fun createTemporaryLobbyLevel(server: MinecraftServer): RuntimeWorldHandle {
        val config = RuntimeWorldConfig()
            .setDimensionType(BuiltinDimensionTypes.OVERWORLD)
            .setGenerator(VoidChunkGenerator(server))
        return Fantasy.get(server).openTemporaryWorld(config)
    }

    public companion object: CodecProvider<SimpleMinigamesTemplate> {
        override val ID: ResourceLocation = ResourceUtils.arcade("simple")

        override val CODEC: MapCodec<out SimpleMinigamesTemplate> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.encodedOptionalFieldOf("name", "default").forGetter(SimpleMinigamesTemplate::name),
                LobbyTemplate.CODEC.encodedOptionalFieldOf("lobby", LobbyTemplate.DEFAULT).forGetter(
                    SimpleMinigamesTemplate::lobby),
                Level.RESOURCE_KEY_CODEC.encodedOptionalFieldOf("lobby_dimension").forGetter(SimpleMinigamesTemplate::dimension),
                Codec.STRING.listOf().encodedOptionalFieldOf("operators", listOf()).forGetter(SimpleMinigamesTemplate::operators),
                MinigameData.CODEC.listOf().encodedOptionalFieldOf("minigames", listOf()).forGetter(
                    SimpleMinigamesTemplate::minigames),
                Codec.BOOL.encodedOptionalFieldOf("repeat", true).forGetter(SimpleMinigamesTemplate::repeat)
            ).apply(instance, ::SimpleMinigamesTemplate)
        }
    }
}