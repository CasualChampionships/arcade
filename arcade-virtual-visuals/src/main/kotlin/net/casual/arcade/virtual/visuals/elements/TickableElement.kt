/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals.elements

import net.casual.arcade.virtual.visuals.data.DynamicVisualValues
import net.minecraft.server.MinecraftServer

/**
 * This interface represents something which updates every tick.
 *
 * State shared between multiple [PlayerSpecificElement]s should live
 * in a [TickableElement] which is registered with [DynamicVisualValues], rather
 * than being updated by the elements reading it, so that it is
 * updated exactly once per tick.
 *
 * @see DynamicVisualValues
 */
public fun interface TickableElement {
    /**
     * This is called every tick.
     *
     * @param server The [MinecraftServer] instance.
     */
    public fun tick(server: MinecraftServer)
}
