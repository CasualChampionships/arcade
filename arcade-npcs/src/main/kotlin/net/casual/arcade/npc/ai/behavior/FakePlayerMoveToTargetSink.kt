/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.ai.behavior

import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.pathfinding.Path
import net.casual.arcade.npc.pathfinding.navigation.PathNavigation
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.behavior.EntityTracker
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.entity.ai.util.RandomPos
import net.minecraft.world.phys.Vec3
import kotlin.jvm.optionals.getOrNull

public class FakePlayerMoveToTargetSink(
    minDuration: Int = 150,
    maxDuration: Int = 250
): Behavior<FakePlayer>(CONDITIONS, minDuration, maxDuration) {
    private var remainingCooldown = 0
    private var speedModifier: Float = 1.0F

    private var path: Path? = null
    private var lastTargetPos: BlockPos? = null

    override fun checkExtraStartConditions(level: ServerLevel, player: FakePlayer): Boolean {
        if (this.remainingCooldown > 0) {
            this.remainingCooldown--
            return false
        }

        val brain = player.brain
        val target = brain.getMemory(MemoryModuleType.WALK_TARGET).get()
        val reached = this.hasReachedTarget(player, target)
        if (!reached && this.tryComputePath(player, target, level.gameTime)) {
            this.lastTargetPos = target.target.currentBlockPosition()
            return true
        }

        brain.eraseMemory(MemoryModuleType.WALK_TARGET)
        if (reached) {
            brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
        }

        return false
    }

    override fun canStillUse(level: ServerLevel, player: FakePlayer, gameTime: Long): Boolean {
        if (this.path == null || this.lastTargetPos == null) {
            return false
        }
        val target = player.brain.getMemory(MemoryModuleType.WALK_TARGET).getOrNull() ?: return false
        if (this.isWalkTargetSpectator(target) || this.hasReachedTarget(player, target)) {
            return false
        }
        return !player.navigation.isDone()
    }

    override fun start(level: ServerLevel, player: FakePlayer, gameTime: Long) {
        val path = this.path ?: return
        // TODO: Should we create a new memory module for our custom path type?
        player.brain.setMemory(MemoryModuleType.PATH, path.asVanillaPath())
        player.navigation.moveTo(path, this.speedModifier.toDouble())
    }

    override fun tick(level: ServerLevel, player: FakePlayer, gameTime: Long) {
        val path = player.navigation.path
        val brain = player.brain
        if (this.path !== path) {
            this.path = path
            brain.setMemory(MemoryModuleType.PATH, path?.asVanillaPath())
        }

        val lastTarget = this.lastTargetPos ?: return
        if (path == null) {
            return
        }

        val walkTarget = brain.getMemory(MemoryModuleType.WALK_TARGET).orElse(null) ?: return
        val moved = walkTarget.target.currentBlockPosition().distSqr(lastTarget)
        if (moved > REPATH_DISTANCE_SQR && this.tryComputePath(player, walkTarget, level.gameTime)) {
            this.lastTargetPos = walkTarget.target.currentBlockPosition()
            this.start(level, player, gameTime)
        }
    }

    override fun stop(level: ServerLevel, player: FakePlayer, gameTime: Long) {
        val brain = player.brain
        if (brain.hasMemoryValue(MemoryModuleType.WALK_TARGET) && player.navigation.isStuck) {
            if (!this.hasReachedTarget(player, brain.getMemory(MemoryModuleType.WALK_TARGET).get())) {
                this.remainingCooldown = level.random.nextInt(40)
            }
        }

        player.navigation.stop()
        brain.eraseMemory(MemoryModuleType.WALK_TARGET)
        brain.eraseMemory(MemoryModuleType.PATH)
        this.path = null
    }

    private fun tryComputePath(player: FakePlayer, target: WalkTarget, time: Long): Boolean {
        val blockPos = target.target.currentBlockPosition()
        this.path = player.navigation.createPath(blockPos, 0)
        this.speedModifier = target.speedModifier

        val brain = player.brain
        if (this.hasReachedTarget(player, target)) {
            brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
            return false
        }

        val path = this.path
        if (path != null && path.reachesTarget) {
            brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
        } else if (!brain.hasMemoryValue(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) {
            brain.setMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, time)
        }

        if (path != null) {
            return true
        }

        val towards = this.getPosTowards(player, 10.0, 7, Vec3.atBottomCenterOf(blockPos), Math.PI / 2)
            ?: return false
        this.path = player.navigation.createPath(towards.x, towards.y, towards.z, 0)
        return this.path != null
    }

    @Suppress("SameParameterValue")
    private fun getPosTowards(
        player: FakePlayer,
        radius: Double,
        yRange: Int,
        position: Vec3,
        amplifier: Double
    ): Vec3? {
        val delta = position.subtract(player.x, player.y, player.z)
        return RandomPos.generateRandomPos({
            val pos = RandomPos.generateRandomDirectionWithinRadians(
                player.random, radius, radius, yRange, 0, delta.x, delta.z, amplifier
            )
            if (pos == null) null else BlockPos.containing(player.x + pos.x, player.y + pos.y, player.z + pos.z)
        }, { _ -> 0.0 })
    }

    private fun hasReachedTarget(player: FakePlayer, target: WalkTarget): Boolean {
        return target.target.currentBlockPosition().distManhattan(player.blockPosition()) <= target.closeEnoughDist
    }

    private fun isWalkTargetSpectator(walkTarget: WalkTarget): Boolean {
        val target = walkTarget.target
        return target is EntityTracker && target.entity.isSpectator
    }

    public companion object {
        private const val REPATH_DISTANCE_SQR = 4.0

        private val CONDITIONS = mapOf(
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE to MemoryStatus.REGISTERED,
            MemoryModuleType.PATH to MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.WALK_TARGET to MemoryStatus.VALUE_PRESENT
        )
    }
}
