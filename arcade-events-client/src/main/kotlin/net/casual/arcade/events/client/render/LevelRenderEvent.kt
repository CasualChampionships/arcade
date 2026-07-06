/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.client.render

import com.mojang.blaze3d.vertex.PoseStack
import net.casual.arcade.events.common.ClientSideEvent
import net.casual.arcade.events.common.Event
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.state.level.LevelRenderState

public data class LevelRenderEvent(
    val renderer: LevelRenderer,
    val state: LevelRenderState,
    val collector: SubmitNodeCollector,
    val stack: PoseStack,
): ClientSideEvent {
    public companion object {
        public const val ENTITIES: Int = 3
        public const val BLOCK_ENTITIES: Int = 4
        public const val DEBUG: Int = 5
    }
}