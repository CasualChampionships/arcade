package net.casual.arcade.resources.font

import net.casual.arcade.utils.ComponentUtils
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation

public abstract class IndexedBitmapFontResources(id: ResourceLocation): FontResources(id) {
    private val components = ArrayList<ComponentUtils.ConstantComponentGenerator>()

    protected fun indexed(
        texture: ResourceLocation,
        @Suppress("SameParameterValue") ascent: Int,
        height: Int
    ) {
        this.components.add(this.bitmap(texture, ascent, height))
    }

    protected fun get(index: Int): MutableComponent {
        return this.components[index].generate()
    }
}