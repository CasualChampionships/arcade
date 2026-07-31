/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals.ready

import org.jetbrains.annotations.ApiStatus.OverrideOnly

public interface ReadyBroadcaster<P> {
    @OverrideOnly
    public fun broadcastReadyCheck(participant: P, state: ReadyParticipantState)

    @OverrideOnly
    public fun broadcastSuccess()

    @OverrideOnly
    public fun broadcastFailure()
}