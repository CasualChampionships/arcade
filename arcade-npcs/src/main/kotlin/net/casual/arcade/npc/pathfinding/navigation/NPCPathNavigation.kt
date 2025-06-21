/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.navigation

import me.senseiwells.debug.api.server.DebugToolsPackets
import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.pathfinding.NPCPathfinder
import net.casual.arcade.npc.pathfinding.evaluator.NPCNodeEvaluator
import net.casual.arcade.utils.isOf
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.BlockTags
import net.minecraft.util.Mth
import net.minecraft.util.profiling.Profiler
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.PathNavigationRegion
import net.minecraft.world.level.pathfinder.Path
import net.minecraft.world.level.pathfinder.PathType
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.math.abs

public abstract class NPCPathNavigation(public val player: FakePlayer) {
    private val pathFinder: NPCPathfinder = this.createPathfinder(
        Mth.floor(this.getFollowRange() * 16.0)
    )

    private var reachRange: Int = 0
    private var maxVisitedNodesMultiplier: Float = 1.0f
    private var requiredPathLength: Float = 16.0f

    protected var tick: Int = 0
    protected var lastStuckCheck: Int = 0
    protected var lastStuckCheckPos: Vec3 = Vec3.ZERO
    protected var timeoutCachedNode: Vec3i = Vec3i.ZERO
    protected var timeoutTimer: Long = 0L
    protected var lastTimeoutCheck: Long = 0L
    protected var timeoutLimit: Double = 0.0
    protected var hasDelayedRecomputation: Boolean = false
    protected var timeLastRecompute: Long = 0L

    public var speedModifier: Double = 0.0
    public var path: Path? = null
        protected set
    public var maxDistanceToWaypoint: Float = 0.5f
        protected set
    public var targetPos: BlockPos? = null
        protected set
    public var isStuck: Boolean = false
        private set
    public lateinit var nodeEvaluator: NPCNodeEvaluator
        protected set

    private val maxPathLength: Float
        get() = maxOf(this.getFollowRange().toFloat(), this.requiredPathLength)

    public val level: ServerLevel
        get() = this.player.level()

    public fun updatePathfinderMaxVisitedNodes() {
        val maxNodes = Mth.floor(this.maxPathLength * 16.0f)
        this.pathFinder.setMaxVisitedNodes(maxNodes)
    }

    public fun setRequiredPathLength(f: Float) {
        this.requiredPathLength = f
        this.updatePathfinderMaxVisitedNodes()
    }

    public fun resetMaxVisitedNodesMultiplier() {
        this.maxVisitedNodesMultiplier = 1.0f
    }

    public fun setMaxVisitedNodesMultiplier(multiplier: Float) {
        this.maxVisitedNodesMultiplier = multiplier
    }

    public fun recomputePath() {
        if (this.level.gameTime - this.timeLastRecompute > MAX_TIME_RECOMPUTE) {
            val target = this.targetPos
            if (target != null) {
                this.path = null
                this.path = this.createPath(target, this.reachRange)
                this.timeLastRecompute = this.level.gameTime
                this.hasDelayedRecomputation = false
            }
        } else {
            this.hasDelayedRecomputation = true
        }
    }

    public open fun createPath(x: Double, y: Double, z: Double, accuracy: Int): Path? {
        return this.createPath(BlockPos.containing(x, y, z), accuracy)
    }

    public open fun createPath(targets: Stream<BlockPos>, accuracy: Int): Path? {
        val set = targets.collect(Collectors.toSet())
        return this.createPath(set, 8, false, accuracy)
    }

    public open fun createPath(positions: Set<BlockPos>, distance: Int): Path? {
        return this.createPath(positions, 8, false, distance)
    }

    public open fun createPath(pos: BlockPos, accuracy: Int): Path? {
        return this.createPath(setOf(pos), 8, false, accuracy)
    }

    public open fun createPath(pos: BlockPos, regionOffset: Int, accuracy: Int): Path? {
        return this.createPath(setOf(pos), 8, false, regionOffset, accuracy.toFloat())
    }

