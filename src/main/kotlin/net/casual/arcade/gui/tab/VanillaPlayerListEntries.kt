package net.casual.arcade.gui.tab

import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

public class VanillaPlayerListEntries(
    private val order: Comparator<ServerPlayer> = DEFAULT_ORDER
): PlayerListEntries {
    private var entries: List<ServerPlayer> = listOf()

    override val size: Int
        get() = this.entries.size

    override fun getEntryAt(index: Int): PlayerListEntries.Entry {
        val player = this.entries[index]
        return PlayerListEntries.Entry.fromPlayer(player)
    }

    override fun tick(server: MinecraftServer) {
        this.entries = server.playerList.players.sortedWith(this.order)
    }

    public companion object {
        public val DEFAULT_ORDER: Comparator<ServerPlayer> = Comparator.comparing(::getTeamOrEmpty)
            .thenComparing(ServerPlayer::getScoreboardName) { a, b -> a.compareTo(b, true) }

        private fun getTeamOrEmpty(player: ServerPlayer): String {
            return player.team?.name ?: ""
        }
    }
}