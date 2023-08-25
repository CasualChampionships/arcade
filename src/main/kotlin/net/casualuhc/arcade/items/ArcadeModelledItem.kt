package net.casualuhc.arcade.items

import eu.pb4.polymer.core.api.item.PolymerItem
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

interface ArcadeModelledItem: PolymerItem {
    fun getStates(): ModelledItemStates

    override fun getPolymerCustomModelData(itemStack: ItemStack, player: ServerPlayer?): Int {
        return this.getStates().getModelId(itemStack)
    }
}