/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.client.render

import com.mojang.blaze3d.vertex.PoseStack
import net.casual.arcade.events.common.Event
import net.minecraft.client.Camera
import net.minecraft.client.DeltaTracker
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.MultiBufferSource

public data class LevelRenderEvent(
    val renderer: LevelRenderer,
    val camera: Camera,
    val buffers: MultiBufferSource.BufferSource,
    val stack: PoseStack,
    val deltas: DeltaTracker
): Event {
    public companion object {
        public const val ENTITIES: String = "entities"
        public const val BLOCK_ENTITIES: String = "block-entities"
        public const val DEBUG: String = "debug"
    }
}