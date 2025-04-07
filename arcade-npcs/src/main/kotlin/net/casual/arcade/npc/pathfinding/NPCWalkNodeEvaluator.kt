/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap
import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.utils.EnumUtils
import net.casual.arcade.utils.isOf
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.tags.FluidTags
import net.minecraft.util.Mth
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.PathNavigationRegion
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.level.pathfinder.*
import net.minecraft.world.level.pathfinder.Target
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.function.LongFunction
import java.util.function.Predicate

public class NPCWalkNodeEvaluator: NPCNodeEvaluator() {
    private val pathTypesByPosCacheByMob = Long2ObjectOpenHashMap<PathType>()
    private val collisionCache = Object2BooleanOpenHashMap<AABB>()
    private val reusableNeighbors = arrayOfNulls<Node>(Direction.Plane.HORIZONTAL.length())

    override fun done() {
        this.pathTypesByPosCacheByMob.clear()
        this.collisionCache.clear()
        super.done()
    }

    override fun getStart(): Node {
        val pos = BlockPos.MutableBlockPos()
        val player = this.player!!
        val context = this.currentContext!!
        var y = player.blockY
        var state = context.getBlockState(pos.set(player.x, y.toDouble(), player.z))
        if (!player.canStandOnFluid(state.fluidState)) {
            if (this.canFloat && player.isInWater) {
                while (true) {
                    if (!state.isOf(Blocks.WATER) && state.fluidState != Fluids.WATER.getSource(false)) {
                        y--
                        break
                    }
                    state = context.getBlockState(pos.set(player.x, (++y).toDouble(), player.z))
                }
            } else if (player.onGround()) {
                y = Mth.floor(player.y + 0.5)
            } else {
                pos.set(player.x, player.y + 1.0, player.z)
                while (pos.y > context.level.minY) {
                    y = pos.y
                    pos.setY(pos.y - 1)
                    val blockState2 = context.getBlockState(pos)
                    if (!blockState2.isAir && !blockState2.isPathfindable(PathComputationType.LAND)) {
                        break
                    }
                }
            }
        } else {
            while (player.canStandOnFluid(state.fluidState)) {
                state = this.currentContext!!.getBlockState(
                    pos.set(player.x, (++y).toDouble(), player.z)
                )
            }
            y--
        }
        val blockPos = player.blockPosition()
        if (!this.canStartAt(pos.set(blockPos.x, y, blockPos.z))) {
            val aabb: AABB = player.boundingBox
            if (this.canStartAt(pos.set(aabb.minX, y.toDouble(), aabb.minZ)) ||
                this.canStartAt(pos.set(aabb.minX, y.toDouble(), aabb.maxZ)) ||
                this.canStartAt(pos.set(aabb.maxX, y.toDouble(), aabb.minZ)) ||
                this.canStartAt(pos.set(aabb.maxX, y.toDouble(), aabb.maxZ))
            ) {
                return this.getStartNode(pos)
            }
        }
        return this.getStartNode(BlockPos(blockPos.x, y, blockPos.z))
    }

    protected fun getStartNode(pos: BlockPos): Node {
        val node = this.getNode(pos)
        node.type = this.getCachedPathType(node.x, node.y, node.z)
        node.costMalus = this.player!!.getPathfindingMalus(node.type)
        return node
    }

    protected fun canStartAt(pos: BlockPos): Boolean {
        val pathType = this.getCachedPathType(pos.x, pos.y, pos.z)
        return pathType != PathType.OPEN && this.player!!.getPathfindingMalus(pathType) >= 0.0f
    }

    override fun getTarget(x: Double, y: Double, z: Double): Target {
        return this.getTargetNodeAt(x, y, z)
    }

