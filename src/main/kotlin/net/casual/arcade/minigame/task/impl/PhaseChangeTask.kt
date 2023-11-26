package net.casual.arcade.minigame.task.impl

import com.google.gson.JsonObject
import net.casual.arcade.Arcade
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigamePhase
import net.casual.arcade.minigame.task.AnyMinigameTaskFactory
import net.casual.arcade.task.SavableTask
import net.casual.arcade.task.Task
import net.casual.arcade.task.serialization.TaskCreationContext
import net.casual.arcade.task.serialization.TaskWriteContext
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.MinigameUtils.getPhase

public class PhaseChangeTask<M: Minigame<M>>(
    private val minigame: Minigame<M>,
    private val phase: MinigamePhase<M>
): SavableTask {
    override val id: String = "$${Arcade.MOD_ID}_phase_change"

    override fun run() {
        this.minigame.setPhase(this.phase)
    }

    override fun writeCustomData(context: TaskWriteContext): JsonObject {
        val data = super.writeCustomData(context)
        data.addProperty("phase", this.phase.id)
        return data
    }

    public companion object: AnyMinigameTaskFactory {
        override val id: String = "$${Arcade.MOD_ID}_phase_change"

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