    public open fun createPath(entity: Entity, accuracy: Int): Path? {
        return this.createPath(setOf(entity.blockPosition()), 16, true, accuracy)
    }

    protected fun createPath(
        targets: Set<BlockPos>,
        regionOffset: Int,
        offsetUpward: Boolean,
        accuracy: Int
    ): Path? {
        return this.createPath(targets, regionOffset, offsetUpward, accuracy, this.maxPathLength)
    }

    protected fun createPath(
        targets: Set<BlockPos>,
        regionOffset: Int,
        offsetUpward: Boolean,
        accuracy: Int,
        followRange: Float
    ): Path? {
        if (targets.isEmpty()) {
            return null
        }
        if (this.player.y < level.minY.toDouble()) {
            return null
        }
        if (!this.canUpdatePath()) {
            return null
        }
        val path = this.path
        if (path != null && !path.isDone && targets.contains(this.targetPos)) {
            return path
        }
        val profiler = Profiler.get()
        profiler.push("pathfind")
        val currentPos = player.blockPosition()
        val blockPos = if (offsetUpward) currentPos.above() else currentPos
        val i = (followRange + regionOffset).toInt()
        val region = PathNavigationRegion(this.level, blockPos.offset(-i, -i, -i), blockPos.offset(i, i, i))
        val foundPath = this.pathFinder.findPath(region, player, targets, followRange, accuracy, maxVisitedNodesMultiplier)
        profiler.pop()
        if (foundPath?.target != null) {
            this.targetPos = foundPath.target
            this.reachRange = accuracy
            this.resetStuckTimeout()
        }
        return foundPath
    }

    public fun moveTo(x: Double, y: Double, z: Double, speed: Double): Boolean {
        return this.moveTo(this.createPath(x, y, z, 1), speed)
    }

    public fun moveTo(x: Double, y: Double, z: Double, accuracy: Int, speed: Double): Boolean {
        return this.moveTo(this.createPath(x, y, z, accuracy), speed)
    }

    public fun moveTo(entity: Entity, speed: Double): Boolean {
        val path = this.createPath(entity, 1)
        return path != null && this.moveTo(path, speed)
    }

    public fun moveTo(path: Path?, speed: Double): Boolean {
        if (path == null) {
            this.path = null
            return false
        } else {
            if (!path.sameAs(this.path)) {
                this.path = path
            }
            if (this.isDone()) {
                return false
            }
            this.trimPath()
            if (this.path!!.nodeCount <= 0) {
                return false
            }
            this.speedModifier = speed
            val tempPos = this.getTempMobPos()
            this.lastStuckCheck = this.tick
            this.lastStuckCheckPos = tempPos
            return true
        }
    }

    public fun tick() {
        this.tick++
        if (this.hasDelayedRecomputation) {
            this.recomputePath()
        }
        if (!this.isDone()) {
            if (this.canUpdatePath()) {
                this.followThePath()
            } else {
                val localPath = this.path
                if (localPath != null && !localPath.isDone) {
                    val tempPos = this.getTempMobPos()
                    val nextEntityPos = localPath.getNextEntityPos(this.player)
                    if (tempPos.y > nextEntityPos.y &&
                        !this.player.onGround() &&
                        Mth.floor(tempPos.x) == Mth.floor(nextEntityPos.x) &&
                        Mth.floor(tempPos.z) == Mth.floor(nextEntityPos.z)
                    ) {
                        localPath.advance()
                    }
                }
            }
            DebugToolsPackets.getInstance().sendPathfindingPacket(
                this.level, this.player, this.path, this.maxDistanceToWaypoint
            )

            if (!this.isDone()) {
                val path = this.path!!
                val next = if (this.player.isSprinting && path.nextNodeIndex + 1 < path.nodeCount) {
                    path.getEntityPosAtNode(this.player, path.nextNodeIndex + 1)
                } else {
                    path.getNextEntityPos(this.player)
                }
                val modified = next.with(Direction.Axis.Y, this.getGroundY(next))
                this.player.moveControl.setTarget(modified, this.speedModifier)
            }
        }
    }

