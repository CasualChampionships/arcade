package net.casual.arcade.utils

import net.casual.arcade.stats.Stat

public object StatUtils {
    public operator fun Stat<Int>.plusAssign(delta: Int) {
        this.value += delta
    }

    public operator fun Stat<Int>.minusAssign(delta: Int) {
        this.value -= delta
    }

    public operator fun Stat<Double>.plusAssign(delta: Double) {
        this.value += delta
    }

    public operator fun Stat<Double>.minusAssign(delta: Double) {
        this.value -= delta
    }
}