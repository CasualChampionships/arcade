/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.ready

public class ReadyParticipantState internal constructor(
    private val tracker: ReadyTracker<*>
) {
    private var state = ReadyState.Awaiting

    public fun isReady(): Boolean {
        return this.state == ReadyState.Ready
    }

    public fun markReady(listener: () -> Unit) {
        if (this.state != ReadyState.Ready) {
            this.state = ReadyState.Ready
            listener.invoke()
            this.tracker.checkReady()
        }
    }

    public fun markNotReady(listener: () -> Unit) {
        if (this.state != ReadyState.NotReady) {
            this.state = ReadyState.NotReady
            listener.invoke()
        }
    }
}