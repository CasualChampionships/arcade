package net.casual.arcade.minigame.task.impl

import com.google.gson.JsonObject
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.task.MinigameTaskCreationContext
import net.casual.arcade.minigame.task.MinigameTaskFactory
import net.casual.arcade.scheduler.task.SavableTask
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.serialization.TaskSerializationContext
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.ResourceUtils
import net.minecraft.resources.ResourceLocation

public class PhaseChangeTask(
    private val minigame: Minigame,
    private val phase: Phase<out Minigame>
): SavableTask {
    override val id: ResourceLocation = Companion.id

    override fun run() {
        this.minigame.setPhase(this.phase)
    }

    override fun serialize(context: TaskSerializationContext): JsonObject {
        val data = super.serialize(context)
        data.addProperty("phase", this.phase.id)
        return data
    }

    public companion object: MinigameTaskFactory<Minigame> {
        override val id: ResourceLocation = ResourceUtils.arcade("phase_change")

        override fun create(context: MinigameTaskCreationContext<Minigame>): Task {
            val phaseId = context.data.string("phase")
            val minigame = context.minigame
            val phase = minigame.getPhase(phaseId)
            requireNotNull(phase) {
                "Failed to create PhaseChangeTask, no such phase $phaseId for minigame ${minigame.id}"
            }
            return PhaseChangeTask(minigame, phase)
        }
    }
}