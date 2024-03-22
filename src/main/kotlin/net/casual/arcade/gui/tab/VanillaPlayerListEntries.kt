package net.casual.arcade.gui.tab

import net.minecraft.server.MinecraftServer

public class VanillaPlayerListEntries(
    private val server: MinecraftServer
): PlayerListEntries {
    override val size: Int
        get() = this.server.playerList.playerCount

    override fun getEntryAt(index: Int): PlayerListEntries.Entry {
        val player = this.server.playerList.players[index]
        return PlayerListEntries.Entry.fromPlayer(player)
    }
}