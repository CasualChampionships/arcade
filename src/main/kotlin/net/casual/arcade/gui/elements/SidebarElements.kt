package net.casual.arcade.gui.elements

import net.casual.arcade.gui.sidebar.SidebarComponent
import net.minecraft.network.chat.Component

public object SidebarElements {
    public fun empty(): PlayerSpecificElement<SidebarComponent>  {
        return withNoScore(Component.empty())
    }

    public fun of(component: SidebarComponent): PlayerSpecificElement<SidebarComponent> {
        return UniversalElement.constant(component)
    }

    public fun withNoScore(component: Component): PlayerSpecificElement<SidebarComponent>  {
        return of(SidebarComponent.withNoScore(component))
    }

    public fun withCustomScore(component: Component, score: Component): PlayerSpecificElement<SidebarComponent>  {
        return of(SidebarComponent.withCustomScore(component, score))
    }
}