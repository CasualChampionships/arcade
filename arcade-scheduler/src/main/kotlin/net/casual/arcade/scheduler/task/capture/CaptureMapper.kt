package net.casual.arcade.scheduler.task.capture

import org.jetbrains.annotations.ApiStatus.Internal
import java.io.Serializable

@Internal
public fun interface CaptureMapper<C, K>: Serializable {
    public fun map(capture: C): K
}