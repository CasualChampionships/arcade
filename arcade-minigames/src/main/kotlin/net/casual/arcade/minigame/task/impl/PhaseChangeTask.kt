/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.task.impl

import com.mojang.serialization.DataResult
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.task.MinigameTaskCreationContext
import net.casual.arcade.minigame.task.MinigameTaskFactory
import net.casual.arcade.scheduler.task.SavableTask
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.serialization.TaskSerializationContext
import net.casual.arcade.utils.IdentifierUtils
import net.casual.arcade.utils.error.RichResult
import net.minecraft.resources.Identifier
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import kotlin.jvm.optionals.getOrNull

public class PhaseChangeTask(
    private val minigame: Minigame,
    private val phase: Phase<out Minigame>
): SavableTask {
    override val id: Identifier = Companion.id

    override fun run() {
        this.minigame.setPhase(this.phase)
    }

    override fun serialize(output: ValueOutput, context: TaskSerializationContext) {
        super.serialize(output, context)
        output.putString("phase", this.phase.id)
    }

    public companion object: MinigameTaskFactory<Minigame> {
        override val id: Identifier = IdentifierUtils.arcade("phase_change")

        override fun create(input: ValueInput, context: MinigameTaskCreationContext<Minigame>): RichResult<Task> {
            val phaseId = input.getString("phase").getOrNull() ?: return RichResult.failure("No input phase")
            val minigame = context.minigame
            val phase = minigame.getPhase(phaseId)
                ?: return RichResult.failure("No such phase $phaseId for minigame ${minigame.id}")
            return RichResult.success(PhaseChangeTask(minigame, phase))
        }
    }
}