/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import net.minecraft.core.Holder
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer
import net.minecraft.world.level.storage.loot.entries.UniformContainerBase
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider
import net.minecraft.world.level.storage.loot.providers.number.floats.ConstantValue as ConstantFloatValue
import net.minecraft.world.level.storage.loot.providers.number.floats.UniformGenerator as UniformFloatGenerator
import net.minecraft.world.level.storage.loot.providers.number.ints.ConstantValue as ConstantIntValue
import net.minecraft.world.level.storage.loot.providers.number.ints.UniformGenerator as UniformIntGenerator

public object LootTableUtils {
    public fun exactly(value: Float): ConstantFloatValue {
        return ConstantFloatValue(value)
    }

    public fun between(min: Float, max: Float): UniformFloatGenerator {
        return UniformFloatGenerator(Holder.direct(exactly(min)), Holder.direct(exactly(max)))
    }

    public fun exactly(value: Int): ConstantIntValue {
        return ConstantIntValue(value)
    }

    public fun between(min: Int, max: Int): UniformIntGenerator {
        return UniformIntGenerator(Holder.direct(exactly(min)), Holder.direct(exactly(max)))
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
        applier: UniformContainerBase.Builder<*>.() -> Unit
    ) {
        this.add(LootItem.lootTableItem(item), applier)
    }

    public fun UniformContainerBase.Builder<*>.count(provider: Holder<ContextIntProvider>) {
        this.apply(SetItemCountFunction.setCount(provider))
    }

    public fun UniformContainerBase.Builder<*>.durability(provider: Holder<ContextFloatProvider>) {
        this.apply(SetItemDamageFunction.setDamage(provider))
    }

    public fun UniformContainerBase.Builder<*>.enchant(lookup: HolderLookup.Provider) {
        this.apply(EnchantRandomlyFunction.randomApplicableEnchantment(lookup.lookupOrThrow(Registries.ENCHANTMENT)))
    }

    public fun UniformContainerBase.Builder<*>.enchant(lookup: HolderLookup.Provider, levels: Holder<ContextIntProvider>) {
        this.apply(EnchantWithLevelsFunction.enchantWithLevels(lookup.lookupOrThrow(Registries.ENCHANTMENT), levels))
    }
}