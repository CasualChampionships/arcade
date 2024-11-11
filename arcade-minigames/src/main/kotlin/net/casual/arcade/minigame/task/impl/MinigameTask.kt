package net.casual.arcade.minigame.task.impl

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.capture.CaptureConsumerTask
import net.casual.arcade.scheduler.task.capture.CaptureSerializer
import net.casual.arcade.scheduler.task.capture.CaptureTask
import java.util.*

private class MinigameSerializer<M: Minigame>: CaptureSerializer<M, UUID> {
    override fun serialize(capture: M): UUID {
        return capture.uuid
    }

    override fun deserialize(serialized: UUID): M {
        @Suppress("UNCHECKED_CAST")
        return Minigames.get(serialized) as M
    }
}

@Suppress("FunctionName")
public fun <M: Minigame> MinigameTask(minigame: M, task: CaptureConsumerTask<M>): Task {
    return CaptureTask(minigame, { it }, MinigameSerializer(), task)
}
