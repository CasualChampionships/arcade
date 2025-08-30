/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.recorder.chunk

/**
 * This interface represents an object that can be recorded
 * by a [ReplayChunkRecorder].
 *
 * If a [ReplayChunkRecorder] is added to this object via [addRecorder]
 * any packets that are a result of this object should also then be
 * recorded by that recorder.
 *
 * In order to update the recorders that are able to record this
 * object you should call [ReplayChunkRecorders.updateRecordable] to
 * add and remove any [ReplayChunkRecorder]s as necessary.
 *
 * For example:
 * ```kotlin
 * class MyChunkRecordable: ReplayChunkRecordable {
 *     // ...
 *
 *     fun tick() {
 *         // The level that your recordable object is in.
 *         val level: ServerLevel = // ...
 *         // If your object is within a chunk:
 *         val chunkPos: ChunkPos = // ...
 *         ChunkRecorders.updateRecordable(this, level.dimension(), chunkPos)
 *
 *         // Alternatively if your object spans multiple chunks:
 *         val boundingBox: BoundingBox = // ...
 *         ChunkRecorders.updateRecordable(this, level.dimension(), boundingBox)
 *     }
 *
 *     // ...
 * }
 * ```
 *
 * @see ReplayChunkRecorder
 * @see ReplayChunkRecorders.updateRecordable
 */
public interface ReplayChunkRecordable {
    /**
     * This gets all the [ReplayChunkRecorder]s that are currently
     * recording this object.
     *
     * @return All the chunk recorders recording this.
     */
    public fun getRecorders(): Collection<ReplayChunkRecorder>

    /**
     * Adds a [ReplayChunkRecorder] to record all packets produced
     * by this object.
     *
     * @param recorder The recorder to add.
     */
    public fun addRecorder(recorder: ReplayChunkRecorder)

    /**
     * Re-sends the packets to the given chunk recorder.
     *
     * @param recorder The recorder to resend the packets to.
     */
    public fun resendPackets(recorder: ReplayChunkRecorder)

    /**
     * Removes a [ReplayChunkRecorder] from recording packets
     * produced by this object.
     *
     * @param recorder The recorder to remove.
     */
    public fun removeRecorder(recorder: ReplayChunkRecorder)

    /**
     * Removes all [ReplayChunkRecorder]s from recording
     * packets produced by this object.
     */
    public fun removeAllRecorders()
}