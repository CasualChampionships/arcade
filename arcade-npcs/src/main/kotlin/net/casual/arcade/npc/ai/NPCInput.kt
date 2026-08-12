/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.ai

import net.casual.arcade.npc.pathfinding.execution.MovementControls
import net.minecraft.world.entity.player.Input
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

public class NPCInput {
    public var keyPresses: Input = Input.EMPTY

    public var moveVector: Vec2 = Vec2.ZERO

    public var moveDirection: Vec3? = null
        private set

    public var moveSpeed: Float = 1.0F
        private set

    public fun setMoveDirection(direction: Vec3, speed: Float) {
        this.moveDirection = direction
        this.moveSpeed = speed
    }

    public var forward: Boolean
        get() = this.keyPresses.forward()
        set(value) { this.keyPresses = this.keyPresses.withForward(value) }

    public var backward: Boolean
        get() = this.keyPresses.backward()
        set(value) { this.keyPresses = this.keyPresses.withBackward(value) }

    public var left: Boolean
        get() = this.keyPresses.left()
        set(value) { this.keyPresses = this.keyPresses.withLeft(value) }

    public var right: Boolean
        get() = this.keyPresses.right()
        set(value) { this.keyPresses = this.keyPresses.withRight(value) }

    public var jump: Boolean
        get() = this.keyPresses.jump()
        set(value) { this.keyPresses = this.keyPresses.withJump(value) }

    public var shift: Boolean
        get() = this.keyPresses.shift()
        set(value) { this.keyPresses = this.keyPresses.withShift(value) }

    public var sprint: Boolean
        get() = this.keyPresses.sprint()
        set(value) { this.keyPresses = this.keyPresses.withSprint(value) }

    public fun setMoveVector(left: Float, forward: Float) {
        this.moveDirection = null
        this.moveVector = Vec2(left, forward)
        this.keyPresses = Input(
            forward > 0.0F,
            forward < 0.0F,
            left > 0.0F,
            left < 0.0F,
            this.keyPresses.jump(),
            this.keyPresses.shift(),
            this.keyPresses.sprint()
        )
    }

    public fun hasForwardImpulse(): Boolean {
        return this.moveVector.y > 1.0E-5F
    }

    public fun scaleMoveVector(factor: Float) {
        if (factor == 1.0F) {
            return
        }
        val direction = this.moveDirection
        val speed = this.moveSpeed
        this.setMoveVector(this.moveVector.x * factor, this.moveVector.y * factor)
        if (direction != null) {
            this.setMoveDirection(direction, speed * factor)
        }
    }

    public fun reset() {
        this.keyPresses = Input.EMPTY
        this.moveVector = Vec2.ZERO
        this.moveDirection = null
        this.moveSpeed = 1.0F
    }

    private fun Input.withForward(value: Boolean): Input {
        return Input(value, this.backward(), this.left(), this.right(), this.jump(), this.shift(), this.sprint())
    }

    private fun Input.withBackward(value: Boolean): Input {
        return Input(this.forward(), value, this.left(), this.right(), this.jump(), this.shift(), this.sprint())
    }

    private fun Input.withLeft(value: Boolean): Input {
        return Input(this.forward(), this.backward(), value, this.right(), this.jump(), this.shift(), this.sprint())
    }

    private fun Input.withRight(value: Boolean): Input {
        return Input(this.forward(), this.backward(), this.left(), value, this.jump(), this.shift(), this.sprint())
    }

    private fun Input.withJump(value: Boolean): Input {
        return Input(this.forward(), this.backward(), this.left(), this.right(), value, this.shift(), this.sprint())
    }

    private fun Input.withShift(value: Boolean): Input {
        return Input(this.forward(), this.backward(), this.left(), this.right(), this.jump(), value, this.sprint())
    }

    private fun Input.withSprint(value: Boolean): Input {
        return Input(this.forward(), this.backward(), this.left(), this.right(), this.jump(), this.shift(), value)
    }
}
