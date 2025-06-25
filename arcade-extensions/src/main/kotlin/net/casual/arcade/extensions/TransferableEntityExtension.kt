/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions

import net.minecraft.world.entity.Entity

public interface TransferableEntityExtension: Extension {
    public fun transfer(entity: Entity, reason: TransferReason): Extension

    public enum class TransferReason {
        /**
         * The player died and is now respawning.
         */
        Respawned,

        /**
         * The player transferred between minigames,
         * and the minigame doesn't keep player data.
         */
        Minigame,

        /**
         * Other reasons an entity may be re-constructed,
         * for example, travelling between dimensions.
         */
        Other
    }
}