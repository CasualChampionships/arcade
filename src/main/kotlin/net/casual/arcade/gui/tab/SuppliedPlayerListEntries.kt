package net.casual.arcade.gui.tab

import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

public open class SuppliedPlayerListEntries: PlayerListEntries {
    private var entries: List<ServerPlayer> = listOf()

    override val size: Int
        get() = this.entries.size

    override fun getEntryAt(index: Int): PlayerListEntries.Entry {
        val player = this.entries[index]
        return PlayerListEntries.Entry.fromPlayer(player)
    }

    override fun tick(server: MinecraftServer) {
        this.entries = this.getPlayers(server)
    }

    protected open fun getPlayers(server: MinecraftServer): List<ServerPlayer> {
        return server.playerList.players
    }
}