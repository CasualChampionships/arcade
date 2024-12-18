/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.resources.font

import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

public abstract class IndexedFontResources(
    id: ResourceLocation,
    pua: FontPUA = FontPUA.Plane0
): FontResources(id, pua) {
    @PublishedApi internal val components: ArrayList<Component> = ArrayList()

    protected inline fun indexed(component: () -> Component) {
        this.components.add(component.invoke())
    }

    protected fun get(index: Int): Component {
        return this.components[index]
    }
}