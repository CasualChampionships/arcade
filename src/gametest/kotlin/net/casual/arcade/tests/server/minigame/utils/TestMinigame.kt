/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.minigame.utils

import com.mojang.serialization.MapCodec
import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.dimensions.level.vanilla.VanillaDimension
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.managers.MinigameLevelManager.LevelOwnership
import net.casual.arcade.minigame.phase.MinigamePhase
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.casual.arcade.minigame.serialization.SerializableMinigame
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.arcade
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import java.util.*

class TestMinigame(
    server: MinecraftServer,
    uuid: UUID
): Minigame(server, uuid, ID, TestMinigamePhase.entries), SerializableMinigame {
    val recordedStages: MutableList<TestMinigameStage> = ArrayList()
    val playersAtStage: MutableMap<TestMinigameStage, Int> = EnumMap(TestMinigameStage::class.java)

    override val settings: TestMinigameSettings = TestMinigameSettings(this)

    var observedPhase: MinigamePhase? = null
    var score: Int = 0

    init {
        this.phases.routines[TestMinigamePhase.Grace] = TestGraceRoutine(20.Ticks)
        this.phases.routines[TestMinigamePhase.Active] = TestActiveRoutine()
    }

    fun record(stage: TestMinigameStage) {
        this.recordedStages.add(stage)
        this.playersAtStage[stage] = this.players.all.size
    }

    fun addLevel(): CustomLevel {
        val level = CustomLevelBuilder.build(this.server) {
            randomDimensionKey()
            vanillaDefaults(VanillaDimension.Overworld)
            persistence(LevelPersistence.Permanent)
        }
        this.levels.add(LEVEL, level, LevelOwnership.Owned)
        return level
    }

    override fun serialize(output: ValueOutput) {
        output.putInt("score", this.score)
    }

    override fun deserialize(input: ValueInput, version: Int) {
        this.score = input.getIntOr("score", 0)
    }

    override fun factory(): MinigameFactory {
        return TestMinigame
    }

    companion object: MinigameFactory {
        val ID: Identifier = arcade("auto_test_minigame")
        val LEVEL: Identifier = arcade("test_level")

        private val CODEC = MapCodec.unit(this)

        override fun create(context: MinigameCreationContext): Minigame {
            return TestMinigame(context.server, context.uuid)
        }

        override fun codec(): MapCodec<out MinigameFactory> {
            return CODEC
        }
    }
}
