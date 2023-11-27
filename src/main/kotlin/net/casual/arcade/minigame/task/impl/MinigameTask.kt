package net.casual.arcade.minigame.task.impl

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.task.Task
import net.casual.arcade.task.capture.CaptureConsumerTask
import net.casual.arcade.task.capture.CaptureMapper
import net.casual.arcade.task.capture.CaptureSerializer
import net.casual.arcade.task.capture.CaptureTask
import java.io.Serializable
import java.util.*

private class MinigameSerializer<M: Minigame<M>>: CaptureSerializer<M, UUID> {
    override fun serialize(capture: M): UUID {
        return capture.uuid
    }

    override fun deserialize(serialized: UUID): M {
        @Suppress("UNCHECKED_CAST")
        return Minigames.get(serialized) as M
    }
}

@Suppress("FunctionName")
public fun <M: Minigame<M>> MinigameTask(minigame: M, task: CaptureConsumerTask<M>): Task {
    return CaptureTask(minigame, { it }, MinigameSerializer(), task)
}
