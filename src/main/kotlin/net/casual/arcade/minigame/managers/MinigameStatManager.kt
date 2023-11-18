package net.casual.arcade.minigame.managers

import net.casual.arcade.stats.Stat
import net.casual.arcade.stats.StatTracker
import net.casual.arcade.stats.StatType
import net.minecraft.server.level.ServerPlayer
import java.util.*

public class MinigameStatManager {
    private val stats = HashMap<UUID, StatTracker>()

    public fun <T> getOrCreateStat(player: ServerPlayer, type: StatType<T>): Stat<T> {
        return this.stats.getOrPut(player.uuid) { StatTracker() }.getOrCreateStat(type)
    }
}