    public fun isDone(): Boolean {
        val localPath = this.path
        return localPath == null || localPath.isDone
    }

    public fun isInProgress(): Boolean {
        return !this.isDone()
    }

    public fun stop() {
        this.path = null
    }

    public fun canCutCorner(pathType: PathType): Boolean {
        return pathType != PathType.DANGER_FIRE &&
            pathType != PathType.DANGER_OTHER &&
            pathType != PathType.WALKABLE_DOOR
    }

    public open fun isStableDestination(pos: BlockPos): Boolean {
        return this.level.getBlockState(pos.below()).isSolidRender
    }

    public open fun setCanFloat(canSwim: Boolean) {
        this.nodeEvaluator.canFloat = canSwim
    }

    public fun canFloat(): Boolean {
        return this.nodeEvaluator.canFloat
    }

    public fun shouldRecomputePath(pos: BlockPos): Boolean {
        if (this.hasDelayedRecomputation) {
            return false
        }
        val localPath = this.path
        if (localPath != null && !localPath.isDone && localPath.nodeCount != 0) {
            val node = localPath.endNode!!
            val midX = (node.x + this.player.x) / 2.0
            val midY = (node.y + this.player.y) / 2.0
            val midZ = (node.z + this.player.z) / 2.0
            val midPoint = Vec3(midX, midY, midZ)
            return pos.closerToCenterThan(midPoint, (localPath.nodeCount - localPath.nextNodeIndex).toDouble())
        }
        return false
    }

    protected abstract fun createPathfinder(maxVisitedNodes: Int): NPCPathfinder

    protected abstract fun getTempMobPos(): Vec3

    protected abstract fun canUpdatePath(): Boolean

    protected fun followThePath() {
        val localPath = this.path ?: return
        val tempPos = this.getTempMobPos()
        if (this.player.bbWidth > 0.75f) {
            this.maxDistanceToWaypoint = this.player.bbWidth / 2.0f
        } else {
            this.maxDistanceToWaypoint = 1.5F - this.player.bbWidth / 2.0f
        }
        val nextNodePos = localPath.nextNodePos
        val d = abs(this.player.x - (nextNodePos.x + 0.5))
        val e = abs(this.player.y - nextNodePos.y.toDouble())
        val f = abs(this.player.z - (nextNodePos.z + 0.5))
        val withinThreshold = d < this.maxDistanceToWaypoint &&
                f < this.maxDistanceToWaypoint &&
                e < 1.0
        if (withinThreshold || (this.canCutCorner(localPath.nextNode.type) && this.shouldTargetNextNodeInDirection(tempPos))) {
            localPath.advance()
        }
        this.doStuckDetection(tempPos)
    }

    protected open fun getGroundY(vec: Vec3): Double {
        val blockPos = BlockPos.containing(vec)
        return if (this.level.getBlockState(blockPos.below()).isAir) {
            vec.y
        } else {
            WalkNodeEvaluator.getFloorLevel(this.level, blockPos)
        }
    }

    protected fun isClearForMovementBetween(player: FakePlayer, start: Vec3, end: Vec3, allowSwimming: Boolean): Boolean {
        val adjustedEnd = Vec3(end.x, end.y + player.bbHeight * 0.5, end.z)
        return player.level().clip(
            ClipContext(
                start,
                adjustedEnd,
                ClipContext.Block.COLLIDER,
                if (allowSwimming) ClipContext.Fluid.ANY else ClipContext.Fluid.NONE,
                player
            )
        ).type == HitResult.Type.MISS
    }

