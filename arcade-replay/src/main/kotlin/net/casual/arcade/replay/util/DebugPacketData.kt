/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.util

public data class DebugPacketData(
    val type: String,
    var count: Int,
    var size: Long
) {
    public fun increment(size: Int) {
        this.count++
        this.size += size
    }

    public fun format(): String {
        return "Type: ${this.type}, Size: ${FileUtils.formatSize(this.size)}, Count: ${this.count}"
    }
}