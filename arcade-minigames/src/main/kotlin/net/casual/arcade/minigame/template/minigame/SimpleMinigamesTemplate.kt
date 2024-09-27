package net.casual.arcade.minigame.template.minigame

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.dimensions.utils.addCustomLevel
import net.casual.arcade.dimensions.utils.impl.VoidChunkGenerator
import net.casual.arcade.minigame.lobby.LobbyMinigame
import net.casual.arcade.minigame.template.lobby.LobbyTemplate
import net.casual.arcade.resources.pack.PackInfo
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.utils.encodedOptionalFieldOf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.dimension.BuiltinDimensionTypes

public open class SimpleMinigamesTemplate(
    override val name: String = "default",
    public val lobby: LobbyTemplate = LobbyTemplate.DEFAULT,
    public val operators: List<String> = listOf(),
    override val minigames: List<MinigameData> = listOf(),
    override val repeat: Boolean = true,
): MinigamesTemplate {
    override fun createLobby(server: MinecraftServer): LobbyMinigame {
        val level = this.getLobbyLevel(server)
        return this.lobby.create(level).createMinigame(server)
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

    protected open fun getLobbyLevel(server: MinecraftServer): ServerLevel {
        return server.addCustomLevel {
            randomDimensionKey()
            dimensionType(BuiltinDimensionTypes.OVERWORLD)
            chunkGenerator(VoidChunkGenerator(server))
            defaultLevelProperties()
        }
    }

    public companion object: CodecProvider<SimpleMinigamesTemplate> {
        override val ID: ResourceLocation = ResourceUtils.arcade("simple")

        override val CODEC: MapCodec<out SimpleMinigamesTemplate> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.encodedOptionalFieldOf("name", "default").forGetter(SimpleMinigamesTemplate::name),
                LobbyTemplate.CODEC.encodedOptionalFieldOf("lobby", LobbyTemplate.DEFAULT).forGetter(SimpleMinigamesTemplate::lobby),
                Codec.STRING.listOf().encodedOptionalFieldOf("operators", listOf()).forGetter(SimpleMinigamesTemplate::operators),
                MinigameData.CODEC.listOf().encodedOptionalFieldOf("minigames", listOf()).forGetter(SimpleMinigamesTemplate::minigames),
                Codec.BOOL.encodedOptionalFieldOf("repeat", true).forGetter(SimpleMinigamesTemplate::repeat)
            ).apply(instance, ::SimpleMinigamesTemplate)
        }
    }
}