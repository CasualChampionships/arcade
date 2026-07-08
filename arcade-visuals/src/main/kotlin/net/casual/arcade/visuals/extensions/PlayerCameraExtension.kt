/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.extensions

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.utils.register
import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.extensions.event.PlayerExtensionEvent
import net.casual.arcade.extensions.utils.getExtension
import net.casual.arcade.visuals.camera.PlayerCamera
import net.minecraft.server.level.ServerPlayer

internal class PlayerCameraExtension(player: ServerPlayer): PlayerExtension(player) {
    private var current: PlayerCamera? = null

    fun set(camera: PlayerCamera) {
        this.current?.removePlayer(this.player)
        this.current = camera
    }

    fun remove() {
        this.current = null
    }

    fun get(): PlayerCamera? {
        return this.current
    }

    companion object {
        @JvmStatic
        val ServerPlayer.cameraExtension: PlayerCameraExtension
            get() = this.getExtension()

        fun registerEvents() {
            GlobalEventHandler.Server.register<PlayerExtensionEvent> {
                it.addExtension(::PlayerCameraExtension)
            }
        }
    }
}