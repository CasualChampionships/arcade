package net.casual.arcade.scheduler.task.utils

import com.mojang.serialization.MapCodec
import net.casual.arcade.scheduler.task.serialization.TaskFactory
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.registries.RegistryKeySupplier
import net.casual.arcade.utils.registries.RegistrySupplier
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

public object TaskRegistryKeys: RegistryKeySupplier(ArcadeUtils.MOD_ID) {
    public val TASK_FACTORY: ResourceKey<Registry<TaskFactory>> = create("task_factory")
}

public object TaskRegisties: RegistrySupplier() {
    public val TASK_FACTORY: Registry<TaskFactory> = create(TaskRegistryKeys.TASK_FACTORY, TaskFactory::bootstrap)
}