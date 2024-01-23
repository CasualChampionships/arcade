package net.casual.arcade.utils

import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer

public object LootTableUtils {
    public fun create(applier: LootTable.Builder.() -> Unit): LootTable {
        val builder = LootTable.lootTable()
        applier(builder)
        return builder.build()
    }

    public fun LootTable.Builder.createPool(applier: LootPool.Builder.() -> Unit) {
        val builder = LootPool.lootPool()
        applier(builder)
        this.withPool(builder)
    }

    public fun <T: LootPoolEntryContainer.Builder<*>> LootPool.Builder.add(
        element: T,
        applier: T.() -> Unit
    ) {
        applier(element)
        this.add(element)
    }

    public fun LootPool.Builder.addItem(
        item: ItemLike,
        applier: LootPoolSingletonContainer.Builder<*>.() -> Unit
    ) {
        this.add(LootItem.lootTableItem(item), applier)
    }
}