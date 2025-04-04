package net.casual.arcade.npc.pathfinding

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.casual.arcade.npc.FakePlayer
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.entity.Mob
import net.minecraft.world.level.PathNavigationRegion
import net.minecraft.world.level.pathfinder.Node
import net.minecraft.world.level.pathfinder.PathType
import net.minecraft.world.level.pathfinder.PathfindingContext
import net.minecraft.world.level.pathfinder.Target as TargetNode

public abstract class NPCNodeEvaluator {
    protected var currentContext: NPCPathfindingContext? = null
    protected var player: FakePlayer? = null
    protected val nodes: Int2ObjectMap<Node> = Int2ObjectOpenHashMap()
    protected var entityWidth: Int = 0
    protected var entityHeight: Int = 0
    protected var entityDepth: Int = 0

    public var canPassDoors: Boolean = true
    public var canOpenDoors: Boolean = false
    public var canFloat: Boolean = false
    public var canWalkOverFences: Boolean = false

    public open fun prepare(level: PathNavigationRegion, player: FakePlayer) {
        this.currentContext = NPCPathfindingContext(level, player)
        this.player = player
        this.nodes.clear()
        this.entityWidth = Mth.floor(player.bbWidth + 1.0F)
        this.entityHeight = Mth.floor(player.bbHeight + 1.0F)
        this.entityDepth = Mth.floor(player.bbWidth + 1.0F)
    }

    public open fun done() {
        this.currentContext = null
        this.player = null
    }

    public abstract fun getStart(): Node

    public abstract fun getTarget(x: Double, y: Double, z: Double): TargetNode

    public abstract fun getNeighbors(outputArray: Array<Node?>, node: Node): Int

    public abstract fun getPathTypeOfMob(context: NPCPathfindingContext, x: Int, y: Int, z: Int, player: FakePlayer): PathType

    public abstract fun getPathType(context: NPCPathfindingContext, x: Int, y: Int, z: Int): PathType

    public fun getPathType(player: FakePlayer, pos: BlockPos): PathType {
        return this.getPathType(NPCPathfindingContext(player.level(), player), pos.x, pos.y, pos.z)
    }

    protected fun getNode(pos: BlockPos): Node {
        return this.getNode(pos.x, pos.y, pos.z)
    }

    protected fun getNode(x: Int, y: Int, z: Int): Node {
        return this.nodes.getOrPut(Node.createHash(x, y, z)) { Node(x, y, z) }
    }

    protected fun getTargetNodeAt(x: Double, y: Double, z: Double): TargetNode {
        return TargetNode(this.getNode(Mth.floor(x), Mth.floor(y), Mth.floor(z)))
    }
}
