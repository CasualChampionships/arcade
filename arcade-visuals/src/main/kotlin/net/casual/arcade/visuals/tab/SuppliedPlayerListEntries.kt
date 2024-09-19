package net.casual.arcade.visuals.tab

import net.casual.arcade.visuals.tab.PlayerListEntries
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

public open class SuppliedPlayerListEntries: PlayerListEntries {
    private var entries: List<ServerPlayer> = listOf()

    override val size: Int
        get() = this.entries.size

    override fun getEntryAt(index: Int): PlayerListEntries.Entry {
        val player = this.getPlayerAt(index)
        return PlayerListEntries.Entry.fromPlayer(player)
    }

    override fun tick(server: MinecraftServer) {
        this.entries = this.getPlayers(server)
    }

    protected fun getPlayerAt(index: Int): ServerPlayer {
        return this.entries[index]
    }

    protected open fun getPlayers(server: MinecraftServer): List<ServerPlayer> {
        return server.playerList.players
    }
}