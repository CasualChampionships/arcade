/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.ai.behavior

import net.casual.arcade.npc.FakePlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.behavior.PositionTracker
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus

public class FakePlayerLookAtTargetSink(
    minDuration: Int,
    maxDuration: Int
): Behavior<FakePlayer>(CONDITIONS, minDuration, maxDuration) {
    override fun canStillUse(level: ServerLevel, player: FakePlayer, time: Long): Boolean {
        return player.brain.getMemory(MemoryModuleType.LOOK_TARGET).filter { tracker ->
            tracker.isVisibleBy(player)
        }.isPresent
    }

    override fun tick(level: ServerLevel, player: FakePlayer, gameTime: Long) {
        player.brain.getMemory(MemoryModuleType.LOOK_TARGET).ifPresent { tracker ->
            player.lookControl.setLookAt(tracker.currentPosition())
        }
    }

    override fun stop(level: ServerLevel, player: FakePlayer, time: Long) {
        player.brain.eraseMemory(MemoryModuleType.LOOK_TARGET)
    }

    public companion object {
        private val CONDITIONS = mapOf<MemoryModuleType<*>, _>(
            MemoryModuleType.LOOK_TARGET to MemoryStatus.VALUE_PRESENT
        )
    }
}