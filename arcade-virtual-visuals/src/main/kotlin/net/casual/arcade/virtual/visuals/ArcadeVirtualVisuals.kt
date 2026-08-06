/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.player.PlayerLeaveEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.observer.utils.asObserver
import net.casual.arcade.virtual.visuals.extensions.*
import net.casual.arcade.virtual.visuals.utils.stopObservingVisuals
import net.fabricmc.api.ModInitializer

public object ArcadeVirtualVisuals: ModInitializer {
    public const val MOD_ID: String = "arcade-virtual-visuals"

    override fun onInitialize() {
        PlayerCameraOverlayExtension.registerEvents()

        GlobalEventHandler.Server.register<PlayerLeaveEvent> { (player) ->
            player.asObserver().stopObservingVisuals()
        }
    }
}
