/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins.registry;

import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryLoadTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RegistryLoadTask.class)
public interface RegistryLoadTaskAccessor<T> {
    @Accessor("registryWriteLock")
    Object arcade_accessRegistryWriteLock();

    @Accessor("registry")
    WritableRegistry<T> arcade_accessRegistry();
}
