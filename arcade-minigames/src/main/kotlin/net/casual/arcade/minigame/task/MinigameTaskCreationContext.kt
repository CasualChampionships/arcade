package net.casual.arcade.minigame.task

import com.google.gson.JsonObject
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.serialization.TaskCreationContext

public interface MinigameTaskCreationContext<M: Minigame>: TaskCreationContext {
    public val minigame: M

    public override fun createSubContext(data: JsonObject): MinigameTaskCreationContext<M> {
        return Child(this, data)
    }

    private class Child<M: Minigame>(
        private val parent: MinigameTaskCreationContext<M>,
        override val data: JsonObject,
    ): MinigameTaskCreationContext<M> {
        override val minigame: M
            get() = this.parent.minigame

        override fun createTask(uid: Int): Task? {
            return this.parent.createTask(uid)
        }
    }
}