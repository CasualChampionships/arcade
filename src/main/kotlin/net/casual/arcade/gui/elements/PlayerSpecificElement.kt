package net.casual.arcade.gui.elements

import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

/**
 * This is a functional interface that represents an
 * element that is generated on a per-player basis.
 *
 * Usually this is called multiple times in succession
 * when generating UI elements.
 *
 * @see UniversalElement
 */
public fun interface PlayerSpecificElement<E: Any> {
    /**
     * This generates the element.
     *
     * @param player The player to generate the element for.
     * @return The player-specific element.
     */
    public fun get(player: ServerPlayer): E

    /**
     * This will be called every tick, to update
     * any state used to generate the element.
     *
     * @param server The [MinecraftServer] instance.
     */
    public fun tick(server: MinecraftServer) {

    }
}