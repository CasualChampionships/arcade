package net.casual.arcade.npc.pathfinding

import com.google.common.collect.Lists
import net.casual.arcade.npc.FakePlayer
import net.minecraft.core.BlockPos
import net.minecraft.util.profiling.Profiler
import net.minecraft.util.profiling.ProfilerFiller
import net.minecraft.util.profiling.metrics.MetricCategory
import net.minecraft.world.level.PathNavigationRegion
import net.minecraft.world.level.pathfinder.BinaryHeap
import net.minecraft.world.level.pathfinder.Node
import net.minecraft.world.level.pathfinder.Path
import java.util.*
import kotlin.jvm.optionals.getOrNull
import net.minecraft.world.level.pathfinder.Target as TargetNode

public class NPCPathfinder(
    private val nodeEvaluator: NPCNodeEvaluator,
    private var maxVisitedNodes: Int
) {
    private val neighbors: Array<Node?> = arrayOfNulls(32)
    private val openSet: BinaryHeap = BinaryHeap()

    public fun setMaxVisitedNodes(i: Int) {
        this.maxVisitedNodes = i
    }

    public fun findPath(
        region: PathNavigationRegion,
        player: FakePlayer,
        targetPositions: Set<BlockPos>,
        maxRange: Float,
        accuracy: Int,
        searchDepthMultiplier: Float
    ): Path? {
        this.openSet.clear()
        this.nodeEvaluator.prepare(region, player)
        val start = this.nodeEvaluator.getStart()

        val map = targetPositions.associateBy { pos ->
            this.nodeEvaluator.getTarget(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
        }
        val path = this.findPath(start, map, maxRange, accuracy, searchDepthMultiplier)
        this.nodeEvaluator.done()
        return path
    }

    private fun distance(first: Node, second: Node): Float {
        return first.distanceTo(second)
    }

    private fun findPath(
        startNode: Node,
        targetMap: Map<TargetNode, BlockPos>,
        maxRange: Float,
        accuracy: Int,
        searchDepthMultiplier: Float
    ): Path? {
        val profiler: ProfilerFiller = Profiler.get()
        profiler.push("find_path")
        profiler.markForCharting(MetricCategory.PATH_FINDING)
        val targets = targetMap.keys
        startNode.g = 0.0f
        startNode.h = this.getBestH(startNode, targets)
        startNode.f = startNode.h
        this.openSet.clear()
        this.openSet.insert(startNode)
        val reachedTargets = HashSet<TargetNode>()
        var iterations = 0
        val maxIterations: Int = (this.maxVisitedNodes * searchDepthMultiplier).toInt()

        while (!this.openSet.isEmpty) {
            iterations++
            if (iterations >= maxIterations) {
                break
            }

            val current = this.openSet.pop()
            current.closed = true

            for (target in targets) {
                if (current.distanceManhattan(target) <= accuracy.toFloat()) {
                    target.setReached()
                    reachedTargets.add(target)
                }
            }

            if (reachedTargets.isNotEmpty()) {
                break
            }

            if (current.distanceTo(startNode) < maxRange) {
                val numNeighbors = this.nodeEvaluator.getNeighbors(this.neighbors, current)
                for (m in 0 until numNeighbors) {
                    val neighbor = this.neighbors[m]!!
                    val dist = this.distance(current, neighbor)
                    neighbor.walkedDistance = current.walkedDistance + dist
                    val newG = current.g + dist + neighbor.costMalus
                    if (neighbor.walkedDistance < maxRange && (!neighbor.inOpenSet() || newG < neighbor.g)) {
                        neighbor.cameFrom = current
                        neighbor.g = newG
                        neighbor.h = this.getBestH(neighbor, targets) * FUDGING
                        if (neighbor.inOpenSet()) {
                            this.openSet.changeCost(neighbor, neighbor.g + neighbor.h)
                        } else {
                            neighbor.f = neighbor.g + neighbor.h
                            this.openSet.insert(neighbor)
                        }
                    }
                }
            }
        }

        val optional: Optional<Path> = if (reachedTargets.isNotEmpty()) {
            reachedTargets.stream()
                .map { target -> this.reconstructPath(target.bestNode, targetMap[target]!!, true) }
                .min(Comparator.comparingInt(Path::getNodeCount))
        } else {
            targets.stream()
                .map { target -> this.reconstructPath(target.bestNode, targetMap[target]!!, false) }
                .min(Comparator.comparingDouble { path: Path ->
                    path.distToTarget.toDouble()
                }.thenComparingInt(Path::getNodeCount))
        }
        profiler.pop()
        return optional.getOrNull()
    }

    private fun getBestH(node: Node, targets: Set<TargetNode>): Float {
        var best = Float.MAX_VALUE
        for (target in targets) {
            val h: Float = node.distanceTo(target)
            target.updateBest(h, node)
            best = if (h < best) h else best
        }
        return best
    }

    /**
     * Converts a recursive path point structure into a path.
     */
    private fun reconstructPath(point: Node, targetPos: BlockPos, reachesTarget: Boolean): Path {
        val nodes: MutableList<Node> = Lists.newArrayList()
        var current: Node? = point
        while (current != null) {
            nodes.add(0, current)
            current = current.cameFrom
        }
        return Path(nodes, targetPos, reachesTarget)
    }

    public companion object {
        private const val FUDGING: Float = 1.5f
        private const val DEBUG: Boolean = false
    }
}
