/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals.utils.elements.component

import net.casual.arcade.utils.ColorUtils
import net.casual.arcade.utils.component.color
import net.casual.arcade.utils.server.calculateMSPT
import net.casual.arcade.virtual.visuals.elements.UniversalElement
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer

public object MSPTComponentElement: UniversalElement<Component> {
    override fun get(server: MinecraftServer): Component {
        val mspt = server.calculateMSPT()
        return Component.literal("MSPT: ")
            .append(Component.literal("%.2f".format(mspt)).color(ColorUtils.heatmap(1 - mspt / 50.0F)))
    }
}