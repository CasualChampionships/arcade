/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.utils

import net.casual.arcade.visuals.extensions.PlayerCameraOverlayExtension.Companion.cameraOverlayExtension
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

public fun ServerPlayer.setCameraOverlay(overlay: ResourceLocation) {
    this.cameraOverlayExtension.setOverlay(overlay)
}

public fun ServerPlayer.clearCameraOverlay() {
    this.cameraOverlayExtension.clearOverlay()
}