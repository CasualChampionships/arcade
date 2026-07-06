/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.client.render

import net.casual.arcade.events.common.ClientSideEvent
import net.casual.arcade.events.common.Event
import net.minecraft.client.Camera
import net.minecraft.client.DeltaTracker
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.extract.LevelExtractor
import net.minecraft.client.renderer.state.level.LevelRenderState

public data class LevelRenderExtractEvent(
    val extractor: LevelExtractor,
    val level: ClientLevel,
    val state: LevelRenderState,
    val camera: Camera,
    val deltas: DeltaTracker,
    val frustum: Frustum
): ClientSideEvent