    override fun getNeighbors(outputArray: Array<Node?>, node: Node): Int {
        var i = 0
        var j = 0
        val above = this.getCachedPathType(node.x, node.y + 1, node.z)
        val current = this.getCachedPathType(node.x, node.y, node.z)
        val player = this.player!!
        if (player.getPathfindingMalus(above) >= 0.0f && current != PathType.STICKY_HONEY) {
            j = Mth.floor(maxOf(1.0f, player.maxUpStep()))
        }
        val d = this.getFloorLevel(BlockPos(node.x, node.y, node.z))
        for (direction in Direction.Plane.HORIZONTAL) {
            val accepted = this.findAcceptedNode(
                node.x + direction.stepX, node.y, node.z + direction.stepZ, j, d, direction, current
            )
            this.reusableNeighbors[direction.get2DDataValue()] = accepted
            if (this.isNeighborValid(accepted, node)) {
                outputArray[i] = accepted
                i++
            }
        }
        for (direction in Direction.Plane.HORIZONTAL) {
            val direction2 = direction.clockWise
            val x = this.reusableNeighbors[direction.get2DDataValue()]
            val z = this.reusableNeighbors[direction2.get2DDataValue()]
            if (this.isDiagonalValid(node, x, z)) {
                val accepted = this.findAcceptedNode(
                    node.x + direction.stepX + direction2.stepX, node.y, node.z + direction.stepZ + direction2.stepZ, j, d, direction, current
                )
                if (this.isDiagonalValid(accepted)) {
                    outputArray[i] = accepted
                    i++
                }
            }
        }
        return i
    }

    protected fun isNeighborValid(neighbor: Node?, node: Node): Boolean {
        return neighbor != null && !neighbor.closed && (neighbor.costMalus >= 0.0f || node.costMalus < 0.0f)
    }

    protected fun isDiagonalValid(root: Node, xNode: Node?, zNode: Node?): Boolean {
        if (zNode == null || xNode == null || zNode.y > root.y || xNode.y > root.y) {
            return false
        } else if (xNode.type != PathType.WALKABLE_DOOR && zNode.type != PathType.WALKABLE_DOOR) {
            val bl = (zNode.type == PathType.FENCE && xNode.type == PathType.FENCE && this.player!!.bbWidth < 0.5f)
            return (zNode.y < root.y || zNode.costMalus >= 0.0f || bl) &&
                    (xNode.y < root.y || xNode.costMalus >= 0.0f || bl)
        } else {
            return false
        }
    }

    protected fun isDiagonalValid(node: Node?): Boolean {
        if (node == null || node.closed) {
            return false
        }
        return if (node.type == PathType.WALKABLE_DOOR) false else node.costMalus >= 0.0f
    }

    private fun doesBlockHavePartialCollision(pathType: PathType): Boolean {
        return pathType == PathType.FENCE ||
                pathType == PathType.DOOR_WOOD_CLOSED ||
                pathType == PathType.DOOR_IRON_CLOSED
    }

    private fun canReachWithoutCollision(node: Node): Boolean {
        val player = this.player!!
        val aabb: AABB = player.boundingBox
        val vec3 = Vec3(
            node.x - player.x + aabb.xsize / 2.0,
            node.y - player.y + aabb.ysize / 2.0,
            node.z - player.z + aabb.zsize / 2.0
        )
        val i = Mth.ceil(vec3.length() / aabb.size)
        val step = vec3.scale(1.0 / i)
        var currentAABB = aabb
        for (j in 1..i) {
            currentAABB = currentAABB.move(step)
            if (this.hasCollisions(currentAABB)) {
                return false
            }
        }
        return true
    }

    protected fun getFloorLevel(pos: BlockPos): Double {
        val blockGetter: BlockGetter = this.currentContext!!.level
        if ((this.canFloat || this.isAmphibious()) && blockGetter.getFluidState(pos).`is`(FluidTags.WATER)) {
            return pos.y + 0.5
        }
        return WalkNodeEvaluator.getFloorLevel(blockGetter, pos)
    }

    protected fun isAmphibious(): Boolean = false

