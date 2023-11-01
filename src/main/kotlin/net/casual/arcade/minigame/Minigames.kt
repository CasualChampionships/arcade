package net.casual.arcade.minigame

import net.minecraft.resources.ResourceLocation
import java.util.*

/**
 * This object is used for registering and holding
 * all the current minigames that are running.
 */
public object Minigames {
    private val ALL = LinkedHashMap<UUID, Minigame<*>>()
    private val BY_ID = LinkedHashMap<ResourceLocation, ArrayList<Minigame<*>>>()
    private val FACTORIES = LinkedHashMap<String, MinigameFactory>()

    /**
     * This method gets all the current running minigames.
     *
     * @return All the current running minigames.
     */
    public fun all(): Collection<Minigame<*>> {
        return Collections.unmodifiableCollection(ALL.values)
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

    public fun get(uuid: UUID): Minigame<*>? {
        return this.ALL[uuid]
    }

    public fun get(id: ResourceLocation): List<Minigame<*>> {
        return Collections.unmodifiableList(this.BY_ID[id] ?: return emptyList())
    }

    internal fun allById(): Map<ResourceLocation, ArrayList<Minigame<*>>> {
        return this.BY_ID
    }

    internal fun register(minigame: Minigame<*>) {
        this.ALL[minigame.uuid] = minigame
        this.BY_ID.getOrPut(minigame.id) { ArrayList() }.add(minigame)
    }

    internal fun unregister(minigame: Minigame<*>) {
        this.ALL.remove(minigame.uuid)
        this.BY_ID[minigame.id]?.remove(minigame)
    }
}