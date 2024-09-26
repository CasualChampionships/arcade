package net.casual.arcade.events.registry

import net.casual.arcade.events.server.SafeServerlessEvent
import net.minecraft.core.WritableRegistry

public data class RegistryLoadedFromResourcesEvent<T>(
    val registry: WritableRegistry<T>
): SafeServerlessEvent