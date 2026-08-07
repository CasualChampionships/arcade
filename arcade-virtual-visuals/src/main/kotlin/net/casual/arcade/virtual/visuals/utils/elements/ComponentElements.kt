/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals.utils.elements

import net.casual.arcade.utils.component.Component
import net.casual.arcade.utils.component.ComponentBuilderContext
import net.casual.arcade.virtual.visuals.elements.PlayerSpecificElement
import net.casual.arcade.virtual.visuals.elements.UniversalElement
import net.minecraft.network.chat.Component

public object ComponentElements {
    private val EMPTY = UniversalElement.constant<Component>(Component.empty())

    public fun of(component: Component): PlayerSpecificElement<Component> {
        return UniversalElement.constant(component)
    }

    public fun of(builder: ComponentBuilderContext.() -> Component): PlayerSpecificElement<Component> {
        return UniversalElement.constant(Component(builder))
    }

    public fun empty(): PlayerSpecificElement<Component> {
        return EMPTY
    }
}