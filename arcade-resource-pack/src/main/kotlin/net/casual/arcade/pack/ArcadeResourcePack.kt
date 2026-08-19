/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack

import net.casual.arcade.pack.extensions.PlayerPackExtension
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path

public object ArcadeResourcePack: ModInitializer {
    private val container = FabricLoader.getInstance().getModContainer("arcade-resource-pack").get()

    override fun onInitialize() {
        PlayerPackExtension.registerEvents()
    }

    public fun path(file: String): Path {
        return this.container.findPath(file).get()
    }
}
