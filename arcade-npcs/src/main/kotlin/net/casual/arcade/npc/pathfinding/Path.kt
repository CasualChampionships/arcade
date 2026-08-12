/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding

import net.casual.arcade.npc.pathfinding.movement.Movement
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.level.pathfinder.Node
import net.minecraft.world.level.pathfinder.PathType
import net.minecraft.world.level.pathfinder.Target
import net.minecraft.world.level.pathfinder.Path as VanillaPath

/**
 * A route for a player to follow, as a sequence of [Movement]s.
 *
 * Unlike a vanilla's path, each step stores what type of movement to use.
 *
 * @param movements The movements to perform, in order.
 * @param start Where the path begins.
 * @param target The position the path was asked to reach.
 * @param reachesTarget Whether the path actually gets there.
 */
public class Path(
    public val movements: List<Movement>,
    public val start: PathNode,
    public val target: BlockPos,
    public val reachesTarget: Boolean
) {
    private var vanilla: VanillaPath? = null

    public var index: Int = 0
        set(value) {
            field = value.coerceIn(0, this.movements.size)
        }

    public val size: Int
        get() = this.movements.size

    public val current: Movement?
        get() = this.movements.getOrNull(this.index)

    public val next: Movement?
        get() = this.movements.getOrNull(this.index + 1)

    public val destination: PathNode
        get() = this.movements.lastOrNull()?.to ?: this.start

    public fun isDone(): Boolean {
        return this.index >= this.movements.size
    }

    public fun advance() {
        this.index++
    }

    public fun nodeAt(index: Int): PathNode {
        if (index <= 0) {
            return this.start
        }
        return this.movements[minOf(index, this.movements.size) - 1].to
    }

    public fun asVanillaPath(): VanillaPath {
        var path = this.vanilla
        if (path == null) {
            path = this.createVanillaPath()
            this.vanilla = path
        }
        path.nextNodeIndex = this.index
        return path
    }

    override fun toString(): String {
        return "NPCPath(${this.size} movements, target=${this.target}, reaches=${this.reachesTarget})"
    }

    private fun createVanillaPath(): VanillaPath {
        val nodes = ArrayList<Node>(this.movements.size + 1)
        nodes.add(toVanillaNode(this.start, PathType.WALKABLE))
        for (movement in this.movements) {
            nodes.add(toVanillaNode(movement.to, movement.pathType))
        }
        val path = VanillaPath(nodes, this.target, this.reachesTarget)
//        path.setDebug(NO_NODES, NO_NODES, setOf(Target(this.target.x, this.target.y, this.target.z)))
        return path
    }

    private companion object {
        private val NO_NODES = arrayOf<Node>()

        private fun toVanillaNode(node: PathNode, type: PathType): Node {
            val vanilla = Node(node.x, Mth.floor(node.surface), node.z)
            vanilla.type = type
            return vanilla
        }
    }
}
