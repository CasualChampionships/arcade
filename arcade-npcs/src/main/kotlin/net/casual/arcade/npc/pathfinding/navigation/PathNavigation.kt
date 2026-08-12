/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.navigation

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.level.LevelBlockChangedEvent
import net.casual.arcade.events.server.player.PlayerDimensionChangeEvent
import net.casual.arcade.events.server.player.PlayerLeaveEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.ai.NPCInput
import net.casual.arcade.npc.extensions.LevelNavigatingPlayersExtension.Companion.navigatingPlayersExtension
import net.casual.arcade.npc.pathfinding.Path
import net.casual.arcade.npc.pathfinding.Pathfinder
import net.casual.arcade.npc.pathfinding.PathfindingContext
import net.casual.arcade.npc.pathfinding.PathfindingRegion
import net.casual.arcade.npc.pathfinding.PathfindingSettings
import net.casual.arcade.npc.pathfinding.execution.PathExecutor
import net.casual.arcade.npc.pathfinding.execution.PathStatus
import net.casual.arcade.npc.pathfinding.movement.PathCostModifier
import net.casual.arcade.npc.pathfinding.movement.MovementTypeSet
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.BooleanOp
import net.minecraft.world.phys.shapes.Shapes

/**
 * Finds and follows paths for an NPC.
 *
 * What an NPC can do while pathfinding is configured rather than subclassed: [settings] gates
 * the built-in movements, and [movements] decides which movements exist at all.
 *
 * ```kotlin
 * player.navigation.settings.canSprint = true
 * player.navigation.settings.maxFallDistance = 5
 * player.navigation.movements = MovementTypeSet.DEFAULT + GrappleMovementType
 * player.navigation.moveTo(target, speed = 1.0)
 * ```
 *
 * @param player The NPC being navigated.
 */
public open class PathNavigation(public val player: FakePlayer) {
    private var pathfinder: Pathfinder? = null
    private var executor: PathExecutor? = null
    private var lastRecompute: Long = 0L
    private var attempts: Int = 0
    private var closest: Double = Double.MAX_VALUE
    private var dirty: Boolean = false
    private var tracked: ServerLevel? = null

    public var settings: PathfindingSettings = PathfindingSettings()

    public var movements: MovementTypeSet = MovementTypeSet.DEFAULT
        set(value) {
            field = value
            this.pathfinder = null
        }

    public val costModifiers: MutableList<PathCostModifier> = ArrayList()

    public var path: Path? = null
        protected set

    public var targetPos: BlockPos? = null
        protected set

    public var speedModifier: Double = 1.0

    public var isStuck: Boolean = false
        protected set

    public val maxDistanceToWaypoint: Double
        get() = maxOf(MIN_WAYPOINT_DISTANCE, this.player.bbWidth / 2.0)

    public val level: ServerLevel
        get() = this.player.level()

    public open fun createPath(pos: BlockPos, accuracy: Int = 1): Path? {
        return this.createPath(setOf(pos), accuracy)
    }

    public open fun createPath(x: Double, y: Double, z: Double, accuracy: Int = 1): Path? {
        return this.createPath(BlockPos.containing(x, y, z), accuracy)
    }

    public open fun createPath(entity: Entity, accuracy: Int = 1): Path? {
        return this.createPath(entity.blockPosition(), accuracy)
    }

    public open fun createPath(targets: Set<BlockPos>, accuracy: Int = 1): Path? {
        if (targets.isEmpty() || this.player.y < this.level.minY) {
            return null
        }

        return this.pathfinder().findPath(this.createContext(), targets, accuracy)
    }

    public fun moveTo(pos: BlockPos, speed: Double = 1.0, accuracy: Int = 1): Boolean {
        return this.moveTo(this.createPath(pos, accuracy), speed)
    }

    public fun moveTo(x: Double, y: Double, z: Double, speed: Double = 1.0, accuracy: Int = 1): Boolean {
        return this.moveTo(this.createPath(x, y, z, accuracy), speed)
    }

    public fun moveTo(entity: Entity, speed: Double = 1.0, accuracy: Int = 1): Boolean {
        return this.moveTo(this.createPath(entity, accuracy), speed)
    }

    public open fun moveTo(path: Path?, speed: Double = 1.0): Boolean {
        if (path == null || path.isDone()) {
            this.stop()
            return false
        }

        this.speedModifier = speed
        this.isStuck = false
        this.attempts = 0
        this.closest = Double.MAX_VALUE
        return this.follow(path)
    }

    public open fun tick(input: NPCInput): Boolean {
        if (this.executor == null) {
            return false
        }
        this.trackProgress()

        if (this.dirty) {
            this.recomputePath()
        }

        val executor = this.executor ?: return false
        executor.speed = this.speedModifier

        return when (executor.tick(input)) {
            PathStatus.Following -> {
                this.extendIfEnding()
                true
            }
            PathStatus.Completed -> {
                val reached = this.path?.reachesTarget ?: true
                if (!reached && this.extend()) {
                    return true
                }
                this.isStuck = !reached
                this.stop()
                true
            }
            PathStatus.Failed -> {
                if (!this.recover()) {
                    this.isStuck = true
                    this.stop()
                }
                true
            }
        }
    }

