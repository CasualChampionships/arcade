package net.casual.arcade.minigame

import java.util.*

/**
 * This object is used for registering and holding
 * all the current minigames that are running.
 */
public object Minigames {
    private val ALL = LinkedHashMap<UUID, Minigame<*>>()

    /**
     * This method gets all the current running minigames.
     *
     * @return All the current running minigames.
     */
    public fun all(): Collection<Minigame<*>> {
        return ALL.values
    }

    internal fun get(uuid: UUID): Minigame<*>? {
        return this.ALL[uuid]
    }

    internal fun register(minigame: Minigame<*>) {
        this.ALL[minigame.uuid] = minigame
    }
}