/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.stats

import com.google.gson.JsonElement

public class Stat<T>(
    public val type: StatType<T>
) {
    public var value: T = this.type.default
        private set
    public var frozen: Boolean = false

    public fun modify(modifier: (current: T) -> T) {
        if (!this.frozen) {
            this.value = modifier(this.value)
        }
    }

    public fun serialize(): JsonElement {
        return this.type.codec.serialize(this.value)
    }

    public fun deserialize(element: JsonElement) {
        this.value = this.type.codec.deserialize(element)
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