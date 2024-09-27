package net.casual.arcade.scheduler.task.impl

import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.capture.CaptureConsumerTask
import net.casual.arcade.scheduler.task.capture.CaptureSerializer
import net.casual.arcade.scheduler.task.capture.CaptureTask
import net.casual.arcade.utils.ServerUtils
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level

private object LevelSerializer: CaptureSerializer<ResourceKey<Level>, String> {
    override fun serialize(capture: ResourceKey<Level>): String {
        return capture.location().toString()
    }

    override fun deserialize(serialized: String): ResourceKey<Level> {
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(serialized))
    }

    private fun readResolve(): Any {
        return LevelSerializer
    }
}

@Suppress("FunctionName")
public fun LevelTask(level: ServerLevel, task: CaptureConsumerTask<ServerLevel>): Task {
    return CaptureTask(level.dimension(), { ServerUtils.getServerOrNull()?.getLevel(it) }, LevelSerializer, task)
}
