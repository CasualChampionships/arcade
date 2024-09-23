package net.casual.arcade.minigame.task.impl

import com.google.gson.JsonObject
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.task.AnyMinigameTaskFactory
import net.casual.arcade.minigame.utils.MinigameUtils.getPhase
import net.casual.arcade.scheduler.task.SavableTask
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.serialization.TaskCreationContext
import net.casual.arcade.scheduler.task.serialization.TaskWriteContext
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.JsonUtils.string

public class PhaseChangeTask<M: Minigame<M>>(
    private val minigame: Minigame<M>,
    private val phase: Phase<M>
): SavableTask {
    override val id: String = "$${ArcadeUtils.MOD_ID}_phase_change"

    override fun run() {
        this.minigame.setPhase(this.phase)
    }

    override fun writeCustomData(context: TaskWriteContext): JsonObject {
        val data = super.writeCustomData(context)
        data.addProperty("phase", this.phase.id)
        return data
    }

    public companion object: AnyMinigameTaskFactory {
        override val id: String = "$${ArcadeUtils.MOD_ID}_phase_change"

        override fun create(minigame: Minigame<*>, context: TaskCreationContext): Task {
            return this.create(minigame, context.getCustomData().string("phase"))
        }

        private fun <M: Minigame<M>> create(minigame: Minigame<M>, phaseId: String): Task {
            val phase = minigame.getPhase(phaseId)
            if (phase == null) {
                val message = "Failed to create PhaseChangeTask, no such phase $phaseId for minigame ${minigame.id}"
                throw IllegalStateException(message)
            }
            return PhaseChangeTask(minigame, phase)
        }
    }
}