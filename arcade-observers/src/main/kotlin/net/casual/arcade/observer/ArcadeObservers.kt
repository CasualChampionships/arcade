/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.observer

import net.casual.arcade.observer.extensions.EntityObserversExtension
import net.casual.arcade.observer.extensions.LevelObserversExtension
import net.casual.arcade.observer.extensions.PlayerObserverExtension
import net.fabricmc.api.ModInitializer

public object ArcadeObservers: ModInitializer {
    public const val MOD_ID: String = "arcade-observers"

    override fun onInitialize() {
        EntityObserversExtension.registerEvents()
        LevelObserversExtension.registerEvents()
        PlayerObserverExtension.registerEvents()
    }
}