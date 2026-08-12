/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.casual.arcade.npc.pathfinding.movement.*
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.util.profiling.Profiler
import kotlin.math.abs

public class Pathfinder(
    public val movements: MovementTypeSet,
    public val modifiers: List<PathCostModifier> = emptyList()
) {
    private val nodes = Long2ObjectOpenHashMap<PathNode>()
    private val open = PathNodeHeap()

    public fun findPath(
        context: PathfindingContext,
        targets: Set<BlockPos>,
        accuracy: Int
    ): Path? {
        if (targets.isEmpty()) {
            return null
        }

        val profiler = Profiler.get()
        profiler.push("player_pathfinding")
        try {
            val start = this.findStart(context) ?: return null
            return Search(this, context, targets.toList(), accuracy, start).run()
        } finally {
            this.nodes.clear()
            this.open.clear()
            profiler.pop()
        }
    }

    private fun findStart(context: PathfindingContext): PathNode? {
        val player = context.player
        val ceiling = player.y + context.stepHeight
        val floor = context.minY.toDouble()

        val x = Mth.floor(player.x)
        val z = Mth.floor(player.z)

        // A swimming NPC is held up by nothing, so looking for a block under it would start the
        // search from the bottom of the water rather than from where the NPC actually is
        if (context.settings.canSwim && player.isInWater && !player.onGround()) {
            return this.getNode(x, z, Mth.floor(player.y).toDouble())
        }

        val support = context.findSupport(x, z, ceiling, floor)
        if (support != PathfindingContext.NO_BLOCK) {
            return this.getNode(context, x, support, z)
        }

        // Standing on the edge of a block, so try the corners the NPC actually overlaps
        val box = player.boundingBox
        for (cornerX in intArrayOf(Mth.floor(box.minX), Mth.floor(box.maxX))) {
            for (cornerZ in intArrayOf(Mth.floor(box.minZ), Mth.floor(box.maxZ))) {
                val corner = context.findSupport(cornerX, cornerZ, ceiling, floor)
                if (corner != PathfindingContext.NO_BLOCK) {
                    return this.getNode(context, cornerX, corner, cornerZ)
                }
            }
        }
        return null
    }

    private fun getNode(context: PathfindingContext, x: Int, y: Int, z: Int): PathNode? {
        val surface = context.getSurface(x, y, z)
        if (surface == PathfindingContext.NO_SUPPORT) {
            return null
        }
        return this.getNode(x, z, surface)
    }

    private fun getNode(x: Int, z: Int, surface: Double): PathNode {
        val key = PathNode.key(x, z, surface)
        val existing = this.nodes.get(key)
        if (existing != null) {
            return existing
        }
        val node = PathNode(x, z, surface)
        node.cost = Double.MAX_VALUE
        this.nodes.put(key, node)
        return node
    }

    private class Search(
        private val pathfinder: Pathfinder,
        private val context: PathfindingContext,
        private val targets: List<BlockPos>,
        private val accuracy: Int,
        private val start: PathNode
    ): MovementCandidates {
        private val maxDistance = this.context.settings.maxPathLength.toDouble()

        private var current = this.start
        private var best = this.start
        private var bestHeuristic = 0.0

        private lateinit var currentType: MovementType

        fun run(): Path {
            val start = this.start
            start.cost = 0.0
            start.heuristic = this.heuristic(start)
            start.total = start.heuristic
            this.bestHeuristic = start.heuristic
            this.pathfinder.open.insert(start)

            var reached: PathNode? = null
            var visited = 0
            val limit = this.context.settings.maxVisitedNodes

            while (!this.pathfinder.open.isEmpty() && visited < limit) {
                visited++
                val current = this.pathfinder.open.pop()
                current.closed = true

                if (this.hasReached(current)) {
                    reached = current
                    break
                }

                this.current = current
                for (type in this.pathfinder.movements) {
                    this.currentType = type
                    type.candidates(this.context, current, this)
                }
            }

            return this.reconstruct(reached ?: this.best, reached != null)
        }

        override fun accept(x: Int, y: Int, z: Int, cost: Double, data: Int) {
            val neighbour = this.pathfinder.getNode(this.context, x, y, z) ?: return
            this.offer(neighbour, cost, data)
        }

        override fun acceptSurface(x: Int, z: Int, surface: Double, cost: Double, data: Int) {
            if (!surface.isNaN() && !surface.isInfinite()) {
                this.offer(this.pathfinder.getNode(x, z, surface), cost, data)
            }
        }

        private fun offer(neighbour: PathNode, cost: Double, data: Int) {
            if (cost.isNaN() || cost.isInfinite() || cost < 0.0) {
                return
            }

            val from = this.current
            if (neighbour.closed || neighbour === from) {
                return
            }

            var total = cost
            for (modifier in this.pathfinder.modifiers) {
                val extra = modifier.cost(this.context, this.currentType, from, neighbour)
                if (extra.isInfinite() || extra.isNaN()) {
                    return
                }
                total += extra
            }

            val next = from.cost + total
            if (next >= neighbour.cost) {
                return
            }

            if (neighbour.distanceTo(this.start) > this.maxDistance) {
                return
            }

            neighbour.previous = from
            neighbour.previousType = this.currentType
            neighbour.previousData = data
            neighbour.previousCost = cost
            neighbour.cost = next
            neighbour.heuristic = this.heuristic(neighbour)

            val estimate = next + neighbour.heuristic
            if (neighbour.isOpen()) {
                this.pathfinder.open.changeCost(neighbour, estimate)
            } else {
                neighbour.total = estimate
                this.pathfinder.open.insert(neighbour)
            }

            if (neighbour.heuristic < this.bestHeuristic) {
                this.bestHeuristic = neighbour.heuristic
                this.best = neighbour
            }
        }

        private fun heuristic(node: PathNode): Double {
            var best = Double.MAX_VALUE
            for (target in this.targets) {
                val distance = node.distanceTo(target.x + 0.5, target.y.toDouble(), target.z + 0.5)
                if (distance < best) {
                    best = distance
                }
            }
            return best * this.pathfinder.movements.minimumCostPerBlock * this.context.settings.heuristicWeight
        }

        private fun hasReached(node: PathNode): Boolean {
            for (target in this.targets) {
                val dx = abs(node.x - target.x)
                val dy = abs(Mth.floor(node.surface) - target.y)
                val dz = abs(node.z - target.z)
                if (dx + dy + dz <= this.accuracy) {
                    return true
                }
            }
            return false
        }

        private fun reconstruct(end: PathNode, reached: Boolean): Path {
            val chain = ArrayList<PathNode>()
            var current: PathNode? = end
            while (current != null) {
                chain.add(current)
                current = current.previous
            }
            chain.reverse()

            val movements = ArrayList<Movement>(chain.size)
            for (i in 1 until chain.size) {
                val to = chain[i]
                val type = to.previousType ?: continue
                movements.add(type.create(this.context, chain[i - 1], to, to.previousData, to.previousCost))
            }

            val target = this.targets.minByOrNull {
                end.distanceTo(it.x + 0.5, it.y.toDouble(), it.z + 0.5)
            }
            return Path(movements, this.start, target ?: end.feet, reached)
        }
    }
}
