/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.animation.molang

import org.joml.Vector3f

public class MolangVec3(
    public val x: MolangExpression,
    public val y: MolangExpression,
    public val z: MolangExpression
) {
    public val isConstant: Boolean
        get() = this.x.isConstant && this.y.isConstant && this.z.isConstant

    public fun eval(scope: MolangScope, dest: Vector3f = Vector3f()): Vector3f {
        return dest.set(this.x.eval(scope), this.y.eval(scope), this.z.eval(scope))
    }

    public companion object {
        public val ZERO: MolangVec3 = MolangVec3(MolangExpression.ZERO, MolangExpression.ZERO, MolangExpression.ZERO)
        public val ONE: MolangVec3 = MolangVec3(MolangExpression.ONE, MolangExpression.ONE, MolangExpression.ONE)

        public fun constant(x: Float, y: Float, z: Float): MolangVec3 {
            return MolangVec3(MolangExpression.constant(x), MolangExpression.constant(y), MolangExpression.constant(z))
        }
    }
}