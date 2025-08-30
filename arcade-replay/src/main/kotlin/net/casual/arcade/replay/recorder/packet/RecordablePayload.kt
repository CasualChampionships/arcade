/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.recorder.packet

import net.casual.arcade.replay.recorder.ReplayRecorder
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

/**
 * Interface for [CustomPacketPayload]s to indicate whether
 * they should be recorded with ServerReplay or not.
 */
public interface RecordablePayload {
    /**
     * Whether this payload should be recorded
     * by ServerReplay.
     *
     * @return Whether the payload should be recorded.
     */
    public fun shouldRecord(recorder: ReplayRecorder): Boolean

    /**
     * Writes the custom payload data manually.
     *
     * @param buf The byte buf to write to.
     */
    public fun record(buf: FriendlyByteBuf)
}