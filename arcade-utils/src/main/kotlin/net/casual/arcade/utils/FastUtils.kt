/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import it.unimi.dsi.fastutil.Pair
import it.unimi.dsi.fastutil.doubles.DoubleDoublePair
import it.unimi.dsi.fastutil.floats.FloatFloatPair
import it.unimi.dsi.fastutil.ints.IntIntPair

public operator fun <A: Any, B> Pair<A, B>.component1(): A {
    return this.left()
}

public operator fun <A: Any, B> Pair<A, B>.component2(): B {
    return this.right()
}

public operator fun IntIntPair.component1(): Int {
    return this.leftInt()
}

public operator fun IntIntPair.component2(): Int {
    return this.rightInt()
}

public operator fun FloatFloatPair.component1(): Float {
    return this.leftFloat()
}

public operator fun FloatFloatPair.component2(): Float {
    return this.rightFloat()
}

public operator fun DoubleDoublePair.component1(): Double {
    return this.leftDouble()
}

public operator fun DoubleDoublePair.component2(): Double {
    return this.rightDouble()
}