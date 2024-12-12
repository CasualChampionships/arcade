package net.casual.arcade.events.common.registry

import net.casual.arcade.events.common.MissingExecutorEvent
import net.minecraft.core.WritableRegistry

public data class RegistryLoadedFromResourcesEvent<T>(
    val registry: WritableRegistry<T>
): MissingExecutorEvent