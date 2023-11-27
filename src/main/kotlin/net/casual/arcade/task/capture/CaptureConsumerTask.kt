package net.casual.arcade.task.capture

public fun interface CaptureConsumerTask<C> {
    public fun run(capture: C)
}