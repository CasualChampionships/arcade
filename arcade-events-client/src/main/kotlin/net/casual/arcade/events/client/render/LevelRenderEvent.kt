/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.client.render

import com.mojang.blaze3d.vertex.PoseStack
import net.casual.arcade.events.common.Event
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.state.level.LevelRenderState

public data class LevelRenderEvent(
    val renderer: LevelRenderer,
    val state: LevelRenderState,
    val buffers: MultiBufferSource.BufferSource,
    val stack: PoseStack,
): Event {
    public companion object {
        public const val ENTITIES: String = "entities"
        public const val BLOCK_ENTITIES: String = "block-entities"
        public const val DEBUG: String = "debug"
    }
}