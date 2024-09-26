package net.casual.arcade.dimensions.utils

import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level
import org.jetbrains.annotations.ApiStatus.Internal

@Internal
public object LevelPersistenceTracker {
    private val persistent = LinkedHashSet<ResourceKey<Level>>()

    internal fun mark(key: ResourceKey<Level>) {
        this.persistent.add(key)
    }

    internal fun unmark(key: ResourceKey<Level>) {
        this.persistent.remove(key)
    }

    @JvmStatic
    public fun get(): Collection<ResourceKey<Level>> {
        return this.persistent
    }
}