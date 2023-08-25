package net.casualuhc.arcade.minigame

import java.util.*

object Minigames {
    private val ALL = LinkedHashMap<UUID, Minigame>()

    fun all(): Collection<Minigame> {
        return ALL.values
    }

    internal fun get(uuid: UUID): Minigame? {
        return this.ALL[uuid]
    }

    internal fun register(minigame: Minigame) {
        this.ALL[minigame.uuid] = minigame
    }
}