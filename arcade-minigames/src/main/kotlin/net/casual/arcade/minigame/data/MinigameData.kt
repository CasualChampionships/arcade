/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.data

import net.casual.arcade.minigame.component.MinigameComponent
import net.casual.arcade.minigame.data.impl.MinigameWorldData

/**
 * This interface represents some *static* data used
 * inside a minigame.
 *
 * Then intention for this is to provide a way for
 * minigames to be data driven. A good example of this
 * being minigame maps. A minigame can load with a
 * specified [MinigameDataSet] which contain
 * [MinigameWorldData] which provides the map for the
 * minigame. Other data modules may include player
 * spawns, loot tables, etc.
 *
 * For dynamic minigame data, or modules that can modify
 * minigame logic [MinigameComponent]s should be used instead.
 *
 * @see MinigameDataSet
 * @see MinigameDataProvider
 */
public interface MinigameData {
    /**
     * The type of this minigame data.
     *
     * @return The minigame data type.
     */
    public fun type(): MinigameDataType<*>
}
