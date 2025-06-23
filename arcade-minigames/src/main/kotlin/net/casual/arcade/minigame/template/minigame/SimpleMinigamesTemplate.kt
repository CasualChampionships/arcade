/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.template.minigame

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.lobby.LobbyMinigame
import net.casual.arcade.minigame.lobby.LobbyMinigameFactory
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.casual.arcade.resources.pack.PackInfo
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.utils.codec.OrderedRecordCodecBuilder
import net.casual.arcade.utils.encodedOptionalFieldOf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

public open class SimpleMinigamesTemplate(
    override val name: String = "default",
    public val lobby: MinigameFactory = LobbyMinigameFactory.DEFAULT,
    public val operators: List<String> = listOf(),
    override val minigames: List<MinigameFactory> = listOf(),
    override val repeat: Boolean = true,
): MinigamesTemplate {
    override fun createLobby(server: MinecraftServer): LobbyMinigame {
        val minigame = this.lobby.create(MinigameCreationContext(server))
        if (minigame !is LobbyMinigame) {
            throw IllegalArgumentException("Minigame factory must provide a lobby minigame")
        }
        return minigame
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

    public companion object: CodecProvider<SimpleMinigamesTemplate> {
        override val ID: ResourceLocation = ResourceUtils.arcade("simple")

        override val CODEC: MapCodec<out SimpleMinigamesTemplate> = OrderedRecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.encodedOptionalFieldOf("name", "default").forGetter(SimpleMinigamesTemplate::name),
                MinigameFactory.CODEC.encodedOptionalFieldOf("lobby", LobbyMinigameFactory.DEFAULT).forGetter(SimpleMinigamesTemplate::lobby),
                Codec.STRING.listOf().encodedOptionalFieldOf("operators", listOf()).forGetter(SimpleMinigamesTemplate::operators),
                MinigameFactory.CODEC.listOf().encodedOptionalFieldOf("minigames", listOf()).forGetter(SimpleMinigamesTemplate::minigames),
                Codec.BOOL.encodedOptionalFieldOf("repeat", true).forGetter(SimpleMinigamesTemplate::repeat)
            ).apply(instance, ::SimpleMinigamesTemplate)
        }
    }
}