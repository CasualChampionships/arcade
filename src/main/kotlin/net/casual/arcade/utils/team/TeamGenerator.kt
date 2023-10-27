package net.casual.arcade.utils.team

import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

public class TeamGenerator(
    private val server: MinecraftServer
) {
    private val players = ArrayList<ServerPlayer>()

    public fun addPlayers(players: Collection<ServerPlayer>) {
        this.players.addAll(players)


    }

    public fun generate() {

    }
}