package net.casual.arcade.utils

import net.casual.arcade.stats.Stat

public object StatUtils {
    public fun Stat<Int>.increment(delta: Int = 1) {
        this.value += delta
    }

    public fun Stat<Float>.increment(delta: Float = 1.0F) {
        this.value += delta
    }

    public fun Stat<Double>.increment(delta: Double = 1.0) {
        this.value += delta
    }
}