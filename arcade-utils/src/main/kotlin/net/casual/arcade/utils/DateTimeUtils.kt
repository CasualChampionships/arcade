/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration

public object DateTimeUtils {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd--HH-mm-ss")

    public fun getFormattedDate(): String {
        return LocalDateTime.now().format(formatter)
    }

    public fun Duration.formatHHMMSS(): String {
        val seconds = this.inWholeSeconds
        val hours = seconds / 3600
        return "%02d:".format(hours) + this.formatMMSS()
    }

    public fun Duration.formatMMSS(): String {
        val seconds = this.inWholeSeconds
        val minutes = seconds % 3600 / 60
        val secs = seconds % 60
        return "%02d:%02d".format(minutes, secs)
    }
}