package net.casual.arcade.minigame.events

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.Arcade
import net.casual.arcade.minigame.events.lobby.Lobby
import net.casual.arcade.minigame.events.lobby.LobbyMinigame
import net.casual.arcade.minigame.events.lobby.templates.LobbyTemplate
import net.casual.arcade.resources.PackInfo
import net.casual.arcade.utils.CodecUtils.encodedOptionalFieldOf
import net.casual.arcade.utils.serialization.CodecProvider
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

public open class SimpleMinigamesEvent(
    override val name: String = "default",
    public val lobby: LobbyTemplate = LobbyTemplate.DEFAULT,
    public val dimension: Optional<ResourceKey<Level>> = Optional.empty(),
    public val operators: List<String> = listOf(),
    override val minigames: List<ResourceLocation> = listOf(),
    override val repeat: Boolean = true,
): MinigamesEvent {
    override fun createLobby(server: MinecraftServer): LobbyMinigame {
        val dimension = this.dimension.getOrNull()
        val either: Either<RuntimeWorldHandle, ServerLevel> = if (dimension == null) {
            Either.left(createTemporaryLobbyLevel(server))
        } else {
            val level = server.getLevel(dimension)
            if (level == null) Either.left(createTemporaryLobbyLevel(server)) else Either.right(level)
        }
        val level = either.map({ it.asWorld() }, { it })
        val lobby = this.createLobbyMinigame(server, this.lobby.create(level))
        either.ifLeft(lobby.levels::add)
        return lobby
    }

    override fun isAdmin(player: ServerPlayer): Boolean {
        return this.operators.contains(player.scoreboardName)
    }

    override fun getAdditionalPacks(): Iterable<PackInfo> {
        return listOf()
    }

    override fun codec(): MapCodec<out MinigamesEvent> {
        return CODEC
    }

    protected open fun createLobbyMinigame(server: MinecraftServer, lobby: Lobby): LobbyMinigame {
        return LobbyMinigame(server, lobby)
    }

    protected open fun createTemporaryLobbyLevel(server: MinecraftServer): RuntimeWorldHandle {
        val config = RuntimeWorldConfig()
            .setDimensionType(BuiltinDimensionTypes.OVERWORLD)
            .setGenerator(VoidChunkGenerator(server))
        return Fantasy.get(server).openTemporaryWorld(config)
    }

    public companion object: CodecProvider<SimpleMinigamesEvent> {
        override val ID: ResourceLocation = Arcade.id("simple")

        override val CODEC: MapCodec<out SimpleMinigamesEvent> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.encodedOptionalFieldOf("name", "default").forGetter(SimpleMinigamesEvent::name),
                LobbyTemplate.CODEC.encodedOptionalFieldOf("lobby", LobbyTemplate.DEFAULT).forGetter(SimpleMinigamesEvent::lobby),
                Level.RESOURCE_KEY_CODEC.encodedOptionalFieldOf("lobby_dimension").forGetter(SimpleMinigamesEvent::dimension),
                Codec.STRING.listOf().encodedOptionalFieldOf("operators", listOf()).forGetter(SimpleMinigamesEvent::operators),
                ResourceLocation.CODEC.listOf().encodedOptionalFieldOf("minigames", listOf()).forGetter(SimpleMinigamesEvent::minigames),
                Codec.BOOL.encodedOptionalFieldOf("repeat", true).forGetter(SimpleMinigamesEvent::repeat)
            ).apply(instance, ::SimpleMinigamesEvent)
        }
    }
}