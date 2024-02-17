package net.casual.arcade.gui.screen

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.AbstractContainerMenu

/**
 * This can be implemented by any [AbstractContainerMenu]
 * to indicate that the player can use this menu even
 * if they are frozen.
 */
public interface FrozenUsableScreen {
    public fun isFrozenUsable(player: ServerPlayer): Boolean {
        return false
    }
}