    protected fun doStuckDetection(positionVec3: Vec3) {
        if (this.tick - this.lastStuckCheck > STUCK_CHECK_INTERVAL) {
            val currentSpeed = if (this.player.speed >= 1.0f) this.player.speed else this.player.speed * this.player.speed
            val threshold = currentSpeed * 100.0f * STUCK_THRESHOLD_DISTANCE_FACTOR
            if (positionVec3.distanceToSqr(this.lastStuckCheckPos) < (threshold * threshold).toDouble()) {
                this.isStuck = true
                this.stop()
            } else {
                this.isStuck = false
            }
            this.lastStuckCheck = this.tick
            this.lastStuckCheckPos = positionVec3
        }
        val localPath = this.path
        if (localPath != null && !localPath.isDone) {
            val nextNode = localPath.nextNodePos
            val currentTime = this.level.gameTime
            if (nextNode == this.timeoutCachedNode) {
                this.timeoutTimer += (currentTime - this.lastTimeoutCheck)
            } else {
                this.timeoutCachedNode = nextNode
                val d = positionVec3.distanceTo(Vec3.atBottomCenterOf(this.timeoutCachedNode))
                this.timeoutLimit = if (this.player.speed > 0.0f) d / this.player.speed.toDouble() * 20.0 else 0.0
            }
            if (this.timeoutLimit > 0.0 && this.timeoutTimer > this.timeoutLimit * 3.0) {
                this.timeoutPath()
            }
            this.lastTimeoutCheck = currentTime
        }
    }

    protected open fun trimPath() {
        val localPath = this.path
        if (localPath != null) {
            for (i in 0 until localPath.nodeCount) {
                val node = localPath.getNode(i)
                val nextNode = if (i + 1 < localPath.nodeCount) localPath.getNode(i + 1) else null
                val blockState = this.level.getBlockState(BlockPos(node.x, node.y, node.z))
                if (blockState.isOf(BlockTags.CAULDRONS)) {
                    localPath.replaceNode(i, node.cloneAndMove(node.x, node.y + 1, node.z))
                    if (nextNode != null && node.y >= nextNode.y) {
                        localPath.replaceNode(i + 1, node.cloneAndMove(nextNode.x, node.y + 1, nextNode.z))
                    }
                }
            }
        }
    }

    protected open fun canMoveDirectly(start: Vec3, end: Vec3): Boolean {
        return false
    }

    // Private methods
    private fun shouldTargetNextNodeInDirection(vec: Vec3): Boolean {
        val localPath = this.path ?: return false
        if (localPath.nextNodeIndex + 1 >= localPath.nodeCount) {
            return false
        }
        val nextNodeCenter = Vec3.atBottomCenterOf(localPath.nextNodePos)
        if (!vec.closerThan(nextNodeCenter, 2.0)) {
            return false
        }
        if (this.canMoveDirectly(vec, localPath.getNextEntityPos(this.player))) {
            return true
        }
        val secondNodeCenter = Vec3.atBottomCenterOf(localPath.getNodePos(localPath.nextNodeIndex + 1))
        val diff1 = nextNodeCenter.subtract(vec)
        val diff2 = secondNodeCenter.subtract(vec)
        val d = diff1.lengthSqr()
        val e = diff2.lengthSqr()
        val closer = e < d
        val smallDistance = d < 0.5
        if (!closer && !smallDistance) {
            return false
        }
        val norm1 = diff1.normalize()
        val norm2 = diff2.normalize()
        return norm2.dot(norm1) < 0.0
    }

    private fun timeoutPath() {
        this.resetStuckTimeout()
        this.stop()
    }

    private fun resetStuckTimeout() {
        this.timeoutCachedNode = Vec3i.ZERO
        this.timeoutTimer = 0L
        this.timeoutLimit = 0.0
        this.isStuck = false
    }

    private fun getFollowRange(): Double {
        return 32.0
    }

    public companion object {
        private const val MAX_TIME_RECOMPUTE = 20
        private const val STUCK_CHECK_INTERVAL = 100
        private const val STUCK_THRESHOLD_DISTANCE_FACTOR = 0.25f
    }
}
