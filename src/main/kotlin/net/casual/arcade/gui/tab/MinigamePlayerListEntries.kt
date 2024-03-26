package net.casual.arcade.gui.tab

import net.casual.arcade.minigame.Minigame
import net.minecraft.server.level.ServerPlayer

public class MinigamePlayerListEntries(
    public val minigame: Minigame<*>,
    private val order: Comparator<ServerPlayer> = VanillaPlayerListEntries.DEFAULT_ORDER
): PlayerListEntries {
    private var entries: List<ServerPlayer> = listOf()

    override val size: Int
        get() = this.entries.size

    override fun getEntryAt(index: Int): PlayerListEntries.Entry {
        val player = this.entries[index]
        return PlayerListEntries.Entry.fromPlayer(player)
    }

    override fun tick() {
        this.entries = this.minigame.getAllPlayers().sortedWith(this.order)
    }
}