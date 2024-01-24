package net.casual.arcade.utils

import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator

public object LootTableUtils {
    public fun exactly(value: Number): ConstantValue {
        return ConstantValue.exactly(value.toFloat())
    }

    public fun between(min: Number, max: Number): UniformGenerator {
        return UniformGenerator.between(min.toFloat(), max.toFloat())
    }

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

    public fun LootPoolSingletonContainer.Builder<*>.count(provider: NumberProvider) {
        this.apply(SetItemCountFunction.setCount(provider))
    }

    public fun LootPoolSingletonContainer.Builder<*>.durability(provider: NumberProvider) {
        this.apply(SetItemDamageFunction.setDamage(provider))
    }

    public fun LootPoolSingletonContainer.Builder<*>.enchant() {
        this.apply(EnchantRandomlyFunction.randomApplicableEnchantment())
    }

    public fun LootPoolSingletonContainer.Builder<*>.enchant(levels: NumberProvider) {
        this.apply(EnchantWithLevelsFunction.enchantWithLevels(levels))
    }
}