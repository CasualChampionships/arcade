package net.casual.arcade.scheduler.task.capture

import java.io.Serializable

public fun interface CaptureMapper<C, K>: Serializable {
    public fun map(capture: C): K
}