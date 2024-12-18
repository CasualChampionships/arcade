/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.task.capture

import java.io.Serializable

public fun interface CaptureConsumerTask<C>: Serializable {
    public fun run(capture: C)
}