    protected fun findAcceptedNode(
        x: Int,
        y: Int,
        z: Int,
        verticalDeltaLimit: Int,
        nodeFloorLevel: Double,
        direction: Direction,
        pathType: PathType
    ): Node? {
        var node: Node? = null
        val mutablePos = BlockPos.MutableBlockPos()
        val d = this.getFloorLevel(mutablePos.set(x, y, z))
        if (d - nodeFloorLevel > this.getMobJumpHeight()) {
            return null
        } else {
            val pathType2 = this.getCachedPathType(x, y, z)
            val f: Float = this.player!!.getPathfindingMalus(pathType2)
            if (f >= 0.0f) {
                node = this.getNodeAndUpdateCostToMax(x, y, z, pathType2, f)
            }
            if (doesBlockHavePartialCollision(pathType) && node != null && node.costMalus >= 0.0f && !this.canReachWithoutCollision(node)) {
                node = null
            }
            if (pathType2 != PathType.WALKABLE && (!this.isAmphibious() || pathType2 != PathType.WATER)) {
                if ((node == null || node.costMalus < 0.0f) &&
                    verticalDeltaLimit > 0 &&
                    (pathType2 != PathType.FENCE || this.canWalkOverFences) &&
                    pathType2 != PathType.UNPASSABLE_RAIL &&
                    pathType2 != PathType.TRAPDOOR &&
                    pathType2 != PathType.POWDER_SNOW
                ) {
                    node = this.tryJumpOn(x, y, z, verticalDeltaLimit, nodeFloorLevel, direction, pathType, mutablePos)
                } else if (!this.isAmphibious() && pathType2 == PathType.WATER && !this.canFloat) {
                    node = this.tryFindFirstNonWaterBelow(x, y, z, node)
                } else if (pathType2 == PathType.OPEN) {
                    node = this.tryFindFirstGroundNodeBelow(x, y, z)
                } else if (doesBlockHavePartialCollision(pathType2) && node == null) {
                    node = this.getClosedNode(x, y, z, pathType2)
                }
                return node
            } else {
                return node
            }
        }
    }

    private fun getMobJumpHeight(): Double {
        return maxOf(DEFAULT_MOB_JUMP_HEIGHT, this.player!!.maxUpStep().toDouble())
    }

    private fun getNodeAndUpdateCostToMax(x: Int, y: Int, z: Int, pathType: PathType, malus: Float): Node {
        val node = this.getNode(x, y, z)
        node.type = pathType
        node.costMalus = maxOf(node.costMalus, malus)
        return node
    }

    private fun getBlockedNode(x: Int, y: Int, z: Int): Node {
        val node = this.getNode(x, y, z)
        node.type = PathType.BLOCKED
        node.costMalus = -1.0f
        return node
    }

    private fun getClosedNode(x: Int, y: Int, z: Int, pathType: PathType): Node {
        val node = this.getNode(x, y, z)
        node.closed = true
        node.type = pathType
        node.costMalus = pathType.malus
        return node
    }

    private fun tryJumpOn(
        x: Int,
        y: Int,
        z: Int,
        verticalDeltaLimit: Int,
        nodeFloorLevel: Double,
        direction: Direction,
        pathType: PathType,
        pos: BlockPos.MutableBlockPos
    ): Node? {
        val player = this.player!!
        val node = this.findAcceptedNode(x, y + 1, z, verticalDeltaLimit - 1, nodeFloorLevel, direction, pathType) ?: return null
        if (player.bbWidth >= 1.0f) {
            return node
        } else if (node.type != PathType.OPEN && node.type != PathType.WALKABLE) {
            return node
        } else {
            val d = (x - direction.stepX) + 0.5
            val e = (z - direction.stepZ) + 0.5
            val f = player.bbWidth / 2.0
            pos.set(d, (y + 1).toDouble(), e)
            val floorLevel = this.getFloorLevel(pos)
            val nodeFloor = this.getFloorLevel(pos.set(node.x.toDouble(), node.y.toDouble(), node.z.toDouble()))
            val aabb = AABB(d - f, floorLevel + 0.001, e - f, d + f, player.bbHeight + nodeFloor - 0.002, e + f)
            return if (this.hasCollisions(aabb)) null else node
        }
    }

    private fun tryFindFirstNonWaterBelow(x: Int, y: Int, z: Int, node: Node?): Node? {
        val player = this.player!!
        var newNode = node
        var newY = y - 1
        while (newY > player.level().minY) {
            val pathType = this.getCachedPathType(x, newY, z)
            if (pathType != PathType.WATER) {
                return newNode
            }
            newNode = this.getNodeAndUpdateCostToMax(x, newY, z, pathType, player.getPathfindingMalus(pathType))
            newY--
        }
        return newNode
    }

