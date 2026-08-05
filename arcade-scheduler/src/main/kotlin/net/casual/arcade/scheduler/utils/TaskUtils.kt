/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.utils

import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.utils.ArcadeUtils

public fun Task.runSafely() {
    try {
        this.run()
    } catch (exception: Exception) {
        ArcadeUtils.logger.error("Exception while completing task ${this.javaClass.name}", exception)
    }
}