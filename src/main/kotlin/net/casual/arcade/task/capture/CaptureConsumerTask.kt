package net.casual.arcade.task.capture

import java.io.Serializable

public fun interface CaptureConsumerTask<C>: Serializable {
    public fun run(capture: C)
}