    private fun tryFindFirstGroundNodeBelow(x: Int, y: Int, z: Int): Node {
        val player = this.player!!
        for (i in y - 1 downTo player.level().minY) {
            if (y - i > player.maxFallDistance) {
                return this.getBlockedNode(x, i, z)
            }
            val pathType = this.getCachedPathType(x, i, z)
            val f = player.getPathfindingMalus(pathType)
            if (pathType != PathType.OPEN) {
                if (f >= 0.0f) {
                    return this.getNodeAndUpdateCostToMax(x, i, z, pathType, f)
                }
                return this.getBlockedNode(x, i, z)
            }
        }
        return this.getBlockedNode(x, y, z)
    }

    private fun hasCollisions(boundingBox: AABB): Boolean {
        return this.collisionCache.computeIfAbsent(boundingBox, Predicate {
            val context = this.currentContext!!
            !context.level.noCollision(context.player, boundingBox)
        })
    }

    protected fun getCachedPathType(x: Int, y: Int, z: Int): PathType {
        return this.pathTypesByPosCacheByMob.computeIfAbsent(BlockPos.asLong(x, y, z), LongFunction {
            val context = this.currentContext!!
            this.getPathTypeOfMob(context, x, y, z, context.player)
        })
    }

    override fun getPathTypeOfMob(context: NPCPathfindingContext, x: Int, y: Int, z: Int, player: FakePlayer): PathType {
        val set: Set<PathType> = this.getPathTypeWithinMobBB(context, x, y, z)
        if (set.contains(PathType.FENCE)) {
            return PathType.FENCE
        } else if (set.contains(PathType.UNPASSABLE_RAIL)) {
            return PathType.UNPASSABLE_RAIL
        } else {
            var pathType = PathType.BLOCKED
            for (pathType2 in set) {
                if (player.getPathfindingMalus(pathType2) < 0.0f) {
                    return pathType2
                }
                if (player.getPathfindingMalus(pathType2) >= player.getPathfindingMalus(pathType)) {
                    pathType = pathType2
                }
            }
            return if (this.entityWidth <= 1 &&
                pathType != PathType.OPEN &&
                player.getPathfindingMalus(pathType) == 0.0f &&
                this.getPathType(context, x, y, z) == PathType.OPEN
            ) {
                PathType.OPEN
            } else {
                pathType
            }
        }
    }

    public fun getPathTypeWithinMobBB(context: NPCPathfindingContext, x: Int, y: Int, z: Int): Set<PathType> {
        val enumSet = EnumUtils.emptySet<PathType>()
        for (i in 0 until this.entityWidth) {
            for (j in 0 until this.entityHeight) {
                for (k in 0 until this.entityDepth) {
                    val l = i + x
                    val m = j + y
                    val n = k + z
                    var pathType = this.getPathType(context, l, m, n)
                    val blockPos = context.player.blockPosition()
                    val bl = this.canPassDoors
                    if (pathType == PathType.DOOR_WOOD_CLOSED && this.canOpenDoors && bl) {
                        pathType = PathType.WALKABLE_DOOR
                    }
                    if (pathType == PathType.DOOR_OPEN && !bl) {
                        pathType = PathType.BLOCKED
                    }
                    if (pathType == PathType.RAIL &&
                        this.getPathType(context, blockPos.x, blockPos.y, blockPos.z) != PathType.RAIL &&
                        this.getPathType(context, blockPos.x, blockPos.y - 1, blockPos.z) != PathType.RAIL
                    ) {
                        pathType = PathType.UNPASSABLE_RAIL
                    }
                    enumSet.add(pathType)
                }
            }
        }
        return enumSet
    }

    public override fun getPathType(context: NPCPathfindingContext, x: Int, y: Int, z: Int): PathType {
        return WalkNodeEvaluator.getPathTypeStatic(context.asPathfindingContext(), BlockPos.MutableBlockPos(x, y, z))
    }

    public companion object {
        public const val SPACE_BETWEEN_WALL_POSTS: Double = 0.5
        private const val DEFAULT_MOB_JUMP_HEIGHT: Double = 1.125
    }
}
