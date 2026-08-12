/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding

import net.casual.arcade.npc.FakePlayer
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes

/**
 * What an NPC's jump can and cannot reach, worked out by running vanilla's own movement a tick
 * at a time.
 *
 * Everything here is calculated with attributes.
 *
 * ```kotlin
 * val arc: SimulatedJumpArc = context.jumps.arc(sprinting = true, room = context.jumps.roomSteps)
 * val travelled: Double = arc.descentTo(-1.0)
 * ```
 *
 * @param player The NPC being planned for.
 * @see SimulatedJumpArc
 */
public class JumpPhysicsSimulation(player: FakePlayer) {
    private val airDrag = modified(AIR_DRAG, player.getAttributeValue(Attributes.AIR_DRAG_MODIFIER))
    private val fallDrag = modified(FALL_DRAG, player.getAttributeValue(Attributes.AIR_DRAG_MODIFIER))
    private val friction = modified(GROUND_FRICTION, player.getAttributeValue(Attributes.FRICTION_MODIFIER)) * this.airDrag
    private val gravity = player.getAttributeValue(Attributes.GRAVITY)
    private val power = player.getAttributeValue(Attributes.JUMP_STRENGTH) + player.jumpBoostPower

    private val arcs = arrayOfNulls<SimulatedJumpArc>((ROOM_STEPS + 1) * 2)

    public val width: Double = player.bbWidth.toDouble()

    public val walkSpeed: Double

    public val sprintSpeed: Double

    /**
     * The furthest below a launch that a jump is followed; anything deeper is a fall.
     */
    public val depth: Double = MAX_DEPTH

    /**
     * How far past the middle of a block the NPC can be and still be standing on it, and so the
     * furthest out it can jump from.
     *
     * An entity is held up by any part of its hitbox overlapping the block below it, which leaves
     * it able to stand all but a sliver of the way off an edge.
     */
    public val maxLaunchOffset: Double = 0.5 + this.width / 2.0 - EDGE_EPSILON

    /**
     * How high the NPC's feet get with nothing overhead.
     */
    public val apex: Double

    /**
     * How many increments [arc] divides the room above a jump into.
     */
    public val roomSteps: Int = ROOM_STEPS

    private val walkAcceleration: Double
    private val sprintAcceleration: Double
    private val roomStep: Double

    init {
        val speed = player.getAttributeValue(Attributes.MOVEMENT_SPEED)
        val base = if (player.isSprinting) speed / SPRINT_MULTIPLIER else speed
        this.walkAcceleration = base * INPUT_SCALE
        this.sprintAcceleration = base * SPRINT_MULTIPLIER * INPUT_SCALE
        this.walkSpeed = this.walkAcceleration / (1.0 - this.friction)
        this.sprintSpeed = this.sprintAcceleration / (1.0 - this.friction)

        val unobstructed = this.simulate(sprinting = false, room = Double.MAX_VALUE)
        this.arcs[ROOM_STEPS] = unobstructed
        this.apex = unobstructed.apex
        this.roomStep = this.apex / ROOM_STEPS
    }

    public fun roomAt(step: Int): Double {
        return step * this.roomStep
    }

    /**
     * The arc the NPC follows jumping with [room] increments of space above its feet.
     *
     * @param sprinting Whether the NPC takes a running start.
     * @param room How much space is above the NPC, in [roomAt] increments.
     * @return The jump's arc.
     */
    public fun arc(sprinting: Boolean, room: Int): SimulatedJumpArc {
        val step = Mth.clamp(room, 0, ROOM_STEPS)
        val index = if (sprinting) step + ROOM_STEPS + 1 else step
        return this.arcs[index] ?: this.simulate(sprinting, this.roomAt(step)).also { this.arcs[index] = it }
    }

    /**
     * How far past the middle of the launch block the NPC can be relied on to leave from.
     *
     * It jumps on the last tick it is still standing on the block, so in the worst case it is a
     * whole tick's travel short of the edge when that comes around.
     *
     * @param sprinting Whether the NPC takes a running start.
     * @return The offset, in blocks.
     */
    public fun launchOffset(sprinting: Boolean): Double {
        return maxOf(0.0, this.maxLaunchOffset - if (sprinting) this.sprintSpeed else this.walkSpeed)
    }

