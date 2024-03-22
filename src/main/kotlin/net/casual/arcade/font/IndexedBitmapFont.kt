package net.casual.arcade.font

import net.casual.arcade.utils.ComponentUtils
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation

public abstract class IndexedBitmapFont(id: ResourceLocation): BitmapFont(id) {
    private val components = ArrayList<ComponentUtils.ConstantComponentGenerator>()

    protected fun indexed(texture: ResourceLocation, ascent: Int, height: Int) {
        this.components.add(this.add(texture, ascent, height))
    }

    protected fun get(index: Int): MutableComponent {
        return this.components[index].generate()
    }
}