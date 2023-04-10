package net.casualuhc.arcade.advancements

import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike

class AdvancementBuilder private constructor() {
    var id: ResourceLocation? = null
    var display: ItemStack = ItemStack.EMPTY
    var title: Component = Component.empty()
    var description: Component = Component.empty()
    var background: ResourceLocation? = null
    var toast = false
    var announce = false
    var hidden = false

    fun id(id: ResourceLocation): AdvancementBuilder {
        this.id = id
        return this
    }

    fun display(display: ItemStack): AdvancementBuilder {
        this.display = display
        return this
    }

    fun display(display: ItemLike): AdvancementBuilder {
        this.display = display.asItem().defaultInstance
        return this
    }

    fun title(title: Component): AdvancementBuilder {
        this.title = title
        return this
    }

    fun description(description: Component): AdvancementBuilder {
        this.description = description
        return this
    }

    fun background(background: ResourceLocation): AdvancementBuilder {
        this.background = background
        return this
    }

    fun toast(): AdvancementBuilder {
        this.toast = true
        return this
    }

    fun announce(): AdvancementBuilder {
        this.announce = true
        return this
    }

    fun hidden(): AdvancementBuilder {
        this.hidden = true
        return this
    }

    companion object {
        fun create(block: AdvancementBuilder.() -> Unit): AdvancementBuilder {
            return AdvancementBuilder().apply(block)
        }
    }
}