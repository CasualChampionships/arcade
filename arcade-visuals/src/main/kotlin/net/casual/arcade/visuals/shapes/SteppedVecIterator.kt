/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.shapes

import net.minecraft.world.phys.Vec3

public class SteppedVecIterator(
    private val steps: Int,
    private val cycle: Boolean,
    private val wrapped: Iterator<Vec3>
): Iterator<Vec3> {
    private val delta = 1.0 / this.steps
    private var step = 0

    private val start: Vec3 by lazy(this.wrapped::next)

    private lateinit var current: Vec3
    private lateinit var target: Vec3

    private var finished: Boolean = false

    override fun hasNext(): Boolean {
        return !this.finished
    }

    override fun next(): Vec3 {
        // This should only be the case on the first call
        if (!this::current.isInitialized) {
            this.current = this.start
            if (!this.wrapped.hasNext()) {
                this.finished = true
                return this.current
            }
            this.target = this.wrapped.next()
        }

        if (this.step > this.steps) {
            this.current = this.target
            if (!this.wrapped.hasNext()) {
                if (!this.cycle || this.target == this.start) {
                    this.finished = true
                    return this.current
                }
                this.target = this.start
            } else {
                this.target = this.wrapped.next()
            }
            this.step = 0
        }

        return this.current.lerp(this.target, this.step++ * this.delta)
    }
}