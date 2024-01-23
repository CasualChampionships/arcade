package net.casual.arcade.utils

import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable

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
}