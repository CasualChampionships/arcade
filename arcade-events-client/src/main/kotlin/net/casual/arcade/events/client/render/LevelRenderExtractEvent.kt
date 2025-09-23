/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.client.render

import net.casual.arcade.events.common.Event
import net.minecraft.client.Camera
import net.minecraft.client.DeltaTracker
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.state.LevelRenderState

// TODO: Make LevelRenderState extensible
public class LevelRenderExtractEvent(
    public val renderer: LevelRenderer,
    public val level: ClientLevel,
    public val state: LevelRenderState,
    public val camera: Camera,
    public val deltas: DeltaTracker,
    public val frustum: Frustum
): Event