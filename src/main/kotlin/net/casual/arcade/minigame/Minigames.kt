package net.casual.arcade.minigame

import java.util.*

/**
 * This object is used for registering and holding
 * all the current minigames that are running.
 */
public object Minigames {
    private val ALL = LinkedHashMap<UUID, Minigame<*>>()
    private val FACTORIES = LinkedHashMap<String, MinigameFactory>()

    /**
     * This method gets all the current running minigames.
     *
     * @return All the current running minigames.
     */
    public fun all(): Collection<Minigame<*>> {
        return ALL.values
    }

    public fun registerFactory(id: String, factory: MinigameFactory) {
        this.FACTORIES[id] = factory
    }

    public fun getFactory(id: String): MinigameFactory? {
        return this.FACTORIES[id]
    }

    public fun getAllFactoryIds(): MutableSet<String> {
        return this.FACTORIES.keys
    }

    internal fun get(uuid: UUID): Minigame<*>? {
        return this.ALL[uuid]
    }

    internal fun register(minigame: Minigame<*>) {
        this.ALL[minigame.uuid] = minigame
    }

    internal fun unregister(minigame: Minigame<*>) {
        this.ALL.remove(minigame.uuid)
    }
}