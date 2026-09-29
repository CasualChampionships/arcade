/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.animation.molang

import gg.moonflower.molangcompiler.api.MolangCompiler
import gg.moonflower.molangcompiler.api.exception.MolangRuntimeException
import net.casual.arcade.model.animation.molang.exception.MolangCompilationException
import net.casual.arcade.model.animation.molang.exception.MolangEvaluationException
import java.util.concurrent.ConcurrentHashMap

public class MolangExpression private constructor(
    public val source: String,
    private val compiled: CompiledMolangExpression
) {
    public val isConstant: Boolean
        get() = this.compiled.isConstant

    public fun eval(scope: MolangScope): Float {
        try {
            return scope.eval(this.compiled)
        } catch (e: MolangRuntimeException) {
            throw MolangEvaluationException(this, "Failed to evaluate expression", e)
        }
    }

    public fun negate(): MolangExpression {
        val underlying = this.compiled
        if (underlying.isConstant) {
            return constant(-underlying.constant)
        }
        val delegate = CompiledMolangExpression { env -> -env.resolve(underlying) }
        return MolangExpression("-1 * (${this.source})", delegate)
    }

    override fun toString(): String {
        return "Molang(${this.source})"
    }

    public companion object {
        private val COMPILER = MolangCompiler.create(MolangCompiler.DEFAULT_FLAGS, MolangExpression::class.java.classLoader)
        private val CACHE = ConcurrentHashMap<String, MolangExpression>()

        public val ZERO: MolangExpression = constant(0.0F)
        public val ONE: MolangExpression = constant(1.0F)

        public fun constant(value: Float): MolangExpression {
            return MolangExpression("$value", CompiledMolangExpression.of(value))
        }

        public fun parse(source: String): MolangExpression {
            val trimmed = source.trim()
            if (trimmed.isEmpty()) {
                return ZERO
            }
            val constant = trimmed.toFloatOrNull()
            if (constant != null) {
                return constant(constant)
            }
            return CACHE.computeIfAbsent(source, ::compile)
        }

        private fun compile(source: String): MolangExpression {
            try {
                val compiled = COMPILER.compile(source)
                return MolangExpression(source, compiled)
            } catch (e: Exception) {
                throw MolangCompilationException(source, "Failed to compile molang expression", e)
            }
        }
    }
}