    public fun isDone(): Boolean {
        val path = this.path
        return path == null || path.isDone()
    }

    public fun isInProgress(): Boolean {
        return !this.isDone()
    }

    public open fun stop() {
        this.executor?.stop()
        this.executor = null
        this.path = null
        this.targetPos = null
        this.untrack()
    }

    public open fun recomputePath(): Boolean {
        if (this.targetPos == null || !this.canRepath()) {
            return false
        }
        val time = this.level.gameTime
        if (time - this.lastRecompute < RECOMPUTE_INTERVAL) {
            return false
        }
        this.lastRecompute = time
        this.dirty = false
        return this.repath()
    }

    private fun repath(): Boolean {
        return this.follow(this.searchToTarget() ?: return false)
    }

    private fun searchToTarget(): Path? {
        val target = this.targetPos ?: return null
        val path = this.createPath(target) ?: return null
        return if (path.isDone()) null else path
    }

    public open fun shouldRecomputePath(pos: BlockPos): Boolean {
        val path = this.path ?: return false
        if (path.isDone()) {
            return false
        }
        val remaining = minOf(path.size - path.index, MAX_RECOMPUTE_RADIUS)
        return pos.closerToCenterThan(this.player.position(), remaining.toDouble())
    }

    protected open fun canRepath(): Boolean {
        return this.player.onGround() || this.player.isInWater || this.player.onClimbable()
    }

    private fun follow(path: Path): Boolean {
        this.path = path
        this.targetPos = path.target
        this.executor = PathExecutor(this.player, path, this.settings)
        this.track()
        return true
    }

    private fun track() {
        val level = this.level
        if (this.tracked != level) {
            this.untrack()
            this.tracked = level
        }
        level.navigatingPlayersExtension.add(this.player)
    }

    private fun untrack() {
        this.tracked?.navigatingPlayersExtension?.remove(this.player)
        this.tracked = null
    }

    private fun trackProgress() {
        val target = this.targetPos ?: return
        val distance = this.player.position().distanceTo(Vec3.atCenterOf(target))
        if (distance < this.closest - PROGRESS_DISTANCE) {
            this.closest = distance
            this.attempts = 0
        }
    }

    private fun recover(): Boolean {
        if (++this.attempts > MAX_RECOVERY_ATTEMPTS) {
            return false
        }
        val path = this.searchToTarget() ?: return false
        this.lastRecompute = this.level.gameTime
        return this.follow(path)
    }

    private fun extendIfEnding() {
        val path = this.path ?: return
        if (path.reachesTarget || path.size - path.index > EXTEND_LOOKAHEAD) {
            return
        }
        if (this.level.gameTime - this.lastRecompute < RECOMPUTE_INTERVAL || !this.canRepath()) {
            return
        }
        this.lastRecompute = this.level.gameTime
        this.extend()
    }

    private fun extend(): Boolean {
        val target = this.targetPos ?: return false
        val path = this.searchToTarget() ?: return false
        if (!path.reachesTarget) {
            val centre = Vec3.atBottomCenterOf(target)
            val gained = this.player.position().distanceTo(centre) -
                path.destination.distanceTo(centre.x, centre.y, centre.z)
            if (gained < MIN_EXTENSION_GAIN) {
                return false
            }
        }
        return this.follow(path)
    }

    protected open fun createContext(): PathfindingContext {
        return PathfindingContext(PathfindingRegion(this.level), this.player, this.settings)
    }

    private fun pathfinder(): Pathfinder {
        val existing = this.pathfinder
        if (existing == null) {
            val pathfinder = Pathfinder(this.movements, this.costModifiers)
            this.pathfinder = pathfinder
            return pathfinder
        }
        return existing
    }

    public companion object {
        public const val RECOMPUTE_INTERVAL: Int = 20
        public const val MAX_RECOVERY_ATTEMPTS: Int = 3
        public const val PROGRESS_DISTANCE: Double = 1.0
        public const val EXTEND_LOOKAHEAD: Int = 4
        public const val MIN_EXTENSION_GAIN: Double = 1.0
        public const val MAX_RECOMPUTE_RADIUS: Int = 16

        private const val MIN_WAYPOINT_DISTANCE = 0.5

        internal fun registerEvents() {
            GlobalEventHandler.Server.register<LevelBlockChangedEvent> { (level, pos, old, new) ->
                val extension = level.navigatingPlayersExtension
                val shouldUpdateNavigating = !extension.empty()
                    && Shapes.joinIsNotEmpty(old.getCollisionShape(level, pos), new.getCollisionShape(level, pos), BooleanOp.NOT_SAME)
                if (shouldUpdateNavigating) {
                    extension.forEachPlayer { player ->
                        if (player.navigation.shouldRecomputePath(pos)) {
                            player.navigation.dirty = true
                        }
                    }
                }
            }
            GlobalEventHandler.Server.register<PlayerDimensionChangeEvent> { (player) ->
                if (player is FakePlayer) {
                    player.navigation.stop()
                }
            }
            GlobalEventHandler.Server.register<PlayerLeaveEvent> { (player) ->
                if (player is FakePlayer) {
                    player.navigation.stop()
                }
            }
        }
    }
}
