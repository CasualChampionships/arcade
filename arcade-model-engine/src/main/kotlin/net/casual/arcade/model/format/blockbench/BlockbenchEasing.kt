/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.format.blockbench

import net.casual.arcade.utils.math.Easing

internal object BlockbenchEasing {
    private val BASES: Map<String, (DoubleArray) -> Easing> = mapOf(
        "Quad" to { _ -> Easing.QUAD },
        "Cubic" to { _ -> Easing.CUBIC },
        "Quart" to { _ -> Easing.QUART },
        "Quint" to { _ -> Easing.QUINT },
        "Sine" to { _ -> Easing.SINE },
        "Circ" to { _ -> Easing.CIRCLE },
        "Expo" to { _ -> Easing.EXPO },
        "Elastic" to { args -> if (args.isEmpty()) Easing.ELASTIC else Easing.elastic(args[0]) },
        "Back" to { args -> if (args.isEmpty()) Easing.BACK else Easing.back(args[0]) },
        "Bounce" to { args -> if (args.isEmpty()) Easing.BOUNCE else Easing.bounce(args[0]) }
    )

    fun from(name: String?, args: DoubleArray): Easing? {
        if (name == null) {
            return Easing.LINEAR
        }
        val (type, base) = when {
            name.startsWith("easeIn") -> EaseType.In to name.removePrefix("easeIn")
            name.startsWith("easeOut") -> EaseType.Out to name.removePrefix("easeOut")
            name.startsWith("easeInOut") -> EaseType.InOut to name.removePrefix("easeInOut")
            else -> return this.fromLegacy(name, args)
        }
        val easing = BASES[base]?.invoke(args) ?: return null
        return when (type) {
            EaseType.In -> easing
            EaseType.Out -> easing.out()
            EaseType.InOut -> easing.inOut()
        }
    }

    private fun fromLegacy(name: String, args: DoubleArray): Easing? {
        return when (name) {
            "linear", "step" -> Easing.LINEAR
            "quad" -> Easing.QUAD
            "cubic" -> Easing.CUBIC
            "poly" -> if (args.isEmpty()) Easing.poly() else Easing.poly(args[0])
            "sin" -> Easing.SINE
            "circle" -> Easing.CIRCLE
            "exp" -> Easing.EXPO
            "elastic" -> if (args.isEmpty()) Easing.ELASTIC else Easing.elastic(args[0])
            "back" -> if (args.isEmpty()) Easing.BACK else Easing.back(args[0])
            "bounce" -> if (args.isEmpty()) Easing.BOUNCE else Easing.bounce(args[0])
            else -> null
        }
    }

    private enum class EaseType {
        In, Out, InOut
    }
}