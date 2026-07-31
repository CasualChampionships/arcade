/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.utils

import com.mojang.serialization.MapCodec
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.task.serialization.TaskFactory
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.registries.RegistryKeySupplier
import net.casual.arcade.utils.registries.RegistrySupplier
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

public object TaskRegistryKeys: RegistryKeySupplier(ArcadeUtils.MOD_ID) {
    public val TASK_FACTORY: ResourceKey<Registry<TaskFactory>> = create("task_factory")
    public val ROUTINE: ResourceKey<Registry<MapCodec<out Routine<*>>>> = create("routine")
}

public object TaskRegistries: RegistrySupplier() {
    public val TASK_FACTORY: Registry<TaskFactory> = create(TaskRegistryKeys.TASK_FACTORY, TaskFactory::bootstrap)
    public val ROUTINE: Registry<MapCodec<out Routine<*>>> = create(TaskRegistryKeys.ROUTINE, Routine.Companion::bootstrap)
}