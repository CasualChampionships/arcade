/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay

import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorders
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorders
import net.casual.arcade.replay.viewer.ReplayViewer
import net.casual.arcade.utils.Identifier
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModOrigin
import net.fabricmc.loader.impl.metadata.AbstractModMetadata
import net.minecraft.resources.Identifier

// TODO:
//  - Add some sort of plugin system to allow compile only dependency
//  - Add nametag support for replays
public object ArcadeReplay: ModInitializer {
    public const val MOD_ID: String = "arcade-replay"

    override fun onInitialize() {
        ReplayChunkRecorders.registerEvents()
        ReplayPlayerRecorders.registerEvents()

        ReplayViewer.registerEvents()
    }

    public fun id(path: String): Identifier {
        return Identifier(MOD_ID, path)
    }

    internal fun getLoadedMods(): Map<String, String> {
        return FabricLoader.getInstance().allMods
            .filter { it.origin.kind != ModOrigin.Kind.NESTED }
            .filter { it.metadata.type != AbstractModMetadata.TYPE_BUILTIN }
            .associateBy({ it.metadata.id }, { it.metadata.version.friendlyString })
    }
}