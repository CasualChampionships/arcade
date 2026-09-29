/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.animation.molang

import gg.moonflower.molangcompiler.api.MolangRuntime
import gg.moonflower.molangcompiler.api.bridge.MolangVariable

public class MolangScope {
    private val animTimeVar = MolangVariable.create()
    private val lifeTimeVar = MolangVariable.create()

    private val runtime = MolangRuntime.runtime()
        .setQuery("anim_time", CompiledMolangExpression.of(this.animTimeVar))
        .setQuery("life_time", CompiledMolangExpression.of(this.lifeTimeVar))
        .create()

    public var animTime: Float by this.animTimeVar
    public var lifeTime: Float by this.lifeTimeVar

    internal fun eval(expression: CompiledMolangExpression): Float {
        return this.runtime.resolve(expression)
    }
}