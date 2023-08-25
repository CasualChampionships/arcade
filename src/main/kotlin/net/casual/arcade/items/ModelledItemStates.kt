package net.casual.arcade.items

import eu.pb4.polymer.core.api.item.PolymerItem
import eu.pb4.polymer.resourcepack.api.PolymerModelData
import eu.pb4.polymer.resourcepack.api.ResourcePackCreator
import net.casual.arcade.utils.ItemUtils.putIntElement
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

class ModelledItemStates(
    private val polymer: PolymerItem,
    private val creator: ResourcePackCreator
) {
    private val states = ArrayList<ResourceLocation>()

    private val server: Item
    private val client: Item

    init {
        if (this.polymer !is Item) {
            throw IllegalArgumentException("PolymerItem must extend Item!")
        }
        this.server = this.polymer
        this.client = this.polymer.getPolymerItem(this.polymer.defaultInstance, null)
    }

    fun createModel(id: ResourceLocation): ResourceLocation {
        this.states.add(id)
        // Load it for pack
        this.creator.requestModel(this.client, id)
        return id
    }

    fun getState(state: Int): ResourceLocation {
        return this.states[state]
    }

    fun getModel(state: Int): PolymerModelData {
        return this.creator.requestModel(this.client, this.getState(state))
    }

    fun getModel(id: ResourceLocation): PolymerModelData {
        if (!this.states.contains(id)) {
            throw IllegalArgumentException("No state '$id' has been defined")
        }
        return this.creator.requestModel(this.client, id)
    }

    fun createStack(state: Int): ItemStack {
        return this.createStack(this.getState(state))
    }

    fun createStack(id: ResourceLocation): ItemStack {
        val stack = ItemStack(this.server)
        stack.putIntElement(ID, this.getModel(id).value())
        return stack
    }

    fun getServerItem(): Item {
        return this.server
    }

    fun getClientItem(): Item {
        return this.client
    }

    fun getStates(): List<ResourceLocation> {
        return this.states
    }

    internal fun getModelId(stack: ItemStack): Int {
        if (!stack.`is`(this.getServerItem())) {
            throw IllegalArgumentException("Cannot get model ID for incorrect stack '${stack}'")
        }
        val tag = stack.tag ?: return -1
        if (tag.contains(ID, Tag.TAG_INT.toInt())) {
            return tag.getInt(ID)
        }
        return -1
    }

    companion object {
        private const val ID = "arcade_custom_model"
    }
}