/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.area

public interface PlaceableArea: Area {
    public fun place(): Boolean

    public fun replace(): Boolean {
        this.removeAllButPlayers()
        return this.place()
    }
}