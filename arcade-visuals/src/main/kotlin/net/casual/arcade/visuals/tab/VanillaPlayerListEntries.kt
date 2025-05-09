/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.tab

import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

public open class VanillaPlayerListEntries(
    private val supplier: (MinecraftServer) -> List<ServerPlayer> = { it.playerList.players },
    private val order: Comparator<ServerPlayer> = DEFAULT_ORDER
): SuppliedPlayerListEntries() {
    override fun getPlayers(server: MinecraftServer): List<ServerPlayer> {
        return this.supplier.invoke(server).sortedWith(this.order)
    }

    public companion object {
        public val DEFAULT_ORDER: Comparator<ServerPlayer> = Comparator.comparing(Companion::getTeamOrEmpty)
            .thenComparing(ServerPlayer::getScoreboardName) { a, b -> a.compareTo(b, true) }

        private fun getTeamOrEmpty(player: ServerPlayer): String {
            return player.team?.name ?: ""
        }
    }
}