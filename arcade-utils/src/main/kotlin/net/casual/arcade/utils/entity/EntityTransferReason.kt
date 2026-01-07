/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.entity

public enum class EntityTransferReason {
    /**
     * The player died and is now respawning.
     */
    Respawned,

    /**
     * An entity was converted from another entity.
     */
    Converted,

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