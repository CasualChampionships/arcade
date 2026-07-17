/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.networking

import net.casual.arcade.networking.extensions.PlayerObserverExtension
import net.fabricmc.api.ModInitializer

public object ArcadeObservers: ModInitializer {
    public const val MOD_ID: String = "arcade-observers"

    override fun onInitialize() {
        PlayerObserverExtension.registerEvents()
    }
}