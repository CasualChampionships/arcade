/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.stats

public class Stat<T: Any>(
    public val type: StatType<T>
) {
    internal var frozen: Boolean = false

    public var value: T = this.type.default
        private set

    public fun modify(modifier: (current: T) -> T) {
        if (!this.frozen) {
            this.value = modifier(this.value)
        }
    }

    internal fun set(value: T) {
        this.value = value
    }

    public companion object {
        public fun Stat<Int>.increment(delta: Int = 1) {
            this.modify { it + delta }
        }

        public fun Stat<Float>.increment(delta: Float = 1.0F) {
            this.modify { it + delta }
        }

        public fun Stat<Double>.increment(delta: Double = 1.0) {
            this.modify { it + delta }
        }
    }
}