package net.casual.arcade.gui

import net.minecraft.server.MinecraftServer

/**
 * This interface represents a UI element that is tickable.
 */
public interface TickableUI {
    /**
     * This method is called every tick.
     */
    public fun tick(server: MinecraftServer)
}