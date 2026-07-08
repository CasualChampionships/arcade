/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.phase

import kotlin.collections.fold

@JvmInline
public value class EventPhases private constructor(internal val bits: Long) {
    public operator fun contains(phase: Int): Boolean {
        return (this.bits and ((1 shl phase).toLong())) != 0L
    }

    public companion object {
        @JvmStatic
        @JvmName("of")
        public fun of(vararg phases: Int): EventPhases {
            return EventPhases(phases.fold(0) { acc, p -> acc or ((1 shl p).toLong()) })
        }
    }
}