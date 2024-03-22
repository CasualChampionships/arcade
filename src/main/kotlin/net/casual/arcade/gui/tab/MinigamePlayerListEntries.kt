package net.casual.arcade.gui.tab

import net.casual.arcade.minigame.Minigame
import net.minecraft.server.level.ServerPlayer

public class MinigamePlayerListEntries(
    public val minigame: Minigame<*>
): PlayerListEntries {
    private var cached: List<ServerPlayer> = listOf()
    override val size: Int
        get() {
            this.cached = this.minigame.getAllPlayers()
            return this.cached.size
        }

    override fun getEntryAt(index: Int): PlayerListEntries.Entry {
        val player = this.cached[index]
        return PlayerListEntries.Entry.fromPlayer(player)
    }
}