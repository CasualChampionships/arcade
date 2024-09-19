package net.casual.arcade.utils

import it.unimi.dsi.fastutil.Pair

public operator fun <A: Any, B> Pair<A, B>.component1(): A {
    return this.left()
}

public operator fun <A: Any, B> Pair<A, B>.component2(): B {
    return this.right()
}