    /**
     * The furthest block a jump can land on, measured between the launch and landing columns.
     *
     * @param sprinting Whether the NPC takes a running start.
     * @param room How much space is above the NPC, in [roomAt] increments.
     * @param landing How far above the launch the landing is; negative for a drop.
     * @return The distance, in blocks, or `0` if the landing is out of reach entirely.
     */
    public fun furthest(sprinting: Boolean, room: Int, landing: Double): Int {
        val reach = this.arc(sprinting, room).descentTo(landing)
        if (reach.isNaN()) {
            return 0
        }
        return Mth.floor(this.distanceFor(reach, sprinting))
    }

    /**
     * The distance between the launch and landing columns that travelling [reach] blocks covers.
     *
     * Landing is the NPC's leading edge catching the far block, measured from the middle of the
     * block it left.
     *
     * @param reach How far the NPC travels, in blocks.
     * @param sprinting Whether the NPC takes a running start.
     * @return The distance, in blocks.
     */
    public fun distanceFor(reach: Double, sprinting: Boolean): Double {
        return reach + this.launchOffset(sprinting) + 0.5 + this.width / 2.0
    }

    /**
     * How far the NPC has to travel to land [distance] blocks away.
     *
     * @param distance The distance between the launch and landing columns, in blocks.
     * @param sprinting Whether the NPC takes a running start.
     * @return The travel required, in blocks.
     */
    public fun reachFor(distance: Int, sprinting: Boolean): Double {
        return distance - this.launchOffset(sprinting) - 0.5 - this.width / 2.0
    }

    private fun simulate(sprinting: Boolean, room: Double): SimulatedJumpArc {
        val acceleration = if (sprinting) this.sprintAcceleration else this.walkAcceleration
        val air = (if (sprinting) SPRINT_AIR_SPEED else WALK_AIR_SPEED) * INPUT_SCALE

        // The player arrives at the edge up to speed and leaves the ground while still standing on
        // it, so the launch tick is both accelerated and slowed by the ground
        var speed = acceleration / (1.0 - this.friction) * this.friction + acceleration
        if (sprinting) {
            speed += SPRINT_JUMP_BOOST
        }
        var drag = this.friction
        var velocity = this.power
        var travelled = 0.0
        var height = 0.0

        val travel = DoubleArray(MAX_TICKS)
        val heights = DoubleArray(MAX_TICKS)
        var ticks = 1
        while (ticks < MAX_TICKS && height > -MAX_DEPTH) {
            var climb = velocity
            if (climb > 0.0 && height + climb > room) {
                // Head hitting
                climb = maxOf(0.0, room - height)
                velocity = 0.0
            }

            travelled += speed
            height += climb
            travel[ticks] = travelled
            heights[ticks] = height
            ticks++

            speed = speed * drag + air
            drag = this.airDrag
            velocity = (velocity - this.gravity) * this.fallDrag
        }
        return SimulatedJumpArc(travel.copyOf(ticks), heights.copyOf(ticks))
    }

    private companion object {
        private const val ROOM_STEPS = 16
        private const val MAX_DEPTH = 1.0
        private const val MAX_TICKS = 64
        private const val EDGE_EPSILON = 0.01

        // Vanilla's movement constants
        private const val WALK_AIR_SPEED = 0.02
        private const val SPRINT_AIR_SPEED = 0.026
        private const val INPUT_SCALE = 0.98
        private const val SPRINT_MULTIPLIER = 1.3
        private const val SPRINT_JUMP_BOOST = 0.2
        private const val GROUND_FRICTION = 0.6
        private const val AIR_DRAG = 0.91
        private const val FALL_DRAG = 0.98

        /**
         * @see LivingEntity.computeModifiedFriction
         */
        private fun modified(friction: Double, modifier: Double): Double {
            return Mth.clamp(1.0 - (1.0 - friction) * modifier, 0.0, 1.0)
        }
    }
}
