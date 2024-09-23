package net.casual.arcade.visuals.elements

import net.minecraft.network.chat.Component

public object ComponentElements {
    private val EMPTY = UniversalElement.constant<Component>(Component.empty())

    public fun of(component: Component): PlayerSpecificElement<Component> {
        return UniversalElement.constant(component)
    }

    public fun empty(): PlayerSpecificElement<Component> {
        return EMPTY
    }
}