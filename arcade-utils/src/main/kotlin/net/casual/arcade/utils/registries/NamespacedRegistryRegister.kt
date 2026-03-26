package net.casual.arcade.utils.registries

import net.minecraft.core.Registry
import net.minecraft.resources.Identifier

public class NamespacedRegistryRegister<T: Any>(
    private val registry: Registry<T>,
    private val namespace: (String) -> Identifier
) {
    public fun register(path: String, element: T) {
        Registry.register(this.registry, this.namespace.invoke(path), element)
    }

    public operator fun invoke(path: String, element: T) {
        this.register(path, element)
    }
}