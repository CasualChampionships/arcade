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