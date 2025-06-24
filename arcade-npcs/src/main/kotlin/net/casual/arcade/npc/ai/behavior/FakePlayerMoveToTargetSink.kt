/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.ai.behavior

import net.casual.arcade.npc.FakePlayer
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.behavior.EntityTracker
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.entity.ai.util.RandomPos
import net.minecraft.world.level.pathfinder.Path
import net.minecraft.world.phys.Vec3

public class FakePlayerMoveToTargetSink(
    minDuration: Int = 150,
    maxDuration: Int = 250,
    private val sprint: Boolean = true,
    private val jump: Boolean = true
): Behavior<FakePlayer>(CONDITIONS, minDuration, maxDuration) {
    private var remainingCooldown = 0
    private var speedModifier: Float = 0.0F

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
        if (this.path != null && this.lastTargetPos != null) {
            val optional = player.brain.getMemory(MemoryModuleType.WALK_TARGET)
            val isTargetSpectator = optional.map(this::isWalkTargetSpectator).orElse(false)
            val navigation = player.navigation
            return !navigation.isDone() && optional.isPresent
                    && !this.hasReachedTarget(player, optional.get()) && !isTargetSpectator
        }
        return false
    }

    override fun start(level: ServerLevel, player: FakePlayer, gameTime: Long) {
        player.brain.setMemory(MemoryModuleType.PATH, this.path)
        player.navigation.moveTo(this.path, speedModifier.toDouble())

        if (this.sprint) {
            player.moveControl.sprinting = true
        }
    }

    override fun tick(level: ServerLevel, player: FakePlayer, gameTime: Long) {
        val path = player.navigation.path
        val brain = player.brain
        if (this.path != path) {
            this.path = path
            brain.setMemory(MemoryModuleType.PATH, path)
        }

        val lastTarget = this.lastTargetPos
        if (path != null && lastTarget != null) {
            val walkTarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get()
            val progress = walkTarget.target.currentBlockPosition().distSqr(lastTarget)
            if (this.jump && progress > 2.0) {
                player.moveControl.jump()
            }
            if (progress > 4.0 && this.tryComputePath(player, walkTarget, level.gameTime)) {
                this.lastTargetPos = walkTarget.target.currentBlockPosition()
                this.start(level, player, gameTime)
            }
        }
    }

    override fun stop(level: ServerLevel, player: FakePlayer, gameTime: Long) {
        if (player.brain.hasMemoryValue(MemoryModuleType.WALK_TARGET) && player.navigation.isStuck) {
            if (!this.hasReachedTarget(player, player.brain.getMemory(MemoryModuleType.WALK_TARGET).get())) {
                this.remainingCooldown = level.getRandom().nextInt(40)
            }
        }

        player.navigation.stop()
        player.brain.eraseMemory(MemoryModuleType.WALK_TARGET)
        player.brain.eraseMemory(MemoryModuleType.PATH)
        this.path = null

        if (this.sprint) {
            player.moveControl.sprinting = false
        }
    }

    private fun tryComputePath(player: FakePlayer, target: WalkTarget, time: Long): Boolean {
        val blockPos = target.target.currentBlockPosition()
        val path = player.navigation.createPath(blockPos, 0)
        this.path = path
        this.speedModifier = target.speedModifier
        val brain = player.brain
        if (this.hasReachedTarget(player, target)) {
            brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
            return false
        }

        val canReach = path != null && path.canReach()
        if (canReach) {
            brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
        } else if (!brain.hasMemoryValue(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) {
            brain.setMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, time)
        }

        if (path != null) {
            return true
        }

        val vec3 = this.getPosTowards(
            player, 10, 7, Vec3.atBottomCenterOf(blockPos), Math.PI / 2
        )
        if (vec3 != null) {
            this.path = player.navigation.createPath(vec3.x, vec3.y, vec3.z, 0)
            return this.path != null
        }

        return false
    }

    @Suppress("SameParameterValue")
    private fun getPosTowards(player: FakePlayer, radius: Int, yRange: Int, position: Vec3, amplifier: Double): Vec3? {
        val delta = position.subtract(player.x, player.y, player.z)
        return RandomPos.generateRandomPos({
            val pos = RandomPos.generateRandomDirectionWithinRadians(
                player.random, radius, yRange, 0, delta.x, delta.z, amplifier
            )
            if (pos == null) null else BlockPos.containing(player.x + pos.x, player.y + pos.y, player.z + pos.z)
        }, { _ -> 0.0 })
    }

    private fun hasReachedTarget(player: FakePlayer, target: WalkTarget): Boolean {
        return target.target.currentBlockPosition().distManhattan(player.blockPosition()) <= target.closeEnoughDist
    }

    private fun isWalkTargetSpectator(walkTarget: WalkTarget): Boolean {
        val target = walkTarget.target
        return if (target is EntityTracker) target.entity.isSpectator else false
    }

    public companion object {
        private val CONDITIONS = mapOf(
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE to MemoryStatus.REGISTERED,
            MemoryModuleType.PATH to MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.WALK_TARGET to MemoryStatus.VALUE_PRESENT
        )
    }
}