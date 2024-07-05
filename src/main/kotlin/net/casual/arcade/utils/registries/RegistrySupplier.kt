package net.casual.arcade.utils.registries

import com.mojang.serialization.Lifecycle
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

public abstract class RegistrySupplier {
    private val loaders = ArrayList<() -> Unit>()

    init {
        for (load in this.loaders) {
            load.invoke()
        }
        this.loaders.clear()
    }

    public fun noop() {

    }

    protected fun <T> create(key: ResourceKey<Registry<T>>, bootstrap: (Registry<T>) -> Unit): Registry<T> {
        val registry = MappedRegistry(key, Lifecycle.stable(), false)
        this.loaders.add { bootstrap.invoke(registry) }
        return registry
    }
}