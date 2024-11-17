package net.casual.arcade.visuals.elements.component

import net.casual.arcade.utils.ColorUtils
import net.casual.arcade.utils.ComponentUtils.color
import net.casual.arcade.utils.calculateTPS
import net.casual.arcade.visuals.elements.UniversalElement
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer

public object TPSComponentElement: UniversalElement<Component> {
    override fun get(server: MinecraftServer): Component {
        val tps = server.calculateTPS()
        return Component.literal("TPS: %.2f".format(tps)).color(ColorUtils.heatmap(tps / 20.0F))
    }
}