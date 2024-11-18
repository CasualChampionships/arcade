package net.casual.arcade.visuals.elements.component

import net.casual.arcade.utils.ColorUtils
import net.casual.arcade.utils.ComponentUtils.color
import net.casual.arcade.utils.calculateMSPT
import net.casual.arcade.visuals.elements.UniversalElement
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer

public object MSPTComponentElement: UniversalElement<Component> {
    override fun get(server: MinecraftServer): Component {
        val mspt = server.calculateMSPT()
        return Component.literal("MSPT: ")
            .append(Component.literal("%.2f".format(mspt)).color(ColorUtils.heatmap(1 - mspt / 50.0F)))
    }
}