package net.casualuhc.arcade.items

import eu.pb4.polymer.core.api.item.PolymerItem
import eu.pb4.polymer.resourcepack.api.PolymerModelData
import eu.pb4.polymer.resourcepack.api.ResourcePackCreator
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

    fun create(id: ResourceLocation): ResourceLocation {
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

    fun createStack(state: Int, model: (ItemStack, PolymerModelData) -> Unit): ItemStack {
        return this.createStack(this.getState(state), model)
    }

    fun createStack(id: ResourceLocation, model: (ItemStack, PolymerModelData) -> Unit): ItemStack {
        val stack = ItemStack(this.server)
        model(stack, this.getModel(id))
        return stack
    }

    fun getServerItem(): Item {
        return this.client
    }

    fun getClientItem(): Item {
        return this.client
    }

    fun getStates(): List<ResourceLocation> {
        return this.states
    }
}