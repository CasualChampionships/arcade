/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.core.container

public enum class ContainerGuiClickBehavior {
    /**
     * This completely swallows the click.
     */
    None,

    /**
     * This lets the [ContainerGui] handle
     * the click with [ContainerGui.click].
     */
    Gui,

    /**
     * This lets the vanilla menu slot handle
     * the click, this is required for bound slots
     *
     * @see BindableContainerGui
     */
    Vanilla
}