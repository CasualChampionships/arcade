/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.core

import net.minecraft.server.MinecraftServer

/**
 * This interface represents a UI element that is tickable.
 */
public interface TickableUI {
    /**
     * This method is called every tick.
     */
    public fun tick(server: MinecraftServer)

    public fun shouldTickWhenPaused(): Boolean {
        return false
    }
}