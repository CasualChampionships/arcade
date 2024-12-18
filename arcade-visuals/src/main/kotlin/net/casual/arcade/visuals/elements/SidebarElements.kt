/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.elements

import net.casual.arcade.visuals.sidebar.SidebarComponent
import net.minecraft.network.chat.Component

public object SidebarElements {
    public fun empty(): PlayerSpecificElement<SidebarComponent> {
        return withNoScore(Component.empty())
    }

    public fun of(component: SidebarComponent): PlayerSpecificElement<SidebarComponent> {
        return UniversalElement.constant(component)
    }

    public fun withNoScore(component: Component): PlayerSpecificElement<SidebarComponent> {
        return of(SidebarComponent.withNoScore(component))
    }

    public fun withCustomScore(component: Component, score: Component): PlayerSpecificElement<SidebarComponent> {
        return of(SidebarComponent.withCustomScore(component, score))
    }
}