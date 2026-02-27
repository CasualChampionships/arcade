/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.ready

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import net.minecraft.network.chat.Component

public class ReadyTracker<P>(private val broadcaster: ReadyBroadcaster<P>) {
    private val success = CompletableDeferred<Unit>()
    private val tracked = LinkedHashMap<ReadyParticipantState, Component>()

    public fun isReady(): Boolean {
        return this.tracked().all(ReadyParticipantState::isReady)
    }

    public fun getAwaiting(): Collection<Component> {
        return this.tracked.values
    }

    public suspend fun awaitSuccess() {
        try {
            this.success.await()
            this.broadcaster.broadcastSuccess()
        } catch (exception: CancellationException) {
            this.broadcaster.broadcastFailure()
            throw exception
        }
    }

    internal fun initialize(participants: Collection<P>, pretty: (P) -> Component) {
        for (participant in participants) {
            val state = ReadyParticipantState(this)
            this.tracked[state] = pretty.invoke(participant)
            this.broadcaster.broadcastReadyCheck(participant, state)
        }
    }

    internal fun checkReady() {
        if (!this.success.isCompleted && this.isReady()) {
            this.success.complete(Unit)
        }
    }

    internal fun tracked(): Collection<ReadyParticipantState> {
        return this.tracked.keys
    }
}