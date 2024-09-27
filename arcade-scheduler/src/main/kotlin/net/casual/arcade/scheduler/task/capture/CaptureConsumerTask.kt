package net.casual.arcade.scheduler.task.capture

import org.jetbrains.annotations.ApiStatus.Internal
import java.io.Serializable

@Internal
public fun interface CaptureConsumerTask<C>: Serializable {
    public fun run(capture: C)
}