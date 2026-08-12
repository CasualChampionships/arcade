/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.execution

import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.ai.NPCInput
import net.casual.arcade.npc.pathfinding.Path
import net.casual.arcade.npc.pathfinding.PathfindingSettings
import net.casual.arcade.npc.pathfinding.movement.Movement
import net.minecraft.world.phys.Vec3
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

public class PathExecutor(
    public val player: FakePlayer,
    public val path: Path,
    public val settings: PathfindingSettings
) {
    private var executor: MovementExecutor? = null
    private var elapsed: Int = 0
    private var strayed: Int = 0
    private var closest: Double = Double.MAX_VALUE
    private var idle: Int = 0

    public var speed: Double = 1.0

    public var isSprinting: Boolean = false
        private set

    public fun tick(input: NPCInput): PathStatus {
        this.skipAhead()

        if (this.path.isDone()) {
            this.finish()
            return PathStatus.Completed
        }

        val movement = this.path.current!!
        if (this.executor == null) {
            this.begin(movement)
        }
        this.elapsed++

        if (this.hasStrayed(movement) || this.hasStalled(movement) || this.hasIdled(movement)) {
            this.finish()
            return PathStatus.Failed
        }

        val status = this.executor!!.tick(this.player, input)
        this.applySprint(input, movement)
        input.scaleMoveVector(this.speed.toFloat())

        return when (status) {
            MovementStatus.Moving -> PathStatus.Following
            MovementStatus.Failed -> {
                this.finish()
                PathStatus.Failed
            }
            MovementStatus.Completed -> {
                this.path.advance()
                this.release()
                if (this.path.isDone()) PathStatus.Completed else PathStatus.Following
            }
        }
    }

    public fun stop() {
        this.finish()
    }

    private fun begin(movement: Movement) {
        this.elapsed = 0
        this.strayed = 0
        this.idle = 0
        this.closest = Double.MAX_VALUE
        this.executor = movement.createExecutor().also { it.start(this.player) }
    }

    private fun release() {
        this.executor?.stop(this.player)
        this.executor = null
    }

    private fun finish() {
        this.release()
        this.isSprinting = false
    }

    private fun applySprint(input: NPCInput, movement: Movement) {
        this.isSprinting = this.shouldSprint(movement) && input.hasForwardImpulse()
        input.sprint = this.isSprinting
    }

    private fun shouldSprint(movement: Movement): Boolean {
        if (!this.settings.canSprint || !movement.sprintable) {
            return false
        }
        // A movement that cannot be done at a walk keeps sprint regardless of what follows
        if (movement.requiresSprint) {
            return true
        }
        // Sprinting into the last movement overshoots whatever the NPC was walking to
        val next = this.path.next ?: return false
        // Slow down a movement early when the one after it needs the NPC under control
        if (!next.sprintable && !next.requiresSprint) {
            return false
        }
        return this.turnCosine(movement, next) > SPRINT_TURN_COSINE
    }

    private fun turnCosine(from: Movement, to: Movement): Double {
        val a = from.to.target.subtract(from.from.target)
        val b = to.to.target.subtract(to.from.target)
        val lengthA = sqrt(a.x * a.x + a.z * a.z)
        val lengthB = sqrt(b.x * b.x + b.z * b.z)
        if (lengthA < MIN_LENGTH || lengthB < MIN_LENGTH) {
            return 0.0
        }
        return (a.x * b.x + a.z * b.z) / (lengthA * lengthB)
    }

    private fun hasStalled(movement: Movement): Boolean {
        return this.elapsed > movement.cost * STALL_FACTOR + STALL_GRACE
    }

    private fun hasIdled(movement: Movement): Boolean {
        val distance = this.player.position().distanceTo(movement.target)
        if (distance < this.closest - IDLE_PROGRESS) {
            this.closest = distance
            this.idle = 0
            return false
        }
        return ++this.idle > MAX_IDLE_TICKS
    }

    private fun hasStrayed(movement: Movement): Boolean {
        val distance = horizontalDistanceToSegment(
            this.player.position(),
            movement.from.target,
            movement.to.target
        )
        if (distance > MAX_DEVIATION) {
            this.strayed++
        } else {
            this.strayed = 0
        }
        return this.strayed > MAX_STRAY_TICKS
    }

    private fun skipAhead() {
        var index = this.path.index
        val limit = minOf(this.path.size, index + SKIP_LOOKAHEAD)
        var found = index
        while (index < limit) {
            val node = this.path.nodeAt(index + 1)
            if (MovementControls.hasReached(this.player, node.target, SKIP_RADIUS, SKIP_HEIGHT)) {
                found = index + 1
            }
            index++
        }
        if (found != this.path.index) {
            this.path.index = found
            this.release()
        }
    }

    public companion object {
        public const val MAX_DEVIATION: Double = 1.5
        public const val MAX_STRAY_TICKS: Int = 20
        public const val STALL_FACTOR: Double = 4.0
        public const val STALL_GRACE: Int = 20
        public const val MAX_IDLE_TICKS: Int = 20
        public const val IDLE_PROGRESS: Double = 0.05
        public const val SPRINT_TURN_COSINE: Double = 0.707
        public const val SKIP_LOOKAHEAD: Int = 3

        private const val SKIP_RADIUS = 0.4
        private const val SKIP_HEIGHT = 0.4
        private const val MIN_LENGTH = 1.0E-4

        private fun horizontalDistanceToSegment(point: Vec3, start: Vec3, end: Vec3): Double {
            val dx = end.x - start.x
            val dz = end.z - start.z
            val lengthSqr = dx * dx + dz * dz
            if (lengthSqr < MIN_LENGTH) {
                return sqrt((point.x - start.x) * (point.x - start.x) + (point.z - start.z) * (point.z - start.z))
            }
            val t = max(0.0, min(1.0, ((point.x - start.x) * dx + (point.z - start.z) * dz) / lengthSqr))
            val closestX = start.x + t * dx
            val closestZ = start.z + t * dz
            return sqrt((point.x - closestX) * (point.x - closestX) + (point.z - closestZ) * (point.z - closestZ))
        }
    }
}
